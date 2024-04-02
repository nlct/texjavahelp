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

import com.dickimawbooks.texparserlib.TeXParser;
import com.dickimawbooks.texparserlib.TeXObjectList;
import com.dickimawbooks.texparserlib.ControlSequence;
import com.dickimawbooks.texparserlib.TeXParserUtils;

import com.dickimawbooks.texparserlib.latex.glossaries.GlossariesSty;
import com.dickimawbooks.texparserlib.latex.glossaries.AbstractGlsCommand;
import com.dickimawbooks.texparserlib.latex.glossaries.GlsLabel;

public class FilterTerms extends AbstractGlsCommand
{
   public FilterTerms(GlossariesSty sty)
   {
      this("filterterms", sty);
   }

   public FilterTerms(String name, GlossariesSty sty)
   {
      super(name, sty);
   }

   @Override
   public Object clone()
   {
      return new FilterTerms(getName(), sty);
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      GlsLabel glslabel = popEntryLabel(parser, stack);

      String catLabel = glslabel.getCategory();

      TeXObjectList expanded = parser.getListener().createStack();

      if (catLabel == null
        || !(catLabel.equals("term") || catLabel.equals("abbreviation")))
      {
         ControlSequence cs 
           = parser.getListener().getControlSequence("printunsrtglossaryskipentry");

         expanded.add(cs);
      }

      return expanded;
   }

   @Override
   public void process(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      GlsLabel glslabel = popEntryLabel(parser, stack);

      String catLabel = glslabel.getCategory();

      if (catLabel == null
        || !(catLabel.equals("term") || catLabel.equals("abbreviation")))
      {
         ControlSequence cs 
           = parser.getListener().getControlSequence("printunsrtglossaryskipentry");

         TeXParserUtils.process(cs, parser, stack);
      }
   }

   @Override
   public void process(TeXParser parser)
     throws IOException
   {
      process(parser, parser);
   }

}
