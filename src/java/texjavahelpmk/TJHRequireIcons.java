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

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.texparserlib.latex.CsvList;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;

public class TJHRequireIcons extends ControlSequence
{
   public TJHRequireIcons()
   {
      this("TJHRequireIcons");
   }

   public TJHRequireIcons(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new TJHRequireIcons(getName());
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

      CsvList csvList = TeXParserUtils.popCsvList(parser, stack);

      String smallSize = popLabelString(parser, stack);

      String largeSize = popLabelString(parser, stack);

      TeXJavaHelpLib helpLib = listener.getHelpLib();

      helpLib.setSmallIconSuffix("-"+smallSize);
      helpLib.setLargeIconSuffix("-"+largeSize);

      TeXObjectList substack = listener.createStack();
      ControlSequence inputCs = listener.getControlSequence("input");

      String smallSuffix = "-"+smallSize+".def";
      String largeSuffix = "-"+largeSize+".def";

      for (int i = 0; i < csvList.size(); i++)
      {
         TeXObject obj = csvList.getValue(i, true);

         String base = parser.expandToString(obj, stack);

         substack.add(inputCs);
         substack.add(listener.createGroup(base+smallSuffix));

         substack.add(inputCs);
         substack.add(listener.createGroup(base+largeSuffix));
      }

      TeXParserUtils.process(substack, parser, stack);
   }

}
