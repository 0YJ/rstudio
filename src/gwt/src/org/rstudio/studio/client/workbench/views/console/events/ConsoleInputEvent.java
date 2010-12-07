/*
 * ConsoleInputEvent.java
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

import com.google.gwt.event.shared.GwtEvent;

public class ConsoleInputEvent extends GwtEvent<ConsoleInputHandler>
{
   public static final GwtEvent.Type<ConsoleInputHandler> TYPE =
      new GwtEvent.Type<ConsoleInputHandler>();
    
   public ConsoleInputEvent(String input)
   {
      input_ = input;
   }
   
   public String getInput()
   {
      return input_;
   }
   
   @Override
   protected void dispatch(ConsoleInputHandler handler)
   {
      handler.onConsoleInput(this);
   }

   @Override
   public GwtEvent.Type<ConsoleInputHandler> getAssociatedType()
   {
      return TYPE;
   }
   
   private final String input_;
}
