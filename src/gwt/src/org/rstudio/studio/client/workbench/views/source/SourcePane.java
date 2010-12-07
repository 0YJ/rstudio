/*
 * SourcePane.java
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

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import org.rstudio.core.client.events.*;
import org.rstudio.core.client.theme.DocTabLayoutPanel;
import org.rstudio.core.client.theme.res.ThemeResources;
import org.rstudio.core.client.theme.res.ThemeStyles;
import org.rstudio.core.client.widget.BeforeShowCallback;
import org.rstudio.studio.client.common.AutoGlassPanel;
import org.rstudio.studio.client.workbench.views.source.Source.Display;

public class SourcePane extends Composite implements Display,
                                                     HasEnsureVisibleHandlers,
                                                     RequiresResize,
                                                     ProvidesResize,
                                                     BeforeShowCallback
{
   @Inject
   public SourcePane()
   {
      final int UTILITY_AREA_SIZE = 74;

      panel_ = new LayoutPanel();

      tabPanel_ = new DocTabLayoutPanel(true, 65, UTILITY_AREA_SIZE);
      panel_.add(tabPanel_);
      panel_.setWidgetTopBottom(tabPanel_, 0, Unit.PX, 0, Unit.PX);
      panel_.setWidgetLeftRight(tabPanel_, 0, Unit.PX, 0, Unit.PX);

      utilPanel_ = new HTML();
      utilPanel_.setStylePrimaryName(ThemeStyles.INSTANCE.multiPodUtilityArea());
      panel_.add(utilPanel_);
      panel_.setWidgetRightWidth(utilPanel_,
                                 0, Unit.PX,
                                 UTILITY_AREA_SIZE, Unit.PX);
      panel_.setWidgetTopHeight(utilPanel_, 0, Unit.PX, 22, Unit.PX);

      tabOverflowPopup_ = new TabOverflowPopupPanel();
      tabOverflowPopup_.addCloseHandler(new CloseHandler<PopupPanel>()
      {
         public void onClose(CloseEvent<PopupPanel> popupPanelCloseEvent)
         {
            manageChevronVisibility();
         }
      });
      chevron_ = new Image(ThemeResources.INSTANCE.chevron());
      chevron_.getElement().getStyle().setCursor(Cursor.POINTER);
      chevron_.addClickHandler(new ClickHandler()
      {
         public void onClick(ClickEvent event)
         {
            tabOverflowPopup_.showRelativeTo(chevron_);
         }
      });

      panel_.add(chevron_);
      panel_.setWidgetTopHeight(chevron_,
                               8, Unit.PX,
                               chevron_.getHeight(), Unit.PX);
      panel_.setWidgetRightWidth(chevron_,
                                52, Unit.PX,
                                chevron_.getWidth(), Unit.PX);

      AutoGlassPanel glassPanel = new AutoGlassPanel(panel_);
      glassPanel.setSize("100%", "100%");
      initWidget(glassPanel);
   }

   @Override
   protected void onLoad()
   {
      super.onLoad();
      DeferredCommand.addCommand(new Command()
      {
         public void execute()
         {
            onResize();
         }
      });
   }

   public void addTab(Widget widget,
                      ImageResource icon,
                      String name,
                      String tooltip,
                      boolean switchToTab)
   {
      tabPanel_.add(widget, icon, name, tooltip);
      if (switchToTab)
         tabPanel_.selectTab(widget);
   }

   public void closeTab(Widget child, boolean interactive)
   {
      closeTab(tabPanel_.getWidgetIndex(child), interactive);
   }

   public void closeTab(int index, boolean interactive)
   {
      if (interactive)
         tabPanel_.tryCloseTab(index);
      else
         tabPanel_.closeTab(index);
   }

   public void setDirty(Widget widget, boolean dirty)
   {
      Widget tab = tabPanel_.getTabWidget(widget);
      if (dirty)
         tab.addStyleName(ThemeStyles.INSTANCE.dirtyTab());
      else
         tab.removeStyleName(ThemeStyles.INSTANCE.dirtyTab());
   }

   public void ensureVisible()
   {
      fireEvent(new EnsureVisibleEvent());
   }

   public void renameTab(Widget child,
                         ImageResource icon,
                         String value,
                         String tooltip)
   {
      tabPanel_.replaceDocName(tabPanel_.getWidgetIndex(child),
                               icon,
                               value,
                               tooltip);
   }

   public int getActiveTabIndex()
   {
      return tabPanel_.getSelectedIndex();
   }

   public void selectTab(int tabIndex)
   {
      tabPanel_.selectTab(tabIndex);
   }

   public void selectTab(Widget child)
   {
      tabPanel_.selectTab(child);
   }

   public int getTabCount()
   {
      return tabPanel_.getWidgetCount();
   }

   public HandlerRegistration addTabClosingHandler(TabClosingHandler handler)
   {
      return tabPanel_.addTabClosingHandler(handler);
   }

   public HandlerRegistration addTabClosedHandler(TabClosedHandler handler)
   {
      return tabPanel_.addTabClosedHandler(handler);
   }

   public HandlerRegistration addSelectionHandler(SelectionHandler<Integer> handler)
   {
      return tabPanel_.addSelectionHandler(handler);
   }

   public HandlerRegistration addBeforeSelectionHandler(BeforeSelectionHandler<Integer> handler)
   {
      return tabPanel_.addBeforeSelectionHandler(handler);
   }

   public Widget toWidget()
   {
      return this;
   }

   public HandlerRegistration addEnsureVisibleHandler(EnsureVisibleHandler handler)
   {
      return addHandler(handler, EnsureVisibleEvent.TYPE);
   }

   public void onResize()
   {
      panel_.onResize();
      manageChevronVisibility();
   }

   public void manageChevronVisibility()
   {
      int tabsWidth = tabPanel_.getTabsEffectiveWidth();
      setOverflowVisible(tabsWidth > getOffsetWidth() - 50);
   }

   public void showOverflowPopup()
   {
      setOverflowVisible(true);
      tabOverflowPopup_.showRelativeTo(chevron_);
   }

   private void setOverflowVisible(boolean visible)
   {
      utilPanel_.setVisible(visible);
      chevron_.setVisible(visible);
   }

   public void onBeforeShow()
   {
      fireEvent(new BeforeShowEvent());   
   }

   public HandlerRegistration addBeforeShowHandler(BeforeShowHandler handler)
   {
      return addHandler(handler, BeforeShowEvent.TYPE);
   }


   private DocTabLayoutPanel tabPanel_;
   private HTML utilPanel_;
   private Image chevron_;
   private LayoutPanel panel_;
   private PopupPanel tabOverflowPopup_;
}
