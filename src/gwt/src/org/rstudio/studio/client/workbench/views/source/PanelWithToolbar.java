/*
 * PanelWithToolbar.java
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
package org.rstudio.studio.client.workbench.views.source;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import org.rstudio.core.client.widget.Toolbar;

public class PanelWithToolbar extends ResizeComposite
{
   public PanelWithToolbar(Toolbar toolbar, Widget mainWidget)
   {
      mainWidget_ = mainWidget;

      panel_ = new DockLayoutPanel(Unit.PX);
      panel_.addNorth(toolbar, toolbar.getHeight());

      panel_.add(mainWidget_);

      initWidget(panel_);
   }

   public void insertNorth(Widget widget, double size, Widget before) 
   {
      if (before == null)
         before = mainWidget_;
      panel_.insertNorth(widget, size, before);
      panel_.forceLayout();
   }

   public void remove(Widget widget)
   {
      panel_.remove(widget);
      panel_.forceLayout();
   }

   private DockLayoutPanel panel_;
   private Widget mainWidget_;
}
