package org.rstudio.studio.client.workbench.views.vcs.diff;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.rstudio.core.client.SafeHtmlUtil;

public class LineActionButtonRenderer
{
   interface Resources
   {
      ImageResource buttonLeft();

      @ImageOptions(repeatStyle = ImageResource.RepeatStyle.Horizontal)
      ImageResource buttonTile();

      ImageResource buttonRight();

      Styles styles();
   }

   interface Styles extends CssResource
   {
      String button();
      String left();
      String center();
      String right();
   }

   interface BlueResources extends Resources, ClientBundle
   {
      @Override
      @Source("images/SmallBlueButtonLeft.png")
      ImageResource buttonLeft();

      @Override
      @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
      @Source("images/SmallBlueButtonTile.png")
      ImageResource buttonTile();

      @Override
      @Source("images/SmallBlueButtonRight.png")
      ImageResource buttonRight();

      @Source("LineActionButton.css")
      BlueStyles styles();
   }

   interface BlueStyles extends Styles
   {}

   interface GrayResources extends Resources, ClientBundle
   {
      @Override
      @Source("images/SmallGrayButtonLeft.png")
      ImageResource buttonLeft();

      @Override
      @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
      @Source("images/SmallGrayButtonTile.png")
      ImageResource buttonTile();

      @Override
      @Source("images/SmallGrayButtonRight.png")
      ImageResource buttonRight();

      @Source("LineActionButton.css")
      GrayStyles styles();
   }

   interface GrayStyles extends Styles
   {}

   public static LineActionButtonRenderer createBlue()
   {
      return new LineActionButtonRenderer(GWT.<Resources>create(BlueResources.class));
   }

   public static LineActionButtonRenderer createGray()
   {
      return new LineActionButtonRenderer(GWT.<Resources>create(GrayResources.class));
   }

   protected LineActionButtonRenderer(Resources resources)
   {
      resources_ = resources;
      resources_.styles().ensureInjected();
   }

   public void render(SafeHtmlBuilder builder, String text, String action)
   {
      {
         builder.append(SafeHtmlUtil.createOpenTag(
               "div",
               "class", resources_.styles().button(),
               "data-action", action));
         {
               builder.append(SafeHtmlUtil.createOpenTag(
                     "div",
                     "class", resources_.styles().left()));
               builder.appendHtmlConstant("<br/></div>");

               builder.append(SafeHtmlUtil.createOpenTag(
                     "div",
                     "class", resources_.styles().center()));
               {
                  builder.appendEscaped(text);
               }
               builder.appendHtmlConstant("</div>");

               builder.append(SafeHtmlUtil.createOpenTag(
                     "div",
                     "class", resources_.styles().right()));
               builder.appendHtmlConstant("<br/></div>");
         }
         builder.appendHtmlConstant("</div>");
      }
   }

   private final Resources resources_;
}
