/*
 * DcfParser.hpp
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

#ifndef DCF_PARSER_HPP
#define DCF_PARSER_HPP

#include <string>
#include <map>

namespace core {

class Error;
class FilePath;

namespace text {

core::Error parseDcfFile(const FilePath& dcfFilePath,
                         bool preserveKeyCase,
                         std::map<std::string,std::string>* pFields);


std::string dcfMultilineAsFolded(const std::string& line);

} // namespace text
} // namespace core

#endif // DCF_PARSER_HPP
