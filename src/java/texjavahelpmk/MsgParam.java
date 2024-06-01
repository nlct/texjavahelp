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

package com.dickimawbooks.texjavahelpmk;

import java.io.IOException;

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.texparserlib.primitives.RomanNumeral;

public class MsgParam extends Command
{
   public MsgParam()
   {
      this("msgparam");
   }

   public MsgParam(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new MsgParam(getName());
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      TeXParserListener listener = parser.getListener();
      int n = popInt(parser, stack);

      TeXObjectList expanded = listener.createStack();

      ControlSequence cs = parser.getControlSequence(
        "msgparam@"+RomanNumeral.romannumeral(n));

      if (cs == null)
      {
         expanded.add(listener.getControlSequence("meta"));
         expanded.add(listener.createGroup("param-"+n));
      }
      else
      {
         expanded.add(cs);
      }

      return expanded;
   }

}
