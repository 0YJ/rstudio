/*
 * ColumnSortInfo.java
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
package org.rstudio.core.client.cellview;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;

public class ColumnSortInfo extends JavaScriptObject
{
   protected ColumnSortInfo()
   {  
   }
   
   public static native ColumnSortInfo create(int columnIndex,
                                              boolean ascending) /*-{
      var sortInfo = new Object();
      sortInfo.columnIndex = columnIndex;
      sortInfo.ascending = ascending;
      return sortInfo;
   }-*/;
   
   public final native int getColumnIndex() /*-{
      return this.columnIndex;
   }-*/;
   
   public final native boolean getAscending() /*-{
      return this.ascending;
   }-*/;

   public final ColumnSortList.ColumnSortInfo toGwtSortInfo(CellTable<?> table)
   {
      return new ColumnSortList.ColumnSortInfo(
            table.getColumn(getColumnIndex()), getAscending());
   }

   @SuppressWarnings("unchecked")
   public static ColumnSortInfo fromGwtSortInfo(CellTable table,
                                               ColumnSortList.ColumnSortInfo si)
   {
      return ColumnSortInfo.create(table.getColumnIndex(si.getColumn()),
                                   si.isAscending());
   }

   public static ColumnSortList setSortList(CellTable<?> table,
                                            JsArray<ColumnSortInfo> sortArray)
   {
      ColumnSortList list = table.getColumnSortList();
      list.clear();
      for (int i = 0; i < sortArray.length(); i++)
         list.insert(i, sortArray.get(i).toGwtSortInfo(table));
      return list;
   }

   public static JsArray<ColumnSortInfo> getSortList(CellTable table)
   {
      ColumnSortList sortList = table.getColumnSortList();
      JsArray<ColumnSortInfo> result = JsArray.createArray().cast();
      for (int i = 0; i < sortList.size(); i++)
         result.push(fromGwtSortInfo(table, sortList.get(i)));
      return result;
   }
}
