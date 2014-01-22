/*
 * ShinyApplicationPresenter.java
 *
 * Copyright (C) 2009-14 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.shiny;

import org.rstudio.core.client.command.CommandBinder;
import org.rstudio.core.client.command.Handler;
import org.rstudio.studio.client.application.events.EventBus;
import org.rstudio.studio.client.common.GlobalDisplay;
import org.rstudio.studio.client.common.satellite.Satellite;
import org.rstudio.studio.client.shiny.model.ShinyApplicationParams;
import org.rstudio.studio.client.shiny.events.ShinyApplicationStatusEvent;
import org.rstudio.studio.client.workbench.commands.Commands;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ShinyApplicationPresenter 
      implements IsWidget, ShinyApplicationStatusEvent.Handler
{
   public interface Binder 
          extends CommandBinder<Commands, ShinyApplicationPresenter>
   {}

   public interface Display extends IsWidget
   {
      String getDocumentTitle();
      String getUrl();
      String getAbsoluteUrl();
      void showApp(ShinyApplicationParams params);
      void reloadApp();
   }
   
   @Inject
   public ShinyApplicationPresenter(Display view,
                               GlobalDisplay globalDisplay,
                               Binder binder,
                               final Commands commands,
                               EventBus eventBus,
                               Satellite satellite)
   {
      view_ = view;
      satellite_ = satellite;
      events_ = eventBus;
      globalDisplay_ = globalDisplay;
      
      binder.bind(commands, this);  
      
      initializeEvents();
   }     

   @Override
   public Widget asWidget()
   {
      return view_.asWidget();
   }
   
   @Override
   public void onShinyApplicationStatus(ShinyApplicationStatusEvent event)
   {
      if (event.getParams().getState() == ShinyApplicationParams.STATE_RELOADING)
      {
         view_.reloadApp();
      }
   }
   
   @Handler
   public void onReloadShinyApp()
   {
      view_.reloadApp();
   }
   
   @Handler
   public void onViewerPopout()
   {
      globalDisplay_.openWindow(params_.getUrl());
   }

   public void loadApp(ShinyApplicationParams params) 
   {
      params_ = params;
      view_.showApp(params);
   }
   
   private native void initializeEvents() /*-{  
      var thiz = this;   
      $wnd.addEventListener(
            "message",
            $entry(function(e) {
               thiz.@org.rstudio.studio.client.shiny.ShinyApplicationPresenter::onMessage(Ljava/lang/String;Ljava/lang/String;)(e.data, e.origin);
            }),
            true);

      $wnd.addEventListener(
            "unload",
            $entry(function() {
               thiz.@org.rstudio.studio.client.shiny.ShinyApplicationPresenter::onClose()();
            }),
            true);
   }-*/;
   
   private void onMessage(String data, String origin)
   {  
      if ("disconnected".equals(data))
      {
         // ensure the frame url starts with the specified origin; we need to
         // use the absolute url since 'origin' includes the hostname and 
         // portmapped urls may be relative (i.e. just p/XXXX).
         if (view_.getAbsoluteUrl().startsWith(origin)) 
         {
            appStopped_ = true;
            closeShinyApp();
         }
      }
   }
   
   private void onClose()
   {
      ShinyApplicationParams params = ShinyApplicationParams.create(
            params_.getPath(), 
            params_.getUrl(), 
            appStopped_ ?
               ShinyApplicationParams.STATE_STOPPED :
               ShinyApplicationParams.STATE_STOPPING);
      notifyShinyAppClosed(params);
   }
   
   private final native void closeShinyApp() /*-{
      $wnd.close();
   }-*/;
   
   private final native void notifyShinyAppClosed(JavaScriptObject params) /*-{
      $wnd.opener.notifyShinyAppClosed(params);
   }-*/;

   private final Display view_;
   private final Satellite satellite_;
   private final EventBus events_;
   private final GlobalDisplay globalDisplay_;
   
   private ShinyApplicationParams params_;
   private boolean appStopped_ = false;
   private boolean popoutToBrowser_ = false;
}