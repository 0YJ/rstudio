/*
 * PackagesTab.java
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
package org.rstudio.studio.client.workbench.views.packages;

import com.google.inject.Inject;

import org.rstudio.core.client.command.CommandBinder;
import org.rstudio.core.client.command.Handler;
import org.rstudio.studio.client.workbench.commands.Commands;
import org.rstudio.studio.client.workbench.ui.DelayLoadTabShim;
import org.rstudio.studio.client.workbench.ui.DelayLoadWorkbenchTab;

public class PackagesTab extends DelayLoadWorkbenchTab<Packages>
{
   public interface Binder extends CommandBinder<Commands, Shim> {}
   
   public abstract static class Shim
         extends DelayLoadTabShim<Packages, PackagesTab> 
   {
      @Handler
      public abstract void onInstallPackage();
      @Handler
      public abstract void onUpdatePackages();
      @Handler
      public abstract void onRemovePackage();     
      @Handler
      public abstract void onRefreshPackages();  
   }

   @Inject
   public PackagesTab(Shim shim, Binder binder, Commands commands)
   {
      super("Packages", shim);
      binder.bind(commands, shim);
      
      commands.removePackage().setEnabled(false);
   }
}
