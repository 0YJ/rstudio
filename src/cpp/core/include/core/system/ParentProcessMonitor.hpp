/*
 * ParentProcessMonitor.hpp
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

#ifndef PARENT_PROCESS_MONITOR_HPP
#define PARENT_PROCESS_MONITOR_HPP

#include <core/Error.hpp>
#include <boost/function.hpp>

namespace core {
namespace parent_process_monitor {

Error wrapFork(boost::function<void()> func);

enum ParentTermination {
   ParentTerminationNormal,
   ParentTerminationAbnormal,
   ParentTerminationNoParent,
   ParentTerminationWaitFailure
};

ParentTermination waitForParentTermination();

} // namespace parent_process_monitor
} // namespace core

#endif // PARENT_PROCESS_MONITOR_HPP
