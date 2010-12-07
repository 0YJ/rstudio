/*
 * SessionWorkerContext.hpp
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

#ifndef SESSION_WORKER_CONTEXT_HPP
#define SESSION_WORKER_CONTEXT_HPP

#include <string>

#include <core/json/JsonRpc.hpp>

#include "SessionClientEvent.hpp"

namespace session {   
namespace worker_context {
    
// register a worker method
core::Error registerWorkerRpcMethod(const std::string& name,
                                    const core::json::JsonRpcFunction& function);


// enque client event
void enqueClientEvent(const ClientEvent& event);
   

} // namespace worker_context
} // namespace session

#endif // SESSION_WORKER_CONTEXT_HPP

