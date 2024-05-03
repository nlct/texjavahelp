/*
    Copyright (C) 2024 Nicola L.C. Talbot
    www.dickimaw-books.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package com.dickimawbooks.xml2bib;

import java.io.PrintWriter;
import java.io.IOException;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Entry
{
   public Entry(String key)
   {
      this(key, null);
   }

   public Entry(String key, String value)
   {
      this(key, value, null);
   }

   public Entry(String key, String value, EntryType type)
   {
      if (key == null)
      {
         throw new NullPointerException();
      }

      this.key = key;
      this.value = value;

      if (type == null)
      {
         type = EntryType.getTypeFromKey(key);
      }

      this.type = type;
   }

   public String getKey()
   {
      return key;
   }

   public String getValue()
   {
      return value;
   }

   public void setValue(String value)
   {
      this.value = value;
   }

   public EntryType getEntryType()
   {
      return type;
   }

   public void setParent(Entry parent)
   {
      this.parent = parent;
   }

   public Entry getParent()
   {
      return parent;
   }

   public void write(PrintWriter out) throws IOException
   {
      out.print("@");
      out.print(type.getBibType());
      out.print("{");
      out.print(key);

      if (value != null)
      {
         out.println(",");

         out.print("  name={");

         String cmd = type.getEncap();

         if (cmd == null)
         {
            out.print(encode(value));
         }
         else
         {
            out.print('\\');
            out.print(cmd);
            out.print('{');
            out.print(encode(value));
            out.print('}');
         }

         out.print("}");
      }

      if (parent != null)
      {
         out.println(",");
         out.print("  parent={");
         out.print(parent.getKey());
         out.print("}");
      }

      writeExtraFields(out);

      out.println("}");
   }

   public void writeExtraFields(PrintWriter out) throws IOException
   {
      out.println();
   }

   public static String encode(String str)
   {
      StringBuilder builder = new StringBuilder();

      int cp = -1;

      if (str.indexOf('{') > -1)
      {
         for (int i = 0; i < str.length(); i += Character.charCount(cp))
         {
            cp = str.codePointAt(i);

            if (cp == '\'')
            {
               i += Character.charCount(cp);

               cp = str.codePointAt(i);

               encodeTeXChars(builder, cp);
            }
            else if (cp == '{')
            {
               int idx = str.indexOf('}', i);

               if (idx > -1)
               {
                  String param = str.substring(i+1, idx);

                  Matcher m = PARAM_CHOICE.matcher(param);

                  if (m.matches())
                  {
                     builder.append("\\msgchoiceparam{");
                     builder.append(m.group(1));
                     builder.append("}{");

                     String[] split = m.group(2).split("\\|");

                     for (int j = 0; j < split.length; j++)
                     {
                        if (j > 0)
                        {
                           builder.append(',');
                        }

                        m = PARAM_CHOICE_VALUE.matcher(split[j]);

                        if (m.matches())
                        {
                           builder.append("\\msgchoiceparamitem{");
                           builder.append(m.group(1));
                           builder.append("}{");

                           builder.append(m.group(2).replaceAll("#", "="));

                           builder.append("}{");
                           encodeTeXChars(builder, m.group(3));
                           builder.append("}");
                        }
                     }

                     builder.append('}');
                  }
                  else
                  {
                     m = PARAM.matcher(param);

                     if (m.matches())
                     {
                        builder.append("\\msgparam{");
                        builder.append(m.group(1));
                        builder.append('}');
                     }
                  }

                  i = idx;
               }
               else
               {
                  builder.append("\\{");
               }
            }
            else
            {
               encodeTeXChars(builder, cp);
            }
         }
      }
      else
      {
         encodeTeXChars(builder, str);
      }

      return builder.toString();
   }

   public static void encodeTeXChars(StringBuilder builder, String str)
   {
      for (int i = 0; i < str.length(); )
      {
         int cp = str.codePointAt(i);

         if (cp == '.' && i + 2 < str.length()
             && str.charAt(i+1) == '.' && str.charAt(i+2) == '.')
         {
            builder.append("\\msgellipsis{...}");
            i += 2;
         }
         else if (cp == ':' && i == str.length() - 1)
         {
            builder.append("\\msgendcolon{:}");
         }
         else
         {
            encodeTeXChars(builder, cp);
         }

         i += Character.charCount(cp);
      }
   }

   public static void encodeTeXChars(StringBuilder builder, int cp)
   {
      if (cp == '#' || cp == '%' || cp == '_' || cp == '^'
       || cp == '\\' || cp == '$' || cp == '&'
       || cp == '{' || cp == '}')
      {
         builder.append("\\char`\\");
         builder.appendCodePoint(cp);
         builder.append(' ');
      }
      else
      {
         builder.appendCodePoint(cp);
      }
   }

   protected String key, value;
   protected EntryType type;
   protected Entry parent;

   public static final Pattern PARAM_CHOICE
    = Pattern.compile("(\\d+),choice,(.+)");

   public static final Pattern PARAM_CHOICE_VALUE
    = Pattern.compile("(\\d+)([#<>]+)(.*)");

   public static final Pattern PARAM
    = Pattern.compile("(\\d+)(,.*)?");
}
