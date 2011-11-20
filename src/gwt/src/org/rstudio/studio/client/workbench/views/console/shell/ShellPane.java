/*
 * ShellPane.java
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
package org.rstudio.studio.client.workbench.views.console.shell;

import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import org.rstudio.core.client.CommandWithArg;
import org.rstudio.studio.client.common.filetypes.FileTypeRegistry;
import org.rstudio.studio.client.common.shell.ShellWidget;
import org.rstudio.studio.client.workbench.prefs.model.UIPrefs;
import org.rstudio.studio.client.workbench.views.source.editors.text.AceEditor;

public class ShellPane extends ShellWidget implements Shell.Display
{
   @Inject
   public ShellPane(final AceEditor editor, UIPrefs uiPrefs)
   {
      super(editor);
      
      editor.setFileType(FileTypeRegistry.R, true);
      
      uiPrefs.syntaxColorConsole().bind(new CommandWithArg<Boolean>()
      {
         public void execute(Boolean arg)
         {
            Widget inputWidget = editor.getWidget();
            if (arg)
               inputWidget.removeStyleName("nocolor");
            else
               inputWidget.addStyleName("nocolor");
         }
      });    
   }
}
