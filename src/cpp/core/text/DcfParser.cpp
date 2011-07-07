/*
 * DcfParser.cpp
 *
 * Copyright (C) 2009-11 by RStudio, Inc.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */

#include <core/text/DcfParser.hpp>

#include <iostream>

#include <string>
#include <vector>

#include <boost/format.hpp>
#include <boost/algorithm/string/trim.hpp>
#include <boost/algorithm/string/split.hpp>

#include <boost/regex.hpp>

#include <core/Error.hpp>
#include <core/FilePath.hpp>
#include <core/FileSerializer.hpp>

namespace core {
namespace text {



Error parseDcfFile(const FilePath& dcfFilePath,
                   bool preserveKeyCase,
                   std::map<std::string,std::string>* pFields,
                   std::string* pUserErrMsg)
{
   // read the file
   std::string dcfFileContents;
   Error error = readStringFromFile(dcfFilePath,
                                    &dcfFileContents);
   if (error)
   {
      error.addProperty("dcf-file", dcfFilePath.absolutePath());
      *pUserErrMsg = error.summary();
      return error;
   }

   // split into lines
   std::vector<std::string> dcfLines;
   boost::algorithm::split(dcfLines,
                           dcfFileContents,
                           boost::algorithm::is_any_of("\r\n"));

   // iterate over lines
   int lineNumber = 0;
   std::string currentKey;
   std::string currentValue;
   for(std::vector<std::string>::const_iterator it = dcfLines.begin();
       it != dcfLines.end();
       ++it)
   {
      lineNumber++;

      // skip blank lines
      if (it->empty() || boost::algorithm::trim_copy(*it).empty())
         continue;

      // skip comment lines
      if (it->at(0) == '#')
         continue;

      // define regexes
      boost::regex keyValueRegx("([^\\s]+?)\\s*\\:\\s*(.*)$");
      boost::regex continuationRegex("[\t\\s](.*)");

       // look for a key-value pair line
      boost::smatch keyValueMatch, continuationMatch;
      if (regex_match(*it, keyValueMatch, keyValueRegx))
      {
         // if we have a pending key & value then resolve it
         if (!currentKey.empty())
         {
            pFields->insert(std::make_pair(currentKey,currentValue));
            currentKey.clear();
            currentValue.clear();
         }

         // update the current key and value
         currentKey = preserveKeyCase ?
                                 keyValueMatch[1] :
                                 string_utils::toLower(keyValueMatch[1]);
         currentValue = keyValueMatch[2];
      }

      // look for a continuation
      else if (!currentKey.empty() &&
               regex_match(*it, continuationMatch, continuationRegex))
      {
         currentValue.append("\n");
         currentValue.append(continuationMatch[1]);
      }

      // invalid line
      else
      {
         Error error = systemError(boost::system::errc::protocol_error,
                                   ERROR_LOCATION);
         boost::format fmt("file line number %1% is invalid");
         *pUserErrMsg = boost::str(fmt % lineNumber);
         error.addProperty("parse-error", *pUserErrMsg);
         error.addProperty("line-contents", *it);
         return error;
      }
   }

   // resolve any pending key and value
   if (!currentKey.empty())
      pFields->insert(std::make_pair(currentKey,currentValue));

   return Success();
}

std::string dcfMultilineAsFolded(const std::string& line)
{
   return boost::algorithm::trim_copy(
       boost::regex_replace(line, boost::regex("\\s*\r?\n\\s*"), " "));
}

} // namespace dcf
} // namespace core
