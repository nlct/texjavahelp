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

import com.dickimawbooks.texparserlib.latex.KeyValList;

import com.dickimawbooks.texparserlib.html.L2HConverter;
import com.dickimawbooks.texparserlib.html.HtmlTag;
import com.dickimawbooks.texparserlib.html.StartElement;
import com.dickimawbooks.texparserlib.html.EndElement;

import com.dickimawbooks.texparserlib.latex.glossaries.GlossariesSty;
import com.dickimawbooks.texparserlib.latex.glossaries.Gls;
import com.dickimawbooks.texparserlib.latex.glossaries.GlsLabel;

public class KeyRef extends Gls
{
   public KeyRef(GlossariesSty sty)
   {
      this("keyref", sty);
   }

   public KeyRef(String name, GlossariesSty sty)
   {
      super(name, CaseChange.NO_CHANGE, false, sty);
      setEntryLabelPrefix("manual.keystroke.");
   }

   @Override
   public Object clone()
   {
      return new KeyRef(getName(), sty);
   }

   @Override
   protected void addLinkText(TeXObjectList substack,
     KeyValList glslinkOptions,
     ControlSequence entryfmtCs, GlsLabel glslabel,
     TeXParser parser, TeXObjectList stack)
   throws IOException
   {
      L2HConverter listener = (L2HConverter)parser.getListener();

      substack.add(listener.getControlSequence("@gls@link"));

      if (glslinkOptions != null)
      {  
         substack.add(glslinkOptions);
      }  

      substack.add(glslabel);

      Group grp = listener.createGroup();
      substack.add(grp);

      TeXObject title = glslabel.getField("tooltip");

      if (title == null)
      {
         title = glslabel.getField("description");
      }

      if (title != null)
      {
         String titleStr
           = listener.stripTags(listener.processToString(title, stack)).trim();

         if (titleStr.endsWith("."))
         {
            titleStr = titleStr.substring(0, titleStr.length()-1);
         }

         if (titleStr.isEmpty())
         {
            title = null;
         }
         else
         {
            StartElement elem = new StartElement("span");
            elem.putAttribute("title", HtmlTag.encodeAttributeValue(titleStr, false));

            grp.add(elem);
         }
      }

      grp.add(entryfmtCs);

      if (title != null)
      {
         grp.add(new EndElement("span"));
      }
   }

}
