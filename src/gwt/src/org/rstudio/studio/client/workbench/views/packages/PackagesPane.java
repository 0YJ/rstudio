/*
 * PackagesPane.java
 *
 * Copyright (C) 2009-12 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.workbench.views.packages;

import java.util.ArrayList;
import java.util.List;

import org.rstudio.core.client.cellview.ImageButtonColumn;
import org.rstudio.core.client.cellview.LinkColumn;
import org.rstudio.core.client.files.FileSystemItem;
import org.rstudio.core.client.theme.res.ThemeResources;
import org.rstudio.core.client.theme.res.ThemeStyles;
import org.rstudio.core.client.widget.OperationWithInput;
import org.rstudio.core.client.widget.SearchWidget;
import org.rstudio.core.client.widget.Toolbar;
import org.rstudio.core.client.widget.ToolbarButton;
import org.rstudio.core.client.widget.ToolbarPopupMenu;
import org.rstudio.studio.client.common.GlobalDisplay;
import org.rstudio.studio.client.workbench.commands.Commands;
import org.rstudio.studio.client.workbench.model.Session;
import org.rstudio.studio.client.workbench.ui.WorkbenchPane;
import org.rstudio.studio.client.workbench.views.packages.model.PackageInfo;
import org.rstudio.studio.client.workbench.views.packages.model.PackageInstallContext;
import org.rstudio.studio.client.workbench.views.packages.model.PackageInstallOptions;
import org.rstudio.studio.client.workbench.views.packages.model.PackageInstallRequest;
import org.rstudio.studio.client.workbench.views.packages.model.PackageStatus;
import org.rstudio.studio.client.workbench.views.packages.model.PackagesServerOperations;
import org.rstudio.studio.client.workbench.views.packages.ui.InstallPackageDialog;
import org.rstudio.studio.client.workbench.views.packages.ui.PackagesCellTableResources;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DefaultCellTableBuilder;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.inject.Inject;

public class PackagesPane extends WorkbenchPane implements Packages.Display
{
   @Inject
   public PackagesPane(Commands commands, 
                       Session session)
   {
      super("Packages");
      commands_ = commands;
      session_ = session;
     
      ensureWidget();
   }
   
   @Override
   public void setObserver(PackagesDisplayObserver observer)
   {
      observer_ = observer ;  
   }

   @Override
   public void listPackages(List<PackageInfo> packages)
   {
      packagesTable_.setPageSize(packages.size());
      packagesDataProvider_.setList(packages);
   }
   
   @Override
   public void installPackage(PackageInstallContext installContext,
                              PackageInstallOptions defaultInstallOptions,
                              PackagesServerOperations server,
                              GlobalDisplay globalDisplay,
                              OperationWithInput<PackageInstallRequest> operation)
   {
      new InstallPackageDialog(installContext,
                               defaultInstallOptions,
                               server, 
                               globalDisplay, 
                               operation).showModal();
   }
   
   @Override
   public void setPackageStatus(PackageStatus status)
   {
      int row = packageRow(status.getName(), status.getLib()) ;
      
      if (row != -1)
      {
         List<PackageInfo> packages = packagesDataProvider_.getList();
        
         packages.set(row, status.isLoaded() ? packages.get(row).asLoaded() :
                                               packages.get(row).asUnloaded());
      }
      
      // go through any duplicates to reconcile their status
      List<PackageInfo> packages = packagesDataProvider_.getList();
      for (int i=0; i<packages.size(); i++)
      {
         if (packages.get(i).getName().equals(status.getName()) &&
             i != row)
         {
            packages.set(i, packages.get(i).asUnloaded());
         }
      }
   }
   
   private int packageRow(String packageName, String packageLib)
   {
      // if we haven't retreived packages yet then return not found
      if (packagesDataProvider_ == null)
         return -1;
      
      List<PackageInfo> packages = packagesDataProvider_.getList();
      
      // figure out which row of the table includes this package
      int row = -1;
      for (int i=0; i<packages.size(); i++)
      {
         PackageInfo packageInfo = packages.get(i);
         if (packageInfo.getName().equals(packageName) &&
             packageInfo.getLibrary().equals(packageLib))
         {
            row = i ;
            break;
         }
      }
      return row ;
   }
   
   @Override
   protected Toolbar createMainToolbar()
   {
      Toolbar toolbar = new Toolbar();
     
      // install packages
      toolbar.addLeftWidget(commands_.installPackage().createToolbarButton());
      toolbar.addLeftSeparator();
      
      // update packages
      toolbar.addLeftWidget(commands_.updatePackages().createToolbarButton());
      toolbar.addLeftSeparator();
      
      // packrat
      ToolbarPopupMenu packratMenu = new ToolbarPopupMenu();
      packratMenu.addItem(commands_.packratSnapshot().createMenuItem(false));
      packratMenu.addItem(commands_.packratRestore().createMenuItem(false));
      packratMenu.addItem(commands_.packratClean().createMenuItem(false));
      packratMenu.addSeparator();
      packratMenu.addItem(commands_.packratStatus().createMenuItem(false));
      packratMenu.addItem(commands_.packratBundle().createMenuItem(false));
      packratMenu.addSeparator();
      packratMenu.addItem(commands_.packratHelp().createMenuItem(false));
      
      ToolbarButton packratButton = new ToolbarButton(
    		  "Packrat", commands_.packratButton().getImageResource(), packratMenu
    	);
      
      toolbar.addLeftWidget(packratButton);
      toolbar.addLeftSeparator();
      
      toolbar.addLeftWidget(commands_.refreshPackages().createToolbarButton());
      
      searchWidget_ = new SearchWidget(new SuggestOracle() {
         @Override
         public void requestSuggestions(Request request, Callback callback)
         {
            // no suggestions
            callback.onSuggestionsReady(
                  request,
                  new Response(new ArrayList<Suggestion>()));
         }
      });
      searchWidget_.addValueChangeHandler(new ValueChangeHandler<String>() {
         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            observer_.onPackageFilterChanged(event.getValue().trim());   
         }
      });
      toolbar.addRightWidget(searchWidget_);
      
      return toolbar;
   }
   
   private class VersionCell extends AbstractCell<PackageInfo>
   {
      @Override
      public void render(Context context, PackageInfo value, SafeHtmlBuilder sb)
      {
         sb.appendHtmlConstant("<div title=\"");
         sb.appendEscaped(value.getLibrary());
         sb.appendHtmlConstant("\"");
         sb.appendHtmlConstant(" class=\"");
         sb.appendEscaped(ThemeStyles.INSTANCE.adornedText());
         sb.appendHtmlConstant("\"");
         sb.appendHtmlConstant(">");
         sb.appendEscaped(value.getVersion());
         sb.appendHtmlConstant("</div>"); 
      }
   }
   
   @Override
   protected Widget createMainWidget()
   {
      packagesDataProvider_ = new ListDataProvider<PackageInfo>();
      
      packagesTable_ = new CellTable<PackageInfo>(
        15,
        PackagesCellTableResources.INSTANCE);
      packagesTable_.setTableBuilder(new PackageTableBuilder(packagesTable_));
      packagesTable_.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
      packagesTable_.setSelectionModel(new NoSelectionModel<PackageInfo>());
      packagesTable_.setWidth("100%", false);
        
      LoadedColumn loadedColumn = new LoadedColumn();
      packagesTable_.addColumn(loadedColumn);
      
      NameColumn nameColumn = new NameColumn();
      packagesTable_.addColumn(nameColumn);
    
      TextColumn<PackageInfo> descColumn = new TextColumn<PackageInfo>() {
         @Override
         public String getValue(PackageInfo packageInfo)
         {
            return packageInfo.getDesc();
         } 
      };  
      
      packagesTable_.addColumn(descColumn);
      
      Column<PackageInfo, PackageInfo> versionColumn = 
         new Column<PackageInfo, PackageInfo>(new VersionCell()) {

            @Override
            public PackageInfo getValue(PackageInfo object)
            {
               return object;
            }
      };
      
      packagesTable_.addColumn(versionColumn);
      
      ImageButtonColumn<PackageInfo> removeColumn = 
        new ImageButtonColumn<PackageInfo>(
          AbstractImagePrototype.create(ThemeResources.INSTANCE.removePackage()),
          new OperationWithInput<PackageInfo>() {
            @Override
            public void execute(PackageInfo packageInfo)
            {
               observer_.removePackage(packageInfo);          
            }  
          },
          "Remove package");
      packagesTable_.addColumn(removeColumn);
      packagesTable_.setColumnWidth(removeColumn, 30, Unit.PX);
      
     
      packagesDataProvider_.addDataDisplay(packagesTable_);
      
      ScrollPanel scrollPanel = new ScrollPanel();
      scrollPanel.setWidget(packagesTable_);
      return scrollPanel;
   }
   
   
   class LoadedColumn extends Column<PackageInfo, Boolean>
   {
      public LoadedColumn()
      {
         super(new CheckboxCell(false, false));
         
         setFieldUpdater(new FieldUpdater<PackageInfo,Boolean>() {
            @Override
            public void update(int index, PackageInfo packageInfo, Boolean value)
            {
               if (value.booleanValue())
                  observer_.loadPackage(packageInfo.getName(),
                                        packageInfo.getLibrary()) ;
               else
                  observer_.unloadPackage(packageInfo.getName(),
                                          packageInfo.getLibrary()) ;
               
            }    
         });
      }
      
      @Override
      public Boolean getValue(PackageInfo packageInfo)
      {
         return packageInfo.isLoaded();
      }
      
   }
   
   // package name column which includes a hyperlink to package docs
   class NameColumn extends LinkColumn<PackageInfo>
   {
      public NameColumn()
      {
         super(packagesDataProvider_,
               new OperationWithInput<PackageInfo>() 
               {
                  @Override
                  public void execute(PackageInfo packageInfo)
                  {
                     observer_.showHelp(packageInfo);
                  }
               },
               true);
      }
      
      @Override
      public String getValue(PackageInfo packageInfo)
      {
         return packageInfo.getName();
      }
   }
   
   class PackageTableBuilder extends DefaultCellTableBuilder<PackageInfo>
   {
      public PackageTableBuilder(AbstractCellTable<PackageInfo> cellTable)
      {
         super(cellTable);
      }

      @Override
      public void buildRowImpl(PackageInfo pkg, int idx)
      {
         String library = pkg.getLibrary();
         if (!lastLibrary_.equals(library) || idx == 0)
         {
           TableRowBuilder row = startRow();
           TableCellBuilder cell = row.startTD();
           cell.colSpan(5).className(
                 PackagesCellTableResources.INSTANCE.cellTableStyle()
                 .libraryHeader());
           cell.startH1().text(friendlyNameOfLibrary(library)).endH1();
           cell.startParagraph().text(library).endParagraph();
           row.endTD();
           
           row.endTR();
           lastLibrary_ = library;
         }
         super.buildRowImpl(pkg, idx);
      }
      
      private String lastLibrary_ = "";
   }
   
   private String friendlyNameOfLibrary(String library)
   {
      if (library.startsWith(
            session_.getSessionInfo().getActiveProjectDir().getPath()))
      {
         return "Project Library";
      }
      else if (library.startsWith(FileSystemItem.HOME_PATH))
      {
         return "User Library";
      } 
      else
      {
         return "System Library";
      }
   }
         
   private CellTable<PackageInfo> packagesTable_;
   private ListDataProvider<PackageInfo> packagesDataProvider_;
   private SearchWidget searchWidget_;
   private PackagesDisplayObserver observer_ ;

   private final Commands commands_;
   private final Session session_;
}
