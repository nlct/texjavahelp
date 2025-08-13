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

import com.dickimawbooks.texparserlib.latex.glossaries.GlossariesSty;
import com.dickimawbooks.texparserlib.latex.glossaries.AbstractGlsCommand;
import com.dickimawbooks.texparserlib.latex.glossaries.GlsLabel;

public class IfComponent extends AbstractGlsCommand
{
   public IfComponent(GlossariesSty sty)
   {
      this("ifcomponent", sty);
   }

   public IfComponent(String name, GlossariesSty sty)
   {
      super(name, sty);
   }

   @Override
   public Object clone()
   {
      return new IfComponent(getName(), sty);
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      TeXParserListener listener = parser.getListener();

      GlsLabel glslabel = popEntryLabel(parser, stack);
      TeXObject trueArg = popArg(parser, stack);
      TeXObject falseArg = popArg(parser, stack);

      String category = glslabel.getCategory();

      TeXObjectList expanded;

      if ("button".equals(category) || "widget".equals(category)
         || "dialog".equals(category))
      {
         if (parser.isStack(trueArg))
         {
            expanded = (TeXObjectList)trueArg;
         }
         else
         {
            expanded = parser.getListener().createStack();
            expanded.add(trueArg);
         }
      }
      else
      {
         if (parser.isStack(falseArg))
         {
            expanded = (TeXObjectList)falseArg;
         }
         else
         {
            expanded = parser.getListener().createStack();
            expanded.add(falseArg);
         }
      }

      return expanded;
   }

}
