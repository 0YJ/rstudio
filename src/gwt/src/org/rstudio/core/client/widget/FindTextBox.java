/*
 * FindTextBox.java
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class FindTextBox extends Composite implements HasValue<String>,
                                                      CanFocus
{
   interface MyUiBinder extends UiBinder<Widget, FindTextBox>
   {}
   private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

   public FindTextBox(String cueText)
   {
      textBox_ = new TextBoxWithCue(cueText);
      initWidget(uiBinder.createAndBindUi(this));

      Style style = getElement().getStyle();
      style.setPosition(Position.RELATIVE);
      style.setTop(1, Unit.PX);
   }

   public HandlerRegistration addValueChangeHandler(
                                           ValueChangeHandler<String> handler)
   {
      return textBox_.addValueChangeHandler(handler);
   }

   public String getValue()
   {
      return textBox_.getText() ;
   }

   public void setValue(String text)
   {
      textBox_.setText(text) ;
   }

   public void setValue(String text, boolean fireEvents)
   {
      textBox_.setValue(text, fireEvents);
   }

   public void focus()
   {
      textBox_.setFocus(true);
   }

   public void addKeyDownHandler(KeyDownHandler keyDownHandler)
   {
      textBox_.addKeyDownHandler(keyDownHandler);
   }

   public void setOverrideWidth(int pixels)
   {
      searchDiv_.getStyle().setWidth(pixels, Unit.PX);
   }

   public void selectAll()
   {
      textBox_.selectAll();
   }

   @UiField(provided=true)
   TextBox textBox_;
   @UiField
   DivElement searchDiv_;
}
