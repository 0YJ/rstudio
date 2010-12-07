/*
 * ConsoleResetHistoryEvent.java
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
package org.rstudio.studio.client.workbench.views.console.events;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.shared.GwtEvent;

public class ConsoleResetHistoryEvent extends GwtEvent<ConsoleResetHistoryHandler>
{
   public static final GwtEvent.Type<ConsoleResetHistoryHandler> TYPE =
      new GwtEvent.Type<ConsoleResetHistoryHandler>();
    
   public ConsoleResetHistoryEvent(JsArrayString history)
   {
      history_ = history;
   }
   
   public JsArrayString getHistory()
   {
      return history_;
   }
   
   @Override
   protected void dispatch(ConsoleResetHistoryHandler handler)
   {
      handler.onConsoleResetHistory(this);
   }

   @Override
   public GwtEvent.Type<ConsoleResetHistoryHandler> getAssociatedType()
   {
      return TYPE;
   }
   
   private final JsArrayString history_;
}
