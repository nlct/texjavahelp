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

import java.io.InputStream;
import java.io.IOException;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

public class ImageViewer extends JFrame
{
   public ImageViewer(TeXJavaHelpLib helpLib, String title)
   {
      super(title);
      this.helpLib = helpLib;
      defaultTitle = title;

      transform = new AffineTransform();

      zoomNumberModel = new SpinnerNumberModel(100, 0, 1000, 25);
      zoomSpinner = new JSpinner(zoomNumberModel);
      zoomSpinner.addChangeListener(new ChangeListener()
       {
          @Override
          public void stateChanged(ChangeEvent e)
          {
             double scale = 0.01*zoomNumberModel.getNumber().doubleValue();
             transform.setToScale(scale, scale);
             updateImageCompSize();
             repaint();
          }
       });

      JComponent toolBar = new JPanel();
      getContentPane().add(toolBar, BorderLayout.NORTH);

      toolBar.add(helpLib.createJLabel("imageviewer.magnify", zoomSpinner));
      toolBar.add(zoomSpinner);

      imageComp = new JPanel(null)
       {
          @Override
          protected void paintComponent(Graphics g)
          {
             super.paintComponent(g);
             paintCurrentImage((Graphics2D)g);
          }
       };

      getContentPane().add(new JScrollPane(imageComp), BorderLayout.CENTER);

      messagePane = new JEditorPane("text/html", "");
      messagePane.setEditable(false);

      getContentPane().add(new JScrollPane(messagePane), BorderLayout.SOUTH);

      Toolkit tk = Toolkit.getDefaultToolkit();
      Dimension dim = tk.getScreenSize();
      setSize(dim.width/2, dim.height/2);
      setLocationRelativeTo(null);
   }

   protected void paintCurrentImage(Graphics2D g)
   {
      if (image != null)
      {
         g.drawImage(image, transform, imageComp);
      }
   }

   public void display(AttributeSet as)
   {
      String src = (String)as.getAttribute(HTML.Attribute.SRC);
      String alt = (String)as.getAttribute(HTML.Attribute.ALT);
      String title = (String)as.getAttribute(HTML.Attribute.TITLE);

      if (alt == null)
      {
         messagePane.setText("");
      }
      else
      {
         StringBuilder builder = new StringBuilder();

         HelpFontSettings fontSettings = helpLib.getHelpFontSettings();

         builder.append("<html><head><style>");
         fontSettings.appendRules(builder);
         builder.append("</style></head><body>");
         builder.append(alt);
         builder.append("</body></html>");

         messagePane.setText(builder.toString());
      }

      setTitle(String.format("%s - %s", defaultTitle, 
        title == null ? src : title));

      try
      {
         loadImage(src);
      }
      catch (IOException e)
      {
         helpLib.error(helpLib.getMessage("error.image_load_failed", src),
          e);
      }

      setVisible(true);
   }

   protected void loadImage(String src) throws IOException
   {
      InputStream in = null;
      image = null;

      try
      {
         in = getClass().getResourceAsStream(
            helpLib.getHelpSetResourcePath()+"/"+src);

         image = ImageIO.read(in);
         updateImageCompSize();
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }
   }

   protected void updateImageCompSize()
   {
      if (image != null)
      {
         int height = image.getHeight();
         int width = image.getWidth();

         Dimension dim = new Dimension(
           (int)Math.ceil(width*transform.getScaleX()),
           (int)Math.ceil(height*transform.getScaleY()));

         imageComp.setPreferredSize(dim);
         imageComp.revalidate();
      }
   }

   protected JComponent imageComp;
   protected JEditorPane messagePane;
   protected BufferedImage image;
   protected AffineTransform transform;
   protected SpinnerNumberModel zoomNumberModel;
   protected JSpinner zoomSpinner;

   protected TeXJavaHelpLib helpLib;
   protected String defaultTitle;
}
