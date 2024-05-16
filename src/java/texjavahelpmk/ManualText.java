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

import com.dickimawbooks.texparserlib.latex.glossaries.*;

public class ManualText extends GlsEntryField
{
   public ManualText(GlossariesSty sty)
   {
      this("manualtext", "text", CaseChange.NO_CHANGE, sty);
   }

   public ManualText(String name, String field, GlossariesSty sty)
   {
      this(name, field, CaseChange.NO_CHANGE, sty);
   }

   public ManualText(String name, String field, CaseChange caseChange,
    GlossariesSty sty)
   {
      this(name, field, caseChange, CaseChange.NO_CHANGE, sty);
   }

   public ManualText(String name, String field, CaseChange caseChange,
    CaseChange defValCaseChange, GlossariesSty sty)
   {
      super(name, field, caseChange, false, sty);
      setEntryLabelPrefix("manual.");
      this.defValCaseChange = defValCaseChange;
   }

   @Override
   public Object clone()
   {
      return new ManualText(getName(), getField(), getCaseChange(),
      defValCaseChange, sty);
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      GlsLabel glslabel = popEntryLabel(parser, stack);
      TeXObject defText = popArg(parser, stack);

      GlossaryEntry entry = glslabel.getEntry();

      TeXObjectList expanded;

      if (entry == null)
      {
         if (parser.isStack(defText))
         {
            expanded = (TeXObjectList)defText;
         }
         else
         {
            expanded = parser.getListener().createStack();
            expanded.add(defText);
         }

         ControlSequence caseChangeCs = null;

         switch (defValCaseChange)
         {
            case SENTENCE:
               caseChangeCs = new TeXCsRef("makefirstuc");
            break;
            case TITLE:
               caseChangeCs = new TeXCsRef("glsxtrfieldtitlecasecs");
            break;
            case TO_UPPER:
               caseChangeCs = new TeXCsRef("mfirstucMakeUppercase");
            break;
         }

         if (caseChangeCs != null)
         {
            Group grp = parser.getListener().createGroup();
            grp.addAll(expanded);
            expanded.clear();
            expanded.add(caseChangeCs);
            expanded.add(grp);
         }
      }
      else
      {
         expanded = expand(glslabel, getField(), caseChange, parser, stack);
      }

      return expanded;
   }

   protected CaseChange defValCaseChange;
}
