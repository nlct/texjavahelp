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
import com.dickimawbooks.texparserlib.html.StartElement;
import com.dickimawbooks.texparserlib.html.EndElement;

/** 
 * Short stack for LaTeX but not HTML. Just replace newline with
 * space.
 */
public class TableCellStack extends ControlSequence
{
   public TableCellStack()
   {
      this("tablecellstack");
   }

   public TableCellStack(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new TableCellStack(getName());
   }

   protected void replaceCr(TeXParser parser, TeXObjectList list)
   {
      for (int i = 0; i < list.size(); i++)
      {
         TeXObject object = list.get(i);

         if (object instanceof TeXObjectList)
         {
            replaceCr(parser, (TeXObjectList) object);
         }
         else if (TeXParserUtils.isControlSequence(object, "\\"))
         {
            list.set(i, parser.getListener().getSpace());
         }
      }
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

      TeXObject content = popArg(parser, stack);

      if (content instanceof TeXObjectList)
      {
         replaceCr(parser, (TeXObjectList)content);
      }

      TeXParserUtils.process(content, parser, stack);
   }

}
