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
import com.dickimawbooks.texparserlib.latex.*;

public class PreFrontMatterBlock extends Declaration
{
   public PreFrontMatterBlock()
   {
      this("prefrontmatterblock");
   }

   public PreFrontMatterBlock(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new PreFrontMatterBlock(getName());
   }

   @Override
   public boolean canExpand()
   {
      return true;
   }

   @Override
   public boolean isModeSwitcher()
   {
      return false;
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
    throws IOException
   {
      TeXParserListener listener = parser.getListener();

      String label = popLabelString(parser, stack);
      TeXObject titleArg = popArg(parser, stack);

      TeXObjectList expanded = listener.createStack();

      ControlSequence cs = listener.getControlSequence("HeadlessSection");

      expanded.add(cs);

      expanded.add(listener.createGroup(label));
      expanded.add(listener.createGroup("chapter"));
      expanded.add(TeXParserUtils.createGroup(listener, titleArg));

      return expanded;
   }

   @Override
   public void end(TeXParser parser, TeXObjectList stack)
    throws IOException
   {
   }
}
