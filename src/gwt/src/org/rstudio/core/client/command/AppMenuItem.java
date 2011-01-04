/*
 * AppMenuItem.java
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
import org.rstudio.core.client.regex.Match;
import org.rstudio.core.client.regex.Pattern;
import org.rstudio.core.client.regex.Pattern.ReplaceOperation;

public class AppMenuItem extends MenuItem
{
   public AppMenuItem(AppCommand cmd, boolean mainMenu)
   {
      super(cmd.getMenuHTML(mainMenu), true, cmd);
      cmd_ = cmd;
      mainMenu_ = mainMenu;
      setTitle(cmd_.getDesc());
   }

   @Override
   public Command getCommand()
   {
      return getCommand(false);
   }

   public Command getCommand(boolean evenIfDisabled)
   {
      return evenIfDisabled || cmd_.isEnabled() ? super.getCommand() : null;
   }

   public void onShow()
   {
      if (cmd_.isEnabled())
         getElement().removeClassName("disabled");
      else
         getElement().addClassName("disabled");

      setVisible(cmd_.isVisible());

      setHTML(cmd_.getMenuHTML(mainMenu_));
      setTitle(cmd_.getDesc());
   }

   public static String replaceMnemonics(String label, final String replacement)
   {
      return Pattern.create("_(_?)").replaceAll(label, new ReplaceOperation()
      {
         public String replace(Match m)
         {
            if (m.getGroup(1).length() > 0)
               return "_";
            else
               return replacement;
         }
      });
   }

   private final AppCommand cmd_;
   private final boolean mainMenu_;
}
