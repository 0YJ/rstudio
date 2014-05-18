/*
 * Main.cpp
 *
 * Copyright (C) 2009-12 by RStudio, Inc.
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

#include <iostream>

#include <boost/test/minimal.hpp>
#include <boost/foreach.hpp>

#include <core/Error.hpp>
#include <core/Log.hpp>
#include <core/system/System.hpp>

#include <core/r_util/RVersions.hpp>

using namespace core ;

int test_main(int argc, char * argv[])
{
   try
   { 
      // setup log
      initializeStderrLog("coredev", core::system::kLogLevelWarning);

      // ignore sigpipe
      Error error = core::system::ignoreSignal(core::system::SigPipe);
      if (error)
         LOG_ERROR(error);

      std::vector<r_util::RVersion> rVersions = r_util::enumerateRVersions(
               std::vector<FilePath>(),
               FilePath(),
               std::string());

      BOOST_FOREACH(const r_util::RVersion& rVersion, rVersions)
      {
         std::cerr << rVersion << std::endl;
      }

      return EXIT_SUCCESS;
   }
   CATCH_UNEXPECTED_EXCEPTION
   
   // if we got this far we had an unexpected exception
   return EXIT_FAILURE ;
}

