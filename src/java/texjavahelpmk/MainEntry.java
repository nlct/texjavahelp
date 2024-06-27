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
import com.dickimawbooks.texparserlib.latex.*;

import com.dickimawbooks.texparserlib.latex.glossaries.*;

public class MainEntry extends AbstractGlsCommand
{
   public MainEntry(GlossariesSty sty)
   {
      this("mainentry", sty);
   }

   public MainEntry(String name, GlossariesSty sty)
   {
      super(name, sty);
   }

   @Override
   public Object clone()
   {
      return new MainEntry(getName(), sty);
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
    throws IOException
   {
      TeXParserListener listener = parser.getListener();
      TeXObjectList expanded = listener.createStack();

      GlsLabel glslabel = popEntryLabel(parser, stack);

      expanded.add(listener.getControlSequence("glsxtrglossentry"));
      expanded.add(TeXParserUtils.createGroup(listener, glslabel));
      expanded.add(listener.getControlSequence("glsadd"));
      expanded.add(TeXParserUtils.createGroup(listener, glslabel));

      return expanded;
   }

   @Override
   public void process(TeXParser parser, TeXObjectList stack)
    throws IOException
   {
      TeXParserListener listener = parser.getListener();
      TeXObjectList expanded = listener.createStack();

      GlsLabel glslabel = popEntryLabel(parser, stack);

      expanded.add(listener.getControlSequence("glsxtrglossentry"));
      expanded.add(glslabel);
      expanded.add(listener.getControlSequence("glsadd"));
      expanded.add(glslabel);

      TeXParserUtils.process(expanded, parser, stack);
   }

   @Override
   public void process(TeXParser parser)
    throws IOException
   {
      process(parser, parser);
   }
}
