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
package com.dickimawbooks.texjavahelplib;

import java.util.Locale;

import java.awt.Color;
import java.awt.Font;

import javax.swing.text.html.StyleSheet;

public class HelpFontSettings
{
   public void addFontRulesToStyleSheet(StyleSheet styles, int modifiers)
   {
      if ((modifiers & HelpFontChangeEvent.BODY_FONT)
             == HelpFontChangeEvent.BODY_FONT
          || (modifiers & HelpFontChangeEvent.BODY_SIZE)
             == HelpFontChangeEvent.BODY_SIZE
         )
      {
         styles.addRule(getBodyFontRule());
      }

      if ((modifiers & HelpFontChangeEvent.ICON_FONT)
             == HelpFontChangeEvent.ICON_FONT
         )
      {
         styles.addRule(getIconFontRule());
      }

      if ((modifiers & HelpFontChangeEvent.KEYSTROKE_FONT)
             == HelpFontChangeEvent.KEYSTROKE_FONT
         )
      {
         styles.addRule(getKeyStrokeFontRule());
      }

      if ((modifiers & HelpFontChangeEvent.MONO_FONT)
             == HelpFontChangeEvent.MONO_FONT
         )
      {
         styles.addRule(getMonoFontRule());
      }

      if ((modifiers & HelpFontChangeEvent.LINK_COLOR)
             == HelpFontChangeEvent.LINK_COLOR
          || (modifiers & HelpFontChangeEvent.LINK_DECORATION)
             == HelpFontChangeEvent.LINK_DECORATION
         )
      {
         styles.addRule(getLinkRule());
      }
   }

   public void addFontRulesToStyleSheet(StyleSheet styles)
   {
      styles.addRule(getBodyFontRule());
      styles.addRule(getIconFontRule());
      styles.addRule(getKeyStrokeFontRule());
      styles.addRule(getMonoFontRule());
      styles.addRule(getLinkRule());
   }

   public void appendRules(StringBuilder builder, int modifiers)
   {
      if ((modifiers & HelpFontChangeEvent.BODY_FONT)
             == HelpFontChangeEvent.BODY_FONT
          || (modifiers & HelpFontChangeEvent.BODY_SIZE)
             == HelpFontChangeEvent.BODY_SIZE
         )
      {
         builder.append(getBodyFontRule());
      }

      if ((modifiers & HelpFontChangeEvent.ICON_FONT)
             == HelpFontChangeEvent.ICON_FONT
         )
      {
         builder.append(getIconFontRule());
      }

      if ((modifiers & HelpFontChangeEvent.KEYSTROKE_FONT)
             == HelpFontChangeEvent.KEYSTROKE_FONT
         )
      {
         builder.append(getKeyStrokeFontRule());
      }

      if ((modifiers & HelpFontChangeEvent.MONO_FONT)
             == HelpFontChangeEvent.MONO_FONT
         )
      {
         builder.append(getMonoFontRule());
      }

      if ((modifiers & HelpFontChangeEvent.LINK_COLOR)
             == HelpFontChangeEvent.LINK_COLOR
          || (modifiers & HelpFontChangeEvent.LINK_DECORATION)
             == HelpFontChangeEvent.LINK_DECORATION
         )
      {
         builder.append(getLinkRule());
      }

   }

   public void appendRules(StringBuilder builder)
   {
      builder.append(getBodyFontRule());
      builder.append(getIconFontRule());
      builder.append(getKeyStrokeFontRule());
      builder.append(getMonoFontRule());
      builder.append(getLinkRule());
   }

   public void addBodyFontRuleToStyleSheet(StyleSheet styles)
   {
      styles.addRule(getBodyFontRule());
   }

   public void addIconFontRuleToStyleSheet(StyleSheet styles)
   {
      styles.addRule(getIconFontRule());
   }

   public void addKeyStrokeFontRuleToStyleSheet(StyleSheet styles)
   {
      styles.addRule(getKeyStrokeFontRule());
   }

   public void addMonoFontRuleToStyleSheet(StyleSheet styles)
   {
      styles.addRule(getMonoFontRule());
   }

   public void addLinkRuleToStyleSheet(StyleSheet styles)
   {
      styles.addRule(getLinkRule());
   }

   public String getIconFontCssName()
   {
      return iconFontName;
   }

   public String getIconFontName()
   {
      return getFontNameFromCss(iconFontName);
   }

   public void setIconFontCssName(String cssName)
   {
      this.iconFontName = cssName;

      iconFontNameNeedsQuotes = fontNameNeedsQuotes(cssName);
   }

   public String getIconFontRule()
   {
      return getIconFontRule(iconFontNameNeedsQuotes, iconFontName);
   }

   public String getKeyStrokeFontCssName()
   {
      return keystrokeFontName;
   }

   public String getKeyStrokeFontName()
   {
      return getFontNameFromCss(keystrokeFontName);
   }

