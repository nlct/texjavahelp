/*
    Copyright (C) 2026 Nicola L.C. Talbot
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

public class ClsFile extends AbstractGlsCommand
{
   public ClsFile(GlossariesSty sty)
   {
      this("clsfile", sty);
   }

   public ClsFile(String name, GlossariesSty sty)
   {
      super(name, sty);
      setEntryLabelPrefix("cls.");
   }

   @Override
   public Object clone()
   {
      return new ClsFile(getName(), sty);
   }

   @Override
   public boolean canExpand()
   {
      return true;
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
   throws IOException
   {
      TeXParserListener listener = parser.getListener();

      KeyValList opts = TeXParserUtils.popOptKeyValList(parser, stack);

      GlsLabel glsLabel = popEntryLabel(parser, stack);

      String base = glsLabel.getLabel();
      String prefix = getEntryLabelPrefix();

      if (base.startsWith(prefix))
      {
         base = base.substring(prefix.length());
      }

      TeXObjectList expanded = listener.createStack();

      expanded.add(new TeXCsRef("glslink"));

      if (opts != null)
      {
         expanded.add(listener.getOther('['));
         expanded.add(opts);
         expanded.add(listener.getOther(']'));
      }

      expanded.add(TeXParserUtils.createGroup(listener, glsLabel));

      expanded.add(TeXParserUtils.createGroup(listener,
       new TeXCsRef("filefmt"),
       listener.createGroup(base+".cls")));

      return expanded;
   }
}
