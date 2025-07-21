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
 * Dialog for adjusting lower navigation panel settings.
 */
public class HelpLowerNavSettingsDialog extends JDialog
implements OkayAction
{
   public HelpLowerNavSettingsDialog(Window owner, final HelpPageNavPanel navPanel)
   {
      super(owner,
        navPanel.getHelpLib().getMessage("help_settings_nav.title"), 
        Dialog.ModalityType.APPLICATION_MODAL);

      this.navPanel = navPanel;

      init();
   }

   private void init()
   {
      TeXJavaHelpLib helpLib = navPanel.getHelpLib();

      Box box = Box.createVerticalBox();
      getContentPane().add(box, "Center");

      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      panel.setAlignmentX(0.0f);
      box.add(panel);

      JLabel limitLabel = helpLib.createJLabel("help_settings_nav.label_limit");
      limitLabel.setAlignmentX(0.0f);

      labelLimitSpinnerModel = new SpinnerNumberModel(
        navPanel.getLowerNavLabelLimit(), 4, 100, 1);
      labelLimitSpinner = new JSpinner(labelLimitSpinnerModel);
      labelLimitSpinner.setAlignmentX(0.0f);

      limitLabel.setLabelFor(labelLimitSpinner);

      panel.add(limitLabel);
      panel.add(labelLimitSpinner);

      showLabelCheckBox = helpLib.createJCheckBox("help_settings_nav",
       "show_label", navPanel.isLowerNavLabelTextOn());

      box.add(showLabelCheckBox);

      JPanel buttonPanel = new JPanel();
      getContentPane().add(buttonPanel, "South");

      buttonPanel.add(helpLib.createCancelButton(this));
      buttonPanel.add(helpLib.createOkayButton((OkayAction)this, getRootPane()));

      pack();
   }

   @Override
   public void okay()
   {
      TeXJavaHelpLib helpLib = navPanel.getHelpLib();

      helpLib.fireLowerNavSettingUpdate(
       new LowerNavSettingsChangeEvent(
         this,
         labelLimitSpinnerModel.getNumber().intValue(),
         showLabelCheckBox.isSelected()
        )
      );

      setVisible(false);
   }

   public void open(Component comp)
   {
      setLocationRelativeTo(comp);
      open();
   }

   public void open()
   {
      setVisible(true);

      showLabelCheckBox.setSelected(navPanel.isLowerNavLabelTextOn());
      setLabelLimit(navPanel.getLowerNavLabelLimit());
   }

   public void setLabelLimit(int limit)
   {
      if (limit != labelLimitSpinnerModel.getNumber().intValue())
      {
         labelLimitSpinnerModel.setValue(Integer.valueOf(limit));
      }
   }

   protected HelpPageNavPanel navPanel;
   protected SpinnerNumberModel labelLimitSpinnerModel;
   protected JSpinner labelLimitSpinner;
   protected JCheckBox showLabelCheckBox;
}
