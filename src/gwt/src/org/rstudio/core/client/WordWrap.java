/*
 * WordWrap.java
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
package org.rstudio.core.client;

public class WordWrap
{
   public WordWrap(int maxLineLength, boolean hardWrapIfNecessary)
   {
      maxLineLength_ = maxLineLength;
      hardWrapIfNecessary_ = hardWrapIfNecessary;
   }

   public void appendLine(String line)
   {
      if (forceWrapBefore(line))
         if (!atBeginningOfLine())
            wrap();

      processLine(line);

      if (forceWrapAfter(line))
         appendRaw("\n");
   }

   private boolean atBeginningOfLine()
   {
      // Don't need to worry about indentation here because indent isn't
      // written until some content is.
      return lineLength_ == 0;
   }

   public String getOutput()
   {
      return output_.toString();
   }

   private void processLine(String line)
   {
      Debug.devlog("processLine(" + line + ")");

      assert line.indexOf('\n') < 0;

      line = line.trim();

      if (line.length() > 0 &&
          lineLength_ > 0 &&
          lineLength_ < maxLineLength_)
      {
         // We're about to append some content and we're not at the beginning
         // of the line.
         appendRaw(" ");
      }

      // Loop while "line" is too big to fit in the current line without
      // wrapping
      while (true)
      {
         Debug.devlogf("Line: {0}\n" +
                       "LineLength: {1}\n" +
                       "MaxLineLength: {2}",
                       line, lineLength_, maxLineLength_);

         // chars left
         int charsLeft = lineLength_ == 0 ? maxLineLength_ - indent_.length()
                                          : maxLineLength_ - lineLength_;

         if (line.length() <= charsLeft)
            break;

         int breakChars = 1;

         // Look for the last space that will fit on the current line
         int index = line.lastIndexOf(' ', charsLeft);
         if (index == -1)
         {
            if (lineLength_ == 0)
            {
               index = line.indexOf(' ', charsLeft);
               if (index == -1)
                  index = line.length();

               if (hardWrapIfNecessary_ && index > charsLeft)
               {
                  index = charsLeft;
                  breakChars = 0;
               }
            }
         }

         int insertionPoint = lineLength_;
         if (index > 0)
            appendRawWithIndent(line.substring(0, index));
         wrap();
         onChunkWritten(line, index, insertionPoint);
         line = line.substring(Math.min(line.length(), index + breakChars));
         line = line.trim();
      }

      // Now just append the rest of the line
      appendRawWithIndent(line);
   }

   protected void onChunkWritten(String chunk, int length, int insertionPoint)
   {

   }

   protected boolean forceWrapBefore(String line)
   {
      return isEmpty(line);
   }

   protected boolean forceWrapAfter(String line)
   {
      return isEmpty(line);
   }

   private boolean isEmpty(String line)
   {
      return line.trim().length() == 0;
   }

   private void wrap()
   {
      if (output_.length() > 0)
         appendRaw("\n");
   }

   private void appendRawWithIndent(String value)
   {
      assert value.indexOf('\n') < 0;
      if (lineLength_ == 0 && indent_ != null)
         appendRaw(indent_);
      appendRaw(value);
   }

   private void appendRaw(String value)
   {
      if (value.length() == 0)
         return;

      output_.append(value);
      int index = value.lastIndexOf('\n');
      if (index < 0)
         lineLength_ += value.length();
      else
         lineLength_ = value.length() - (index + 1);
   }

   protected String indent_ = "";

   private StringBuilder output_ = new StringBuilder();
   private int lineLength_;
   private final int maxLineLength_;
   private final boolean hardWrapIfNecessary_;
}
