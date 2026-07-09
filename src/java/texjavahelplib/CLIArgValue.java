/*
    Copyright (C) 2025 Nicola L.C. Talbot
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
package com.dickimawbooks.texjavahelplib;

// adapted from com.dickimawbooks.bibgls.common.BibGlsArgValue

public class CLIArgValue
{
   public CLIArgValue(String value)
   {
      if (value == null)
      {
         throw new NullPointerException();
      }

      stringValue = value;
   }

   public CLIArgValue(String value, Number numValue)
   {
      this(value);
      this.numValue = numValue;
   }

   public static CLIArgValue create(TeXJavaHelpLib helpLib, String option,
      String value, CLIArgValueType type)
    throws InvalidSyntaxException
   {
      if (value == null)
      {
         throw new InvalidSyntaxException(helpLib.getMessage(
           "error.clisyntax.missing_value", option));
      }

      CLIArgValue argValue = new CLIArgValue(value);

      switch (type)
      {
         case INT:

           try
           {
              argValue.numValue = Integer.valueOf(value);
           }
           catch (NumberFormatException e)
           {
              throw new InvalidSyntaxException(helpLib.getMessage(
                "error.clisyntax.invalid.int_value", option, value));
           }

         break;

         case LONG:

           try
           {
              argValue.numValue = Long.valueOf(value);
           }
           catch (NumberFormatException e)
           {
              throw new InvalidSyntaxException(helpLib.getMessage(
                "error.clisyntax.invalid.long_value", option, value));
           }

         break;

         case FLOAT:

           try
           {
              argValue.numValue = Float.valueOf(value);
           }
           catch (NumberFormatException e)
           {
              throw new InvalidSyntaxException(helpLib.getMessage(
                "error.clisyntax.invalid.float_value", option, value));
           }

         break;

         case DOUBLE:

           try
           {
              argValue.numValue = Double.valueOf(value);
           }
           catch (NumberFormatException e)
           {
              throw new InvalidSyntaxException(helpLib.getMessage(
                "error.clisyntax.invalid.double_value", option, value));
           }

         break;

         case LIST:

           argValue.listValue = value.trim().split(" *, *");

         break;
      }

      return argValue;
   }

   public String toString()
   {
      return stringValue;
   }

   public Number numberValue()
   {
      return numValue;
   }

   public int intValue()
   {
      return numValue == null ? 0 : numValue.intValue();
   }

   public long longValue()
   {
      return numValue == null ? 0L : numValue.longValue();
   }

   public double doubleValue()
   {
      return numValue == null ? 0 : numValue.doubleValue();
   }

   public float floatValue()
   {
      return numValue == null ? 0f : numValue.floatValue();
   }

   public String[] listValue()
   {
      return listValue;
   }

   private String stringValue;
   private Number numValue;
   private String[] listValue;
}
