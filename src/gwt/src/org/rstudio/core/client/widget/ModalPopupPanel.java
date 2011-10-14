/*
 * ModalPopupPanel.java
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
package org.rstudio.core.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.PopupPanel;
import org.rstudio.core.client.command.ShortcutManager;
import org.rstudio.core.client.command.ShortcutManager.Handle;
import org.rstudio.core.client.dom.DomUtils;

public class ModalPopupPanel extends PopupPanel
{
   public ModalPopupPanel(boolean autoHide,
                          boolean modal,
                          boolean closeOnEscape)
   {
      super(autoHide, modal);
      closeOnEscape_ = closeOnEscape;
      setGlassEnabled(true);
   }

   @Override
   protected void onPreviewNativeEvent(NativePreviewEvent event)
   {
      if (closeOnEscape_ &&
          event.getTypeInt() == Event.ONKEYDOWN &&
            event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE)
      {
         close();
      }
      super.onPreviewNativeEvent(event);
   }

   public void close()
   {
      hide();
      removeFromParent();
   }

   @Override
   protected void onLoad()
   {
      super.onLoad();

      originallyFocused_ = DomUtils.getActiveElement();
      if (originallyFocused_ != null)
         originallyFocused_.blur();

      if (shortcutDisableHandle_ != null)
         shortcutDisableHandle_.close();
      shortcutDisableHandle_ = ShortcutManager.INSTANCE.disable();
   }

   @Override
   protected void onUnload()
   {
      if (shortcutDisableHandle_ != null)
         shortcutDisableHandle_.close();
      shortcutDisableHandle_ = null;

      super.onUnload();

      if (originallyFocused_ != null)
         originallyFocused_.focus();
   }

   private Handle shortcutDisableHandle_;
   private Element originallyFocused_;
   private final boolean closeOnEscape_;
}
