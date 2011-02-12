/*
 * RpcResponse.java
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

public class RpcResponse extends JavaScriptObject
{
   protected RpcResponse()
   {
   }
   
   public final native static RpcResponse parse(String json) /*-{
      try
      {
         return $wnd.JSON.parse(json);
      }
      catch( ex )
      {
         return null;
      }
   }-*/;
   
   public final native static RpcResponse create(RpcError error) /*-{
      var response = new Object();
      response.error = error ;
      return response ;
   }-*/;
   
   public final RpcError getError()
   {
      return getField("error");
   }

   public final String getAsyncHandle()
   {
      return getField("asyncHandle");
   }
   
   public final <T> T getResult()
   {
      T field = this.<T>getField("result");
      return field;
   }

   @SuppressWarnings("unused")
   private static Boolean wrapBoolean(boolean value)
   {
      return value;
   }
   
   public final native <T> T getField(String name) /*-{
      var value = this[name];
      if (typeof(value) == 'boolean')
         return @org.rstudio.core.client.jsonrpc.RpcResponse::wrapBoolean(Z)(value);
      return value;
   }-*/;
}
