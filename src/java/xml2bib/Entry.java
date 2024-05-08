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

import java.util.HashMap;
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

      if (type == null)
      {
         type = EntryType.getTypeFromKey(key);
      }

      this.type = type;

      put("name", value,
        !(key.startsWith("index") || key.startsWith("manual")));
   }

   @Override
   public boolean equals(Object other)
   {
      if (this == other) return true;

      if (other == null || !(other instanceof Entry)) return false;

      return key.equals(((Entry)other).key);
   }

   public String getKey()
   {
      return key;
   }

   public EntryType getEntryType()
   {
      return type;
   }

   public void setParent(Entry parentEntry)
   {
      setParent(parentEntry.getKey());
   }

   public void setParent(String parent)
   {
      this.parent = parent;
   }

   public String getParent()
   {
      return parent;
   }

   public void put(String field, String value)
   {
      put(field, value, true);
   }

   public void put(String field, String value, boolean encode)
   {
      if (field.equals("parent"))
      {
         setParent(value);
      }
      else
      {
         if (fields == null)
         {
            fields = new HashMap<String,FieldValue>();
         }

         fields.put(field, new FieldValue(value, encode));
      }
   }

   public String get(String field)
   {
      if (field.equals("parent"))
      {
         return getParent();
      }
      else if (fields == null)
      {
         return null;
      }
      else
      {
         FieldValue value =  fields.get(field);

         return value == null ? null : value.getValue();
      }
   }

   public void write(PrintWriter out) throws IOException
   {
      out.print("@");
      out.print(type.getBibType());
      out.print("{");
      out.print(key);

      if (parent != null)
      {
         out.println(",");
         out.print("  parent={");
         out.print(parent);
         out.print("}");
      }

      writeFields(out);

      out.println("}");
   }

   public void writeFields(PrintWriter out) throws IOException
   {
      if (fields != null)
      {
         for (String field : fields.keySet())
         {
            FieldValue fieldVal = fields.get(field);

            String val = fieldVal.getValue();

            out.println(",");

            if (fieldVal.encode)
            {
               val = encode(val);
            }

            out.format("  %s={%s}", field, val);
         }
      }

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
      if (cp == '#' || cp == '%' || cp == '_'
       || cp == '$' || cp == '&'
       || cp == '{' || cp == '}')
      {
         builder.appendCodePoint('\\');
         builder.appendCodePoint(cp);
      }
      else if (cp == '^' || cp == '\\' || cp == '~')
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

   @Override
   public String toString()
   {
      return String.format("%s[key=%s,parent=%s,type=%s,fields=%s]",
        getClass().getSimpleName(), key, parent, type, fields);
   }

   protected String key, parent;
   protected EntryType type;

   protected HashMap<String,FieldValue> fields;

   public static final Pattern PARAM_CHOICE
    = Pattern.compile("(\\d+),choice,(.+)");

   public static final Pattern PARAM_CHOICE_VALUE
    = Pattern.compile("(\\d+)([#<>]+)(.*)");

   public static final Pattern PARAM
    = Pattern.compile("(\\d+)(,.*)?");
}

class FieldValue
{
   public FieldValue(String value)
   {
      this(value, true);
   }

   public FieldValue(String value, boolean encode)
   {
      this.value = value;
      this.encode = encode;
   }

   public String getValue()
   {
      return value;
   }

   public boolean requiresEncoding()
   {
      return encode;
   }

   @Override
   public String toString()
   {
      return value;
   }

   protected String value;
   protected boolean encode;
}
