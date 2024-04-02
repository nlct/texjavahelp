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
import com.dickimawbooks.texparserlib.primitives.IfTrue;
import com.dickimawbooks.texparserlib.latex.KeyValList;
import com.dickimawbooks.texparserlib.latex.AtGobble;
import com.dickimawbooks.texparserlib.latex.glossaries.GlossariesSty;
import com.dickimawbooks.texparserlib.latex.glossaries.PrintUnsrtGlossary;

public class PrintMain extends PrintUnsrtGlossary
{
   public PrintMain(GlossariesSty sty)
   {
      this("printmain", sty);
   }

   public PrintMain(String name, GlossariesSty sty)
   {
      super(name, sty);
   }

   @Override
   public Object clone()
   {
      return new PrintMain(getName(), sty);
   }

   @Override
   public void process(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      parser.startGroup();

      parser.putControlSequence(true, new IfTrue("ifglsnogroupskip"));

      parser.putControlSequence(true, new GenericCommand(true,
          "@@glossaryseclabel", null, TeXParserUtils.createStack(parser,
          new TeXCsRef("label"), parser.getListener().createGroup("glossary"))));

      parser.putControlSequence(true, new AtGobble("glossaryentrynumbers"));

      KeyValList options = sty.popOptKeyValList(stack);

      sty.setGlossaryStyle("tree", stack);
      ControlSequence cs = parser.getListener().getControlSequence("printmaininit");

      TeXParserUtils.process(cs, parser, stack);

      doGlossary(options, parser, stack);

      parser.endGroup();
   }

}
