/*
    Copyright (C) 2024-2025 Nicola L.C. Talbot
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
import com.dickimawbooks.texparserlib.latex.CsvList;
import com.dickimawbooks.texparserlib.html.StartElement;

/** Shortcut for figure environment with sub-figures in texjavahelp.sty but 
   with TeXJavaHelpSty the id is put at the top of the block.
 */
public class FloatSubFigs extends ControlSequence
{
   public FloatSubFigs()
   {
      this("FloatSubFigs");
   }

   public FloatSubFigs(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new FloatSubFigs(getName());
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

      listener.setcounter("subfigure", UserNumber.ZERO);

      boolean isStar = (popModifier(parser, stack, '*') == '*');

      popOptArg(parser, stack);
      String label = popLabelString(parser, stack);
      CsvList csvList = TeXParserUtils.popCsvList(parser, stack);
      TeXObject lof = popOptArg(parser, stack);
      TeXObject caption = popArg(parser, stack);

      TeXObjectList expanded = listener.createStack();

      StartElement elem = listener.newHtml5StartElement("figure", "div", true, true);
      elem.putAttribute("id", label);

      expanded.add(elem);

      listener.stepcounter("figure");

      ControlSequence cs = parser.getControlSequence("fnum@figure");

      TeXObject captionPrefix;
      TeXObjectList labelText;

      if (cs == null)
      {
         ControlSequence figName = parser.getControlSequence("figurecaptionname");

         if (figName == null)
         {
            figName = listener.getControlSequence("figurename");
         }

         ControlSequence theFig = listener.getControlSequence("thefigure");

         labelText = TeXParserUtils.createStack(listener,
           figName, listener.getSpace(), theFig);
         captionPrefix =  TeXParserUtils.createStack(listener,
           figName, listener.getSpace(), theFig);
      }
      else
      {
         labelText = TeXParserUtils.createStack(listener, cs);
         captionPrefix = cs;
      }

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
      boolean addLineBreak = isStar;

      for (int i = 0; i < csvList.size(); i++)
      {
         TeXObject obj = csvList.getValue(i, true);

         if (obj.isEmpty()) continue;

         expanded.add(listener.getControlSequence("SubFigureContent"));

         if (parser.isStack(obj))
         {
            expanded.addAll((TeXObjectList)obj);
         }
         else
         {
            expanded.add(obj);
         }

         if (addLineBreak)
         {
            expanded.add(listener.getControlSequence("newline"));
            addLineBreak = false;
         }
         else
         {
            expanded.add(listener.getSpace());
         }
      }

      expanded.add(listener.newHtml5StartElement("figcaption", "div", true, true));

      expanded.add(listener.getControlSequence("@makecaption"));

      expanded.add(TeXParserUtils.createGroup(listener, captionPrefix));

      expanded.add(TeXParserUtils.createGroup(listener, caption));

      expanded.add(listener.newHtml5EndElement("figcaption", "div", true, true));

      expanded.add(listener.newHtml5EndElement("figure", "div", true, true));

      TeXParserUtils.process(expanded, parser, stack);
   }

}
