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
import java.util.Iterator;

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.texparserlib.latex.KeyValList;
import com.dickimawbooks.texparserlib.html.StartElement;

/** Starts an HTML div.
 * The optional argument provides the element attributes.
 */
public class BeginHTMLBlock extends Command
{
   public BeginHTMLBlock()
   {
      this("BeginHTMLBlock");
   }

   public BeginHTMLBlock(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new BeginHTMLBlock(getName());
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      KeyValList attrs = TeXParserUtils.popOptKeyValList(parser, stack);

      TeXObjectList expanded = parser.getListener().createStack();

      StartElement startElem = new StartElement("div", true, true);

      if (attrs != null)
      {
         for (Iterator<String> it = attrs.keySet().iterator(); it.hasNext(); )
         {
            String key = it.next();
            TeXObject obj = attrs.getValue(key);

            startElem.putAttribute(key, parser.expandToString(obj, stack).trim());
         }
      }

      expanded.add(startElem);

      return expanded;
   }

   @Override
   public TeXObjectList expandfully(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      return expandonce(parser, stack);
   }

   @Override
   public TeXObjectList expandfully(TeXParser parser)
     throws IOException
   {
      return expandonce(parser, parser);
   }

}
