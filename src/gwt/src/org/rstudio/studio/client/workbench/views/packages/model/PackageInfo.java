/*
 * PackageInfo.java
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
package org.rstudio.studio.client.workbench.views.packages.model;


import com.google.gwt.core.client.JavaScriptObject;

public class PackageInfo extends JavaScriptObject 
{
   protected PackageInfo()
   {
   }
   
   
   public final native String getName() /*-{
      return this.name;
   }-*/;
   
   public final native String getLibrary() /*-{
      return this.library;
   }-*/;

   public final native String getDesc() /*-{
      return this.desc;
   }-*/;
   
   public final native String getUrl() /*-{
      return this.url;
   }-*/;
   
   public final native boolean isLoaded() /*-{
      return this.loaded;
   }-*/;
   
   public final PackageInfo asLoaded()
   {
      return asLoadedState(true);
   }
   
   public final PackageInfo asUnloaded()
   {
      return asLoadedState(false);
   }
   
   private final native PackageInfo asLoadedState(boolean loaded) /*-{
      var packageInfo = new Object();
      packageInfo.name = this.name;
      packageInfo.library = this.library;
      packageInfo.desc = this.desc;
      packageInfo.url = this.url;
      packageInfo.loaded = loaded;
      return packageInfo;
   }-*/;
}
