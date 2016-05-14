/*
 * ConnectionsTab.java
 *
 * Copyright (C) 2009-12 by RStudio, Inc.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.workbench.views.connections;

import com.google.inject.Inject;

import org.rstudio.core.client.command.CommandBinder;
import org.rstudio.core.client.command.Handler;
import org.rstudio.studio.client.workbench.commands.Commands;
import org.rstudio.studio.client.workbench.model.Session;
import org.rstudio.studio.client.workbench.prefs.model.UIPrefs;
import org.rstudio.studio.client.workbench.ui.DelayLoadTabShim;
import org.rstudio.studio.client.workbench.ui.DelayLoadWorkbenchTab;

public class ConnectionsTab extends DelayLoadWorkbenchTab<ConnectionsPresenter>
{
   public abstract static class Shim 
        extends DelayLoadTabShim<ConnectionsPresenter, ConnectionsTab> {
      
      @Handler
      public abstract void onNewConnection();
      
   }
   
   public interface Binder extends CommandBinder<Commands, ConnectionsTab.Shim> {}


   @Inject
   public ConnectionsTab(Shim shim, 
                         Binder binder,
                         Commands commands,
                         Session session, 
                         UIPrefs uiPrefs)
   {
      super("Connections", shim);
      binder.bind(commands, shim);
      session_ = session;
      uiPrefs_ = uiPrefs;
   }
   
   @Override
   public boolean isSuppressed()
   {
      return !uiPrefs_.connectionsPaneEnabled().getValue();
   }
   
   @SuppressWarnings("unused")
   private Session session_;
   private UIPrefs uiPrefs_;
}