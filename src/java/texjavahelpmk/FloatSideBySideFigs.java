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

public class FloatSideBySideFigs extends FloatFig
{
   public FloatSideBySideFigs()
   {
      this("FloatSideBySideFigs");
   }

   public FloatSideBySideFigs(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new FloatSideBySideFigs(getName());
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
      // Treat as two \FloatFigs

      popOptArg(parser, stack);
      popOptArg(parser, stack);
      String label1 = popLabelString(parser, stack);
      popOptArg(parser, stack);// ignore
      TeXObject content1 = popArg(parser, stack);
      TeXObject lof1 = popOptArg(parser, stack);
      TeXObject caption1 = popArg(parser, stack);

      TeXObject between = popOptArg(parser, stack);

      String label2 = popLabelString(parser, stack);
      popOptArg(parser, stack);// ignore
      TeXObject content2 = popArg(parser, stack);
      TeXObject lof2 = popOptArg(parser, stack);
      TeXObject caption2 = popArg(parser, stack);

      addFig(label1, content1, lof1, caption1, parser, stack);

      if (between != null)
      {
         TeXParserUtils.process(between, parser, stack);
      }

      addFig(label2, content2, lof2, caption2, parser, stack);
    }

}
