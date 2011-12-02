/*
 * RProjectVcsContext.java
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
package org.rstudio.studio.client.projects.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class RProjectVcsContext extends JavaScriptObject
{
   protected RProjectVcsContext()
   {
   }

   public native final String getActiveVcs() /*-{
      return this.active_vcs;
   }-*/;
   
   public native final JsArrayString getApplicableVcs() /*-{
      return this.applicable_vcs;
   }-*/;
}
