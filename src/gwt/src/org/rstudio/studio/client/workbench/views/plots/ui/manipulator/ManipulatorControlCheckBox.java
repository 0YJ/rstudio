package org.rstudio.studio.client.workbench.views.plots.ui.manipulator;

import org.rstudio.studio.client.workbench.views.plots.model.Manipulator;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class ManipulatorControlCheckBox extends ManipulatorControl
{

   public ManipulatorControlCheckBox(String variable,
                                     boolean value,
                                     Manipulator.CheckBox checkBox,
                               final ManipulatorChangedHandler changedHandler)
   {
      super(variable, checkBox, changedHandler);
      
      // get manipulator styles
      ManipulatorStyles styles = ManipulatorResources.INSTANCE.manipulatorStyles();
   
      // main control
      HorizontalPanel panel = new HorizontalPanel();
      panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
      panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
      
      // checkbox
      checkBox_ = new CheckBox();
      checkBox_.setValue(value);
      checkBox_.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            ManipulatorControlCheckBox.this.onValueChanged(
                  JSONBoolean.getInstance(checkBox_.getValue()));
            
         }
      });
      panel.add(checkBox_);
      
      // label
      Label label = new Label(getLabel());
      panel.add(label);
    
      
      initWidget(panel);
      setStyleName(styles.checkBox());
   }

   @Override
   public void focus()
   {
      checkBox_.setFocus(true);
   }
   
   private CheckBox checkBox_;

}
