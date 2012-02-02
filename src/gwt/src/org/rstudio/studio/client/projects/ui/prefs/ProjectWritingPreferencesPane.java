/*
 * ProjectWritingPreferencesPane.java
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
package org.rstudio.studio.client.projects.ui.prefs;

import org.rstudio.core.client.prefs.PreferencesDialogBaseResources;
import org.rstudio.studio.client.common.rnw.RnwWeaveSelectWidget;
import org.rstudio.studio.client.projects.model.RProjectConfig;
import org.rstudio.studio.client.projects.model.RProjectOptions;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class ProjectWritingPreferencesPane extends ProjectPreferencesPane
{
   @Inject
   public ProjectWritingPreferencesPane()
   {
      PreferencesDialogBaseResources baseRes = PreferencesDialogBaseResources.INSTANCE;

      Label pdfCompilationLabel = new Label("Compile PDF");
      pdfCompilationLabel.addStyleName(baseRes.styles().headerLabel());
      nudgeRight(pdfCompilationLabel);
      add(pdfCompilationLabel);
  
      
      defaultSweaveEngine_ = new RnwWeaveSelectWidget();
      add(defaultSweaveEngine_);  
   }
   
   @Override
   public ImageResource getIcon()
   {
      return PreferencesDialogBaseResources.INSTANCE.iconWriting();
   }

   @Override
   public String getName()
   {
      return "Authoring";
   }

   @Override
   protected void initialize(RProjectOptions options)
   {
      RProjectConfig config = options.getConfig();
      defaultSweaveEngine_.setValue(config.getDefaultSweaveEngine());
   }
   
   @Override
   public boolean validate()
   {
      return true;
   }

   @Override
   public boolean onApply(RProjectOptions options)
   {
      RProjectConfig config = options.getConfig();
      config.setDefaultSweaveEngine(defaultSweaveEngine_.getValue());
      return false;
   }
    
   private RnwWeaveSelectWidget defaultSweaveEngine_;

}
