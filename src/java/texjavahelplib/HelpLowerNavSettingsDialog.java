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

import java.awt.*;

import javax.swing.*;

/**
 * Dialog for showing lower navigation panel settings.
 */
public class HelpLowerNavSettingsDialog extends JDialog
{
   public HelpLowerNavSettingsDialog(final HelpFrame helpFrame)
   {
      super(helpFrame,
        helpFrame.getHelpLib().getMessage("help.settings.nav.title"));

      this.helpFrame = helpFrame;

      init();
   }

   private void init()
   {
      TeXJavaHelpLib helpLib = helpFrame.getHelpLib();

      Box box = Box.createVerticalBox();
      getContentPane().add(box, "Center");

      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      panel.setAlignmentX(0.0f);
      box.add(panel);

      JLabel limitLabel = helpLib.createJLabel("help.settings.nav.label_limit");
      limitLabel.setAlignmentX(0.0f);

      labelLimitSpinnerModel = new SpinnerNumberModel(
        helpFrame.getLowerNavLabelLimit(), 4, 100, 1);
      labelLimitSpinner = new JSpinner(labelLimitSpinnerModel);
      labelLimitSpinner.setAlignmentX(0.0f);

      limitLabel.setLabelFor(labelLimitSpinner);

      panel.add(limitLabel);
      panel.add(labelLimitSpinner);

      showLabelCheckBox = helpLib.createJCheckBox("help.settings.nav",
       "show_label", helpFrame.isLowerNavLabelTextOn());

      box.add(showLabelCheckBox);

      JPanel buttonPanel = new JPanel();
      getContentPane().add(buttonPanel, "South");

      TJHAbstractAction cancelAction
         = new TJHAbstractAction(helpLib, "action", "cancel")
       {
         @Override
         public void doAction()
         {
            setVisible(false);
         }
       };

      buttonPanel.add(new JButton(cancelAction));

      TJHAbstractAction okayAction = new TJHAbstractAction(helpLib, "action", "okay")
       {
         @Override
         public void doAction()
         {
            helpFrame.setLowerNavSettings(showLabelCheckBox.isSelected(),
              labelLimitSpinnerModel.getNumber().intValue());
            setVisible(false);
         }
       };

      JButton okayButton = new JButton(okayAction);
      getRootPane().setDefaultButton(okayButton);

      buttonPanel.add(okayButton);

      pack();
   }

   public void open()
   {
      setLocationRelativeTo(helpFrame);
      setVisible(true);

      showLabelCheckBox.setSelected(helpFrame.isLowerNavLabelTextOn());
      setLabelLimit(helpFrame.getLowerNavLabelLimit());
   }

   public void setLabelLimit(int limit)
   {
      if (limit != labelLimitSpinnerModel.getNumber().intValue())
      {
         labelLimitSpinnerModel.setValue(Integer.valueOf(limit));
      }
   }

   protected HelpFrame helpFrame;
   protected SpinnerNumberModel labelLimitSpinnerModel;
   protected JSpinner labelLimitSpinner;
   protected JCheckBox showLabelCheckBox;
}
