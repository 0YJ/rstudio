/*
 * DesktopNetworkProxyFactory.cpp
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

#include "DesktopNetworkProxyFactory.hpp"

NetworkProxyFactory::NetworkProxyFactory()
{
}

QList<QNetworkProxy> NetworkProxyFactory::queryProxy(const QNetworkProxyQuery& query)
{
   QList<QNetworkProxy> results;

   if (query.peerHostName() == QString::fromAscii("127.0.0.1")
       || query.peerHostName().toLower() == QString::fromAscii("localhost"))
   {
      results.append(QNetworkProxy::NoProxy);
   }
   else
   {
      results = systemProxyForQuery(query);
   }

   return results;
}
