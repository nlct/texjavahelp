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
import com.dickimawbooks.texparserlib.latex.AtGobble;

import com.dickimawbooks.texparserlib.latex.glossaries.GlossariesSty;
import com.dickimawbooks.texparserlib.latex.glossaries.AbstractGlsCommand;
import com.dickimawbooks.texparserlib.latex.glossaries.GlsLabel;
import com.dickimawbooks.texparserlib.latex.glossaries.GlossaryEntry;

public class MenuItemCs extends AbstractGlsCommand
{
   public MenuItemCs(GlossariesSty sty)
   {
      this("menuitem", sty);
   }

   public MenuItemCs(String name, GlossariesSty sty)
   {
      super(name, sty);
   }

   @Override
   public Object clone()
   {
      return new MenuItemCs(getName(), sty);
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

      boolean isStar = (popModifier(parser, stack, '*') != -1);

      KeyValList options = TeXParserUtils.popOptKeyValList(parser, stack);

      if (options == null)
      {
         options = new KeyValList();
      }

      if (isStar && !options.containsKey("format"))
      {
         options.put("format", listener.createString("glsignore"));
      }

      String labelPrefix = popOptLabelString(parser, stack);

      if (labelPrefix == null)
      {
         labelPrefix = "menu.";
      }

      setEntryLabelPrefix(labelPrefix);

      GlsLabel glslabel = popEntryLabel(parser, stack);

      parser.startGroup();

      parser.putControlSequence(true, new AtGobble("msgellipsis"));

      TeXObjectList expanded = listener.createStack();

      expanded.add(listener.getControlSequence("gls"));
      expanded.add(listener.getOther('['));

      expanded.add(options);

      expanded.add(listener.getOther(']'));
      expanded.add(TeXParserUtils.createGroup(listener, glslabel));

      TeXParserUtils.process(expanded, parser, stack);

      parser.endGroup();
   }

}
