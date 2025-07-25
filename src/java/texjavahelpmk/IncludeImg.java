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

public class IncludeImg extends ControlSequence
{
   public IncludeImg()
   {
      this("includeimg", null);
   }

   public IncludeImg(String name, String cssClass)
   {
      super(name);
      this.cssClass = cssClass;
   }

   @Override
   public Object clone()
   {
      return new IncludeImg(getName(), cssClass);
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

      TeXObject options = popOptArg(parser, stack);
      TeXObject content = popArg(parser, stack);

      TeXObjectList expanded = listener.createStack();

      expanded.add(listener.getControlSequence("includegraphics"));

      if (options != null)
      {
         expanded.add(listener.getOther('['));
         expanded.add(options, true);

         if (cssClass != null)
         {
            expanded.add(listener.createString(",class="+cssClass));
         }

         expanded.add(listener.getOther(']'));
      }
      else if (cssClass != null)
      {
         expanded.add(listener.getOther('['));
         expanded.add(listener.createString("class="+cssClass));
         expanded.add(listener.getOther(']'));
      }

      Group grp = listener.createGroup();
      grp.add(content, true);

      expanded.add(grp);

      TeXParserUtils.process(expanded, parser, stack);
   }

   String cssClass;
}
