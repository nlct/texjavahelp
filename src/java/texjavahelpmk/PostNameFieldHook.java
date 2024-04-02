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
import java.awt.Color;
import java.util.Vector;

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.texparserlib.latex.*;
import com.dickimawbooks.texparserlib.latex.glossaries.*;

public class PostNameFieldHook extends AbstractGlsCommand
{
   public PostNameFieldHook(String name, String field, GlossariesSty sty)
   {
      this(name, field, new TeXCsRef("space"), null, sty);
   }

   public PostNameFieldHook(String name, String field, TeXObject preObject,
     TeXObject postObject, GlossariesSty sty)
   {
      super(name, sty);
      this.field = field;
      this.preObject = preObject;
      this.postObject = postObject;
   }

   @Override
   public Object clone()
   {
      return new PostNameFieldHook(getName(), field, preObject, postObject, getSty());
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
   public void process(TeXParser parser, TeXObjectList stack)
   throws IOException
   {
      TeXParserListener listener = parser.getListener();

      ControlSequence labelCs = listener.getControlSequence("glscurrententrylabel");

      GlossaryEntry entry = null;

      if (labelCs instanceof GlsLabel)
      {
         entry = ((GlsLabel)labelCs).getEntry();
      }

      if (entry == null)
      {
         entry = sty.getEntry(parser.expandToString(labelCs, stack));
      }

      if (entry != null)
      {
         TeXObject fieldVal = entry.get(field);

         if (fieldVal != null)
         {
            TeXObjectList list = listener.createStack();

            if (preObject != null)
            {
               list.add((TeXObject)preObject.clone(), true);
            }

            list.add(fieldVal, true);

            if (postObject != null)
            {
               list.add((TeXObject)postObject.clone(), true);
            }

            TeXParserUtils.process(list, parser, stack);
         }
      }
   }

   @Override
   public void process(TeXParser parser)
   throws IOException
   {
      process(parser, parser);
   }

   protected String field;
   protected TeXObject preObject, postObject;
}
