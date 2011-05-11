package org.rstudio.studio.client.workbench.views.plots.ui.export;

import org.rstudio.core.client.dom.WindowEx;
import org.rstudio.core.client.files.FileSystemItem;
import org.rstudio.core.client.widget.MessageDialog;
import org.rstudio.core.client.widget.ModalDialogProgressIndicator;
import org.rstudio.core.client.widget.Operation;
import org.rstudio.core.client.widget.OperationWithInput;
import org.rstudio.studio.client.RStudioGinjector;
import org.rstudio.studio.client.application.Desktop;
import org.rstudio.studio.client.common.GlobalDisplay;
import org.rstudio.studio.client.server.Bool;
import org.rstudio.studio.client.server.ServerError;
import org.rstudio.studio.client.server.ServerRequestCallback;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

public class SavePlotAsHandler
{
   public interface ServerOperations
   { 
      void savePlot(FileSystemItem targetPath, 
                    boolean overwrite,
                    ServerRequestCallback<Bool> requestCallback);
      
      String getFileUrl(FileSystemItem path);
   }
   
   
   public SavePlotAsHandler(GlobalDisplay globalDisplay,
                            ModalDialogProgressIndicator progressIndicator,
                            ServerOperations server)
   {
      globalDisplay_ = globalDisplay;
      progressIndicator_ = progressIndicator;
      server_ = server;
   }
   
   
   public void attemptSave(FileSystemItem targetPath,
                           boolean overwrite,
                           boolean viewAfterSave,
                           final Operation onCompleted)
   {
      if (Desktop.isDesktop() || !viewAfterSave)
         desktopSavePlotAs(targetPath, overwrite, viewAfterSave, onCompleted);
      else
         webSavePlotAs(targetPath, overwrite, viewAfterSave, onCompleted);

   }


   private void desktopSavePlotAs(final FileSystemItem targetPath, 
                                  boolean overwrite,
                                  final boolean viewAfterSave,
                                  Operation onCompleted)
   {
      progressIndicator_.onProgress("Converting Plot...");

      savePlotAs(
            targetPath, 
            overwrite, 
            viewAfterSave,
            onCompleted, 
            new PlotSaveAsUIHandler() {
               @Override
               public void onSuccess()
               {
                  progressIndicator_.clearProgress();

                  if (viewAfterSave)
                  {
                     RStudioGinjector.INSTANCE.getFileTypeRegistry().openFile(
                           targetPath);
                  }
               }

               @Override
               public void onError(ServerError error)
               {
                  progressIndicator_.onError(error.getUserMessage());

               }

               @Override
               public void onOverwritePrompt()
               {
                  progressIndicator_.clearProgress();
               }
            });
   }

   private void webSavePlotAs(final FileSystemItem targetPath, 
                              final boolean overwrite,
                              final boolean viewAfterSave,
                              final Operation onCompleted)
   {
      globalDisplay_.openProgressWindow("_rstudio_save_plot_as",
            "Converting Plot...", 
            new OperationWithInput<WindowEx>() {                                        
         public void execute(final WindowEx window)
         {
            savePlotAs(
                  targetPath, 
                  overwrite, 
                  viewAfterSave,
                  onCompleted, 
                  new PlotSaveAsUIHandler() {
                     @Override
                     public void onSuccess()
                     {
                        // redirect window to view file
                        final String url = server_.getFileUrl(targetPath);
                        Scheduler.get().scheduleDeferred(new ScheduledCommand(){
                           @Override
                           public void execute()
                           {
                              window.replaceLocationHref(url);       
                           }    
                        });
                       
                     }

                     @Override
                     public void onError(ServerError error)
                     {
                        window.close();

                        globalDisplay_.showErrorMessage("Error Saving Plot", 
                              error.getUserMessage());       
                     }

                     @Override
                     public void onOverwritePrompt()
                     {
                        window.close();
                     }
                  });
         }
      });
   }
   
   private interface PlotSaveAsUIHandler
   {
      void onSuccess();
      void onError(ServerError error);
      void onOverwritePrompt();
   }


   private void savePlotAs(final FileSystemItem targetPath, 
                           boolean overwrite,
                           final boolean viewAfterSave,
                           final Operation onCompleted,
                           final PlotSaveAsUIHandler uiHandler)
   {
      server_.savePlot(
            targetPath, 
            overwrite,
            new ServerRequestCallback<Bool>() {

               @Override
               public void onResponseReceived(Bool saved)
               {
                  if (saved.getValue())
                  {
                     uiHandler.onSuccess();

                     // fire onCompleted
                     if (onCompleted != null)
                        onCompleted.execute();
                  }
                  else
                  { 
                     uiHandler.onOverwritePrompt();

                     globalDisplay_.showYesNoMessage(
                           MessageDialog.WARNING, 
                           "File Exists", 
                           "The specified file name already exists. " +
                           "Do you want to overwrite it?", 
                           new Operation() {
                              @Override
                              public void execute()
                              {
                                 attemptSave(targetPath,
                                             true,
                                             viewAfterSave,
                                             onCompleted);
                              }
                           }, 
                           true);

                  }
               }

               @Override
               public void onError(ServerError error)
               {
                  uiHandler.onError(error);
               }

            });     
   }
   
   private final GlobalDisplay globalDisplay_;
   private ModalDialogProgressIndicator progressIndicator_;
   private final ServerOperations server_;
}
