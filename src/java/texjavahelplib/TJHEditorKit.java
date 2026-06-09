/*
    Copyright (C) 2024-2026 Nicola L.C. Talbot
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
package com.dickimawbooks.texjavahelplib;

import java.io.FileNotFoundException;
import java.net.URL;

import java.util.Dictionary;

import java.awt.Image;

import javax.swing.text.*;

import javax.swing.text.html.*;

public class TJHEditorKit extends HTMLEditorKit
{
   public TJHEditorKit(TeXJavaHelpLib helpLib)
   {
      super();
      this.helpLib = helpLib;
      viewFactory = new TJHViewFactory();
   }

   @Override
   public ViewFactory getViewFactory()
   {
      return viewFactory;
   }

   @Override
   public Document createDefaultDocument()
   {
      StyleSheet styles = getStyleSheet();
      StyleSheet ss = helpLib.getHelpSetStyles();

      if (ss == null)
      {
         ss = new StyleSheet();
      }

      ss.addStyleSheet(styles);

      HTMLDocument doc = new HTMLDocument(ss);
      doc.setParser(getParser());
      doc.setAsynchronousLoadPriority(4);
      doc.setTokenThreshold(100);

      Dictionary<URL,Image> imageCache = helpLib.getHelpSetImageCache();

      if (imageCache != null)
      {
         doc.putProperty("imageCache", imageCache);
      }

      return doc;
   }

   class TJHViewFactory extends HTMLEditorKit.HTMLFactory
   {
      TJHViewFactory()
      {
         super();
      }

      @Override
      public View create(Element elem)
      {
         AttributeSet attrs = elem.getAttributes();

         Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);

         Object o = (elementName != null) ?
            null : attrs.getAttribute(StyleConstants.NameAttribute);

         if (o == HTML.Tag.IMG)
         {
            return new TJHImageView(elem);
         }
         else
         {
            return super.create(elem);
         }
      }
   }

   class TJHImageView extends ImageView
   {
      TJHImageView(Element elem)
      {
         super(elem);
      }

      @Override
      public URL getImageURL()
      {
         String src = (String)getElement().getAttributes()
                       .getAttribute(HTML.Attribute.SRC);

         if (src == null) return null;

         String cssClass = (String)getElement().getAttributes()
                       .getAttribute(HTML.Attribute.CLASS);

         try
         {
            return helpLib.getHelpSetImageResource(src, cssClass);
         }
         catch (FileNotFoundException e)
         {
         }

         return super.getImageURL();
      }
   }

   TeXJavaHelpLib helpLib;
   TJHViewFactory viewFactory;
}
