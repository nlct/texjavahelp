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

import java.awt.*;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;


/**
 * Frame for showing font selector.
 */
public class HelpFontSettingsFrame extends JFrame
 implements ItemListener,
   HelpFontChangeListener,
   HyperlinkListener,
   ActionListener
{
   public HelpFontSettingsFrame(final HelpFrame helpFrame)
   {
      super(helpFrame.getHelpLib().getMessage("help_font_settings.title"));

      this.helpFrame = helpFrame;
      sampleHelpFontSettings = new HelpFontSettings();

      init();
   }

   private void init()
   {
      TeXJavaHelpLib helpLib = helpFrame.getHelpLib();
      sampleHelpFontSettings.copyFrom(helpLib.getHelpFontSettings());

      String locPrefix = helpLib.getMessageIfExists("symbol.location_prefix");

      if (locPrefix == null)
      {
         locPrefix = helpLib.getMessage("symbol.help.navigation.history.pointer");
      }

      String iconSample = locPrefix+"\u279C";
      String keystrokeSample = "\u21E7\u21B9";

      StringBuilder builder = new StringBuilder();
      builder.append("<html><head><style>");
      builder.append(TeXJavaHelpLib.KEYSTROKE_CSS);
      builder.append(TeXJavaHelpLib.ICON_CSS);
      builder.append(TeXJavaHelpLib.MONO_CSS_CLASSES);
      builder.append(" { font-family: monospace; }");
      sampleHelpFontSettings.appendRules(builder);
      builder.append("</style></head><body>");

      builder.append(helpLib.getMessage("text.help_font_settings_sample"));
      builder.append("<pre class=\"code\">\\ | / -- * + % ~ # { } [ ] ^ _</pre>");
      builder.append("<p><span class=\"locationprefix\">");
      builder.append(locPrefix);
      builder.append("</span> ");
      builder.append(helpLib.getMessage("menu.help"));
      builder.append("<span title=\"");
      builder.append(helpLib.getMessage("manual.menu_separator_title"));
      builder.append("\" class=\"icon\">\u279C</span>");
      builder.append(helpLib.getMessage("menu.help.manual"));
      builder.append(" <span class=\"keystroke\">I</span>");
      builder.append(" <span class=\"keystroke\">L</span>");
      builder.append(" <span class=\"keystroke\">1</span>");
      builder.append(" <span class=\"keystroke\">\u21E7</span>");
      builder.append(" <span class=\"keystroke\">\u21B9</span>");
      builder.append(" <span class=\"keystroke\">\u2630</span>");

      builder.append("</body></html>");

      sampleComp = new JEditorPane("text/html", builder.toString());
      sampleComp.setEditable(false);
      sampleComp.addHyperlinkListener(this);

      sampleComp.setMargin(new Insets(4, 4, 4, 4));

      Box box = Box.createVerticalBox();
      box.setAlignmentX(0.0f);

      JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        new JScrollPane(sampleComp), new JScrollPane(box));

      splitPane.setOneTouchExpandable(true);

      getContentPane().add(splitPane, "Center");

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
      fontFamily.setSelectedItem(sampleHelpFontSettings.getBodyFontName());
      fontFamily.addItemListener(this);
      fontFamily.setAlignmentX(0.0f);

      panel.add(createJLabel("help_font_settings.family", fontFamily));
      panel.add(fontFamily);

      panel = createRow();
      box.add(panel);

      // Default font size

      fontSizeSpinnerModel = new SpinnerNumberModel(
        sampleHelpFontSettings.getBodyFontSize(), 2, 100, 1);
      fontSizeSpinner = new JSpinner(fontSizeSpinnerModel);
      fontSizeSpinner.addChangeListener(new ChangeListener()
       {
          @Override
          public void stateChanged(ChangeEvent e)
          {
             sampleHelpFontSettings.setBodyFontSize(
              fontSizeSpinnerModel.getNumber().intValue());

             fontChanged(new HelpFontChangeEvent(this, sampleHelpFontSettings,
               HelpFontChangeEvent.BODY_SIZE));
          }
       });
      fontSizeSpinner.setAlignmentX(0.0f);

      panel.add(createJLabel("help_font_settings.size", fontSizeSpinner));
      panel.add(fontSizeSpinner);

      // Icon font family

      panel = createRow();
      box.add(panel);

      iconFontFamily = new JComboBox<String>(iconFontNames);
      iconFontFamily.setSelectedItem(sampleHelpFontSettings.getIconFontName());
      iconFontFamily.addItemListener(this);
      iconFontFamily.setAlignmentX(0.0f);

      panel.add(createJLabel("help_font_settings.icon_font_family", iconFontFamily));
      panel.add(iconFontFamily);

      panel = createRow();
      box.add(panel);

      keystrokeFontFamily = new JComboBox<String>(keystrokeFontNames);
      keystrokeFontFamily.setSelectedItem(
         sampleHelpFontSettings.getKeyStrokeFontName());
      keystrokeFontFamily.addItemListener(this);
      keystrokeFontFamily.setAlignmentX(0.0f);

      panel.add(createJLabel("help_font_settings.keystroke_font_family",
         keystrokeFontFamily));
      panel.add(keystrokeFontFamily);

      panel = createRow();
      box.add(panel);

      monoFontFamily = new JComboBox<String>(monoFontNames);
      monoFontFamily.setSelectedItem(sampleHelpFontSettings.getMonoFontName());
      monoFontFamily.addItemListener(this);
      monoFontFamily.setAlignmentX(0.0f);

      panel.add(createJLabel("help_font_settings.mono_font_family", monoFontFamily));
      panel.add(monoFontFamily);

      // link decoration and colour

      panel = createRow();
      box.add(panel);

      panel.add(helpLib.createJLabel("help_font_settings.hyperlinks"));

      colorChooser = new JColorChooser();
      colorChooserTitle = helpLib.getMessage(
        "help_font_settings.colour_chooser.title");

      swatchComp = new JPanel();
      swatchComp.setPreferredSize(new Dimension(40,20));
      swatchComp.setBackground(sampleHelpFontSettings.getLinkColor());
      swatchComp.setOpaque(true);
      panel.add(swatchComp);

      panel.add(helpLib.createJButton(
        "help_font_settings", "choose_colour", this));

      underlineBox = helpLib.createJCheckBox(
        "help_font_settings", "underline", this);
      panel.add(underlineBox);

      JPanel btnPanel = new JPanel(new BorderLayout());
      getContentPane().add(btnPanel, "South");

      JPanel closeCancelPanel = new JPanel();
      btnPanel.add(closeCancelPanel, "West");

      closeButton = helpLib.createCloseButton(this);
      closeCancelPanel.add(closeButton);
      closeButton.setVisible(false);

      cancelButton = helpLib.createCancelButton(this);
      closeCancelPanel.add(cancelButton);

      closeCancelPanel.add(
        helpLib.createHelpDialogButton(this, "sec:helpfontdialog"));

      JPanel applyOkayPanel = new JPanel();
      btnPanel.add(applyOkayPanel, "East");

      applyOkayPanel.add(helpLib.createApplyButton(this));

      JButton okayButton = helpLib.createOkayButton(this);
      getRootPane().setDefaultButton(okayButton);

      applyOkayPanel.add(okayButton);

      pack();

      helpLib.addHelpFontChangeListener(this);
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
      helpFrame.getHelpLib().notifyFontChange(
       new HelpFontChangeEvent(this, sampleHelpFontSettings));
   }

   @Override
   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("underline"))
      {
         setLinkDecoration(underlineBox.isSelected());
      }
      else if (action.equals("choose_colour"))
      {
         chooseLinkColor();
      }
      else if (action.equals("apply"))
      {
         apply();
         closeButton.setVisible(true);
         cancelButton.setVisible(false);
      }
      else if (action.equals("okay"))
      {
         apply();
         setVisible(false);
      }
      else if (action.equals("cancel") || action.equals("close"))
      {
         setVisible(false);
      }
      else
      {
         System.err.println("Unknown help font setting action "+action);
      }
   }

   @Override
   public void itemStateChanged(ItemEvent e)
   {
      if (e.getSource() == fontFamily)
      {
         sampleHelpFontSettings.setBodyFontCssName(
           HelpFontSettings.getFontNameFromCss(
             (String)fontFamily.getSelectedItem()));

         fontChanged(new HelpFontChangeEvent(this, sampleHelpFontSettings,
           HelpFontChangeEvent.BODY_FONT));
      }
      else if (e.getSource() == iconFontFamily)
      {
         sampleHelpFontSettings.setIconFontCssName(
           HelpFontSettings.getFontNameFromCss(
             (String)iconFontFamily.getSelectedItem()));

         fontChanged(new HelpFontChangeEvent(this, sampleHelpFontSettings,
           HelpFontChangeEvent.ICON_FONT));
      }
      else if (e.getSource() == keystrokeFontFamily)
      {
         sampleHelpFontSettings.setKeyStrokeFontCssName(
           HelpFontSettings.getFontNameFromCss(
             (String)keystrokeFontFamily.getSelectedItem()));

         fontChanged(new HelpFontChangeEvent(this, sampleHelpFontSettings,
           HelpFontChangeEvent.KEYSTROKE_FONT));
      }
      else if (e.getSource() == monoFontFamily)
      {
         sampleHelpFontSettings.setMonoFontCssName(
           HelpFontSettings.getFontNameFromCss(
             (String)monoFontFamily.getSelectedItem()));

         fontChanged(new HelpFontChangeEvent(this, sampleHelpFontSettings,
           HelpFontChangeEvent.MONO_FONT));
      }

      if (closeButton.isVisible())
      {
         closeButton.setVisible(false);
         cancelButton.setVisible(true);
      }
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

      fontFamily.requestFocusInWindow();
   }

   @Override
   public void fontChanged(HelpFontChangeEvent evt)
   { 
      if (evt.getSource() != this)
      {
         sampleHelpFontSettings.copyFrom(evt);

         int modifiers = evt.getModifiers();

         if ( (modifiers & HelpFontChangeEvent.BODY_SIZE)
             == HelpFontChangeEvent.BODY_SIZE )
         {
            setFontSize(sampleHelpFontSettings.getBodyFontSize());
         }

         if ((modifiers & HelpFontChangeEvent.BODY_FONT)
             == HelpFontChangeEvent.BODY_FONT )
         {
            String bodyFont = sampleHelpFontSettings.getBodyFontName();

            if (!bodyFont.equals(fontFamily.getSelectedItem()))
            {
               fontFamily.setSelectedItem(bodyFont);
            }
         }

         if ((modifiers & HelpFontChangeEvent.ICON_FONT)
                == HelpFontChangeEvent.ICON_FONT
            )
         {
            String iconFont = sampleHelpFontSettings.getIconFontName();

            if (!iconFont.equals(iconFont))
            {
               iconFontFamily.setSelectedItem(iconFont);
            }
         }

         if ((modifiers & HelpFontChangeEvent.KEYSTROKE_FONT)
                == HelpFontChangeEvent.KEYSTROKE_FONT
            )
         {
            String keyStrokeFont = sampleHelpFontSettings.getKeyStrokeFontName();

            if (!keyStrokeFont.equals(keystrokeFontFamily.getSelectedItem()))
            {
               keystrokeFontFamily.setSelectedItem(keyStrokeFont);
            }
         }

         if ((modifiers & HelpFontChangeEvent.MONO_FONT)
             == HelpFontChangeEvent.MONO_FONT
            )
         {
            String monoFont = sampleHelpFontSettings.getMonoFontName();

            if (!monoFont.equals(monoFontFamily.getSelectedItem()))
            {
               monoFontFamily.setSelectedItem(monoFont);
            }
         }

         if ( (modifiers & HelpFontChangeEvent.LINK_COLOR)
             == HelpFontChangeEvent.LINK_COLOR )
         {
            setLinkColor(sampleHelpFontSettings.getLinkColor());
         }

         if ( (modifiers & HelpFontChangeEvent.LINK_DECORATION)
             == HelpFontChangeEvent.LINK_DECORATION )
         {
            setLinkDecoration(sampleHelpFontSettings.getLinkDecoration());
         }

      }

      HTMLDocument doc = (HTMLDocument)sampleComp.getDocument();
      StyleSheet styles = doc.getStyleSheet();

      sampleHelpFontSettings.addFontRulesToStyleSheet(
        styles, evt.getModifiers());
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

   protected void chooseLinkColor()
   {
      Color color = colorChooser.showDialog(this, colorChooserTitle,
         sampleHelpFontSettings.getLinkColor());

      if (color != null)
      {
         sampleHelpFontSettings.setLinkColor(color);
         swatchComp.setBackground(color);

         fontChanged(new HelpFontChangeEvent(this, sampleHelpFontSettings,
          HelpFontChangeEvent.LINK_COLOR));
      }
   }

   protected void setLinkColor(Color color)
   {
      swatchComp.setBackground(color);
   }

   protected void setLinkDecoration(boolean underlined)
   {
      underlineBox.setSelected(underlined);
      sampleHelpFontSettings.setLinkDecoration(
         underlined ? "underlined" : "none");

      fontChanged(new HelpFontChangeEvent(this, sampleHelpFontSettings,
          HelpFontChangeEvent.LINK_DECORATION));
   }

   protected void setLinkDecoration(String decoration)
   {
      underlineBox.setSelected(decoration.equals("underline"));
   }

   @Override 
   public void hyperlinkUpdate(HyperlinkEvent evt)
   {
      if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
      {
         String ref = evt.getDescription();

         if (ref != null)
         {
            if (ref.startsWith("#"))
            {
               ref = ref.substring(1);
            }

            if (!ref.isEmpty())
            {
               sampleComp.scrollToReference(ref);
            }
         }
      }
   }

   protected HelpFrame helpFrame;
   protected JComboBox<String> fontFamily, iconFontFamily,
     keystrokeFontFamily, monoFontFamily;
   protected SpinnerNumberModel fontSizeSpinnerModel;
   protected JSpinner fontSizeSpinner;
   protected JEditorPane sampleComp;
   protected JColorChooser colorChooser;
   protected String colorChooserTitle;
   protected JComponent swatchComp;
   protected JCheckBox underlineBox;
   protected JButton closeButton, cancelButton;

   protected HelpFontSettings sampleHelpFontSettings;
   protected Vector<String> fontNames, iconFontNames, 
     keystrokeFontNames, monoFontNames;
}
