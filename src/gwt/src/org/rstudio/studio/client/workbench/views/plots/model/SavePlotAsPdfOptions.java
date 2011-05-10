/*
 * SavePlotAsPdfOptions.java
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
package org.rstudio.studio.client.workbench.views.plots.model;

import com.google.gwt.core.client.JavaScriptObject;

public class SavePlotAsPdfOptions extends JavaScriptObject
{
   protected SavePlotAsPdfOptions()
   {   
   }
   
   public static final SavePlotAsPdfOptions create(double width, double height)
   {
      return create(width, height, true, false);
   }
   
   public static final native SavePlotAsPdfOptions create(
                                                  double width, 
                                                  double height,
                                                  boolean portrait,
                                                  boolean viewAfterSave) /*-{
      var options = new Object();
      options.width = width ;
      options.height = height ;
      options.portrait = portrait;
      options.viewAfterSave = viewAfterSave;
      return options ;
   }-*/;
   
   public static native boolean areEqual(SavePlotAsPdfOptions a, SavePlotAsPdfOptions b) /*-{
      if (a === null ^ b === null)
         return false;
      if (a === null)
         return true;
      return a.width === b.width &&
             a.height === b.height &&
             a.portrait === b.portrait &&
             a.viewAfterSave === b.viewAfterSave;    
   }-*/;
   
   public final native double getWidth() /*-{
      return this.width;
   }-*/;
   
   public final native double getHeight() /*-{
      return this.height;
   }-*/;
   
   public final native boolean getPortrait() /*-{
      return this.portrait;
   }-*/;
   
   public final native boolean getViewAfterSave() /*-{
      return this.viewAfterSave;
   }-*/;
   
}
