/*
 * ServerREnvironment.hpp
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

#ifndef SERVER_R_ENVIRONMENT_HPP
#define SERVER_R_ENVIRONMENT_HPP

#include <string>
#include <vector>

namespace core {
   class Error;
}

namespace server {
namespace r_environment {
   
bool initialize(std::string* pErrMsg);

std::vector<std::pair<std::string,std::string> > variables();

} // namespace r_environment
} // namespace server

#endif // SERVER_R_ENVIRONMENT_HPP

