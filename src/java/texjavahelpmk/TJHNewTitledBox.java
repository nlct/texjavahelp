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
import java.awt.Color;

import com.dickimawbooks.texparserlib.*;

public class TJHNewTitledBox extends ControlSequence
{
   public TJHNewTitledBox(TeXJavaHelpSty sty)
   {
      this("tjhnewtitledbox", sty);
   }

   public TJHNewTitledBox(String name, TeXJavaHelpSty sty)
   {
      super(name);
      this.sty = sty;
   }

   @Override
   public Object clone()
   {
      return new TJHNewTitledBox(getName(), sty);
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
      TJHListener listener = (TJHListener)parser.getListener();

      String envname = popLabelString(parser, stack);
      TeXObject title = popArg(parser, stack);
      String colour = popLabelString(parser, stack).trim();

      Color frameCol = sty.getColorSty().getColor(parser, "named", colour);

      int r = Math.max(240, (frameCol.getRed()+765)/4);
      int g = Math.max(240, (frameCol.getGreen()+765)/4);
      int b = Math.max(240, (frameCol.getBlue()+765)/4);

      Color bg = new Color(r, g, b);

      sty.addTaggedColourBox(envname, false, (TeXFontText)null,
        (Color)null, bg, frameCol, title);
   }

   TeXJavaHelpSty sty;
}
