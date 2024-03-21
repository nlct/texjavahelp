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
import com.dickimawbooks.texparserlib.latex.glossaries.GlossariesSty;
import com.dickimawbooks.texparserlib.latex.color.ColorSty;
import com.dickimawbooks.texparserlib.html.L2HConverter;
import com.dickimawbooks.texparserlib.html.Widget;

import com.dickimawbooks.texparserlib.latex.nlctdoc.*;

public class TeXJavaHelpSty extends UserGuideSty
{
   public TeXJavaHelpSty(KeyValList options, LaTeXParserListener listener,
     boolean loadParentOptions, ColorSty colorSty)
   throws IOException
   {
      super(options, "texjavahelp", listener, loadParentOptions, colorSty);
   }

   @Override
   public void addDefinitions()
   {
      addCssStyles();
      addSemanticCommands();
      addCrossRefCommands();
      addFootnoteCommands();
      addSymbolCommands();
      addGlsIconCommands();
      addTextCommands();
      addListCommands();
      addBoxCommands();
      addInlineDefCommands();
      addLocationCommands();

      registerControlSequence(new PrintIndex("printindex", glossariesSty));
      registerControlSequence(new IndexInitPostNameHooks());
      registerControlSequence(new AbbrPostNameHook(glossariesSty));

      glossariesSty.setModifier(listener.getOther('+'), "format",
        listener.createString("glsnumberformat"));

      glossariesSty.setModifier(listener.getOther('!'), "format",
        listener.createString("glsignore"));


      registerControlSequence(new Widget("menufmt", "menu"));
      registerControlSequence(new Widget("widgetfmt", "widget"));
      registerControlSequence(new Widget("dialogfmt", "dialog"));
   }

   @Override
   protected void preOptions(TeXObjectList stack) throws IOException
   {
      getListener().requirepackage(null, "twemojis", false, stack);
      getListener().requirepackage(null, "fontawesome", false, stack);
      getListener().requirepackage(null, "hyperref", false, stack);

      KeyValList options = new KeyValList();

      options.put("record", getListener().createString("nameref"));
      options.put("indexcounter", null);
      options.put("floats", null);
      options.put("nostyles", null);
      options.put("stylemods", getListener().createString("tree,bookindex,topic"));
      options.put("style", getListener().createString("alttree"));

      glossariesSty = (GlossariesSty)getListener().requirepackage(
        options, "glossaries-extra", false, stack);

      glossariesSty.addField("shortswitch");
      glossariesSty.addField("syntax");
      glossariesSty.addField("defaultvalue");
      glossariesSty.addField("initvalue");
      glossariesSty.addField("note");

   }

   @Override
   public void processOption(String option, TeXObject value)
    throws IOException
   {
      glossariesSty.processOption(option, value);
   }

   @Override
   protected void postOptions(TeXObjectList stack) throws IOException
   {
      super.postOptions(stack);

      TeXParserListener listener = getListener();

      TeXObjectList list = listener.createStack();
      list.add(listener.getControlSequence("setabbreviationstyle"));
      list.add(listener.getOther('['));
      list.add(listener.createString("commonabbreviation"), true);
      list.add(listener.getOther(']'));
      list.add(listener.createGroup("short-nolong"));

      if (stack == null || stack == getParser())
      {
         getParser().add(list);
      }
      else
      {
         list.process(getParser(), stack);
      }
   }

}
