/*
 * JsVectorBoolean.java
 *
 * Copyright (C) 2022 by Posit Software, PBC
 *
 * Unless you have received this program directly from Posit Software pursuant
 * to the terms of a commercial license agreement with Posit Software, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.core.client;

import java.util.function.Predicate;

import com.google.gwt.core.client.JavaScriptObject;

public class JsVectorBoolean extends JavaScriptObject
{
   protected JsVectorBoolean()
   {
   }

   public static final native JsVectorBoolean createVector()
   /*-{
      return [];
   }-*/;

   public static final native JsVectorBoolean concat(JsVectorBoolean lhs, JsVectorBoolean rhs)
   /*-{
      return lhs.concat(rhs);
   }-*/;

   public static final native JsVectorBoolean ofLength(int n)
   /*-{
      var result = [];
      result.length = n;
      return result;
   }-*/;

   public final native JsVectorBoolean concat(JsVectorBoolean other)
   /*-{
      return [].concat.call(this, other);
   }-*/;

   public final boolean contains(boolean value)
   {
      return indexOf(value) != -1;
   }

   public final native void fill(boolean value, int start, int end)
   /*-{
      var i = start;
      while (i < end) {
         this[i] = value;
         i++;
      }
   }-*/;

   public final void fill(boolean value)
   {
      fill(value, 0, length());
   }
   
   public final JsVectorBoolean filter(Predicate predicate)
   {
      JsVectorBoolean result = JsVectorBoolean.createVector();
      
      for (int i = 0, n = length(); i < n; i++)
      {
         boolean value = get(i);
         if (predicate.test(value))
            result.push(value);
      }
      
      return result;
   }

   public final boolean get(int index)
   {
      return get(index, defaultValue());
   }

   public final native boolean get(int index, boolean defaultValue)
   /*-{
      return this[index] || defaultValue;
   }-*/;

   public final native int indexOf(boolean value)
   /*-{
      return this.indexOf(value);
   }-*/;
   
   public final native int indexOf(boolean value, int offset)
   /*-{
      return this.indexOf(value, offset);
   }-*/;

   public final native boolean isEmpty()
   /*-{
      return this.length == 0;
   }-*/;

   public final native boolean isSet(int index)
   /*-{
      return typeof this[index] !== "undefined";
   }-*/;

   public final native void insert(int index, JsVectorBoolean values)
   /*-{
      [].splice.apply(this, [index, 0].concat(values));
   }-*/;

   public final native void insert(int index, boolean value)
   /*-{
      this.splice(index, 0, value);
   }-*/;

   public final native String join(String delimiter)
   /*-{
      return this.join(delimiter);
   }-*/;

   public final String join()
   {
      return join(",");
   }

   public final native int length()
   /*-{
      return this.length || 0;
   }-*/;

   public final boolean peek()
   {
      return peek(defaultValue());
   }

   private native final boolean peek(boolean defaultValue)
   /*-{
      return this[this.length - 1] || defaultValue;
   }-*/;

   public final boolean pop()
   {
      return pop(defaultValue());
   }

   private final native boolean pop(boolean defaultValue)
   /*-{
      return this.pop() || defaultValue;
   }-*/;

   public final native void push(boolean object)
   /*-{
      this.push(object);
   }-*/;

   public final native void push(JsVectorBoolean object)
   /*-{
      [].push.apply(this, object);
   }-*/;

   public final native void remove(int index, int count)
   /*-{
      return this.splice(index, count);
   }-*/;

   public final void remove(int index)
   {
      remove(index, 1);
   }

   public final native void reverse()
   /*-{
      this.reverse();
   }-*/;

   public final native void setLength(int length)
   /*-{
      this.length = length;
   }-*/;

   public final boolean shift()
   {
      return shift(defaultValue());
   }

   private final native boolean shift(boolean defaultValue)
   /*-{
      return this.shift() || defaultValue;
   }-*/;

   public final int size()
   {
      return length();
   }

   public final native JsVectorBoolean slice(int begin, int end)
   /*-{
      return this.slice(begin, end);
   }-*/;

   public final JsVectorBoolean slice(int begin)
   {
      return slice(begin, length());
   }

   public final JsVectorBoolean slice()
   {
      return slice(0, length());
   }

   public final native void splice(int start, int deleteCount, JsVectorBoolean vector)
   /*-{
      this.splice(start, deleteCount, vector);
   }-*/;

   public final native void set(int index, boolean value)
   /*-{
      this[index] = value;
   }-*/;

   public final native void unset(int index)
   /*-{
      this[index] = undefined;
   }-*/;

   public final native int unshift(boolean object)
   /*-{
      return this.unshift(object);
   }-*/;

   public final native int unshift(JsVectorBoolean vector)
   /*-{
      return [].unshift.apply(this, vector);
   }-*/;

   private final native boolean defaultValue()
   /*-{
      return false;
   }-*/;
}
