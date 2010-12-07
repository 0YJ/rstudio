/*
 * ShowHelpEvent.java
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
package org.rstudio.studio.client.workbench.views.help.events;

import com.google.gwt.event.shared.GwtEvent;

public class ShowHelpEvent extends GwtEvent<ShowHelpHandler>
{
   public static final GwtEvent.Type<ShowHelpHandler> TYPE =
      new GwtEvent.Type<ShowHelpHandler>();
   
   public ShowHelpEvent(String topicUrl)
   {
      topicUrl_ = topicUrl;
   }
   
   public String getTopicUrl()
   {
      return topicUrl_;
   }
   
   @Override
   protected void dispatch(ShowHelpHandler handler)
   {
      handler.onShowHelp(this);
   }

   @Override
   public GwtEvent.Type<ShowHelpHandler> getAssociatedType()
   {
      return TYPE;
   }
   
   private final String topicUrl_;
}