/*
    Copyright (C) 2025 Nicola L.C. Talbot
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

import com.dickimawbooks.texparserlib.latex.glossaries.GlossariesSty;
import com.dickimawbooks.texparserlib.latex.glossaries.AbstractGlsCommand;
import com.dickimawbooks.texparserlib.latex.glossaries.GlsLabel;
import com.dickimawbooks.texparserlib.latex.glossaries.GlossaryEntry;

public class KeyDescRef extends AbstractGlsCommand
{
   public KeyDescRef(GlossariesSty sty)
   {
      this("keydescref", CaseChange.TO_LOWER, true, sty);
   }

   public KeyDescRef(String name, CaseChange caseChange, boolean keyFirst,
      GlossariesSty sty)
   {
      super(name, sty);
      this.caseChange = caseChange;
      this.keyFirst = keyFirst;
      setEntryLabelPrefix("manual.keystroke.");
   }

   @Override
   public Object clone()
   {
      return new KeyDescRef(getName(), caseChange, keyFirst, sty);
   }

   @Override
   public TeXObjectList expandonce(TeXParser parser, TeXObjectList stack)
     throws IOException
   {
      TeXParserListener listener = parser.getListener();

      KeyValList options = popOptKeyValList(stack);

      GlsLabel glslabel = popEntryLabel(parser, stack);

      TeXObjectList expanded = parser.getListener().createStack();

      TeXObject desc = glslabel.getField("description");
      String csName = null;

      if (desc != null)
      {
         switch (caseChange)
         {
            case TO_LOWER:
              csName = "glslowercase";
            break;
            case TO_UPPER:
              csName = "glsuppercase";
            break;
            case SENTENCE:
              csName = "glssentencecase";
            break;
         }
      }

      if (!keyFirst && desc != null)
      {
         if (csName == null)
         {
            expanded.add(desc, true);
         }
         else
         {
            expanded.add(listener.getControlSequence(csName));
            Group grp = listener.createGroup();
            grp.add(desc, true);
            expanded.add(grp, true);
         }

         expanded.add(listener.getSpace());
      }

      if (options == null)
      {
         options = new KeyValList();
      }

      if (!options.containsKey("textformat"))
      {
         options.put("textformat", listener.createString("keys"));
      }

      expanded.add(listener.getControlSequence("gls"));
      expanded.add(listener.getOther('['));
      expanded.add(options);
      expanded.add(listener.getOther(']'));
      expanded.add(TeXParserUtils.createGroup(listener, glslabel));

      if (keyFirst && desc != null)
      {
         expanded.add(listener.getSpace());

         if (csName == null)
         {
            expanded.add(desc, true);
         }
         else
         {
            expanded.add(listener.getControlSequence(csName));
            Group grp = listener.createGroup();
            grp.add(desc, true);
            expanded.add(grp, true);
         }
      }

      return expanded;
   }

   private boolean keyFirst;
   private CaseChange caseChange;
}
