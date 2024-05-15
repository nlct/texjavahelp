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
import com.dickimawbooks.texparserlib.latex.glossaries.*;
import com.dickimawbooks.texparserlib.latex.color.ColorSty;

import com.dickimawbooks.texparserlib.html.L2HConverter;
import com.dickimawbooks.texparserlib.html.Widget;
import com.dickimawbooks.texparserlib.html.WidgetKeyStroke;
import com.dickimawbooks.texparserlib.html.StartElement;
import com.dickimawbooks.texparserlib.html.EndElement;
import com.dickimawbooks.texparserlib.html.HtmlTag;

import com.dickimawbooks.texparserlib.latex.nlctdoc.UserGuideSty;
import com.dickimawbooks.texparserlib.latex.nlctdoc.TaggedColourBox;
import com.dickimawbooks.texparserlib.latex.nlctdoc.ColourBox;
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
      TJHListener listener = (TJHListener)getListener();

      addCssStyles();
      addSemanticCommands();

      addSemanticCommand("xmltagfmt", TeXFontFamily.TT,
        null, listener.getOther('<'), listener.getOther('>'));

      addSemanticCommand("varfmt", TeXFontFamily.VERB);

      addDiscretionaryCommands();
      addCrossRefCommands();
      addFootnoteCommands();
      addSymbolCommands();
      addTextCommands();
      addListCommands();

      addBasicBoxCommands();
      addStandaloneDefCommands();
      addCodeBoxCommands();

      addIndexBoxCommands();
      createIndexItemBox(3);
      createIndexItemBox(4);

      addInlineDefCommands();
      addLocationCommands();
      addBib2GlsCommands();
      addGlsCommands();

      // no-op for resource related commands

      registerControlSequence(new GenericCommand("TeXJavaHelpGlsResourceOptions"));
      registerControlSequence(new GenericCommand("TeXJavaHelpSymbolResourceOptions"));
      registerControlSequence(new GenericCommand("TeXJavaHelpGlsFieldAdjustments"));
      registerControlSequence(new GenericCommand("TeXJavaHelpExtraAssignFields"));
      registerControlSequence(new GenericCommand("TeXJavaHelpExtraEntryTypeAliases"));
      registerControlSequence(new GenericCommand("TeXJavaHelpPuncAssignFields"));
      registerControlSequence(new GenericCommand("TeXJavaHelpMaxPuncDesc"));

      // keystroke symbols

      registerControlSequence(listener.createSymbol(
        "backspacekeysym", 0x232B));
      registerControlSequence(listener.createSymbol(
        "leftkeysym", 0x2190));
      registerControlSequence(listener.createSymbol(
        "upkeysym", 0x2191));
      registerControlSequence(listener.createSymbol(
        "rightkeysym", 0x2192));
      registerControlSequence(listener.createSymbol(
        "downkeysym", 0x2193));
      registerControlSequence(listener.createSymbol(
        "shiftsym", 0x21E7));
      registerControlSequence(listener.createSymbol(
        "returnsym", 0x21B5));
      registerControlSequence(listener.createSymbol(
        "tabsym", 0x21B9));

      registerControlSequence(new GenericCommand(true,
        "spacekeysym", null, new HtmlTag("<span class=\"spacekey\"> </span>")));

      registerControlSequence(new FloatFig());

      registerControlSequence(new FilterTerms(glossariesSty));
      registerControlSequence(new PrintMainInit());
      registerControlSequence(new PrintMain(glossariesSty));
      registerControlSequence(new ListEntry(glossariesSty));
      registerControlSequence(new ListEntryDescendents(glossariesSty));
      registerControlSequence(new ListEntryDescendentsInit());
      registerControlSequence(new ListMenuItems(glossariesSty));
      registerControlSequence(new PrintHelpIndex(glossariesSty));
      registerControlSequence(new IndexInitPostNameHooks());
      registerControlSequence(new AbbrPostNameHook(glossariesSty));
      registerControlSequence(new PostSwitchHook(glossariesSty));
      registerControlSequence(new PostMenuHook(glossariesSty));
      registerControlSequence(new PostNameFieldHook("postclihook", "syntax", glossariesSty));
      registerControlSequence(new PostNameFieldHook("postoptionhook", "syntax", 
       listener.getOther('='), null, glossariesSty));

      glossariesSty.setModifier(listener.getOther('+'), "format",
        listener.createString("glsnumberformat"));

      glossariesSty.setModifier(listener.getOther('!'), "format",
        listener.createString("glsignore"));

      registerControlSequence(new GlsFieldLink("shortswitchref",
        "shortswitch", glossariesSty));

      AbstractGlsCommand gcs = new GlsFieldLink("swch", "shortswitch", glossariesSty);
      gcs.setEntryLabelPrefix("switch.");
      registerControlSequence(gcs);


      registerControlSequence(new Dgls("menuitem", CaseChange.NO_CHANGE, glossariesSty));
      registerControlSequence(new Dgls("widget", CaseChange.NO_CHANGE, glossariesSty));

      registerControlSequence(new MenuCs(glossariesSty));
      registerControlSequence(new MenuTrail(glossariesSty));
      registerControlSequence(new MenuItemsStyle(glossariesSty));

      registerControlSequence(new DialogCs());

      registerControlSequence(
        new GlsEntryField("entrymnemonic", "mnemonic", glossariesSty));

      registerControlSequence(
        new GlsEntryField("entrytooltip", "tooltip", glossariesSty));

      registerControlSequence(
        new GlsEntryField("entrykeystroke", "keystroke", glossariesSty));

      registerControlSequence(new Dglsfield("btn", glossariesSty,
         CaseChange.NO_CHANGE, "tooltip"));

      registerControlSequence(new Widget("menufmt", "menu"));
      registerControlSequence(new Widget("widgetfmt", "widget"));
      registerControlSequence(new Widget("dialogfmt", "dialog"));
      registerControlSequence(new WidgetKeyStroke("keystrokefmt"));

      registerControlSequence(new AtFirstOfOne("actualkey"));
      registerControlSequence(new TextualContentCommand("keysep", "+"));
      registerControlSequence(new KeysCs());

      registerControlSequence(new KeyRef(glossariesSty));

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

      registerControlSequence(new AtGobble("menubookmark"));
      registerControlSequence(new GenericCommand(true,
       "menuitemsbetweenskip", null, new TeXCsRef("medskip")));
      registerControlSequence(new GenericCommand(true,
       "menusbetweenskip", null, new TeXCsRef("bigskip")));

      registerControlSequence(new Icon());

      TeXObjectList def = listener.createStack();
      Group grp = listener.createGroup();

      def.add(new TeXCsRef("csuse"));
      def.add(grp);
      grp.add(listener.getParam(1));
      grp.addAll(listener.createString("text"));

      registerControlSequence(new LaTeXGenericCommand(true, "icontext",
       "m", def));

      def = listener.createStack();
      StartElement startElem = new StartElement("span");
      startElem.putAttribute("class", "symbol");
      listener.addCssStyle("span.symbol { font-family: serif; }");
      def.add(startElem);
      def.add(listener.getParam(1));
      def.add(new EndElement("span"));

      registerControlSequence(new LaTeXGenericCommand(true, "symbolfmt",
       "m", def));

      registerControlSequence(new AtFirstOfOne("msgellipsis"));
      registerControlSequence(new AtGobble("msgendcolon"));

      // TODO: \defmsgparam, \msgparam, \msgchoiceparam etc

      // dual prefix list
      def = listener.createString("action.,button.,menu.,widget.,");
        def.add(listener.getControlSequence("empty"));
      registerControlSequence(new GenericCommand(true, "@glsxtr@labelprefixes",
       null, def));

      // wrglossary location formats
      registerControlSequence(new GlsXtrWrGlossaryLocFmt(false));

      def = listener.createStack();
      def.add(new TeXCsRef("glsadd"));
      def.add(TeXParserUtils.createGroup(listener, listener.getParam(1)));

      registerControlSequence(new LaTeXGenericCommand(true, "glssummaryadd",
        "m", def));

      def = listener.createStack();

      def.add(new TeXCsRef("glshyperlink"));
      def.add(TeXParserUtils.createGroup(listener,
        listener.getParam(1)));

      registerControlSequence(new LaTeXGenericCommand(true,
       "menuitemref", "m", def));
   }

   @Override
   protected void preOptions(TeXObjectList stack) throws IOException
   {
      getListener().requirepackage(null, "graphicx", false, stack);
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
      glossariesSty.addField("mnemonic");
      glossariesSty.addField("tooltip");
      glossariesSty.addField("keystroke");

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

      registerControlSequence(new AssignedControlSequence(
        "includeimg", getParser().getControlSequence("includegraphics")));

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

      glossariesSty.createGlossary("messages", null, null, null, null, null,
       true, true, Overwrite.FORBID);
      glossariesSty.createGlossary("keystrokes", null, null, null, null, null,
       true, true, Overwrite.FORBID);
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
