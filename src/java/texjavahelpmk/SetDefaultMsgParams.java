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
import com.dickimawbooks.texparserlib.latex.CsvList;
import com.dickimawbooks.texparserlib.latex.glossaries.AbstractGlsCommand;
import com.dickimawbooks.texparserlib.latex.glossaries.GlsLabel;
import com.dickimawbooks.texparserlib.latex.glossaries.GlossariesSty;
import com.dickimawbooks.texparserlib.latex.latex3.PropertyCommand;

public class SetDefaultMsgParams extends AbstractGlsCommand
{
   public SetDefaultMsgParams(GlossariesSty sty)
   {
      this(sty, "glsxtrprenamehook");
   }

   public SetDefaultMsgParams(GlossariesSty sty, String name)
   {
      super(name, sty);
   }

   @Override
   public Object clone()
   {
      return new SetDefaultMsgParams(sty, getName());
   }

   @Override
   public boolean canExpand()
   {
      return false;
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      return null;
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
      TeXParserListener listener = parser.getListener();

      GlsLabel glsLabel = popEntryLabel(parser, stack);

      PropertyCommand<Integer> propCs
        = PropertyCommand.getPropertyCommand(
          TeXJavaHelpSty.MSG_PARAM_PROP_NAME, parser, true);

      propCs.clear();

      TeXObject val = glsLabel.getField("defaultparams");

      if (val != null)
      {
         CsvList csvList = CsvList.getList(parser, val);

         for (int i = 0; i < csvList.size(); i++)
         {
            TeXObject arg = csvList.getValue(i, true);

            propCs.put(Integer.valueOf(i), arg);
         }
      }
   }

}
