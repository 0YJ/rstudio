/*
 * GitHistoryStrategy.java
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
package org.rstudio.studio.client.workbench.views.vcs.git.dialog;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.rstudio.core.client.files.FileSystemItem;
import org.rstudio.studio.client.common.vcs.GitServerOperations;
import org.rstudio.studio.client.server.ServerRequestCallback;
import org.rstudio.studio.client.workbench.views.vcs.common.events.VcsRefreshHandler;
import org.rstudio.studio.client.workbench.views.vcs.dialog.CommitInfo;
import org.rstudio.studio.client.workbench.views.vcs.dialog.HistoryStrategy;
import org.rstudio.studio.client.workbench.views.vcs.git.model.GitState;

public class GitHistoryStrategy implements HistoryStrategy
{
   @Inject
   public GitHistoryStrategy(GitServerOperations server,
                             GitHistoryAsyncDataProvider dataProvider,
                             Provider<GitState> pVcsState)
   {
      server_ = server;
      dataProvider_ = dataProvider;
      pVcsState_ = pVcsState;
   }

   @Override
   public void setRev(String rev)
   {
      dataProvider_.setRev(rev);
   }

   @Override
   public String idColumnName()
   {
      return "SHA";
   }

   @Override
   public boolean isBranchingSupported()
   {
      return true;
   }

   @Override
   public boolean isShowFileSupported()
   {
      return true;
   }

   @Override
   public void setSearchText(HasValue<String> searchText)
   {
      dataProvider_.setSearchText(searchText);
   }

   @Override
   public void setFileFilter(HasValue<FileSystemItem> fileFilter)
   {
      dataProvider_.setFileFilter(fileFilter);
   }

   @Override
   public void showFile(String revision,
                        String filename,
                        ServerRequestCallback<String> requestCallback)
   {
      server_.gitShowFile(revision, filename, requestCallback);
   }

   @Override
   public HandlerRegistration addVcsRefreshHandler(VcsRefreshHandler handler)
   {
      return pVcsState_.get().addVcsRefreshHandler(handler, false);
   }

   @Override
   public void showCommit(String commitId,
                          boolean noSizeWarning,
                          ServerRequestCallback<String> requestCallback)
   {
      server_.gitShow(commitId, noSizeWarning, requestCallback);
   }

   @Override
   public void addDataDisplay(HasData<CommitInfo> display)
   {
      dataProvider_.addDataDisplay(display);
   }

   @Override
   public void onRangeChanged(HasData<CommitInfo> display)
   {
      dataProvider_.onRangeChanged(display);
   }

   @Override
   public void refreshCount()
   {
      dataProvider_.refreshCount();
   }

   @Override
   public void initializeHistory(HasData<CommitInfo> dataDisplay)
   {
      addDataDisplay(dataDisplay);
      refreshCount();
   }

   private final GitServerOperations server_;
   private final GitHistoryAsyncDataProvider dataProvider_;
   private final Provider<GitState> pVcsState_;
}
