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
import com.dickimawbooks.texparserlib.latex.CsvList;
import com.dickimawbooks.texparserlib.primitives.RomanNumeral;

public class GeneralMsg extends ControlSequence
{
   public GeneralMsg()
   {
      this("generalmsg", "");
   }

   public GeneralMsg(String name, String prefix)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new GeneralMsg(getName(), prefix);
   }

   @Override
   public void process(TeXParser parser)
     throws IOException
   {
      process(parser, parser);
   }

   @Override
   public void process(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      TeXParserListener listener = parser.getListener();

      CsvList csvList = TeXParserUtils.popOptCsvList(parser, stack);
      String label = popLabelString(parser, stack);
      TeXObject optArg = popOptArg(parser, stack);

      parser.startGroup();

      if (csvList != null)
      {
         for (int i = 0; i < csvList.size(); i++)
         {
            TeXObject arg = (TeXObjectList)csvList.getValue(i, true).clone();
            String csname = "msgparam@"+RomanNumeral.romannumeral(i+1);

            parser.putControlSequence(true, new GenericCommand(csname, null, arg));
         }
      }

      TeXObjectList expanded = listener.createStack();
      expanded.add(listener.getControlSequence("gls"));
      expanded.add(listener.createGroup(prefix+label));
      expanded.add(listener.getOther('['));

      if (optArg != null)
      {
         expanded.add(optArg, true);
      }

      expanded.add(listener.getOther(']'));

      TeXParserUtils.process(expanded, parser, stack);

      parser.endGroup();

   }

   String prefix;
}
