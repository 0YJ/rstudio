/*
 * WebTextInput.java
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
package org.rstudio.studio.client.common.impl;

import org.rstudio.core.client.MessageDisplay.PasswordResult;
import org.rstudio.core.client.widget.Operation;
import org.rstudio.core.client.widget.ProgressIndicator;
import org.rstudio.core.client.widget.ProgressOperationWithInput;
import org.rstudio.core.client.widget.TextEntryModalDialog;
import org.rstudio.studio.client.common.TextInput;
import org.rstudio.studio.client.common.Value;

public class WebTextInput implements TextInput
{
   public void promptForText(String title,
                             String label,
                             String initialValue,
                             boolean usePasswordMask,
                             boolean numbersOnly,
                             int selectionStart,
                             int selectionLength,
                             String okButtonCaption,
                             ProgressOperationWithInput<String> okOperation,
                             Operation cancelOperation)
   {
      new TextEntryModalDialog(title,
                               label,
                               initialValue,
                               usePasswordMask,
                               null,
                               false,
                               numbersOnly,
                               selectionStart,
                               selectionLength,
                               okButtonCaption,
                               300,
                               okOperation,
                               cancelOperation).showModal();
   }

   @Override
   public void promptForPassword(String title,
                                 String label,
                                 String initialValue,
                                 String rememberPasswordPrompt,
                                 boolean rememberByDefault,
                                 int selectionStart,
                                 int selectionLength,
                                 String okButtonCaption,
                                 final ProgressOperationWithInput<PasswordResult> okOperation,
                                 Operation cancelOperation)
   {
      // This variable introduces a level of pointer indirection that lets us
      // get around passing TextEntryModalDialog a reference to itself in its
      // own constructor.
      final Value<TextEntryModalDialog> pDialog = new Value<TextEntryModalDialog>(null);

      final TextEntryModalDialog dialog = new TextEntryModalDialog(
            title,
            label,
            initialValue,
            true,
            rememberPasswordPrompt,
            rememberByDefault,
            false,
            selectionStart,
            selectionLength,
            okButtonCaption,
            300,
            new ProgressOperationWithInput<String>()
            {
               @Override
               public void execute(String input, ProgressIndicator indicator)
               {
                  PasswordResult result = new PasswordResult();
                  result.password = input;
                  result.remember = pDialog.getValue().getExtraOption();
                  okOperation.execute(result, indicator);
               }
            },
            cancelOperation);

      pDialog.setValue(dialog, false);

      dialog.showModal();
   }
}
