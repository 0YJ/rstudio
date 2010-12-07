/*
 * PendingFileUpload.java
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
package org.rstudio.studio.client.workbench.views.files.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import org.rstudio.core.client.files.FileSystemItem;

public class PendingFileUpload extends JavaScriptObject
{
   protected PendingFileUpload()
   {
   }
      
   public final native FileUploadToken getToken() /*-{
      return this.token;
   }-*/;

   public final native JsArray<FileSystemItem> getOverwrites() /*-{
      return this.overwrites;
   }-*/;
}
