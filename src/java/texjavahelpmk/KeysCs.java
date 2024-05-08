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

public class KeysCs extends Command
{
   public KeysCs()
   {
      this("keys");
   }

   public KeysCs(String name)
   {
      super(name);
   }

   @Override
   public Object clone()
   {
      return new KeysCs(getName());
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      TeXParserListener listener = parser.getListener();

      TeXObject arg = popArg(parser, stack);

      TeXObjectList expanded = listener.createStack();

      if (parser.isStack(arg))
      {
         TeXObjectList list = (TeXObjectList)arg;
         TeXObjectList current = null;

         while (!list.isEmpty())
         {
            TeXObject obj = list.popStack(parser);

            if (current == null && obj instanceof WhiteSpace)
            {// ignore
            }
            else
            {
               if (current == null)
               {
                  current = listener.createStack();
               }

               if (obj instanceof CharObject
                     && ((CharObject)obj).getCharCode()=='+')
               {
                  current.trimTrailing();

                  if (!expanded.isEmpty())
                  {
                     expanded.add(listener.getControlSequence("keysep"));
                  }

                  expanded.add(listener.getControlSequence("keystrokefmt"));
                  expanded.add(TeXParserUtils.createGroup(listener, current));

                  current = null;
               }
               else
               {
                  current.add(obj);
               }
            }
         }

         if (current != null)
         {
            current.trimTrailing();

            if (!expanded.isEmpty())
            {
               expanded.add(listener.getControlSequence("keysep"));
            }

            expanded.add(listener.getControlSequence("keystrokefmt"));
            expanded.add(TeXParserUtils.createGroup(listener, current));
         }
      }
      else
      {
         expanded.add(arg);
      }

      return expanded;
   }

}
