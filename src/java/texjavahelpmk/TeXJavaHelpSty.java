/*
    Copyright (C) 2024-2025 Nicola L.C. Talbot
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

import com.dickimawbooks.texparserlib.latex.latex3.PropertyCommand;

import com.dickimawbooks.texparserlib.latex.nlctdoc.UserGuideSty;
import com.dickimawbooks.texparserlib.latex.nlctdoc.TaggedColourBox;
import com.dickimawbooks.texparserlib.latex.nlctdoc.ColourBox;
import com.dickimawbooks.texparserlib.latex.nlctdoc.IndexInitPostNameHooks;
import com.dickimawbooks.texparserlib.latex.nlctdoc.AbbrPostNameHook;
import com.dickimawbooks.texparserlib.latex.nlctdoc.ExampleEnv;
import com.dickimawbooks.texparserlib.latex.nlctdoc.InlineGlsDef;

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

      addSemanticCommand("iconfmt", "icon", null, null, null, null);

      addDiscretionaryCommands();
      addCrossRefCommands();
      addFootnoteCommands();
      addDocRefCommands();// NB not all supported by texjavahelp.sty
      addSymbolCommands();
      addTextCommands();
      addListCommands();

      addBasicBoxCommands();

      addStandaloneDefCommands();
      registerControlSequence(new WidgetDef(pinnedBox, rightBox, noteBox, glossariesSty));
      registerControlSequence(new MenuDef(settingsBox, rightBox, noteBox, glossariesSty));
      registerControlSequence(new SectionEntry(glossariesSty));

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
      registerControlSequence(new GenericCommand("TeXJavaHelpGlsSelection"));

      registerControlSequence(new GenericCommand("continueline"));
      registerControlSequence(new GenericCommand("ContExplan"));
      registerControlSequence(listener.createSymbol(
        "continuesymbol", 0x21A9));

      addLangCommands();

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
      registerControlSequence(listener.createSymbol(
        "contextmenusym", 0x2630));

      registerControlSequence(listener.createSymbol("Slash", '/'));

      if (listener.isHtml5())
      {
         registerControlSequence(new GenericCommand(true,
           "spacekeysym", null, new HtmlTag("<span class=\"spacekey\"> </span>")));
      }
      else
      {
         registerControlSequence(AccSuppObject.createSymbol(
           listener, "spacekeysym", 0x2423, 
           getHelpLib().getMessageWithFallback(
              "manual.space_key_title", "space key"),
           true));
      }

      registerControlSequence(AccSuppObject.createSymbol(
        listener, "menusep", 0x279C, 
        getHelpLib().getMessageWithFallback(
           "manual.menu_separator_title", "menu separator"),
        true));

      registerControlSequence(new GenericCommand(true,
       "mnemonicsep", null, new TeXCsRef("menusep")));

      registerControlSequence(new LaTeXGenericCommand(true,
       "mnemonicitemref", "m", 
         TeXParserUtils.createStack(listener, new TeXCsRef("mnemonic"),
           TeXParserUtils.createGroup(listener, listener.getParam(1))
         )));

      registerControlSequence(new LeftQuadPar());

      registerControlSequence(new IconStartPar());
      registerControlSequence(
       new TextualContentCommand("iconstartparsep", "\u2009"));

      registerControlSequence(new FloatTable());
      registerControlSequence(new GenericCommand("posttablecaption"));

      registerControlSequence(new FloatFig());

      listener.newcounter("subfigure", null, "@alph");

      registerControlSequence(new FloatSubFigs());
      registerControlSequence(new SubFigureContent());
      registerControlSequence(new SubFigRef());

      // Not used but define here to prevent error if it needs to be
      // redefined for the PDF.

      registerControlSequence(new GenericCommand("postsubfigcap"));

      registerControlSequence(new LaTeXGenericCommand(true,
       "subfigurefmt", "m", TeXParserUtils.createStack(listener,
         listener.getOther('('), listener.getParam(1),
         listener.getOther(')'))));

      addSemanticCommand("subfigurefmt", new TeXFontText(TeXFontShape.EM),
        null, listener.getOther('('), listener.getOther(')'));

      registerControlSequence(new GenericCommand(true,
       "subfigurelabel", null, TeXParserUtils.createStack(listener,
         new TeXCsRef("subfigurefmt"), new TeXCsRef("thesubfigure"))));

      registerControlSequence(new LaTeXGenericCommand(true,
       "subfigurecap", "m", TeXParserUtils.createStack(listener,
         new TeXCsRef("subfigurelabel"), new TeXCsRef("space"),
         listener.getParam(1))));

      registerControlSequence(new LaTeXGenericCommand(true,
       "subfigure@pfmt", "mm", TeXParserUtils.createStack(listener,
         listener.getParam(1),
         new TeXCsRef("subfigurefmt"), 
         TeXParserUtils.createGroup(listener, listener.getParam(2)))));

      registerControlSequence(new SeeAlsoRefs());
      registerControlSequence(new TextualContentCommand("multiseealsosep", ":"));

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


      KeyValList defOpts = new KeyValList();
      defOpts.put("textformat", listener.createString("widgetfmt"));

      Dglsfield widgetCs = new Dglsfield("widget", glossariesSty, CaseChange.NO_CHANGE, "text");
      widgetCs.setDefaultOptions(defOpts);

      registerControlSequence(widgetCs);

      widgetCs = new Dglsfield("Widget", glossariesSty, CaseChange.SENTENCE, "text");
      widgetCs.setDefaultOptions(defOpts);

      registerControlSequence(widgetCs);

      widgetCs = new Dglsfield("WIDGET", glossariesSty, CaseChange.TO_UPPER, "text");
      widgetCs.setDefaultOptions(defOpts);

      registerControlSequence(widgetCs);

      registerControlSequence(new MenuCs(glossariesSty));
      registerControlSequence(new MenuTrail(glossariesSty));
      registerControlSequence(new MenuItemsStyle(glossariesSty));

      registerControlSequence(new MnemonicTrail(glossariesSty));

      registerControlSequence(new DialogCs());

      registerControlSequence(
        new GlsEntryField("entrymnemonic", "mnemonic", glossariesSty));

      registerControlSequence(
        new GlsEntryField("entrytooltip", "tooltip", glossariesSty));

      registerControlSequence(
        new GlsEntryField("entrykeystroke", "keystroke", glossariesSty));

      registerControlSequence(new Dglsfield("btn", glossariesSty,
         CaseChange.NO_CHANGE, "tooltip"));

      registerControlSequence(new Dglsfield("accelerator", glossariesSty,
         CaseChange.NO_CHANGE, "keystroke"));

      registerControlSequence(new Dglsfield("mnemonic", glossariesSty,
         CaseChange.NO_CHANGE, "mnemonic"));

      glossariesSty.addField("iconimage");

      registerControlSequence(
         new GlsEntryField("entryiconimage", "iconimage", glossariesSty));

      registerControlSequence(new TextualContentCommand("menuiconsep", " "));

      registerControlSequence(new Widget("menufmt", "menu"));
      registerControlSequence(new Widget("widgetfmt", "widget"));
      registerControlSequence(new Widget("dialogfmt", "dialog"));
      registerControlSequence(new Widget("buttonfmt", "button"));
      registerControlSequence(new WidgetKeyStroke("keystrokefmt"));

      registerControlSequence(new AtFirstOfOne("actualkey"));
      registerControlSequence(new TextualContentCommand("keysep", "+"));
      registerControlSequence(new KeysCs());

      registerControlSequence(new KeyRef(glossariesSty));

      registerControlSequence(new KeyDescRef(glossariesSty));
      registerControlSequence(new KeyDescRef("desckeyref",
         CaseChange.TO_LOWER, false, glossariesSty));
      registerControlSequence(new KeyDescRef("Desckeyref",
         CaseChange.SENTENCE, false, glossariesSty));

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
      grp = listener.createGroup();

      def.add(new TeXCsRef("csuse"));
      def.add(grp);
      grp.add(listener.getParam(1));
      grp.addAll(listener.createString("desc"));

      registerControlSequence(new LaTeXGenericCommand(true, "icondesc",
       "m", def));

      registerControlSequence(new AtFirstOfOne("msgellipsis"));
      registerControlSequence(new AtGobble("msgendcolon"));

      registerControlSequence(new PropertyCommand<Integer>(MSG_PARAM_PROP_NAME));

      registerControlSequence(new MsgParam());
      registerControlSequence(new DefMsgParam());
      registerControlSequence(new UndefMsgParam());
      registerControlSequence(new ClearMsgParams());
      registerControlSequence(new SetDefaultMsgParams(glossariesSty));
      registerControlSequence(new AtNumberOfNumber("msgchoiceparamitem", 3, 3));

      // TODO
      registerControlSequence(new LaTeXGenericCommand(true, "msgchoiceparam",
       "mm", listener.createString("[\u2026]")));

      registerControlSequence(new GeneralMsg(glossariesSty));
      registerControlSequence(new GeneralMsg(glossariesSty, "msg", "message."));
      registerControlSequence(new GeneralMsg(glossariesSty, "warnmsg", "warning."));
      registerControlSequence(new GeneralMsg(glossariesSty, "errmsg", "error."));
      registerControlSequence(new GeneralMsg(glossariesSty, "syntaxmsg", "syntax."));

      registerControlSequence(new GeneralMsg(glossariesSty, "manmsg", "manual."));
      registerControlSequence(new GeneralMsg(glossariesSty, "Manmsg", "manual.",
        CaseChange.SENTENCE, false));
      registerControlSequence(new GeneralMsg(glossariesSty, "manmsgpl", "manual.",
        CaseChange.NO_CHANGE, true));
      registerControlSequence(new GeneralMsg(glossariesSty, "Manmsgpl", "manual.",
        CaseChange.SENTENCE, true));

      registerControlSequence(new InlineMsgDef(glossariesSty));

      registerControlSequence(new InlineGlsDef("inlineglspluraldef", "",
        "plural", true, glossariesSty));

      // dual prefix list
      def = listener.createString("action.,button.,menu.,widget.,help.,index.,label.,");
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

      registerControlSequence(new LaTeXGenericCommand(true,
        "symbolparen", "m", TeXParserUtils.createStack(listener,
         listener.getOther('('), listener.getParam(1), listener.getOther(')'))));

      registerControlSequence(new LaTeXGenericCommand(true,
        "cspuncfmt", "m", TeXParserUtils.createStack(listener,
         new TeXCsRef("csfmt"),
          TeXParserUtils.createGroup(listener, listener.getParam(1)))));

      registerControlSequence(new GenericCommand(true, "quad", null, 
       new TeXObject[] {new HtmlTag("<span class=\"quad\">&nbsp; </span>")}));
      registerControlSequence(new GenericCommand(true, "qquad", null, 
       new TeXObject[] {new HtmlTag("<span class=\"qquad\">&nbsp; &nbsp; </span>")}));

   }

   protected void addLangCommands()
   {
      registerControlSequence(new ManualText(glossariesSty));
      registerControlSequence(
        new ManualText("manualplural", "plural", glossariesSty));
      registerControlSequence(
        new ManualText("manualdesc", "description", glossariesSty));

      registerControlSequence(
        new ManualText("Manualtext", "text", CaseChange.SENTENCE, glossariesSty));
      registerControlSequence(
        new ManualText("Manualplural", "plural", CaseChange.SENTENCE, glossariesSty));
      registerControlSequence(
        new ManualText("Manualdesc", "description", CaseChange.SENTENCE, glossariesSty));

      registerControlSequence(
        new ManualText("ManualText", "text", CaseChange.SENTENCE, CaseChange.SENTENCE, glossariesSty));
      registerControlSequence(
        new ManualText("ManualPlural", "plural", CaseChange.SENTENCE, CaseChange.SENTENCE, glossariesSty));
      registerControlSequence(
        new ManualText("ManualDesc", "description", CaseChange.SENTENCE, CaseChange.SENTENCE, glossariesSty));

      registerControlSequence(createLangCs("figure", "figure"));
      registerControlSequence(createLangCs("Figurename",
        "Manualtext", "figure", "Figure"));
      registerControlSequence(createLangCs("figuresname",
        "manualplural", "figure", "figures"));
      registerControlSequence(createLangCs("Figuresname",
        "Manualplural", "figure", "Figures"));

      registerControlSequence(createLangCs("figurecaptionname",
        "ManualText", "figure", "figure"));

      registerControlSequence(new GenericCommand(true,
        "fnum@figure", null, TeXParserUtils.createStack(getListener(),
         new TeXCsRef("figurecaptionname"),
         getListener().getSpace(),
         new TeXCsRef("thefigure"))));

      registerControlSequence(createLangCs("table", "table"));
      registerControlSequence(createLangCs("Tablename",
        "Manualtext", "table", "Table"));
      registerControlSequence(createLangCs("tablesname",
        "manualplural", "table", "tables"));
      registerControlSequence(createLangCs("Tablesname",
        "Manualplural", "table", "Tables"));

      registerControlSequence(createLangCs("tablecaptionname",
        "ManualText", "table", "table"));

      registerControlSequence(new GenericCommand(true,
        "fnum@table", null, TeXParserUtils.createStack(getListener(),
         new TeXCsRef("tablecaptionname"),
         getListener().getSpace(),
         new TeXCsRef("thetable"))));

      registerControlSequence(createLangCs("listofexamples", "Examples"));
      registerControlSequence(createLangCs("contents", "Contents"));
      registerControlSequence(createLangCs("glossary", "Glossary"));
      registerControlSequence(createLangCs("abbreviations", "Abbreviations"));
      registerControlSequence(createLangCs("glssymbolsgroup", "Symbols"));
      registerControlSequence(createLangCs("index", "Index"));
      registerControlSequence(createLangCs("summary", "Summary"));
      registerControlSequence(createLangCs("section", "section"));
      registerControlSequence(createLangCs("seealsoname", "see also"));

      registerControlSequence(createLangCs("example", "example"));
      registerControlSequence(createLangCs("Examplename",
        "Manualtext", "example", "Example"));
      registerControlSequence(createLangCs("examplesname",
        "manualplural", "example", "examples"));
      registerControlSequence(createLangCs("Examplesname",
        "Manualplural", "example", "Examples"));

      registerControlSequence(new GenericCommand(true,
        "nlctexampletag", null, TeXParserUtils.createStack(listener,
          new TeXCsRef("examplename"), listener.getSpace(),
          new TeXCsRef("theexample"))));

      listener.newcounter("example");
      registerControlSequence(new ExampleEnv());

      registerControlSequence(createLangCs("idxpackage", "package"));
      registerControlSequence(createLangCs("idxclass", "class"));
      registerControlSequence(createLangCs("idxenv", "environment"));
      registerControlSequence(createLangCs("idxcounter", "counter"));
      registerControlSequence(createLangCs("idxvariablename", "variable"));
      registerControlSequence(createLangCs("idxmenu", "menu"));
      registerControlSequence(createLangCs("idxbutton", "button"));

      registerControlSequence(new GenericCommand(true, "postmenuname",
       null, TeXParserUtils.createStack(listener,
        listener.getSpace(), new TeXCsRef("idxmenuname"))));

      registerControlSequence(new GenericCommand(true, "postbuttonname",
       null, TeXParserUtils.createStack(listener,
        listener.getSpace(), new TeXCsRef("idxbuttonname"))));

      registerControlSequence(createLangCs("warningtext",
        "manualtext", "warning", "Warning"));
      registerControlSequence(createLangCs("warningdesc",
        "manualdesc", "warning", "a warning"));

      registerControlSequence(createLangCs("importanttext",
        "manualtext", "important", "Important"));
      registerControlSequence(createLangCs("importantdesc",
        "manualdesc", "important", "an important message"));

      registerControlSequence(createLangCs("informationtext",
        "manualtext", "information", "Information"));
      registerControlSequence(createLangCs("informationdesc",
        "manualdesc", "information", "prominent information"));

      registerControlSequence(createLangCs("definitiontext",
        "manualtext", "definition", "Definition"));
      registerControlSequence(createLangCs("informationdesc",
        "manualdesc", "definition",
        "the syntax and usage of a command, environment or option etc"));

      registerControlSequence(createLangCs("valuesettingtext",
        "manualtext", "valuesetting", "Setting"));
      registerControlSequence(createLangCs("valuesettingdesc",
        "manualdesc", "valuesetting", "an option that takes a value"));

      registerControlSequence(createLangCs("novaluesettingtext",
        "manualtext", "novaluesetting", "Valueless Setting"));
      registerControlSequence(createLangCs("novaluesettingdesc",
        "manualdesc", "novaluesetting", "an option that doesn't take a value"));

      registerControlSequence(createLangCs("toggleonsettingtext",
        "manualtext", "toggleonsetting", "Toggle (On)"));
      registerControlSequence(createLangCs("toggleonsettingdesc",
        "manualdesc", "toggleonsetting", "a boolean option that is initially true"));

      registerControlSequence(createLangCs("toggleoffsettingtext",
        "manualtext", "toggleoffsetting", "Toggle (Off)"));
      registerControlSequence(createLangCs("toggleoffsettingdesc",
        "manualdesc", "toggleoffsetting", "a boolean option that is initially false"));

      registerControlSequence(createLangCs("optionvaluetext",
        "manualtext", "optionvaluedef", "Option Value Definition"));
      registerControlSequence(createLangCs("optionvaluedesc",
        "manualdesc", "optionvaluedef", "the definition of an option value"));

      registerControlSequence(createLangCs("countertext",
        "manualtext", "counterdef", "Counter"));
      registerControlSequence(createLangCs("counterdesc",
        "manualdesc", "counterdef", "a counter is being described"));

      registerControlSequence(createLangCs("bannedtext",
        "manualtext", "banned", "Don't use"));
      registerControlSequence(createLangCs("banneddesc",
        "manualdesc", "banned", "a command, environment or option that should not be used with this package"));

      registerControlSequence(createLangCs("terminaltext",
        "manualtext", "terminal", "Terminal"));
      registerControlSequence(createLangCs("terminaldesc",
        "manualdesc", "terminal", "a command-line application invocation that needs to be entered into a terminal or command prompt"));

      registerControlSequence(createLangCs("codetext",
        "manualtext", "codeinput", "Input"));
      registerControlSequence(createLangCs("codedesc",
        "manualdesc", "codeinput", "LaTeX code to insert into your document"));

      registerControlSequence(createLangCs("badcodetext",
        "manualtext", "badcode", "Problematic Input"));
      registerControlSequence(createLangCs("badcodedesc",
        "manualdesc", "badcode", "problematic code which should be avoided"));

      registerControlSequence(createLangCs("unicodetext",
        "manualtext", "unicodeinput", "Unicode Input"));
      registerControlSequence(createLangCs("unicodedesc",
        "manualdesc", "unicodeinput", "code that requires a native Unicode engine (XeLaTeX or LuaLaTeX)"));

      registerControlSequence(createLangCs("resulttext",
        "manualtext", "coderesult", "Result"));
      registerControlSequence(createLangCs("resultdesc",
        "manualdesc", "coderesult", "how the example code should appear in the PDF"));

      registerControlSequence(createLangCs("transcripttext",
        "manualtext", "transcript", "Transcript"));
      registerControlSequence(createLangCs("transcriptdesc",
        "manualdesc", "transcript", "text in a transcript or log file or written to STDOUT or STDERR"));

      registerControlSequence(new IncludeImg());
      registerControlSequence(new IncludeImg("includetimg", "topalign"));

   }

   protected ControlSequence createLangCs(String tag, String defText)
   {
      return createLangCs(tag+"name", "manualtext", tag, defText);
   }

   protected ControlSequence createLangCs(String name, String manualCsName,
     String tag, String defText)
   {
      TeXJavaHelpLib helpLib = getHelpLib();
      TeXParserListener listener = getListener();

      return new GenericCommand(true, name, null,
       TeXParserUtils.createStack(listener,
        new TeXCsRef(manualCsName),
        listener.createGroup(tag),
        listener.createGroup(helpLib.getMessageWithFallback(
          "manual."+tag, defText))));
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
      if (!(option.equals("fnsymleft")
          ||option.equals("fnsymright")
          ||option.equals("vref")
          ||option.equals("novref")
           )
         )
      {
         glossariesSty.processOption(option, value);
      }
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

   public static final String MSG_PARAM_PROP_NAME = "l__texjavahelp_msgparam_prop";
}
