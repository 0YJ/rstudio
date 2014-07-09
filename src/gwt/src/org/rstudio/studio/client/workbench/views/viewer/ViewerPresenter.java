/*
 * ViewerPresenter.java
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
package org.rstudio.studio.client.workbench.views.viewer;

import com.google.gwt.user.client.Command;
import com.google.inject.Inject;

import org.rstudio.core.client.command.AppCommand;
import org.rstudio.core.client.command.CommandBinder;
import org.rstudio.core.client.command.EnabledChangedHandler;
import org.rstudio.core.client.command.Handler;
import org.rstudio.core.client.files.FileSystemItem;
import org.rstudio.studio.client.application.Desktop;
import org.rstudio.studio.client.application.events.EventBus;
import org.rstudio.studio.client.common.rpubs.RPubsPresenter;
import org.rstudio.studio.client.rmarkdown.model.RmdPreviewParams;
import org.rstudio.studio.client.server.VoidServerRequestCallback;
import org.rstudio.studio.client.shiny.events.ShinyApplicationStatusEvent;
import org.rstudio.studio.client.shiny.model.ShinyApplicationParams;
import org.rstudio.studio.client.shiny.model.ShinyViewerType;
import org.rstudio.studio.client.workbench.WorkbenchView;
import org.rstudio.studio.client.workbench.commands.Commands;
import org.rstudio.studio.client.workbench.views.BasePresenter;
import org.rstudio.studio.client.workbench.views.source.SourceShim;
import org.rstudio.studio.client.workbench.views.viewer.events.ViewerClearedEvent;
import org.rstudio.studio.client.workbench.views.viewer.events.ViewerNavigateEvent;
import org.rstudio.studio.client.workbench.views.viewer.events.ViewerPreviewRmdEvent;
import org.rstudio.studio.client.workbench.views.viewer.model.ViewerServerOperations;

public class ViewerPresenter extends BasePresenter 
                             implements ViewerNavigateEvent.Handler, 
                                        ViewerPreviewRmdEvent.Handler,
                                        ShinyApplicationStatusEvent.Handler,
                                        RPubsPresenter.Context
{
   public interface Binder extends CommandBinder<Commands, ViewerPresenter> {}
   
   public interface Display extends WorkbenchView
   {
      void navigate(String url);
      void previewRmd(RmdPreviewParams params);
      String getUrl();
      String getTitle();
      void popout();
      void refresh();
   }
   
   @Inject
   public ViewerPresenter(Display display, 
                          EventBus eventBus,
                          Commands commands,
                          Binder binder,
                          ViewerServerOperations server,
                          SourceShim sourceShim,
                          RPubsPresenter rpubsPresenter)
   {
      super(display);
      display_ = display;
      commands_ = commands;
      server_ = server;
      events_ = eventBus;
      sourceShim_ = sourceShim;
      rpubsPresenter.setContext(this);
      
      binder.bind(commands, this);
      
      enableCommands(false);
      
      // show a stop button when the console is busy (the stop and 
      // clear commands are mutually exclusive)
      commands_.viewerStop().setVisible(commands_.interruptR().isEnabled());
      commands_.viewerClear().setVisible(!commands_.viewerStop().isVisible());
      commands_.interruptR().addEnabledChangedHandler(
                                                new EnabledChangedHandler() {
         @Override
         public void onEnabledChanged(AppCommand command)
         {
            commands_.viewerStop().setVisible(command.isEnabled());
            commands_.viewerClear().setVisible(!command.isEnabled());
         }
      });
      
      eventBus.addHandler(ShinyApplicationStatusEvent.TYPE, this);
      initializeEvents();
   }
   
   @Override
   public void onViewerNavigate(ViewerNavigateEvent event)
   {
      enableCommands(true);
      
      if (event.getURL().length() > 0)
      {
         display_.bringToFront();
      
         int ensureHeight = event.getHeight();
         if (ensureHeight > 0)
            display_.ensureHeight(ensureHeight);
         
         navigate(event.getURL());
      }
      else
      {
         navigate(ViewerPane.ABOUT_BLANK);
      }
   }
   
   @Override
   public void onViewerPreviewRmd(ViewerPreviewRmdEvent event)
   {
      enableCommands(true);
      display_.bringToFront();
      if (!event.isRefresh())
         display_.maximize();
      rmdPreviewParams_ = event.getParams();
      display_.previewRmd(event.getParams());
   }
   
   @Override
   public void onShinyApplicationStatus(ShinyApplicationStatusEvent event)
   {
      if (event.getParams().getViewerType() == 
            ShinyViewerType.SHINY_VIEWER_PANE &&
          event.getParams().getState() == 
            ShinyApplicationParams.STATE_STARTED)
      {
         enableCommands(true);
         display_.bringToFront();
         navigate(event.getParams().getUrl());
         runningShinyAppParams_ = event.getParams();
      }
   }

   @Handler
   public void onViewerPopout() { display_.popout(); }
   @Handler
   public void onViewerRefresh() { display_.refresh(); }
        
   @Handler
   public void onViewerSaveAllAndRefresh()
   {
      sourceShim_.handleUnsavedChangesBeforeExit(
         sourceShim_.getUnsavedChanges(),
         new Command() {
            @Override
            public void execute()
            {
               display_.refresh();
            }
         });
   }
   
   
   @Handler 
   public void onViewerClear()
   {
      stop(false);
   }
   
   @Handler
   public void onViewerStop()
   {
      stop(true);
   }
   
   @Override
   public String getContextId()
   {
      return "RMarkdownPreview";
   }

   @Override
   public String getTitle()
   {
      String title = display_.getTitle();
      if (title != null && !title.isEmpty())
         return title;
      
      String htmlFile = null;
      if (rmdPreviewParams_ != null)
         htmlFile = rmdPreviewParams_.getOutputFile();
      if (htmlFile != null)
      {
         FileSystemItem fsi = FileSystemItem.createFile(htmlFile);
         return fsi.getStem();
      }
      else
      {
         return "(Untitled)";
      }
   }

   @Override
   public String getHtmlFile()
   {
      if (rmdPreviewParams_ != null)
         return rmdPreviewParams_.getOutputFile();
      else
         return "";
   }

   @Override
   public boolean isPublished()
   {
      if (rmdPreviewParams_ != null)
         return rmdPreviewParams_.getResult().getRpubsPublished();
      else
         return false;
   }

   private void navigate(String url)
   {
      if (Desktop.isDesktop())
         Desktop.getFrame().setViewerUrl(url);
      display_.navigate(url);
   }
   
   private void stop(boolean interruptR)
   {
      enableCommands(false);
      navigate(ViewerPane.ABOUT_BLANK);
      if (interruptR)
         commands_.interruptR().execute();
      server_.viewerStopped(new VoidServerRequestCallback());
      
      // If we were viewing a Shiny application, let the rest of the app know
      // that the application has been stopped
      if (runningShinyAppParams_ != null)
      {
         runningShinyAppParams_.setState(ShinyApplicationParams.STATE_STOPPED);
         events_.fireEvent(new ShinyApplicationStatusEvent(
               runningShinyAppParams_));
      }
      runningShinyAppParams_ = null;
      
      events_.fireEvent(new ViewerClearedEvent());
   }
   
   private void enableCommands(boolean enable)
   {
      commands_.viewerPopout().setEnabled(enable);
      commands_.viewerRefresh().setEnabled(enable);
      commands_.viewerClear().setEnabled(enable);
   }
   
   private native void initializeEvents() /*-{  
      var thiz = this;   
      $wnd.addEventListener(
            "message",
            $entry(function(e) {
               thiz.@org.rstudio.studio.client.workbench.views.viewer.ViewerPresenter::onMessage(Ljava/lang/String;Ljava/lang/String;)(e.data, e.origin);
            }),
            true);
   }-*/;
   
   private void onMessage(String data, String origin)
   {  
      if ("disconnected".equals(data))
      {
         // ensure the frame url starts with the specified origin
         if (display_.getUrl().startsWith(origin))
            onViewerClear();
      }
   }
   
   private String normalizeUrl(String url)
   {
      if (url.endsWith("/"))
         return url.substring(0, url.length()-1);
      else
         return url;
   }
   
   private final Display display_ ;
   private final Commands commands_;
   private final ViewerServerOperations server_;
   private final EventBus events_;
   private final SourceShim sourceShim_; 
   
   private ShinyApplicationParams runningShinyAppParams_;
   private RmdPreviewParams rmdPreviewParams_;
}