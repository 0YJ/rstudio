/*
 * SessionTexInputs.cpp
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

#include "SessionTexInputs.hpp"

#include <boost/algorithm/string.hpp>

#include <core/system/Environment.hpp>

#include <r/RExec.hpp>

#include <session/SessionModuleContext.hpp>

using namespace core;

namespace session {
namespace modules { 
namespace tex {
namespace inputs {

namespace {

// this function attempts to emulate the behavior of tools::texi2dvi
// in appending extra paths to TEXINPUTS, BIBINPUTS, & BSTINPUTS
core::system::Option inputsEnvVar(const std::string& name,
                                  const FilePath& extraPath,
                                  bool ensureForwardSlashes)
{
   std::string value = core::system::getenv(name);
   if (value.empty())
      value = ".";

   // on windows tools::texi2dvi replaces \ with / when defining the TEXINPUTS
   // environment variable (but for BIBINPUTS and BSTINPUTS)
#ifdef _WIN32
   if (ensureForwardSlashes)
      boost::algorithm::replace_all(value, "\\", "/");
#endif

   std::string sysPath = string_utils::utf8ToSystem(extraPath.absolutePath());
   core::system::addToPath(&value, sysPath);
   core::system::addToPath(&value, ""); // trailing : required by tex

   return std::make_pair(name, value);
}

} // anonymous namespace

RTexmfPaths rTexmfPaths()
{
   // first determine the R share directory
   std::string rHomeShare;
   r::exec::RFunction rHomeShareFunc("R.home", "share");
   Error error = rHomeShareFunc.call(&rHomeShare);
   if (error)
   {
      LOG_ERROR(error);
      return RTexmfPaths();
   }
   FilePath rHomeSharePath(rHomeShare);
   if (!rHomeSharePath.exists())
   {
      LOG_ERROR(core::pathNotFoundError(rHomeShare, ERROR_LOCATION));
      return RTexmfPaths();
   }

   // R texmf path
   FilePath rTexmfPath(rHomeSharePath.complete("texmf"));
   if (!rTexmfPath.exists())
   {
      LOG_ERROR(core::pathNotFoundError(rTexmfPath.absolutePath(),
                                        ERROR_LOCATION));
      return RTexmfPaths();
   }

   // populate and return struct
   RTexmfPaths texmfPaths;
   texmfPaths.texInputsPath = rTexmfPath.childPath("tex/latex");
   texmfPaths.bibInputsPath = rTexmfPath.childPath("bibtex/bib");
   texmfPaths.bstInputsPath = rTexmfPath.childPath("bibtex/bst");
   return texmfPaths;
}


// build TEXINPUTS, BIBINPUTS etc. by composing any existing value in
// the environment (or . if none) with the R dirs in share/texmf
core::system::Options environmentVars()
{
   core::system::Options envVars;
   RTexmfPaths texmfPaths = rTexmfPaths();
   if (!texmfPaths.empty())
   {
      envVars.push_back(inputsEnvVar("TEXINPUTS",
                                     texmfPaths.texInputsPath,
                                     true));
      envVars.push_back(inputsEnvVar("BIBINPUTS",
                                     texmfPaths.bibInputsPath,
                                     false));
      envVars.push_back(inputsEnvVar("BSTINPUTS",
                                     texmfPaths.bstInputsPath,
                                     false));
   }
   return envVars;
}

} // namespace inputs
} // namespace tex
} // namespace modules
} // namesapce session

