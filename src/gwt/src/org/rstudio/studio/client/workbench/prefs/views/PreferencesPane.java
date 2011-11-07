/*
 * PreferencesPane.java
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
package org.rstudio.studio.client.workbench.prefs.views;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;
import org.rstudio.core.client.prefs.PreferencesDialogPaneBase;
import org.rstudio.core.client.widget.NumericValueWidget;
import org.rstudio.studio.client.workbench.prefs.model.Prefs.PrefValue;
import org.rstudio.studio.client.workbench.prefs.model.RPrefs;

import java.util.ArrayList;

public abstract class PreferencesPane extends PreferencesDialogPaneBase<RPrefs>
{ 
   @Override
   public void onApply(RPrefs rPrefs)
   {
      for (Command cmd : onApplyCommands_)
         cmd.execute();
   }
   
   protected CheckBox checkboxPref(String label,
                                   final PrefValue<Boolean> prefValue)
   {
      final CheckBox checkBox = new CheckBox(label, false);
      spaced(checkBox);
      checkBox.setValue(prefValue.getGlobalValue());
      onApplyCommands_.add(new Command()
      {
         public void execute()
         {
            prefValue.setGlobalValue(checkBox.getValue());
         }
      });
      return checkBox;
   }

  
   protected NumericValueWidget numericPref(String label,
                                            final PrefValue<Integer> prefValue)
   {
      final NumericValueWidget widget = new NumericValueWidget(label);
      spaced(widget);
      registerEnsureVisibleHandler(widget);
      widget.setValue(prefValue.getGlobalValue() + "");
      onApplyCommands_.add(new Command()
      {
         public void execute()
         {
            try
            {
               prefValue.setGlobalValue(Integer.parseInt(widget.getValue()));
            }
            catch (Exception e)
            {
               // It's OK for this to be invalid if we got past validation--
               // that means the associated checkbox wasn't checked
            }
         }
      });
      return widget;
   }

   protected final ArrayList<Command> onApplyCommands_ = new ArrayList<Command>();
}
