/*
 * DisabledMenuItem.java
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
package org.rstudio.core.client.command;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuItem;

public class DisabledMenuItem extends MenuItem
{
   public DisabledMenuItem(String text)
   {
      super(text, new Command()
      {
         public void execute()
         {
         }
      });

      getElement().addClassName("disabled");
   }

   @Override
   public Command getCommand()
   {
      return null;
   }
}
