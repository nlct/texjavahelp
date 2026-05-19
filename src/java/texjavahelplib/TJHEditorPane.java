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

import java.net.URL;
import java.net.MalformedURLException;

import java.io.*;

import java.util.Dictionary;
import java.util.Enumeration;

import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.JEditorPane;

import javax.swing.text.*;

import javax.swing.text.html.*;

public class TJHEditorPane extends JEditorPane
{
   public TJHEditorPane(TeXJavaHelpLib helpLib)
   {
      super();

      this.helpLib = helpLib;

      setContentType("text/html");
      setEditable(false);

      setEditorKit(new TJHEditorKit(helpLib));
   }

   public TJHEditorPane(URL url, TeXJavaHelpLib helpLib) throws IOException
   {
      this(helpLib);
      setPage(url);
   }

   public TJHEditorPane(String type, String text, TeXJavaHelpLib helpLib)
   {
      super(type, text);

      setEditable(false);

      this.helpLib = helpLib;

      setEditorKitForContentType("text/html", new TJHEditorKit(helpLib));
   }

   private Element getElementByIdOrName(String ref)
   {
      Document doc = (HTMLDocument)getDocument();

      return getElementByIdOrName(doc.getDefaultRootElement(), ref);
   }

   private Element getElementByIdOrName(Element e, String ref)
   {
      if (ref == null || ref.isEmpty()) return null;

      Document doc = (HTMLDocument)getDocument();

      AttributeSet attr = e.getAttributes();

      if (attr != null)
      {
         if ( ( attr.isDefined(HTML.Attribute.ID)
                  && ref.equals(attr.getAttribute(HTML.Attribute.ID)) )
             ||
              ( e.getName().equals(HTML.Tag.A.toString())
               && attr.isDefined(HTML.Attribute.NAME)
               && ref.equals(attr.getAttribute(HTML.Attribute.NAME)) )
            )
         {
            return e;
         }

         Enumeration names = attr.getAttributeNames();

         if (names != null)
         {
            while (names.hasMoreElements())
            {
               Object name = names.nextElement();

               if (name instanceof HTML.Tag)
               {
                  Object attrName = attr.getAttribute(name);

                  if (attrName instanceof AttributeSet)
                  {
                     AttributeSet as = (AttributeSet)attrName;

                     if ( ( as.isDefined(HTML.Attribute.ID)
                              && ref.equals(as.getAttribute(HTML.Attribute.ID)) )
                         ||
                          ( e.getName().equals(HTML.Tag.A.toString())
                           && as.isDefined(HTML.Attribute.NAME)
                           && ref.equals(as.getAttribute(HTML.Attribute.NAME)) )
                        )
                     {
                        return e;
                     }
                  }
               }
            }
         }
      }

      if (!e.isLeaf())
      {
         for (int i = 0, n = e.getElementCount(); i < n; i++)
         {
            Element e2 = getElementByIdOrName(e.getElement(i), ref);

            if (e2 != null)
            {
               return e2;
            }
         }
      }

      return null;
   }

   @Override
   public void scrollToReference(String ref)
   {
      Document doc = getDocument();

      if (doc instanceof HTMLDocument)
      {
         // Find any element with id attribute set to ref
         // or "a" element with name set to ref.
         Element element = getElementByIdOrName(ref);

         if (element != null)
         {
            int pos = element.getStartOffset();
            setCaretPosition(pos);

            Rectangle r = null;

            try
            {
               r = modelToView(pos);
            }
            catch (BadLocationException e)
            {
            }

            if (r != null)
            {
               Rectangle vis = getVisibleRect();
               r.height = vis.height;

               scrollRectToVisible(r);
            }
         }
      }
      else
      {
         super.scrollToReference(ref);
      }
   }

   @Override
   protected InputStream getStream(URL page) throws IOException
   {
      HelpsetFile hsf = helpLib == null ? null : helpLib.getHelpSetFile(page);

      if (hsf == null || !hsf.hasContent())
      {
         return super.getStream(page);
      }
      else
      {
         return hsf.getInputStream();
      }
   }

   TeXJavaHelpLib helpLib;
}
