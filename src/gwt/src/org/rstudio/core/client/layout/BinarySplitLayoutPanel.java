/*
 * BinarySplitLayoutPanel.java
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
package org.rstudio.core.client.layout;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;

public class BinarySplitLayoutPanel extends LayoutPanel
      implements MouseDownHandler, MouseMoveHandler, MouseUpHandler
{
   public BinarySplitLayoutPanel(Widget[] widgets, int splitterHeight)
   {
      widgets_ = widgets;
      splitterHeight_ = splitterHeight;

      for (Widget w : widgets)
      {
         add(w);
         setWidgetLeftRight(w, 0, Style.Unit.PX, 0, Style.Unit.PX);
         setWidgetTopHeight(w, 0, Style.Unit.PX, 100, Style.Unit.PX);
         w.setVisible(false);
      }

      splitterPos_ = 300;
      topIsFixed_ = false;
      splitter_ = new HTML();
      splitter_.setStylePrimaryName("gwt-SplitLayoutPanel-VDragger");
      splitter_.addMouseDownHandler(this);
      splitter_.addMouseMoveHandler(this);
      splitter_.addMouseUpHandler(this);
      splitter_.getElement().getStyle().setZIndex(200);
      add(splitter_);
      setWidgetLeftRight(splitter_, 0, Style.Unit.PX, 0, Style.Unit.PX);
      setWidgetBottomHeight(splitter_,
                            splitterPos_, Style.Unit.PX,
                            splitterHeight_, Style.Unit.PX);
   }

   @Override
   protected void onAttach()
   {
      super.onAttach();
      DeferredCommand.addCommand(new Command()
      {
         public void execute()
         {
            offsetHeight_ = getOffsetHeight();
         }
      });
   }

   public HandlerRegistration addSplitterBeforeResizeHandler(
         SplitterBeforeResizeHandler handler)
   {
      return addHandler(handler, SplitterBeforeResizeEvent.TYPE);
   }

   public HandlerRegistration addSplitterResizedHandler(
         SplitterResizedHandler handler)
   {
      return addHandler(handler, SplitterResizedEvent.TYPE);
   }

   public void setTopWidget(Widget widget, boolean manageVisibility)
   {
      if (widget == null)
      {
         setTopWidget(-1, manageVisibility);
         return;
      }

      for (int i = 0; i < widgets_.length; i++)
         if (widgets_[i] == widget)
         {
            setTopWidget(i, manageVisibility);
            return;
         }

      assert false;
   }

   public void setTopWidget(int widgetIndex, boolean manageVisibility)
   {
      if (manageVisibility && top_ >= 0)
         widgets_[top_].setVisible(false);

      top_ = widgetIndex;
      if (bottom_ == top_)
         setBottomWidget(-1, manageVisibility);

      if (manageVisibility && top_ >= 0)
         widgets_[top_].setVisible(true);

      updateLayout();
   }

   public void setBottomWidget(Widget widget, boolean manageVisibility)
   {
      if (widget == null)
      {
         setBottomWidget(-1, manageVisibility);
         return;
      }

      for (int i = 0; i < widgets_.length; i++)
         if (widgets_[i] == widget)
         {
            setBottomWidget(i, manageVisibility);
            return;
         }

      assert false;
   }

   public void setBottomWidget(int widgetIndex, boolean manageVisibility)
   {
      if (manageVisibility && bottom_ >= 0)
         widgets_[bottom_].setVisible(false);

      bottom_ = widgetIndex;
      if (top_ == bottom_)
         setTopWidget(-1, manageVisibility);

      if (manageVisibility && bottom_ >= 0)
         widgets_[bottom_].setVisible(true);

      updateLayout();
   }

   public boolean isSplitterVisible()
   {
      return splitter_.isVisible();
   }

   public void setSplitterVisible(boolean visible)
   {
      splitter_.setVisible(visible);
   }

   public void setSplitterPos(int splitterPos, boolean fromTop)
   {
      if (isVisible() && isAttached() && splitter_.isVisible())
      {
         splitterPos = Math.min(getOffsetHeight() - splitterHeight_,
                                Math.max(0, splitterPos));
      }

      if (splitterPos_ == splitterPos
          && topIsFixed_ == fromTop
          && offsetHeight_ == getOffsetHeight())
      {
         return;
      }

      splitterPos_ = splitterPos;
      topIsFixed_ = fromTop;
      offsetHeight_ = getOffsetHeight();
      if (topIsFixed_)
      {
         setWidgetTopHeight(splitter_,
                            splitterPos_,
                            Style.Unit.PX,
                            splitterHeight_,
                            Style.Unit.PX);
      }
      else
      {
         setWidgetBottomHeight(splitter_,
                               splitterPos_,
                               Style.Unit.PX,
                               splitterHeight_,
                               Style.Unit.PX);
      }

      updateLayout();
   }

   public int getSplitterBottom()
   {
      assert !topIsFixed_;
      return splitterPos_;
   }

   private void updateLayout()
   {
      if (topIsFixed_)
      {
         if (top_ >= 0)
            setWidgetTopHeight(widgets_[top_],
                               0,
                               Style.Unit.PX,
                               splitterPos_,
                               Style.Unit.PX);

         if (bottom_ >= 0)
            setWidgetTopBottom(widgets_[bottom_],
                               splitterPos_ + splitterHeight_,
                               Style.Unit.PX,
                               0,
                               Style.Unit.PX);
      }
      else
      {
         if (top_ >= 0)
            setWidgetTopBottom(widgets_[top_],
                               0,
                               Style.Unit.PX,
                               splitterPos_ + splitterHeight_,
                               Style.Unit.PX);

         if (bottom_ >= 0)
            setWidgetBottomHeight(widgets_[bottom_],
                                  0,
                                  Style.Unit.PX,
                                  splitterPos_,
                                  Style.Unit.PX);
      }

      // Not sure why, but onResize() doesn't seem to get called unless we
      // do this manually. This matters for ShellPane scroll position updating.
      animate(0, new Layout.AnimationCallback()
      {
         public void onAnimationComplete()
         {
            onResize();
         }

         public void onLayout(Layout.Layer layer, double progress)
         {
         }
      });
   }

   @Override
   public void onResize()
   {
      super.onResize();
      if (offsetHeight_ > 0 && splitter_.isVisible())
      {
         double pct = ((double)splitterPos_ / offsetHeight_);
         int newPos = (int) Math.round(getOffsetHeight() * pct);
         setSplitterPos(newPos, topIsFixed_);
      }
   }

   public void onMouseDown(MouseDownEvent event)
   {
      resizing_ = true;
      Event.setCapture(splitter_.getElement());
      event.preventDefault();
      event.stopPropagation();
      fireEvent(new SplitterBeforeResizeEvent());
   }

   public void onMouseMove(MouseMoveEvent event)
   {
      if (event.getNativeButton() == 0)
         resizing_ = false;

      if (!resizing_)
         return;

      event.preventDefault();
      event.stopPropagation();
      if (topIsFixed_)
         setSplitterPos(event.getRelativeY(getElement()), true);
      else
         setSplitterPos(getOffsetHeight() - event.getRelativeY(getElement()),
                        false);
   }

   public void onMouseUp(MouseUpEvent event)
   {
      if (resizing_)
      {
         resizing_ = false;
         Event.releaseCapture(splitter_.getElement());
         fireEvent(new SplitterResizedEvent());
      }
   }

   public int getSplitterHeight()
   {
      return splitterHeight_;
   }

   private int top_;
   private int bottom_;

   private HTML splitter_;
   private int splitterPos_;
   private int splitterHeight_;
   // If true, then bottom widget should scale and top widget should stay
   // fixed. If false, then vice versa.
   private boolean topIsFixed_ = true;
   private final Widget[] widgets_;
   private boolean resizing_;
   private int offsetHeight_;
}
