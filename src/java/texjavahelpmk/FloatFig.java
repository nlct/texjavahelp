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

/** Shortcut for figure environment in texjavahelp.sty but 
   with TeXJavaHelpSty the id is put at the top of the block.
 */
public class FloatFig extends ControlSequence
{
   public FloatFig()
   {
      this("FloatFig");
   }

   public FloatFig(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new FloatFig(getName());
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

      popOptArg(parser, stack);
      String label = popLabelString(parser, stack);
      TeXObject content = popArg(parser, stack);
      TeXObject lof = popOptArg(parser, stack);
      TeXObject caption = popArg(parser, stack);

      TeXObjectList expanded = listener.createStack();

      StartElement elem = listener.newHtml5StartElement("figure");
      elem.putAttribute("id", label);

      expanded.add(elem);

      listener.stepcounter("figure");
      ControlSequence figName = listener.getControlSequence("figurename");
      ControlSequence theFig = listener.getControlSequence("thefigure");

      TeXObjectList labelText = TeXParserUtils.createStack(listener,
       figName, listener.getSpace(), theFig);

      if (lof == null)
      {
         if (!caption.isEmpty())
         {
            labelText.add(listener.getSpace());
            labelText.add((TeXObject)caption.clone(), true);
         }
      }
      else if (!lof.isEmpty())
      {
         labelText.add(listener.getSpace());
         labelText.add(lof, true);
      }

      listener.provideLabel(label, labelText);

      expanded.add(content, true);

      expanded.add(listener.newHtml5StartElement("figcaption"));

      expanded.add(listener.getControlSequence("@makecaption"));

      expanded.add(TeXParserUtils.createGroup(listener,
       figName, listener.getSpace(), theFig));

      expanded.add(TeXParserUtils.createGroup(listener, caption));

      expanded.add(listener.newHtml5EndElement("figcaption"));

      expanded.add(listener.newHtml5EndElement("figure"));

      TeXParserUtils.process(expanded, parser, stack);
   }

}
