/*
 * RGraphicsUtils.hpp
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

#ifndef R_SESSION_GRAPHICS_UTILS_HPP
#define R_SESSION_GRAPHICS_UTILS_HPP

#include <boost/shared_ptr.hpp>

namespace core {
   class Error;
   class FilePath;
}

namespace r {
namespace session {
namespace graphics {

void setCompatibleEngineVersion(int version);
bool validateEngineVersion(std::string* pMessage = NULL);

class RestorePreviousGraphicsDeviceScope
{
public:
   RestorePreviousGraphicsDeviceScope();
   virtual ~RestorePreviousGraphicsDeviceScope();
   
private:
   struct Impl;
   boost::shared_ptr<Impl> pImpl_;
};

} // namespace graphics
} // namespace session
} // namespace r


#endif // R_SESSION_GRAPHICS_UTILS_HPP 

