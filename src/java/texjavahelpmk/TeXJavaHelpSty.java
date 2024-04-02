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
import com.dickimawbooks.texparserlib.latex.glossaries.GlsFieldLink;
import com.dickimawbooks.texparserlib.latex.glossaries.AbstractGlsCommand;
import com.dickimawbooks.texparserlib.latex.color.ColorSty;
import com.dickimawbooks.texparserlib.html.L2HConverter;
import com.dickimawbooks.texparserlib.html.Widget;

import com.dickimawbooks.texparserlib.latex.nlctdoc.UserGuideSty;
import com.dickimawbooks.texparserlib.latex.nlctdoc.TaggedColourBox;
import com.dickimawbooks.texparserlib.latex.nlctdoc.ColourBox;
import com.dickimawbooks.texparserlib.latex.nlctdoc.PrintIndex;
import com.dickimawbooks.texparserlib.latex.nlctdoc.IndexInitPostNameHooks;
import com.dickimawbooks.texparserlib.latex.nlctdoc.AbbrPostNameHook;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;

public class TeXJavaHelpSty extends UserGuideSty
{
   public TeXJavaHelpSty(KeyValList options, LaTeXParserListener listener,
     boolean loadParentOptions, ColorSty colorSty)
   throws IOException
   {
      super(options, "texjavahelp", listener, loadParentOptions, colorSty);
   }

   public TeXJavaHelpLib getHelpLib()
   {
      TJHListener listener = (TJHListener)getListener();

      return listener.getTeXJavaHelpMk().getHelpLib();
   }

   @Override
   public void addDefinitions()
   {
      addCssStyles();
      addSemanticCommands();
      addCrossRefCommands();
      addFootnoteCommands();
      addSymbolCommands();
      addTextCommands();
      addListCommands();

      addBasicBoxCommands();
      addStandaloneDefCommands();
      addCodeBoxCommands();
      addIndexBoxCommands();

      addInlineDefCommands();
      addLocationCommands();
      addBib2GlsCommands();
      addGlsCommands();

      registerControlSequence(new FilterTerms(glossariesSty));
      registerControlSequence(new PrintMainInit());
      registerControlSequence(new PrintMain(glossariesSty));
      registerControlSequence(new ListEntryDescendents(glossariesSty));
      registerControlSequence(new ListEntryDescendentsInit());
      registerControlSequence(new PrintIndex("printindex", "main", "docindex", glossariesSty));
      registerControlSequence(new IndexInitPostNameHooks());
      registerControlSequence(new AbbrPostNameHook(glossariesSty));
      registerControlSequence(new PostSwitchHook(glossariesSty));
      registerControlSequence(new PostNameFieldHook("postclihook", "syntax", glossariesSty));
      registerControlSequence(new PostNameFieldHook("postoptionhook", "syntax", 
       listener.getOther('='), null, glossariesSty));

      glossariesSty.setModifier(listener.getOther('+'), "format",
        listener.createString("glsnumberformat"));

      glossariesSty.setModifier(listener.getOther('!'), "format",
        listener.createString("glsignore"));

      registerControlSequence(new GlsFieldLink("shortswitchref",
        "shortswitch", glossariesSty));

      AbstractGlsCommand gcs = new GlsFieldLink("sswitch", "shortswitch", glossariesSty);
      gcs.setEntryLabelPrefix("switch.");
      registerControlSequence(gcs);

      registerControlSequence(new Widget("menufmt", "menu"));
      registerControlSequence(new Widget("widgetfmt", "widget"));
      registerControlSequence(new Widget("dialogfmt", "dialog"));

      registerControlSequence(new TextualContentCommand("warningtext",
        getHelpLib().getMessage("manual.warning")));
      registerControlSequence(new TextualContentCommand("informationtext",
        getHelpLib().getMessage("manual.information")));
      registerControlSequence(new TextualContentCommand("importanttext",
        getHelpLib().getMessage("manual.important")));
      registerControlSequence(new TextualContentCommand("definitiontext",
        getHelpLib().getMessage("manual.definition")));
      registerControlSequence(new TextualContentCommand("terminaltext",
        getHelpLib().getMessage("manual.terminal")));
      registerControlSequence(new TextualContentCommand("valuesettingtext",
        getHelpLib().getMessage("manual.valuesetting")));
      registerControlSequence(new TextualContentCommand("novaluesettingtext",
        getHelpLib().getMessage("manual.novaluesetting")));
      registerControlSequence(new TextualContentCommand("toggleonsettingtext",
        getHelpLib().getMessage("manual.toggleonsetting")));
      registerControlSequence(new TextualContentCommand("toggleoffsettingtext",
        getHelpLib().getMessage("manual.toggleoffsetting")));
      registerControlSequence(new TextualContentCommand("optionvaluetext",
        getHelpLib().getMessage("manual.optionvalue")));
      registerControlSequence(new TextualContentCommand("countertext",
        getHelpLib().getMessage("manual.counter")));
      registerControlSequence(new TextualContentCommand("codetext",
        getHelpLib().getMessage("manual.code")));
      registerControlSequence(new TextualContentCommand("resulttext",
        getHelpLib().getMessage("manual.result")));
      registerControlSequence(new TextualContentCommand("transcripttext",
        getHelpLib().getMessage("manual.transcript")));

      registerControlSequence(new Icon());

      TeXObjectList def = getListener().createStack();
      Group grp = getListener().createGroup();

      def.add(new TeXCsRef("csuse"));
      def.add(grp);
      grp.add(getListener().getParam(1));
      grp.addAll(getListener().createString("text"));

      registerControlSequence(new LaTeXGenericCommand(true, "icontext",
       "m", def));

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

   @Override
   protected TaggedColourBox createTaggedColourBox(
    FrameBox boxFrame, FrameBox titleFrame, TeXObject tag)
   {
      return new IconTaggedColourBox(boxFrame, titleFrame, tag);
   }

   @Override
   protected FrameBoxEnv createPinnedBox()
   {
      ColourBox fbox = new ColourBox("@pinnedbox", BorderStyle.SOLID,
       AlignHStyle.DEFAULT, AlignVStyle.DEFAULT, false, true,
       new UserDimension(2, TeXUnit.BP), new UserDimension(2, TeXUnit.BP));

      fbox.setId("definition");
      fbox.setForegroundColor(Color.BLACK);
      fbox.setBackgroundColor(BG_DEF);

      getListener().declareFrameBox(fbox, false);

      FrameBoxEnv env = new FrameBoxEnv("pinnedbox", fbox);
      registerControlSequence(env);

      return env;
   }

   @Override
   protected FrameBoxEnv createSettingsBox()
   {
      ColourBox fbox = new ColourBox("@settingsbox", BorderStyle.SOLID,
       AlignHStyle.DEFAULT, AlignVStyle.DEFAULT, false, true,
       new UserDimension(2, TeXUnit.BP), new UserDimension(2, TeXUnit.BP));

      fbox.setId("valuesetting");
      fbox.setForegroundColor(Color.BLACK);
      fbox.setBackgroundColor(BG_OPTION_DEF);

      getListener().declareFrameBox(fbox, false);

      FrameBoxEnv env = new FrameBoxEnv("settingsbox", fbox);
      registerControlSequence(env);

      return env;
   }
}
