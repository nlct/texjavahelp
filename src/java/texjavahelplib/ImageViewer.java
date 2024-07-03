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

import java.util.ArrayDeque;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.*;
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
 implements MouseListener,MouseMotionListener
{
   public ImageViewer(TeXJavaHelpLib helpLib, String title)
   {
      super(title);
      this.helpLib = helpLib;
      defaultTitle = title;

      JComponent mainPanel = new JPanel(new BorderLayout());
      getContentPane().add(mainPanel, BorderLayout.CENTER);

      transform = new AffineTransform();
      zoomDeque = new ArrayDeque<Number>();

      imageComp = new JPanel(null)
       {
          @Override
          protected void paintComponent(Graphics g)
          {
             super.paintComponent(g);
             paintCurrentImage((Graphics2D)g);
          }
       };

      imageComp.setBackground(Color.WHITE);
      imageComp.setOpaque(true);

      imageComp.addMouseListener(this);
      imageComp.addMouseMotionListener(this);

      imageSp = new JScrollPane(imageComp);

      mainPanel.add(imageSp, BorderLayout.CENTER);

      JToolBar toolBar = new JToolBar();
      getContentPane().add(toolBar, BorderLayout.NORTH);

      popupMenu = new JPopupMenu();

      TJHAbstractAction fitToWidth = new TJHAbstractAction(helpLib,
        "menu.imageviewer", "fit_to_width", getRootPane())
       {
          @Override
          public void doAction()
          {
             fitToWidth();
          }
       };

      popupMenu.add(fitToWidth);
      toolBar.add(fitToWidth);

      TJHAbstractAction fitToHeight = new TJHAbstractAction(helpLib,
        "menu.imageviewer", "fit_to_height", getRootPane())
       {
          @Override
          public void doAction()
          {
             fitToHeight();
          }
       };

      popupMenu.add(fitToHeight);
      toolBar.add(fitToHeight);

      TJHAbstractAction fitToPage = new TJHAbstractAction(helpLib,
        "menu.imageviewer", "fit_to_page", getRootPane())
       {
          @Override
          public void doAction()
          {
             fitToPage();
          }
       };

      popupMenu.add(fitToPage);
      toolBar.add(fitToPage);

      TJHAbstractAction incAction = new TJHAbstractAction(helpLib,
        "menu.imageviewer", "increase", getRootPane())
       {
          @Override
          public void doAction()
          {
             Number num = zoomNumberModel.getNumber();
             zoomNumberModel.setValue(Integer.valueOf(num.intValue()+5));
          }
       };

      popupMenu.add(incAction);
      toolBar.add(incAction);

      TJHAbstractAction decAction = new TJHAbstractAction(helpLib,
        "menu.imageviewer", "decrease", getRootPane())
       {
          @Override
          public void doAction()
          {
             Number num = zoomNumberModel.getNumber();
             zoomNumberModel.setValue(Integer.valueOf(
               Math.max(5, num.intValue()-5)));
          }
       };

      popupMenu.add(decAction);
      toolBar.add(decAction);

      TJHAbstractAction zoom1 = new TJHAbstractAction(helpLib,
        "menu.imageviewer", "zoom_1", getRootPane())
       {
          @Override
          public void doAction()
          {
             zoomNumberModel.setValue(Integer.valueOf(100));
          }
       };

      popupMenu.add(zoom1);
      toolBar.add(zoom1);

      popupMenu.add(new TJHAbstractAction(helpLib,
        "menu.imageviewer", "zoom_2", getRootPane())
       {
          @Override
          public void doAction()
          {
             zoomNumberModel.setValue(Integer.valueOf(200));
          }
       });

      popupMenu.add(new TJHAbstractAction(helpLib,
        "menu.imageviewer", "zoom_5", getRootPane())
       {
          @Override
          public void doAction()
          {
             zoomNumberModel.setValue(Integer.valueOf(500));
          }
       });

      zoomNumberModel = new SpinnerNumberModel(100, 1, 1000000, 25);
      zoomSpinner = new JSpinner(zoomNumberModel);
      zoomSpinner.addChangeListener(new ChangeListener()
       {
          @Override
          public void stateChanged(ChangeEvent e)
          {
             if (!updatingZoom)
             {
                if (previousZoom != null)
                {
                   zoomDeque.add(previousZoom);
                }

                previousZoom = zoomNumberModel.getNumber();

                updateZoom(previousZoom);
             }
          }
       });

      JComponent bottomPanel = new JPanel(new BorderLayout());
      mainPanel.add(bottomPanel, BorderLayout.SOUTH);
      posLabel = new JLabel("000,000");
      bottomPanel.add(posLabel, BorderLayout.WEST);
      posLabel.setVisible(helpLib.getApplication().isDebuggingOn());

      JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      bottomPanel.add(zoomPanel, BorderLayout.EAST);

      zoomPanel.add(helpLib.createJLabel("imageviewer.magnify", zoomSpinner));
      zoomPanel.add(zoomSpinner);

      messagePane = new JEditorPane("text/html", "");
      messagePane.setEditable(false);

      mainPanel.add(new JScrollPane(messagePane), BorderLayout.NORTH);

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

      if (band != null)
      {
         Paint oldPaint = g.getPaint();
         Stroke oldStroke = g.getStroke();
         g.setStroke(BAND_STROKE);
         g.setPaint(BAND_COL);
         g.setXORMode(imageComp.getBackground());
         g.draw(band);
         g.setPaintMode();
         g.setStroke(oldStroke);
         g.setPaint(oldPaint);
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
      imageComp.requestFocusInWindow();
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

         zoomDeque.clear();
         previousZoom = Integer.valueOf(100);
         zoomNumberModel.setValue(previousZoom);
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

   protected void updateZoom(Number num)
   {
      Point p = imageSp.getViewport().getViewPosition();
      double x = p.getX()/transform.getScaleX();
      double y = p.getY()/transform.getScaleY();

      double scale = 0.01*num.doubleValue();
      transform.setToScale(scale, scale);

      updateImageCompSize(x, y);
      repaint();
   }

   protected void updateImageCompSize()
   {
      updateImageCompSize(0, 0);
   }

   protected void updateImageCompSize(double x, double y)
   {
      if (image != null)
      {
         int height = image.getHeight();
         int width = image.getWidth();

         double sx = transform.getScaleX();
         double sy = transform.getScaleY();

         Dimension dim = new Dimension(
           (int)Math.ceil(width*sx),
           (int)Math.ceil(height*sy));

         imageComp.setPreferredSize(dim);

         Point p = new Point(
             (int)Math.ceil(x*sx),
             (int)Math.ceil(y*sy));

         imageSp.revalidate();
         revalidate();
         imageSp.getViewport().setViewPosition(p);
      }
   }

   @Override
   public void mouseClicked(MouseEvent evt)
   {
   }

   @Override
   public void mouseEntered(MouseEvent evt)
   {
   }

   @Override
   public void mouseExited(MouseEvent evt)
   {
   }

   @Override
   public void mousePressed(MouseEvent evt)
   {
      if (!checkForPopup(evt))
      {
         int mods = evt.getModifiersEx();

         if ((mods & (MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK))
                == MouseEvent.BUTTON1_DOWN_MASK)
         {
            anchor = evt.getPoint();

            if ((mods & MouseEvent.SHIFT_DOWN_MASK)
                   == MouseEvent.SHIFT_DOWN_MASK)
            {
               setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
               band = new Rectangle(anchor);
            }
            else
            {
               setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
         }
      }
   }

   @Override
   public void mouseReleased(MouseEvent evt)
   {
      if (!checkForPopup(evt))
      {
         if (evt.getClickCount() == 2)
         {
            if (!zoomDeque.isEmpty())
            {
               Number num = zoomDeque.pollLast();
               previousZoom = null;
               zoomNumberModel.setValue(num);
            }
         }
         else if (band != null && band.width > 1 && band.height > 1
           && (evt.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)
                == MouseEvent.SHIFT_DOWN_MASK)
         {
            JViewport viewport = imageSp.getViewport();
            Rectangle viewRect = viewport.getViewRect();

            double x = band.getX()/transform.getScaleX();
            double y = band.getY()/transform.getScaleY();
            double w = band.getWidth()/transform.getScaleX();
            double h = band.getHeight()/transform.getScaleY();
            double scale;

            if (w > h)
            {
               scale = viewRect.getWidth()/w;
            }
            else
            {
               scale = viewRect.getHeight()/h;
            }

            updatingZoom = true;
            zoomNumberModel.setValue(Integer.valueOf((int)Math.round(100*scale)));
            updatingZoom = false;
            transform.setToScale(scale, scale);

            if (previousZoom != null)
            {
               zoomDeque.add(previousZoom);
            }

            previousZoom = zoomNumberModel.getNumber();

            viewRect.x = (int)Math.round(x*scale);
            viewRect.y = (int)Math.round(y*scale);

            imageComp.setPreferredSize(new Dimension(
                 (int)Math.ceil(image.getWidth()*scale),
                 (int)Math.ceil(image.getHeight()*scale)
                ));

            imageComp.revalidate();
            imageSp.revalidate();

            imageSp.getViewport().scrollRectToVisible(viewRect);
            imageComp.repaint();
            band = null;
         }

         clearBand();
      }
   }

   protected void clearBand()
   {
      if (band != null)
      {
         repaintBand();
         band = null;
      }

      if (anchor != null)
      {
         setCursor(Cursor.getDefaultCursor());
         anchor = null;
      }
   }

   protected void repaintBand()
   {
      if (band != null)
      {
         imageComp.repaint(0L, band.x, band.y, band.width+1, band.height+1);
      }
   }

   protected void updateBand(int newX, int newY)
   {
      if (newX < band.x + band.width || newY < band.y + band.height)
      {
         // shrinking or going backwards or up from anchor
         repaintBand();
         band.x = Math.min(anchor.x, newX);
         band.y = Math.min(anchor.y, newY);
         band.width = Math.abs(newX-anchor.x);
         band.height = Math.abs(newY-anchor.y);
      }
      else
      {
         // expanding
         band.add(newX, newY);
         repaintBand();
      }
   }

   protected boolean checkForPopup(MouseEvent evt)
   {
      if (evt.isPopupTrigger())
      {
         popupMenu.show((Component)evt.getSource(), evt.getX(), evt.getY());
         return true;
      }
      else
      {
         return false;
      }
   }

   @Override
   public void mouseDragged(MouseEvent evt)
   {
      posLabel.setText(String.format((java.util.Locale)null,
        "%03d,%03d",
         (int)Math.round(evt.getX()/transform.getScaleX()),
         (int)Math.round(evt.getY()/transform.getScaleY())));

      if (band != null)
      {
         updateBand(evt.getX(), evt.getY());
      }
      else if (anchor != null)
      {
         int dx = evt.getX() - anchor.x;
         int dy = evt.getY() - anchor.y;

         JViewport viewport = imageSp.getViewport();

         int maxX = imageComp.getWidth() - viewport.getWidth();
         int maxY = imageComp.getHeight() - viewport.getHeight();

         Point viewP = viewport.getViewPosition();

         if (imageComp.getWidth() > viewport.getWidth())
         {
            viewP.x -= dx;

            if (viewP.x < 0)
            {
               viewP.x = 0;
               anchor.x = evt.getX();
            }

            if (viewP.x > maxX)
            {
               viewP.x = maxX;
               anchor.x = evt.getX();
            }
         }

         if (imageComp.getHeight() > viewport.getHeight())
         {
            viewP.y -= dy;

            if (viewP.y < 0)
            {
               viewP.y = 0;
               anchor.y = evt.getY();
            }

            if (viewP.y > maxY)
            {
               viewP.y = maxY;
               anchor.y = evt.getY();
            }
         }

         viewport.setViewPosition(viewP);
      }
   }

   @Override
   public void mouseMoved(MouseEvent evt)
   {
      posLabel.setText(String.format((java.util.Locale)null,
        "%03d,%03d",
         (int)Math.round(evt.getX()/transform.getScaleX()),
         (int)Math.round(evt.getY()/transform.getScaleY())));
   }

   protected void fitToPage()
   {
      int width = image.getWidth();
      int height = image.getHeight();

      JViewport viewport = imageSp.getViewport();
      Rectangle rect = viewport.getViewRect();
      double scale;

      if (width > height)
      {
         scale = rect.getWidth()/width;

         if (scale * height > rect.getHeight())
         {
            scale = rect.getHeight()/height;
         }
      }
      else
      {
         scale = rect.getHeight()/height;

         if (scale * width > rect.getWidth())
         {
            scale = rect.getWidth()/width;
         }
      }

      zoomNumberModel.setValue(Integer.valueOf((int)Math.ceil(100*scale)));
   }

   protected void fitToWidth()
   {
      int width = image.getWidth();

      JViewport viewport = imageSp.getViewport();
      Rectangle rect = viewport.getViewRect();
      double scale = rect.getWidth()/width;

      zoomNumberModel.setValue(Integer.valueOf((int)Math.ceil(100*scale)));
   }

   protected void fitToHeight()
   {
      int height = image.getHeight();

      JViewport viewport = imageSp.getViewport();
      Rectangle rect = viewport.getViewRect();

      double scale = rect.getHeight()/height;

      zoomNumberModel.setValue(Integer.valueOf((int)Math.ceil(100*scale)));
   }

   private Point anchor = null;
   private Rectangle band;
   private static final BasicStroke BAND_STROKE
    = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
    10f, new float[] {5f, 5f}, 0f);
   private static final Color BAND_COL = Color.RED;

   protected JComponent imageComp;
   protected JScrollPane imageSp;
   protected JEditorPane messagePane;

   protected SpinnerNumberModel zoomNumberModel;
   protected JSpinner zoomSpinner;
   protected JLabel posLabel;

   protected JPopupMenu popupMenu;

   protected BufferedImage image;
   protected AffineTransform transform;

   protected ArrayDeque<Number> zoomDeque;
   protected Number previousZoom;
   protected boolean updatingZoom = false;

   protected TeXJavaHelpLib helpLib;
   protected String defaultTitle;
}
