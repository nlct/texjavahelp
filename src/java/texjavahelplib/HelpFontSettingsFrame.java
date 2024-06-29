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
import java.util.Vector;

import java.text.Collator;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import javax.swing.*;

import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 * Frame for showing font selector.
 */
public class HelpFontSettingsFrame extends JFrame implements ItemListener
{
   public HelpFontSettingsFrame(final HelpFrame helpFrame)
   {
      super(helpFrame.getHelpLib().getMessage("help.settings.font.title"));

      this.helpFrame = helpFrame;
      sampleHelpFontSettings = new HelpFontSettings();

      init();
   }

   private void init()
   {
      TeXJavaHelpLib helpLib = helpFrame.getHelpLib();

      String locPrefix = helpLib.getMessageIfExists("manual.location_prefix");

      if (locPrefix == null)
      {
         locPrefix = helpLib.getMessage("symbol.help.navigation.history.pointer");
      }

      String iconSample = locPrefix+"\u279C";
      String keystrokeSample = "\u21E7\u21B9";

      String sample = String.format(
         "<html><style>%s %s %s { font-family: monospace; }</style><body>%s<pre class=\"code\">\\ | / -- * + %% ~ # { } [ ] ^ _</pre><p><span class=\"locationprefix\">%s</span> %s<span title=\"%s\" class=\"icon\">\u279C</span>%s <span class=\"keystroke\">I</span> <span class=\"keystroke\">L</span> <span class=\"keystroke\">1</span> <span class=\"keystroke\">\u21E7</span> <span class=\"keystroke\">\u21B9</span></body></html>",
         TeXJavaHelpLib.KEYSTROKE_CSS,
         TeXJavaHelpLib.ICON_CSS,
         TeXJavaHelpLib.MONO_CSS_CLASSES,
         helpLib.getMessage("text.help.settings.font_sample"),
         locPrefix,
         helpLib.getMessage("menu.help"),
         helpLib.getMessage("manual.menu_separator_title"),
         helpLib.getMessage("menu.help.manual"));

      sampleComp = new JEditorPane("text/html", sample);
      sampleComp.setEditable(false);

      sampleComp.setMargin(new Insets(4, 4, 4, 4));

      getContentPane().add(sampleComp, "North");

      Box box = Box.createVerticalBox();
      box.setAlignmentX(0.0f);

      getContentPane().add(new JScrollPane(box), "Center");

      JComponent panel = createRow();
      box.add(panel);

      GraphicsEnvironment grp = GraphicsEnvironment.getLocalGraphicsEnvironment();
      Locale locale = helpLib.getMessagesLocale();

      Font[] allFonts = grp.getAllFonts();
      fontNames = new Vector<String>(allFonts.length);
      iconFontNames = new Vector<String>(allFonts.length);
      keystrokeFontNames = new Vector<String>(allFonts.length);
      monoFontNames = new Vector<String>(allFonts.length);

      for (Font font : allFonts)
      {
         String name = font.getFamily(locale);

         if (!fontNames.contains(name))
         {
            fontNames.add(name);
         }

         if (font.canDisplayUpTo(iconSample) == -1)
         {
            if (!iconFontNames.contains(name))
            {
               iconFontNames.add(name);
            }
         }

         if (font.canDisplayUpTo(keystrokeSample) == -1)
         {
            if (!keystrokeFontNames.contains(name))
            {
               keystrokeFontNames.add(name);
            }
         }

         if (!monoFontNames.contains(name))
         {
            FontMetrics fontMetrics = helpFrame.getHelpFontMetrics(font);

            if (fontMetrics.charWidth('m') == fontMetrics.charWidth('i'))
            {
               monoFontNames.add(name);
            }
         }
      }

      Collator collator = Collator.getInstance(locale);
      fontNames.sort(collator);
      iconFontNames.sort(collator);
      keystrokeFontNames.sort(collator);
      monoFontNames.sort(collator);

      // Default font family

      fontFamily = new JComboBox<String>(fontNames);
      fontFamily.addItemListener(this);
      fontFamily.setAlignmentX(0.0f);

      panel.add(createJLabel("help.settings.font.family", fontFamily));
      panel.add(fontFamily);

      panel = createRow();
      box.add(panel);

      // Default font size

      fontSizeSpinnerModel = new SpinnerNumberModel(12, 2, 100, 1);
      fontSizeSpinner = new JSpinner(fontSizeSpinnerModel);
      fontSizeSpinner.addChangeListener(new ChangeListener()
       {
          @Override
          public void stateChanged(ChangeEvent e)
          {
             updateSample();
          }
       });
      fontSizeSpinner.setAlignmentX(0.0f);

      panel.add(createJLabel("help.settings.font.size", fontSizeSpinner));
      panel.add(fontSizeSpinner);

      // Icon font family

      panel = createRow();
      box.add(panel);

      iconFontFamily = new JComboBox<String>(iconFontNames);
      iconFontFamily.addItemListener(this);
      iconFontFamily.setAlignmentX(0.0f);

      panel.add(createJLabel("help.settings.icon_font.family", iconFontFamily));
      panel.add(iconFontFamily);

      panel = createRow();
      box.add(panel);

      keystrokeFontFamily = new JComboBox<String>(keystrokeFontNames);
      keystrokeFontFamily.addItemListener(this);
      keystrokeFontFamily.setAlignmentX(0.0f);

      panel.add(createJLabel("help.settings.keystroke_font.family",
         keystrokeFontFamily));
      panel.add(keystrokeFontFamily);

      panel = createRow();
      box.add(panel);

      monoFontFamily = new JComboBox<String>(monoFontNames);
      monoFontFamily.addItemListener(this);
      monoFontFamily.setAlignmentX(0.0f);

      panel.add(createJLabel("help.settings.mono_font.family", monoFontFamily));
      panel.add(monoFontFamily);

      box.add(Box.createVerticalStrut(40));

      JPanel btnPanel = new JPanel(new BorderLayout());
      getContentPane().add(btnPanel, "South");

      JPanel closeCancelPanel = new JPanel();
      btnPanel.add(closeCancelPanel, "West");

      TJHAbstractAction closeAction = new TJHAbstractAction(helpLib, "action", "close")
       {
         @Override
         public void doAction()
         {
            setVisible(false);
         }
       };

      closeButton = new JButton(closeAction);
      closeCancelPanel.add(closeButton);
      closeButton.setVisible(false);

      TJHAbstractAction cancelAction = new TJHAbstractAction(helpLib, "action", "cancel")
       {
         @Override
         public void doAction()
         {
            setVisible(false);
         }
       };

      cancelButton = new JButton(cancelAction);
      closeCancelPanel.add(cancelButton);

      JPanel applyOkayPanel = new JPanel();
      btnPanel.add(applyOkayPanel, "East");

      TJHAbstractAction applyAction = new TJHAbstractAction(helpLib, "action", "apply")
       {
         @Override
         public void doAction()
         {
            apply();
            closeButton.setVisible(true);
            cancelButton.setVisible(false);
         }
       };

      applyOkayPanel.add(new JButton(applyAction));

      TJHAbstractAction okayAction = new TJHAbstractAction(helpLib, "action", "okay")
       {
         @Override
         public void doAction()
         {
            apply();
            setVisible(false);
         }
       };

      JButton okayButton = new JButton(okayAction);
      getRootPane().setDefaultButton(okayButton);

      applyOkayPanel.add(okayButton);

      pack();
   }

