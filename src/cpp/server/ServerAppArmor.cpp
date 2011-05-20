/*
 * ServerAppArmor.cpp
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

#include "ServerAppArmor.hpp"

#include <core/Error.hpp>
#include <core/Log.hpp>
#include <core/FilePath.hpp>

#ifndef __APPLE__
#include <dlfcn.h>
#endif

using namespace core;

namespace server {
namespace app_armor {
  
#ifdef __APPLE__

bool isAvailable()
{
   return false;
}

Error changeToRestricted()
{
   return systemError(boost::system::errc::not_supported, ERROR_LOCATION);
}

#else


namespace {

void addLastDLErrorMessage(Error* pError)
{
   const char* msg = ::dlerror();
   if (msg != NULL)
      pError->addProperty("dlerror", std::string(msg));
}

} // anonymous namespace

bool isAvailable()
{
   return FilePath("/etc/apparmor.d/rstudio-server").exists();
}

Error changeToRestricted()
{
   // dynamically load libapparmor
   void* pLibAA = ::dlopen("libapparmor.so.1", RTLD_NOW);
   if (pLibAA == NULL)
   {
      Error error = systemError(boost::system::errc::no_such_file_or_directory,
                                ERROR_LOCATION);
      addLastDLErrorMessage(&error);
      return error;
   }

   // lookup the change hat function
   typedef int (*PtrAAChangeHat)(const char*, unsigned long)  ;
   PtrAAChangeHat pChangeHat = (PtrAAChangeHat)::dlsym(pLibAA, "aa_change_hat");
   if (pChangeHat == NULL)
   {
      Error error = systemError(boost::system::errc::not_supported,
                                ERROR_LOCATION);
      addLastDLErrorMessage(&error);
      return error;
   }

   // change to restricted (pass 0 to ensure we can't revert to root profile)
   if (pChangeHat("restricted", 0) == -1)
      return systemError(errno, ERROR_LOCATION);

   return Success();
}


#endif


} // namespace app_aprmor
} // namespace server

