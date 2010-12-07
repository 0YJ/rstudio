/*
 * UrlContentType.java
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
package org.rstudio.studio.client.common.filetypes;

import org.rstudio.core.client.files.FileSystemItem;
import org.rstudio.studio.client.application.events.EventBus;

public class UrlContentType extends EditableFileType
{
   public UrlContentType()
   {
      super("urlcontent", "Generic Content",
            FileIconResources.INSTANCE.iconText());
   }

   @Override
   public void openFile(FileSystemItem file, EventBus eventBus)
   {
      assert false : "urlcontent doesn't apply to filesystem files";
   }
}
