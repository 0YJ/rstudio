/*
 * HistoryPreferencesPane.java
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
package org.rstudio.studio.client.workbench.prefs.views;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.inject.Inject;

import org.rstudio.core.client.widget.OperationWithInput;
import org.rstudio.studio.client.common.cran.DefaultCRANMirror;
import org.rstudio.studio.client.common.cran.model.CRANMirror;
import org.rstudio.studio.client.workbench.prefs.model.BioconductorMirror;
import org.rstudio.studio.client.workbench.prefs.model.PackagesPrefs;
import org.rstudio.studio.client.workbench.prefs.model.RPrefs;

public class PackagesPreferencesPane extends PreferencesPane
{
   @Inject
   public PackagesPreferencesPane(PreferencesDialogResources res,
                                  final DefaultCRANMirror defaultCRANMirror)
   {
      res_ = res;

      cranMirrorTextBox_ = new TextBoxWithButton(
            "Default CRAN mirror:",
            "Change...",
            new ClickHandler()
            {
               public void onClick(ClickEvent event)
               {
                  defaultCRANMirror.choose(new OperationWithInput<CRANMirror>(){
                     @Override
                     public void execute(CRANMirror cranMirror)
                     {
                        cranMirror_ = cranMirror;
                        cranMirrorTextBox_.setText(cranMirror_.getDisplay());
                     }     
                  });
                 
               }
            });
      cranMirrorTextBox_.setWidth("90%");
      cranMirrorTextBox_.setText("");
      cranMirrorTextBox_.addStyleName(res.styles().cranMirrorTextBox());
      cranMirrorTextBox_.addStyleName(res.styles().extraSpaced());
      add(cranMirrorTextBox_);
      cranMirrorTextBox_.setEnabled(false);
   }

   @Override
   protected void initializeRPrefs(RPrefs rPrefs)
   {
      PackagesPrefs prefs = rPrefs.getPackagesPrefs();
      
      cranMirrorTextBox_.setEnabled(true);
      
      if (!prefs.getCRANMirror().isEmpty())
      {
         cranMirror_ = prefs.getCRANMirror();
         cranMirrorTextBox_.setText(cranMirror_.getDisplay());
      }
      
      bioconductorMirror_ = prefs.getBioconductorMirror();
   }

   @Override
   public ImageResource getIcon()
   {
      return res_.iconPackages();
   }

   @Override
   public boolean validate()
   {
      return true;
   }

   @Override
   public String getName()
   {
      return "Packages";
   }

   @Override
   public void onApply(RPrefs rPrefs)
   {
      super.onApply(rPrefs);
     
      // set packages prefs
      PackagesPrefs packagesPrefs = PackagesPrefs.create(
                                             cranMirror_,
                                             bioconductorMirror_);
                                      
      rPrefs.setPackagesPrefs(packagesPrefs);
   }

   private final PreferencesDialogResources res_;
   
   private CRANMirror cranMirror_ = CRANMirror.empty();
   private BioconductorMirror bioconductorMirror_ = null;
   private TextBoxWithButton cranMirrorTextBox_;
}
