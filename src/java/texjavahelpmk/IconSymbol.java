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

import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;

import java.nio.file.*;

import com.dickimawbooks.texparserlib.*;

import com.dickimawbooks.texparserlib.html.HtmlTag;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;

public class IconSymbol extends AbstractTeXObject
{
   public IconSymbol(String iconTag)
   {
      this(iconTag, "-16x16");
   }

   public IconSymbol(String iconTag, String iconSuffix)
   {
      super();
      this.iconTag = iconTag;
      this.iconSuffix = iconSuffix;
   }

   @Override
   public Object clone()
   {
      return new IconSymbol(iconTag, iconSuffix);
   }

   @Override
   public boolean isDataObject()
   {
      return true;
   }

   @Override
   public TeXObjectList string(TeXParser parser)
    throws IOException
   {
      return getTeXObject(parser, parser).string(parser);
   }

   @Override
   public String toString()
   {
      return String.format("%s[tag=%s]",
        getClass().getSimpleName(), iconTag);
   }

   @Override
   public String format()
   {
      return String.format("\\%ssym", iconTag);
   }

   public String toString(TeXParser parser)
   {
      return String.format("%s%ssym",
        new String(Character.toChars(parser.getEscChar())), iconTag);
   }

   @Override
   public void process(TeXParser parser)
      throws IOException
   {
      getTeXObject(parser, parser).process(parser);
   }

   @Override
   public void process(TeXParser parser, TeXObjectList stack) throws IOException
   {
      getTeXObject(parser, stack).process(parser, stack);
   }

   protected TeXObject getTeXObject(TeXParser parser, TeXObjectList stack)
    throws IOException
   {
      TJHListener listener = (TJHListener)parser.getListener();
      TeXJavaHelpLib helpLib = listener.getTeXJavaHelpMk().getHelpLib();

      String resourcePath = helpLib.getResourcePath();
      String resourceIconPath = helpLib.getIconPath();

      try
      {
         URL url = getClass().getResource(resourceIconPath+"/"+iconTag+iconSuffix+".png");

         if (url != null)
         {
            URI iconURI = url.toURI();

            URI docURI = listener.getTeXJavaHelpMk().getOutDirectory().toURI();

            Path iconPath = Paths.get(iconURI);
            Path docPath = Paths.get(docURI);

            Path relPath = docPath.relativize(iconPath);

            if (!relPath.isAbsolute())
            {
               String alt = iconTag;

               ControlSequence cs = parser.getControlSequence(iconTag+"text");

               if (cs != null)
               {
                  alt = parser.expandToString(cs, stack);
               }

               return new HtmlTag(String.format(
                "<img src=\"%s\" alt=\"%s\">",
                 relPath, TeXJavaHelpLib.encodeHTML(alt, true)));
            }
         }
      }
      catch (URISyntaxException e)
      {
         helpLib.error(e);
      }

      return parser.getListener().getControlSequence(iconTag+"sym");
   }

   protected String iconTag;
   protected String iconSuffix;
}
