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

   public CLIArgValue(String value, int intVal)
   {
      this(value);
      this.intValue = intVal;
   }

   public static CLIArgValue create(TeXJavaHelpLib helpLib, String option,
      String value, CLIArgValueType type)
    throws InvalidSyntaxException
   {
      if (value == null)
      {
         throw new InvalidSyntaxException(helpLib.getMessage(
           "error.clisyntax.missing.value", option));
      }

      CLIArgValue argValue = new CLIArgValue(value);

      switch (type)
      {
         case INT:

           try
           {
              argValue.intValue = Integer.parseInt(value);
           }
           catch (NumberFormatException e)
           {
              throw new InvalidSyntaxException(helpLib.getMessage(
                "error.clisyntax.invalid.int_value", option, value));
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

   public int intValue()
   {
      return intValue;
   }

   public String[] listValue()
   {
      return listValue;
   }

   private String stringValue;
   private int intValue;
   private String[] listValue;
}
