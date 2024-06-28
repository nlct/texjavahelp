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

public class SectionEntry extends AbstractGlsCommand
{
   public SectionEntry(GlossariesSty sty)
   {
      this("sectionentry", sty);
   }

   public SectionEntry(String name, GlossariesSty sty)
   {
      super(name, sty);
   }

   @Override
   public Object clone()
   {
      return new SectionEntry(getName(), sty);
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
    throws IOException
   {
      TeXParserListener listener = parser.getListener();
      TeXObjectList expanded = listener.createStack();

      TeXObject secCs = popOptArg(parser, stack);
      String secLabel = popOptLabelString(parser, stack);
      GlsLabel glslabel = popEntryLabel(parser, stack);

      if (secCs == null)
      {
         expanded.add(listener.getControlSequence("section"));
      }
      else
      {
         expanded.add(secCs, true);
      }

      Group grp = listener.createGroup();
      expanded.add(grp);

      grp.add(listener.getControlSequence("glsxtrglossentry"));
      grp.add(TeXParserUtils.createGroup(listener, glslabel));

      expanded.add(listener.getControlSequence("label"));

      if (secLabel == null)
      {
         expanded.addAll(listener.createString("sec:"+glslabel.getLabel()));
      }
      else
      {
         expanded.addAll(listener.createString(secLabel));
      }

      expanded.add(listener.getControlSequence("glsadd"));
      expanded.add(TeXParserUtils.createGroup(listener, glslabel));

      return expanded;
   }

}
