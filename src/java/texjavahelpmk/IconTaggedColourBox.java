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
import java.awt.Color;

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.texparserlib.latex.*;

import com.dickimawbooks.texparserlib.latex.nlctdoc.TaggedColourBox;

public class IconTaggedColourBox extends TaggedColourBox
{
   public IconTaggedColourBox(FrameBox fbox)
   {
      this(fbox, null, null);
   }

   public IconTaggedColourBox(FrameBox fbox, FrameBox titleBox, TeXObject title)
   {
      super(fbox, titleBox, title);
   }

   public IconTaggedColourBox(String name, FrameBox fbox, FrameBox titleBox, TeXObject title)
   {
      super(name, fbox, titleBox, title);
   }

   @Override
   public Object clone()
   {
      return new IconTaggedColourBox(getName(), fbox, titleBox, defaultTitle);
   }

   @Override
   public void process(TeXParser parser, TeXObjectList stack)
    throws IOException
   {
      if (parser.isDebugMode(TeXParser.DEBUG_PROCESSING))
      {
         parser.logMessage("Processing " + toString(parser));
      }

      TeXObject options = popOptArg(parser, stack);

      preprocess(parser, stack);

      TeXObject obj = new StartFrameBox(fbox);

      TeXParserUtils.process(obj, parser, stack);

      KeyValList keyValList = null;

      TeXObject title = currentTitle;

      if (options != null && !options.isEmpty())
      {
         keyValList = KeyValList.getList(parser, options);

         if (keyValList.containsKey("title"))
         {
            title = keyValList.get("title");
         }
      }

      if (title != null)
      {
         TeXParserUtils.process((TeXObject)title.clone(), parser, stack);
         TeXParserUtils.process(parser.getListener().getSpace(), parser, stack);
      }
   }

}
