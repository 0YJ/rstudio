/*
 * InstalledPackagesChangedEvent.java
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
package org.rstudio.studio.client.workbench.views.packages.events;

import com.google.gwt.event.shared.GwtEvent;

public class InstalledPackagesChangedEvent extends
                              GwtEvent<InstalledPackagesChangedHandler>
{
   public static final GwtEvent.Type<InstalledPackagesChangedHandler> TYPE =
      new GwtEvent.Type<InstalledPackagesChangedHandler>();
   
   public InstalledPackagesChangedEvent()
   {
   }
   
   @Override
   protected void dispatch(InstalledPackagesChangedHandler handler)
   {
      handler.onInstalledPackagesChanged(this);
   }

   @Override
   public GwtEvent.Type<InstalledPackagesChangedHandler> getAssociatedType()
   {
      return TYPE;
   }
  
}
