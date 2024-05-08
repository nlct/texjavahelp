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

import com.dickimawbooks.texparserlib.latex.KeyValList;
import com.dickimawbooks.texparserlib.latex.MissingValue;

import com.dickimawbooks.texparserlib.latex.glossaries.GlossariesSty;
import com.dickimawbooks.texparserlib.latex.glossaries.AbstractGlsCommand;
import com.dickimawbooks.texparserlib.latex.glossaries.GlsLabel;
import com.dickimawbooks.texparserlib.latex.glossaries.GlossaryEntry;

public class MenuTrail extends AbstractGlsCommand
{
   public MenuTrail(GlossariesSty sty)
   {
      this("menutrail", sty);
   }

   public MenuTrail(String name, GlossariesSty sty)
   {
      super(name, sty);
   }

   @Override
   public Object clone()
   {
      return new MenuTrail(getName(), sty);
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      TeXParserListener listener = parser.getListener();

      GlsLabel glslabel = popEntryLabel(parser, stack);

      TeXObjectList expanded = parser.getListener().createStack();

      pushEntry(expanded, glslabel.getEntry(), parser, stack);

      return expanded;
   }

   protected void pushEntry(TeXObjectList expanded, GlossaryEntry entry,
     TeXParser parser, TeXObjectList stack)
   throws IOException
   {
      TeXParserListener listener = parser.getListener();

      if (entry != null)
      {
         expanded.push(listener.createGroup(entry.getLabel()));
         expanded.push(listener.getControlSequence("glsmenuitemref"));

         GlossaryEntry parentEntry = entry.getParent(stack);

         if (parentEntry != null && "menu".equals(parentEntry.getCategory()))
         {
            expanded.push(listener.getControlSequence("menusep"));
            pushEntry(expanded, parentEntry, parser, stack);
         }
      }
   }
}
