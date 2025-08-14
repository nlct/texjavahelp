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

package com.dickimawbooks.texjavahelpmk;

import java.io.IOException;

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.texparserlib.latex.EnumerateDec;
import com.dickimawbooks.texparserlib.html.StartElement;
import com.dickimawbooks.texparserlib.html.EndElement;

public class NumberedDec extends EnumerateDec
{
   public NumberedDec()
   {
      this("numbered");
   }

   public NumberedDec(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new NumberedDec(getName());
   }

   @Override
   public void setup(TeXParser parser, TeXObjectList stack) throws IOException
   {
      TJHListener listener = (TJHListener)parser.getListener();

      listener.writeln("<div class=\"numbered\">");

      parser.putControlSequence(true, new GenericCommand(true,
        "theenumi", null, TeXParserUtils.createStack(listener,
          new TeXCsRef("@arabic"), new TeXCsRef("c@enumi"))));

      parser.putControlSequence(true, new GenericCommand(true,
        "theenumii", null, TeXParserUtils.createStack(listener,
          new TeXCsRef("theenumi"), listener.getOther('.'),
          new TeXCsRef("@arabic"), new TeXCsRef("c@enumii"))));

      parser.putControlSequence(true, new GenericCommand(true,
        "theenumiii", null, TeXParserUtils.createStack(listener,
          new TeXCsRef("theenumii"), listener.getOther('.'),
          new TeXCsRef("@arabic"), new TeXCsRef("c@enumiii"))));

      parser.putControlSequence(true, new GenericCommand(true,
        "theenumiv", null, TeXParserUtils.createStack(listener,
          new TeXCsRef("theenumiii"), listener.getOther('.'),
          new TeXCsRef("@arabic"), new TeXCsRef("c@enumiv"))));

      parser.putControlSequence(true, new GenericCommand(true,
        "labelenumi", null, TeXParserUtils.createStack(listener,
          new TeXCsRef("theenumi"), listener.getOther('.'),
          listener.getSpace())));

      parser.putControlSequence(true, new GenericCommand(true,
        "labelenumii", null, TeXParserUtils.createStack(listener,
          new TeXCsRef("theenumii"), listener.getOther('.'),
          listener.getSpace())));

      parser.putControlSequence(true, new GenericCommand(true,
        "labelenumiii", null, TeXParserUtils.createStack(listener,
          new TeXCsRef("theenumiii"), listener.getOther('.'),
          listener.getSpace())));

      parser.putControlSequence(true, new GenericCommand(true,
        "labelenumiv", null, TeXParserUtils.createStack(listener,
          new TeXCsRef("theenumiv"), listener.getOther('.'),
          listener.getSpace())));

      super.setup(parser, stack);
   }

   @Override
   public void end(TeXParser parser, TeXObjectList stack)
    throws IOException
   {
      TJHListener listener = (TJHListener)parser.getListener();

      super.end(parser, stack);

      listener.writeln("</div>");
   }
}
