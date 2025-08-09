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

import com.dickimawbooks.texparserlib.latex.KeyValList;

import com.dickimawbooks.texparserlib.latex.glossaries.GlossariesSty;
import com.dickimawbooks.texparserlib.latex.glossaries.AbstractGlsCommand;
import com.dickimawbooks.texparserlib.latex.glossaries.GlsLabel;
import com.dickimawbooks.texparserlib.latex.glossaries.GlossaryEntry;

public class MainGls extends AbstractGlsCommand
{
   public MainGls(GlossariesSty sty)
   {
      this("maingls", sty);
   }

   public MainGls(String name, GlossariesSty sty)
   {
      super(name, sty);
   }

   @Override
   public Object clone()
   {
      return new MainGls(getName(), sty);
   }

   @Override
   public boolean canExpand()
   {
      return false;
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      return null;
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

      KeyValList options = TeXParserUtils.popOptKeyValList(parser, stack);

      GlsLabel glslabel = popEntryLabel(parser, stack);

      TeXObjectList expanded = listener.createStack();

      expanded.add(listener.getControlSequence("mainglsadd"));
      expanded.add(glslabel);
      expanded.add(listener.createGroup("term"));

      TeXParserUtils.process(expanded, parser, stack);

      expanded.add(listener.getControlSequence("gls"));

      if (options != null)
      {
         expanded.add(listener.getOther('['));
         expanded.add(options);
         expanded.add(listener.getOther(']'));
      }

      expanded.add(glslabel);

      TeXParserUtils.process(expanded, parser, stack);
   }

}