   public void setKeyStrokeFontCssName(String cssName)
   {
      this.keystrokeFontName = cssName;

      keystrokeFontNameNeedsQuotes = fontNameNeedsQuotes(cssName);
   }

   public String getKeyStrokeFontRule()
   {
      return getKeyStrokeFontRule(keystrokeFontNameNeedsQuotes, keystrokeFontName);
   }

   public String getMonoFontCssName()
   {
      return monoFontName;
   }

   public String getMonoFontName()
   {
      return getFontNameFromCss(monoFontName);
   }

   public void setMonoFontCssName(String cssName)
   {
      this.monoFontName = cssName;
      monoFontNameNeedsQuotes = fontNameNeedsQuotes(cssName);
   }

   public String getMonoFontRule()
   {
      return getMonoFontRule(monoFontNameNeedsQuotes, monoFontName);
   }

   public void setBodyFontCssName(String cssName)
   {
      this.fontName = cssName;

      fontNameNeedsQuotes = fontNameNeedsQuotes(cssName);
   }

   public void setBodyFontSize(int fontSize)
   {
      this.fontSize = fontSize;
   }

   public int getBodyFontSize()
   {
      return fontSize;
   }

   public String getBodyFontCssName()
   {
      return fontName;
   }

   public String getBodyFontName()
   {
      return getFontNameFromCss(fontName);
   }

   public Font getBodyFont()
   {
      return new Font(getFontNameFromCss(fontName), Font.PLAIN, fontSize);
   }

   public String getBodyFontRule()
   {
      return getBodyFontRule(fontNameNeedsQuotes, fontName, fontSize);
   }

   public String getLinkRule()
   {
      return getLinkRule(linkColor, linkDecoration);
   }

   public String getLinkDecoration()
   {
      return linkDecoration;
   }

   public void setLinkDecoration(String decoration)
   {
      linkDecoration = decoration;
   }

   public Color getLinkColor()
   {
      return linkColor;
   }

   public void setLinkColor(Color color)
   {
      linkColor = color;
   }

   public void copyFrom(HelpFontSettings other)
   {
      fontSize = other.fontSize;
      fontName = other.fontName;
      fontNameNeedsQuotes = other.fontNameNeedsQuotes;

      iconFontName = other.iconFontName;
      iconFontNameNeedsQuotes = other.iconFontNameNeedsQuotes;

      keystrokeFontName = other.keystrokeFontName;
      keystrokeFontNameNeedsQuotes = other.keystrokeFontNameNeedsQuotes;

      monoFontName = other.monoFontName;
      monoFontNameNeedsQuotes = other.monoFontNameNeedsQuotes;

      linkDecoration = other.linkDecoration;
      linkColor = other.linkColor;
   }

   public void copyFrom(HelpFontChangeEvent event)
   {
      HelpFontSettings other = event.getSettings();
      int modifiers = event.getModifiers();

      if ( (modifiers & HelpFontChangeEvent.BODY_SIZE)
             == HelpFontChangeEvent.BODY_SIZE )
      {
         fontSize = other.fontSize;
      }

      if ((modifiers & HelpFontChangeEvent.BODY_FONT)
             == HelpFontChangeEvent.BODY_FONT )
      {
         fontName = other.fontName;
         fontNameNeedsQuotes = other.fontNameNeedsQuotes;
      }

      if ((modifiers & HelpFontChangeEvent.ICON_FONT)
             == HelpFontChangeEvent.ICON_FONT
         )
      {
         iconFontName = other.iconFontName;
         iconFontNameNeedsQuotes = other.iconFontNameNeedsQuotes;
      }

      if ((modifiers & HelpFontChangeEvent.KEYSTROKE_FONT)
             == HelpFontChangeEvent.KEYSTROKE_FONT
         )
      {
         keystrokeFontName = other.keystrokeFontName;
         keystrokeFontNameNeedsQuotes = other.keystrokeFontNameNeedsQuotes;
      }

      if ((modifiers & HelpFontChangeEvent.MONO_FONT)
             == HelpFontChangeEvent.MONO_FONT
         )
      {
         monoFontName = other.monoFontName;
         monoFontNameNeedsQuotes = other.monoFontNameNeedsQuotes;
      }

      if ((modifiers & HelpFontChangeEvent.LINK_COLOR)
             == HelpFontChangeEvent.LINK_COLOR
          || (modifiers & HelpFontChangeEvent.LINK_DECORATION)
             == HelpFontChangeEvent.LINK_DECORATION
         )
      {
         linkDecoration = other.linkDecoration;
         linkColor = other.linkColor;
      }
   }

   public static boolean fontNameNeedsQuotes(String name)
   {
      return name.matches("[^\\p{IsAlphabetic}\\-]");
   }

   public static String getFontNameFromCss(String cssName)
   {
      if (cssName.equals("monospace"))
      {
         return "Monospaced";
      }
      else if (cssName.equals("serif"))
      {
         return "Serif";
      }
      else if (cssName.equals("sans-serif"))
      {
         return "SansSerif";
      }

      return cssName;
   }

