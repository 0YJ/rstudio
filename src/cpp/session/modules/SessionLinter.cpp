/*
 * SessionLinter.cpp
 *
 * Copyright (C) 2009-2015 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */

// #define RSTUDIO_ENABLE_PROFILING
// #define RSTUDIO_ENABLE_DEBUG_MACROS
#define RSTUDIO_DEBUG_LABEL "linter"
#include <core/Macros.hpp>

#include "SessionLinter.hpp"
#include "SessionCodeSearch.hpp"

#include <set>

#include <core/Exec.hpp>
#include <core/Error.hpp>

#include <session/SessionModuleContext.hpp>
#include <session/projects/SessionProjects.hpp>

#include <boost/shared_ptr.hpp>
#include <boost/bind.hpp>
#include <boost/foreach.hpp>
#include <boost/range/adaptor/map.hpp>

#include <r/RSexp.hpp>
#include <r/RExec.hpp>
#include <r/RRoutines.hpp>

#include <core/FileUtils.hpp>
#include <core/r_util/RTokenizer.hpp>
#include <core/r_util/RParser.hpp>
#include <core/r_util/RSourceIndex.hpp>
#include <core/FileUtils.hpp>
#include <core/collection/Tree.hpp>
#include <core/collection/Stack.hpp>

namespace rstudio {
namespace session {
namespace modules {
namespace linter {

using namespace core;
using namespace core::r_util;
using namespace core::r_util::token_utils;
using namespace core::collection;

namespace {

std::vector<std::string> makeNSEFunctions()
{
   std::vector<std::string> s;
   
   s.push_back("library");
   s.push_back("require");
   s.push_back("quote");
   s.push_back("substitute");
   s.push_back("enquote");
   s.push_back("expression");
   s.push_back("evalq");
   s.push_back("subset");
   
   return s;
}

static std::vector<std::string> s_nseFunctions =
      makeNSEFunctions();

} // anonymous namespace

namespace {

void addUnreferencedSymbol(const ParseItem& item,
                           LintItems& lint)
{
   const ParseNode* pNode = item.pNode;
   if (!pNode)
      return;
   
   // Attempt to find a similarly named candidate in scope
   std::string candidate = pNode->suggestSimilarSymbolFor(item);
   lint.noSymbolNamed(item, candidate);
   
   // Check to see if there is a symbol in that node of
   // the parse tree (but defined later)
   ParseNode::SymbolPositions& symbols =
         const_cast<ParseNode::SymbolPositions&>(pNode->getDefinedSymbols());
   
   if (symbols.count(item.symbol))
   {
      ParseNode::Positions positions = symbols[item.symbol];
      BOOST_FOREACH(const Position& position, positions)
      {
         lint.symbolDefinedAfterUsage(item, position);
      }
   }
}

Error getAllAvailableRSymbols(const FilePath& filePath,
                              std::set<std::string>* pSymbols)
{
   using namespace r::exec;
   
   Error error = RFunction(".rs.availableRSymbols").call(pSymbols);
   if (error)
      return error;

   // TODO: Think a little more about how to handle cross-file symbol
   // resolution, especially for packages.
   if (filePath.isWithin(projects::projectContext().directory()))
   {
      using namespace session::modules::code_search;
      BOOST_FOREACH(const boost::shared_ptr<RSourceIndex>& pIndex,
                    rSourceIndex().indexMap() | boost::adaptors::map_values)
      {
         BOOST_FOREACH(const RSourceItem& item, pIndex->items())
         {
            pSymbols->insert(item.name());
         }
      }
   }
   return Success();
}

} // end anonymous namespace

ParseResults parse(const std::wstring& rCode,
                   const FilePath& origin)
{
   ParseResults results;
   results = r_util::parse(rCode);
   
   ParseNode* pRoot = results.parseTree();
   if (!pRoot)
   {
      std::string codeSnippet;
      if (rCode.length() > 40)
         codeSnippet = string_utils::wideToUtf8(rCode.substr(0, 40)) + "...";
      else
         codeSnippet = string_utils::wideToUtf8(rCode);
      
      std::string message = std::string() +
            "Parse failed: no parse tree available for code " +
            "'" + codeSnippet + "'";
      
      LOG_ERROR_MESSAGE(message);
      return ParseResults();
   }
   
   std::vector<ParseItem> unresolvedItems;
   pRoot->findAllUnresolvedSymbols(&unresolvedItems);
   
   // Finally, prune the set of lintItems -- we exclude any symbols
   // that were on the search path.
   std::set<std::string> objects;
   Error error = getAllAvailableRSymbols(origin, &objects);
   if (error)
      LOG_ERROR(error);
   else
   {
      BOOST_FOREACH(const ParseItem& item, unresolvedItems)
      {
         if (objects.count(string_utils::strippedOfBackQuotes(item.symbol)) == 0)
         {
            addUnreferencedSymbol(item, results.lint());
         }
      }
   }
   
   return results;
}

ParseResults parse(const std::string& rCode,
                   const FilePath& origin)
{
   return parse(string_utils::utf8ToWide(rCode), origin);
}

namespace {

json::Array lintAsJson(const LintItems& items)
{
   json::Array jsonArray;
   jsonArray.reserve(items.size());
   
   BOOST_FOREACH(const LintItem& item, items)
   {
      json::Object jsonObject;
      
      jsonObject["start.row"] = item.startRow;
      jsonObject["end.row"] = item.endRow;
      jsonObject["start.column"] = item.startColumn;
      jsonObject["end.column"] = item.endColumn;
      jsonObject["text"] = item.message;
      jsonObject["raw"] = item.message;
      jsonObject["type"] = lintTypeToString(item.type);
      
      jsonArray.push_back(jsonObject);
      
   }
   return jsonArray;
}

module_context::SourceMarkerSet asSourceMarkerSet(const LintItems& items,
                                                  const FilePath& filePath)
{
   using namespace module_context;
   std::vector<SourceMarker> markers;
   markers.reserve(items.size());
   BOOST_FOREACH(const LintItem& item, items)
   {
      markers.push_back(SourceMarker(
                           sourceMarkerTypeFromString(lintTypeToString(item.type)),
                           filePath,
                           item.startRow + 1,
                           item.startColumn + 1,
                           core::html_utils::HTML(item.message),
                           true));
   }
   return SourceMarkerSet("Linter", markers);
}

Error extractRCode(const std::string& contents,
                   const std::string& reOpen,
                   const std::string& reClose,
                   std::string* pContent)
{
   using namespace r::exec;
   RFunction extract(".rs.extractRCode");
   extract.addParam(contents);
   extract.addParam(reOpen);
   extract.addParam(reClose);
   Error error = extract.call(pContent);
   return error;
}

Error lintRSourceDocument(const json::JsonRpcRequest& request,
                          json::JsonRpcResponse* pResponse)
{
   using namespace source_database;
   
   std::string documentId;
   bool showMarkersTab = false;
   Error error = json::readParams(request.params,
                                  &documentId,
                                  &showMarkersTab);
   
   pResponse->setResult(json::Array());
   
   if (error)
   {
      LOG_ERROR(error);
      return error;
   }
   
   // Try to get the contents from the database
   boost::shared_ptr<SourceDocument> pDoc(new SourceDocument());
   error = get(documentId, pDoc);
   if (error)
   {
      LOG_ERROR(error);
      return error;
   }
   
   std::string content;
   if (pDoc->type() == SourceDocument::SourceDocumentTypeRSource)
      content = pDoc->contents();
   else if (pDoc->type() == SourceDocument::SourceDocumentTypeRMarkdown)
      error = extractRCode(pDoc->contents(),
                           "^\\s*[`]{3}{\\s*r.*}\\s*$",
                           "^\\s*[`]{3}\\s*$",
                           &content);
   else if (pDoc->type() == SourceDocument::SourceDocumentTypeSweave)
      error = extractRCode(pDoc->contents(),
                           "^\\s*<<.*>>=\\s*$",
                           "^\\s*@\\s*$",
                           &content);
   else if (pDoc->type() == SourceDocument::SourceDocumentTypeCpp)
      error = extractRCode(pDoc->contents(),
                           "^\\s*/[*]{3,}\\s+[rR]\\s*$",
                           "^\\s*[*]+/",
                           &content);
   else
      return Success();
   
   if (error)
   {
      LOG_ERROR(error);
      return error;
   }
   
   FilePath origin = module_context::resolveAliasedPath(pDoc->path());
   ParseResults results = parse(content, origin);
   
   pResponse->setResult(lintAsJson(results.lint()));
   
   if (showMarkersTab)
   {
      using namespace module_context;
      SourceMarkerSet markers = asSourceMarkerSet(results.lint(),
                                                  core::FilePath(pDoc->path()));
      showSourceMarkers(markers, MarkerAutoSelectNone);
   }
   
   return Success();
}

SEXP rs_lintRFile(SEXP filePathSEXP)
{
   using namespace r::sexp;
   
   Protect protect;
   ListBuilder builder(&protect);
   
   std::string path = safeAsString(filePathSEXP);
   FilePath filePath(module_context::resolveAliasedPath(path));
   
   if (!filePath.exists())
      return builder;
   
   std::string contents;
   Error error = module_context::readAndDecodeFile(
            filePath,
            projects::projectContext().defaultEncoding(),
            false,
            &contents);
   
   if (error)
   {
      LOG_ERROR(error);
      return builder;
   }
   
   std::string rCode = file_utils::readFile(filePath);
   ParseResults results = parse(rCode, filePath);
   const std::vector<LintItem>& lint = results.lint().get();
   
   std::size_t n = lint.size();
   for (std::size_t i = 0; i < n; ++i)
   {
      const LintItem& item = lint[i];
      
      ListBuilder el(&protect);
      
      // NOTE: R / document indexing is 1-based, so adjust for that.
      el.add("start.row", item.startRow + 1);
      el.add("start.column", item.startColumn + 1);
      el.add("end.row", item.endRow + 1);
      el.add("end.column", item.endColumn + 1);
      el.add("message", item.message);
      el.add("type", lintTypeToString(item.type));
      
      builder.add(el);
   }
   
   return builder;
}

static std::wstring s_rCode;

SEXP rs_loadString(SEXP rCodeSEXP)
{
   s_rCode = string_utils::utf8ToWide(CHAR(STRING_ELT(rCodeSEXP, 0)));
   return R_NilValue;
}

SEXP rs_parse(SEXP rCodeSEXP)
{
   std::string code = r::sexp::safeAsString(rCodeSEXP);
   ParseResults results = parse(code, FilePath());
   r::sexp::Protect protect;
   return r::sexp::create((int) results.lint().size(), &protect);
}

} // anonymous namespace

core::Error initialize()
{
   using namespace rstudio::core;
   using boost::bind;
   using namespace module_context;
   
   RS_REGISTER_CALL_METHOD(rs_lintRFile, 1);
   RS_REGISTER_CALL_METHOD(rs_loadString, 1);
   RS_REGISTER_CALL_METHOD(rs_parse, 1);
   
   ExecBlock initBlock;
   initBlock.addFunctions()
         (bind(sourceModuleRFile, "SessionLinter.R"))
         (bind(registerRpcMethod, "lint_r_source_document", lintRSourceDocument));

   return initBlock.execute();

}

} // end namespace linter
} // end namespace modules
} // end namespace session
} // end namespace rstudio
