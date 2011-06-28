/*
 * UnsavedChangesDialog.java
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
package org.rstudio.studio.client.workbench.ui.unsaved;

import java.util.ArrayList;

import org.rstudio.core.client.SafeHtmlUtil;
import org.rstudio.core.client.widget.ModalDialog;
import org.rstudio.core.client.widget.OperationWithInput;
import org.rstudio.core.client.widget.ThemedButton;
import org.rstudio.studio.client.workbench.model.UnsavedChangesTarget;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;

public class UnsavedChangesDialog extends ModalDialog<ArrayList<UnsavedChangesTarget>>
{
   public UnsavedChangesDialog(
         ArrayList<UnsavedChangesTarget> dirtyTargets,
         final OperationWithInput<ArrayList<UnsavedChangesTarget>> saveOperation)
   {
      super("Unsaved Changes", saveOperation);
      targets_ = dirtyTargets;
      
      setOkButtonCaption("Save Selected");
           	     
      addLeftButton(new ThemedButton("Don't Save", new ClickHandler() {
         @Override
         public void onClick(ClickEvent event)
         {
           closeDialog();
           saveOperation.execute(new ArrayList<UnsavedChangesTarget>());
         } 
      }));    
   }

   @Override
   protected Widget createMainWidget()
   {
      // create cell table
      targetsCellTable_ = new CellTable<UnsavedChangesTarget>(
                                          15,
                                          UnsavedChangesCellTableResources.INSTANCE,
                                          KEY_PROVIDER);
      selectionModel_ = new MultiSelectionModel<UnsavedChangesTarget>(KEY_PROVIDER);
      targetsCellTable_.setSelectionModel(
         selectionModel_, 
         DefaultSelectionEventManager.<UnsavedChangesTarget> createCheckboxManager());
      targetsCellTable_.setWidth("100%", true);
      
      // add columns
      addSelectionColumn();
      addIconColumn();
      addNameAndPathColumn();
      
      // hook-up data provider 
      dataProvider_ = new ListDataProvider<UnsavedChangesTarget>();
      dataProvider_.setList(targets_);
      dataProvider_.addDataDisplay(targetsCellTable_);
      targetsCellTable_.setPageSize(targets_.size());
      
      // select all by default
      for (UnsavedChangesTarget editingTarget : dataProvider_.getList())
         selectionModel_.setSelected(editingTarget, true);
      
      // enclose cell table in scroll panel
      ScrollPanel scrollPanel = new ScrollPanel();
      scrollPanel.setStylePrimaryName(RESOURCES.styles().targetScrollPanel());
      scrollPanel.setWidget(targetsCellTable_);
      
      // main widget
      VerticalPanel panel = new VerticalPanel();
      Label captionLabel = new Label(
                           "The following documents have unsaved changes:");
      captionLabel.setStylePrimaryName(RESOURCES.styles().captionLabel());
      panel.add(captionLabel);
      panel.add(scrollPanel);      
      return panel;
   }
   
   private Column<UnsavedChangesTarget, Boolean> addSelectionColumn()
   {
      Column<UnsavedChangesTarget, Boolean> checkColumn = 
         new Column<UnsavedChangesTarget, Boolean>(new CheckboxCell(true, false)) 
         {
            @Override
            public Boolean getValue(UnsavedChangesTarget object)
            {
               return selectionModel_.isSelected(object);
            }   
         };
      checkColumn.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
      targetsCellTable_.addColumn(checkColumn); 
      targetsCellTable_.setColumnWidth(checkColumn, 25, Unit.PX);
      
      return checkColumn;
   }
  
   
   private Column<UnsavedChangesTarget, ImageResource> addIconColumn()
   {
      Column<UnsavedChangesTarget, ImageResource> iconColumn = 
         new Column<UnsavedChangesTarget, ImageResource>(new ImageResourceCell()) {

            @Override
            public ImageResource getValue(UnsavedChangesTarget object)
            {
               return object.getIcon();
            }
         };
      targetsCellTable_.addColumn(iconColumn);
      targetsCellTable_.setColumnWidth(iconColumn, 20, Unit.PX);
    
      return iconColumn;
   }
    
   private class NameAndPathCell extends AbstractCell<UnsavedChangesTarget>
   {

      @Override
      public void render(
            com.google.gwt.cell.client.Cell.Context context,
            UnsavedChangesTarget value, SafeHtmlBuilder sb)
      {
         if (value != null) 
         {
           Styles styles = RESOURCES.styles();
           
           String path = value.getPath();
           if (path != null)
           {
              SafeHtmlUtil.appendDiv(sb, styles.targetName(), value.getTitle());
              SafeHtmlUtil.appendDiv(sb, styles.targetPath(), path); 
           }
           else
           {
              SafeHtmlUtil.appendDiv(sb, 
                                     styles.targetUntitled(), 
                                     value.getTitle());
           }
         }
         
      }
      
   }
   
   private IdentityColumn<UnsavedChangesTarget> addNameAndPathColumn()
   {
      IdentityColumn<UnsavedChangesTarget> nameAndPathColumn = 
         new IdentityColumn<UnsavedChangesTarget>(new NameAndPathCell());
      
      targetsCellTable_.addColumn(nameAndPathColumn);
      targetsCellTable_.setColumnWidth(nameAndPathColumn, 350, Unit.PX);
      return nameAndPathColumn;
   }
   
   @Override
   protected ArrayList<UnsavedChangesTarget> collectInput()
   {
      return new ArrayList<UnsavedChangesTarget>(selectionModel_.getSelectedSet());
   }

   @Override
   protected boolean validate(ArrayList<UnsavedChangesTarget> input)
   {
      return true;
   }
   
   static interface Styles extends CssResource
   {
      String targetScrollPanel();
      String captionLabel();
      String targetName();
      String targetPath();
      String targetUntitled();
   }

   static interface Resources extends ClientBundle
   {
      @Source("UnsavedChangesDialog.css")
      Styles styles();
   }

   static Resources RESOURCES = (Resources) GWT.create(Resources.class);

   public static void ensureStylesInjected()
   {
      RESOURCES.styles().ensureInjected();
   }
   
   private static final ProvidesKey<UnsavedChangesTarget> KEY_PROVIDER = 
      new ProvidesKey<UnsavedChangesTarget>() {
         @Override
         public Object getKey(UnsavedChangesTarget item)
         {
            return item.getId();
         }
    };
   
   private final ArrayList<UnsavedChangesTarget> targets_;
   
   private CellTable<UnsavedChangesTarget> targetsCellTable_; 
   private ListDataProvider<UnsavedChangesTarget> dataProvider_;
   private MultiSelectionModel<UnsavedChangesTarget> selectionModel_;


}
