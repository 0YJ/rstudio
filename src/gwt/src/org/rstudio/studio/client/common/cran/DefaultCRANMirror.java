/*
 * DefaultCRANMirror.java
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
package org.rstudio.studio.client.common.cran;

import org.rstudio.core.client.widget.OperationWithInput;
import org.rstudio.studio.client.common.GlobalDisplay;
import org.rstudio.studio.client.common.SimpleRequestCallback;
import org.rstudio.studio.client.common.cran.model.CRANMirror;
import org.rstudio.studio.client.common.cran.model.CRANServerOperations;
import org.rstudio.studio.client.server.ServerDataSource;
import org.rstudio.studio.client.server.ServerRequestCallback;
import org.rstudio.studio.client.server.Void;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Command;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DefaultCRANMirror 
{
   @Inject
   public DefaultCRANMirror(CRANServerOperations server,
                            GlobalDisplay globalDisplay)
   {
      server_ = server;
      globalDisplay_ = globalDisplay;
   }
   
   public void choose(OperationWithInput<CRANMirror> onChosen)
   {
      new ChooseCRANMirrorDialog(globalDisplay_, 
                                 mirrorDS_, 
                                 onChosen).showModal();
   }
   
   public void configure(final Command onConfigured)
   {
      // show dialog
      new ChooseCRANMirrorDialog(
         globalDisplay_,  
         mirrorDS_,
         new OperationWithInput<CRANMirror>() {
            @Override
            public void execute(final CRANMirror mirror)
            {
               server_.setCRANMirror(
                  mirror,
                  new SimpleRequestCallback<Void>("Error Setting CRAN Mirror") {
                      @Override
                      public void onResponseReceived(Void response)
                      {
                         // successfully set, call onConfigured
                         onConfigured.execute();
                      }
                  });             
             }
           }).showModal();
   }
   
   private final CRANServerOperations server_;
   
   private final GlobalDisplay globalDisplay_;
   
   private final ServerDataSource<JsArray<CRANMirror>> mirrorDS_ = 
      new ServerDataSource<JsArray<CRANMirror>>()
      {
         @Override
         public void requestData(
               ServerRequestCallback<JsArray<CRANMirror>> requestCallback)
         {
            server_.getCRANMirrors(requestCallback);
         }
    };
   
}
