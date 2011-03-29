package org.rstudio.core.client;

import org.rstudio.core.client.regex.Match;
import org.rstudio.core.client.regex.Pattern;

/**
 * Simulates a console that behaves like the R console, specifically with
 * regard to \r (carriage return) and \b (backspace) characters.
 */
public class VirtualConsole
{
   public VirtualConsole()
   {
   }

   public void submit(String data)
   {
      if (StringUtil.isNullOrEmpty(data))
         return;

      int tail = 0;
      Match match = CONTROL.match(data, 0);
      while (match != null)
      {
         int pos = match.getIndex();

         // If we passed over any plain text on the way to this control
         // character, add it.
         text(data.substring(tail, pos));

         tail = pos + 1;

         switch (data.charAt(pos))
         {
            case '\r':
               carriageReturn();
               break;
            case '\b':
               backspace();
               break;
            case '\n':
               newline();
               break;
            default:
               assert false : "Unknown control char, please check regex";
               text(data.charAt(pos) + "");
               break;
         }

         match = match.nextMatch();
      }

      // If there was any plain text after the last control character, add it
      text(data.substring(tail));
   }

   private void backspace()
   {
      if (pos == 0)
         return;
      o.deleteCharAt(--pos);
   }

   private void carriageReturn()
   {
      if (pos == 0)
         return;
      while (pos > 0 && o.charAt(pos - 1) != '\n')
         pos--;
      // Now we're either at the beginning of the buffer, or just past a '\n'
   }

   private void newline()
   {
      while (pos < o.length() && o.charAt(pos) != '\n')
         pos++;
      // Now we're either at the end of the buffer, or on top of a '\n'
      text("\n");
   }

   private void text(String text)
   {
      assert text.indexOf('\r') < 0 && text.indexOf('\b') < 0;

      o.replace(pos, pos + text.length(), text);
      pos += text.length();
   }

   @Override
   public String toString()
   {
      return o.toString();
   }

   public static String consolify(String text)
   {
      VirtualConsole console = new VirtualConsole();
      console.submit(text);
      return console.toString();
   }

   private final StringBuilder o = new StringBuilder();
   private int pos = 0;
   private static final Pattern CONTROL = Pattern.create("[\r\b\n]");
}
