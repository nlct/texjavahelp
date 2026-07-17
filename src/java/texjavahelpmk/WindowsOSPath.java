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

public class WindowsOSPath extends Command
{
   public WindowsOSPath()
   {
      this("WindowsOSPath");
   }

   public WindowsOSPath(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new WindowsOSPath(getName());
   }

   @Override
   public boolean canExpand()
   {
      return true;
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      TeXParserListener listener = parser.getListener();

      String pathStr = popLabelString(parser, stack);

      String[] split = pathStr.split("/");

      TeXObjectList expanded = listener.createStack();

      expanded.add(new TeXCsRef("filefmt"));

      Group grp = listener.createGroup();

      expanded.add(grp);

      for (int i = 0; i < split.length; i++)
      {
         if (i > 0)
         {
            grp.add(new TeXCsRef("windowsosfiledirsep"));
         }

         for (int j = 0; j < split[i].length(); )
         {
            int cp = split[i].codePointAt(j);
            j += Character.charCount(cp);

            if (parser.isLetter(cp))
            {
               grp.add(listener.getLetter(cp));
            }
            else if (
                        parser.isCategoryCode(CategoryCode.SPACE, cp)
                     || parser.isCategoryCode(CategoryCode.EOL, cp)
                    )
            {
               grp.add(listener.getSpace());
            }
            else
            {
               grp.add(listener.getOther(cp));
            }
         }
      }

      return expanded;
   }

}
