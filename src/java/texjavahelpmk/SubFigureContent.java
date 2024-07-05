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
import com.dickimawbooks.texparserlib.html.StartElement;
import com.dickimawbooks.texparserlib.html.EndElement;

public class SubFigureContent extends ControlSequence
{
   public SubFigureContent()
   {
      this("SubFigureContent");
   }

   public SubFigureContent(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new SubFigureContent(getName());
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

      String label = popLabelString(parser, stack);
      TeXObject content = popArg(parser, stack);
      TeXObject lof = popOptArg(parser, stack);
      TeXObject caption = popArg(parser, stack);

      TeXObjectList expanded = listener.createStack();

      listener.stepcounter("subfigure");

      StartElement elem = new StartElement("div");
      elem.putAttribute("class", "subfigure");
      elem.putAttribute("id", label);

      expanded.add(elem);

      TeXObjectList labelText;

      ControlSequence theFig = listener.getControlSequence("thefigure");
      ControlSequence theSubFigLabel = listener.getControlSequence("subfigurelabel");

      if (lof == null)
      {
         labelText = TeXParserUtils.createStack(listener,
           theFig, theSubFigLabel, listener.getSpace());

         labelText.add((TeXObject)caption.clone(), true);
      }
      else
      {
         labelText = TeXParserUtils.createStack(listener,
           theFig, theSubFigLabel, listener.getSpace(), lof);
      }

      listener.provideLabel(label, labelText);

      expanded.add(content, true);

      expanded.add(listener.createVoidElement("br"));

      expanded.add(listener.getControlSequence("subfigurecap"));
      expanded.add(TeXParserUtils.createGroup(listener,
         caption));

      expanded.add(new EndElement("div"));

      TeXParserUtils.process(expanded, parser, stack);
   }
}
