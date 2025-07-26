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

public class GeneralMsg extends AbstractGlsCommand
{
   public GeneralMsg(GlossariesSty sty)
   {
      this(sty, "generalmsg", "", CaseChange.NO_CHANGE, false);
   }

   public GeneralMsg(GlossariesSty sty, String name, String prefix)
   {
      this(sty, name, prefix, CaseChange.NO_CHANGE, false);
   }

   public GeneralMsg(GlossariesSty sty, String name, String prefix,
     CaseChange caseChange, boolean isPlural)
   {
      super(name, sty);

      if (prefix == null)
      {
         throw new NullPointerException();
      }

      setEntryLabelPrefix(prefix);

      this.caseChange = caseChange;
      this.isPlural = isPlural;
   }

   @Override
   public Object clone()
   {
      return new GeneralMsg(sty, getName(), getEntryLabelPrefix(),
       caseChange, isPlural);
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

      CsvList csvList = TeXParserUtils.popOptCsvList(parser, stack);
      GlsLabel glsLabel = popEntryLabel(parser, stack);
      TeXObject optArg = popOptArg(parser, stack);

      parser.startGroup();

      if (csvList == null)
      {
         TeXObject val = glsLabel.getField("defaultparams");

         if (val != null)
         {
            csvList = CsvList.getList(parser, val);
         }
      }

      if (csvList != null)
      {
         PropertyCommand<Integer> propCs
           = PropertyCommand.getPropertyCommand(
             TeXJavaHelpSty.MSG_PARAM_PROP_NAME, parser, true);

         for (int i = 0; i < csvList.size(); i++)
         {
            TeXObject arg = csvList.getValue(i, true);

            propCs.put(Integer.valueOf(i), arg);
         }
      }

      String glsCsName = "gls";

      switch (caseChange)
      {
         case SENTENCE:
            glsCsName = "Gls";
         break;
         case TO_UPPER:
            glsCsName = "GLS";
         break;
      }

      if (isPlural)
      {
         glsCsName += "pl";
      }

      TeXObjectList expanded = listener.createStack();
      expanded.add(listener.getControlSequence(glsCsName));
      expanded.add(glsLabel);
      expanded.add(listener.getOther('['));

      if (optArg != null)
      {
         expanded.add(optArg, true);
      }

      expanded.add(listener.getOther(']'));

      TeXParserUtils.process(expanded, parser, stack);

      parser.endGroup();

   }

   CaseChange caseChange;
   boolean isPlural = false;
}