   protected JComponent createRow()
   {
      JComponent comp = new JPanel(new FlowLayout(FlowLayout.LEADING));
      comp.setAlignmentX(0.0f);
      return comp;
   }

   protected JLabel createJLabel(String label, JComponent comp)
   {
      JLabel jl = helpFrame.getHelpLib().createJLabel(label);
      jl.setAlignmentX(0.0f);

      if (comp != null)
      {
         jl.setLabelFor(comp);
      }

      return jl;
   }

   protected void apply()
   {
      updateSampleSettings();
      helpFrame.setHelpFont(sampleHelpFontSettings);
   }

   @Override
   public void itemStateChanged(ItemEvent e)
   {
      updateSample();

      if (closeButton.isVisible())
      {
         closeButton.setVisible(false);
         cancelButton.setVisible(true);
      }
   }

   protected void updateSampleSettings()
   {
      sampleHelpFontSettings.setBodyFontSize(
         fontSizeSpinnerModel.getNumber().intValue());

      sampleHelpFontSettings.setBodyFontCssName(
        HelpFontSettings.getFontCssName(
           (String)fontFamily.getSelectedItem()));

      sampleHelpFontSettings.setIconFontCssName(
        HelpFontSettings.getFontCssName(
           (String)iconFontFamily.getSelectedItem()));

      sampleHelpFontSettings.setKeyStrokeFontCssName(
        HelpFontSettings.getFontCssName(
           (String)keystrokeFontFamily.getSelectedItem()));

      sampleHelpFontSettings.setMonoFontCssName(
        HelpFontSettings.getFontCssName(
           (String)monoFontFamily.getSelectedItem()));
   }

   protected void updateSample()
   {
      updateSampleSettings();

      HTMLDocument doc = (HTMLDocument)sampleComp.getDocument();
      StyleSheet styles = doc.getStyleSheet();

      sampleHelpFontSettings.addFontRulesToStyleSheet(styles);
   }

   public void open()
   {
      closeButton.setVisible(false);
      cancelButton.setVisible(true);

      if (isVisible())
      {
         toFront();
      }
      else
      {
         setLocationRelativeTo(helpFrame);
         setVisible(true);
      }

      sampleHelpFontSettings.copyFrom(helpFrame.getHelpFontSettings());

      int size = sampleHelpFontSettings.getBodyFontSize();
      String bodyFont = sampleHelpFontSettings.getBodyFontName();
      String iconFont = sampleHelpFontSettings.getIconFontName();
      String keyStrokeFont = sampleHelpFontSettings.getKeyStrokeFontName();
      String monoFont = sampleHelpFontSettings.getMonoFontName();

      setFontSize(size);

      fontFamily.setSelectedItem(bodyFont);
      iconFontFamily.setSelectedItem(iconFont);
      keystrokeFontFamily.setSelectedItem(keyStrokeFont);
      monoFontFamily.setSelectedItem(monoFont);

      fontFamily.requestFocusInWindow();
   }

   public void setFontSize(int size)
   {
      if (size != fontSizeSpinnerModel.getNumber().intValue())
      {
         fontSizeSpinnerModel.setValue(Integer.valueOf(size));
      }
   }

   public Vector<String> getAvailableFontNames()
   {
      return fontNames;
   }

   public Vector<String> getAvailableIconFontNames()
   {
      return iconFontNames;
   }

   public Vector<String> getAvailableKeyStrokeFontNames()
   {
      return keystrokeFontNames;
   }

   public Vector<String> getAvailableMonoFontNames()
   {
      return monoFontNames;
   }

   protected HelpFrame helpFrame;
   protected JComboBox<String> fontFamily, iconFontFamily,
     keystrokeFontFamily, monoFontFamily;
   protected SpinnerNumberModel fontSizeSpinnerModel;
   protected JSpinner fontSizeSpinner;
   protected JEditorPane sampleComp;
   protected JButton closeButton, cancelButton;

   protected HelpFontSettings sampleHelpFontSettings;
   protected Vector<String> fontNames, iconFontNames, 
     keystrokeFontNames, monoFontNames;
}
