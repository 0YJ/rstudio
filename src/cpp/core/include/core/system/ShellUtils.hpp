/*
 * ShellUtils.hpp
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

#ifndef CORE_SHELL_UTILS_HPP
#define CORE_SHELL_UTILS_HPP

#include <string>
#include <vector>

#include <boost/regex.hpp>

#include <core/FilePath.hpp>
#include <core/StringUtils.hpp>

namespace core {

namespace shell_utils {

std::string escape(const std::string& arg);
std::string escape(const FilePath& path);

std::string join(const std::string& command1, const std::string& command2);
std::string join_and(const std::string& command1, const std::string& command2);
std::string join_or(const std::string& command1, const std::string& command2);

std::string pipe(const std::string& command1, const std::string& command2);

std::string sendStdErrToStdOut(const std::string& command);
std::string sendAllOutputToNull(const std::string& command);
std::string sendStdErrToNull(const std::string& command);
std::string sendNullToStdIn(const std::string& command);

const FilePath& devnull();

class ShellCommand
{
public:
   explicit ShellCommand(const core::FilePath& filePath)
   {
      output_ = escape(string_utils::utf8ToSystem(filePath.absolutePath()));
   }

   explicit ShellCommand(const std::string& program)
   {
      boost::regex simpleCommand("^[a-zA-Z]+$");
      if (boost::regex_match(program, simpleCommand))
         output_ = program;
      else
         output_ = escape(program);
   }

   ShellCommand& operator<<(const std::string& arg);
   ShellCommand& operator<<(const FilePath& path);
   ShellCommand& operator<<(const std::vector<std::string> args);
   ShellCommand& operator<<(const std::vector<FilePath> args);

   operator std::string() const
   {
      return output_;
   }

   std::string string() const
   {
      return output_;
   }

private:
   std::string output_;
};

class ShellArgs
{
public:
   ShellArgs& operator<<(const std::string& arg);
   ShellArgs& operator<<(const FilePath& path);
   ShellArgs& operator<<(const std::vector<std::string> args);
   ShellArgs& operator<<(const std::vector<FilePath> args);

   operator std::vector<std::string>() const
   {
      return args_;
   }

   std::vector<std::string> args() const
   {
      return args_;
   }

private:
   std::vector<std::string> args_;
};

}
}

#endif // CORE_SHELL_UTILS_HPP
