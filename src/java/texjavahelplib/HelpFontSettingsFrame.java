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

import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.Box;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 * Frame for showing font selector.
 */
public class HelpFontSettingsFrame extends JFrame
{
   public HelpFontSettingsFrame(final HelpFrame helpFrame)
   {
      super(helpFrame.getHelpLib().getMessage("help.settings.font.title"));

      this.helpFrame = helpFrame;

      init();
   }

   private void init()
   {
      TeXJavaHelpLib helpLib = helpFrame.getHelpLib();

      sampleComp = new JTextArea(helpLib.getMessage("help.settings.font_sample"), 3, 0);
      sampleComp.setLineWrap(true);
      sampleComp.setWrapStyleWord(true);
      sampleComp.setEditable(false);

      sampleComp.setMargin(new Insets(4, 4, 4, 4));

      getContentPane().add(sampleComp, "North");

      Box box = Box.createVerticalBox();
      box.setAlignmentX(0.0f);

      getContentPane().add(box, "Center");

      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      panel.setAlignmentX(0.0f);

      box.add(panel);

      JLabel familyLabel = helpLib.createJLabel("help.settings.font.family");
      familyLabel.setAlignmentX(0.0f);

      GraphicsEnvironment grp = GraphicsEnvironment.getLocalGraphicsEnvironment();
      fontFamily = new JComboBox<String>(grp.getAvailableFontFamilyNames());
      fontFamily.addItemListener(new ItemListener()
       {
          @Override
          public void itemStateChanged(ItemEvent e)
          {
             updateSample();
          }
       });
      fontFamily.setAlignmentX(0.0f);

      familyLabel.setLabelFor(fontFamily);

      panel.add(familyLabel);
      panel.add(fontFamily);

      panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      panel.setAlignmentX(0.0f);
      box.add(panel);

      JLabel sizeLabel = helpLib.createJLabel("help.settings.font.size");
      sizeLabel.setAlignmentX(0.0f);

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

      sizeLabel.setLabelFor(fontSizeSpinner);

      panel.add(sizeLabel);
      panel.add(fontSizeSpinner);

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
            helpFrame.setHelpFont((String)fontFamily.getSelectedItem(),
              fontSizeSpinnerModel.getNumber().intValue());

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
            helpFrame.setHelpFont((String)fontFamily.getSelectedItem(),
              fontSizeSpinnerModel.getNumber().intValue());
            setVisible(false);
         }
       };

      JButton okayButton = new JButton(okayAction);
      getRootPane().setDefaultButton(okayButton);

      applyOkayPanel.add(okayButton);

      pack();
   }

   protected void updateSample()
   {
      sampleComp.setFont(new Font((String)fontFamily.getSelectedItem(),
        Font.PLAIN, fontSizeSpinnerModel.getNumber().intValue()));
   }

   public void open(Font f)
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

      fontFamily.setSelectedItem(f.getFamily());

      setFontSize(f.getSize());
   }

   public void setFontSize(int size)
   {
      if (size != fontSizeSpinnerModel.getNumber().intValue())
      {
         fontSizeSpinnerModel.setValue(Integer.valueOf(size));
      }
   }

   protected HelpFrame helpFrame;
   protected JComboBox<String> fontFamily;
   protected SpinnerNumberModel fontSizeSpinnerModel;
   protected JSpinner fontSizeSpinner;
   protected JTextArea sampleComp;
   protected JButton closeButton, cancelButton;
}
