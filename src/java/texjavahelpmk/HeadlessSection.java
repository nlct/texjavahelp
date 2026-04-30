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
import com.dickimawbooks.texparserlib.html.*;

public class HeadlessSection extends ControlSequence
{
   public HeadlessSection()
   {
      this("HeadlessSection");
   }

   public HeadlessSection(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new HeadlessSection(getName());
   }

   @Override
   public void process(TeXParser parser, TeXObjectList stack)
   throws IOException
   {
      L2HConverter listener = (L2HConverter)parser.getListener();

      String label = popLabelString(parser, stack);
      String sectionType = popLabelString(parser, stack);
      TeXObject nodeTitle = popArg(parser, stack);

      listener.startSection(false, sectionType, sectionType, label, stack);

      listener.setCurrentBlockType(DocumentBlockType.BODY);
   }

   @Override
   public void process(TeXParser parser)
   throws IOException
   {
      process(parser, parser);
   }

}
