/*
 * RpcUnderlyingError.java
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

package org.rstudio.core.client.jsonrpc;

import com.google.gwt.core.client.JavaScriptObject;

public class RpcUnderlyingError extends JavaScriptObject
{
   protected RpcUnderlyingError()
   {
   }
   
   public final native int getCode() /*-{
      return this.code;
   }-*/;

   public final native String getCategory() /*-{
      return this.category;
   }-*/;
   
   public final native String getMessage() /*-{
      return this.message;
   }-*/;

}