   public static String getFontCssName(String name)
   {
      if (name.equals("Monospaced"))
      {
         return "monospace";
      }
      else if (name.equals("Serif"))
      {
         return "serif";
      }
      else if (name.equals("SansSerif"))
      {
         return "sans-serif";
      }

      return name;
   }

   public static String getBodyFontRule(boolean fontNameNeedsQuotes,
      String fontName, int fontSize)
   {
      String rule;

      if (fontName.equals(FALLBACK_FONT_KEYWORD))
      {
         rule = String.format((Locale)null,
           "body { font-family: %s; font-size: %d; }",
           fontName, fontSize);
      }
      else if (fontNameNeedsQuotes)
      {
         rule = String.format((Locale)null,
           "body { font-family: \"%s\", %s; font-size: %d; }",
           fontName, FALLBACK_FONT_KEYWORD, fontSize);
      }
      else
      {
         rule = String.format((Locale)null,
           "body { font-family: %s, %s; font-size: %d; }",
           fontName, FALLBACK_FONT_KEYWORD, fontSize);
      }

      return rule;
   }

   public static String getMonoFontRule(boolean fontNameNeedsQuotes,
      String fontName)
   {
      String rule;

      if (fontName.equals(FALLBACK_MONO_FONT_KEYWORD))
      {
         rule = String.format("%s { font-family: %s; }",
           TeXJavaHelpLib.MONO_CSS_CLASSES, fontName);
      }
      else if (fontNameNeedsQuotes)
      {
         rule = String.format("%s { font-family: \"%s\", %s; }",
           TeXJavaHelpLib.MONO_CSS_CLASSES, fontName,
           FALLBACK_MONO_FONT_KEYWORD);
      }
      else
      {
         rule = String.format("%s { font-family: %s, %s; }",
           TeXJavaHelpLib.MONO_CSS_CLASSES, fontName,
           FALLBACK_MONO_FONT_KEYWORD);
      }

      return rule;
   }

   public static String getKeyStrokeFontRule(boolean fontNameNeedsQuotes,
      String fontName)
   {
      String rule;

      if (fontName.equals(FALLBACK_KEYSTROKE_FONT_KEYWORD))
      {
         rule = String.format(".keystroke { font-family: %s; }", fontName);
      }
      else if (fontNameNeedsQuotes)
      {
         rule = String.format(".keystroke { font-family: \"%s\", %s; }",
           fontName, FALLBACK_KEYSTROKE_FONT_KEYWORD);
      }
      else
      {
         rule = String.format(".keystroke { font-family: %s, %s; }",
           fontName, FALLBACK_KEYSTROKE_FONT_KEYWORD);
      }

      return rule;
   }

   public static String getIconFontRule(boolean fontNameNeedsQuotes,
      String fontName)
   {
      String rule;

      if (fontName.equals(FALLBACK_ICON_FONT_KEYWORD))
      {
         rule = String.format("%s { font-family: %s; }",
           TeXJavaHelpLib.ICON_CSS_CLASSES, fontName);
      }
      else if (fontNameNeedsQuotes)
      {
         rule = String.format("%s { font-family: \"%s\", %s; }",
           TeXJavaHelpLib.ICON_CSS_CLASSES, fontName, FALLBACK_ICON_FONT_KEYWORD);
      }
      else
      {
         rule = String.format("%s { font-family: %s, %s; }",
           TeXJavaHelpLib.ICON_CSS_CLASSES, fontName, FALLBACK_ICON_FONT_KEYWORD);
      }

      return rule;
   }

   public static String getLinkRule(Color col, String decoration)
   {
      return String.format((Locale)null,
        "a { text-decoration: %s; color: rgb(%d,%d,%d); }",
        decoration, col.getRed(), col.getGreen(), col.getBlue());
   }

   public static final String FALLBACK_FONT_KEYWORD = "sans-serif";
   public static final String FALLBACK_FONT_NAME = "SansSerif";
   protected int fontSize = 12;
   protected String fontName = FALLBACK_FONT_KEYWORD;
   protected boolean fontNameNeedsQuotes = false;

   public static final String FALLBACK_ICON_FONT_KEYWORD = "serif";
   protected String iconFontName = FALLBACK_ICON_FONT_KEYWORD;
   protected boolean iconFontNameNeedsQuotes = false;

   public static final String FALLBACK_KEYSTROKE_FONT_KEYWORD = "sans-serif";
   protected String keystrokeFontName = FALLBACK_KEYSTROKE_FONT_KEYWORD;
   protected boolean keystrokeFontNameNeedsQuotes = false;

   public static final String FALLBACK_MONO_FONT_KEYWORD = "monospace";
   protected String monoFontName = FALLBACK_MONO_FONT_KEYWORD;
   protected boolean monoFontNameNeedsQuotes = false;

   protected String linkDecoration = "none";
   protected Color linkColor = Color.BLUE;
}
