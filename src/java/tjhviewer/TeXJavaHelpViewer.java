/*          
    Copyright (C) 2026 Nicola L.C. Talbot
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
package com.dickimawbooks.tjhviewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.xml.sax.SAXException;

import com.dickimawbooks.texjavahelplib.*;

public class TeXJavaHelpViewer extends AbstractCLI
{
   public TeXJavaHelpViewer()
   {
   }

   @Override
   public boolean isGUIMode() { return true; }

   @Override
   public void printCLIAbout()
   {
      System.out.println(getHelpLib().getAboutInfo(false,
        TeXJavaHelpLib.VERSION,
        TeXJavaHelpLib.VERSION_DATE,
        String.format(
         "Copyright (C) %s Nicola L. C. Talbot (%s)",
          getCopyrightDate(),
          getHelpLib().getInfoUrl(false, "www.dickimaw-books.com")),
         TeXJavaHelpLib.LICENSE_GPL3,
         true, null
      ));
   }

   public String getCopyrightStartYear()
   {
      return "2026";
   }

   public String getCopyrightDate()
   {
      String startYr = getCopyrightStartYear();
      String endYr = TeXJavaHelpLib.VERSION_DATE.substring(0, 4);

      if (startYr.equals(endYr))
      {
         return endYr;
      }
      else
      {
         return String.format("%s-%s", startYr, endYr);
      }
   }

   @Override
   public String getCLIApplicationName()
   {
      return NAME;
   }

   @Override
   public String getCLIApplicationVersion()
   {
      return TeXJavaHelpLib.VERSION;
   }

   @Override
   public String getCLIApplicationVersionDate()
   {
      return TeXJavaHelpLib.VERSION_DATE;
   }

   @Override
   public void printCLISyntax()
   {
      versionInfo();

      System.out.println();
      System.out.println(getMessage("clisyntax.usage",
        getMessage("syntax.options", getCLIApplicationName())));

      System.out.println();

      printSyntaxItem(getMessage("syntax.in", "--in", "-i"));
      printSyntaxItem(getMessage("syntax.node", "--node", "-n"));
      printSyntaxItem(getMessage("syntax.anchor", "--anchor", "-a"));

      System.out.println();

      printCommonCLISyntax();

      System.out.println();

      System.out.println(getMessage("clisyntax.bugreport",
        "https://github.com/nlct/texjavahelp"));
   }

   @Override
   protected int getCLIArgCount(String arg)
   {
      if (
           arg.equals("--in") || arg.equals("-i")
        || arg.equals("--node") || arg.equals("-n")
        || arg.equals("--anchor") || arg.equals("-a")
         )
      {
         return 1;
      }

      return 0;
   }

   @Override
   protected void parseNoSwitchCLIArg(String arg) throws InvalidSyntaxException
   {
      if (inFileName != null)
      {
         throw new InvalidSyntaxException(
           getMessage("error.clisyntax.only_one_input"));
      }

      inFileName = arg;
   }

   @Override
   protected boolean parseCLIArg(String arg, CLIArgValue[] returnVals)
     throws InvalidSyntaxException
   {
      if (isArg(arg, "--in", "-i", returnVals))
      {
         if (inFileName != null)
         {
            throw new InvalidSyntaxException(
              getMessage("error.clisyntax.only_one_input"));
         }

         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing_value", arg));
         }

         inFileName = returnVals[0].toString();
      }
      else if (isArg(arg, "--node", "-n", returnVals))
      {
         if (nodeName != null)
         {
            throw new InvalidSyntaxException(
              getMessage("error.clisyntax.only_one", arg));
         }

         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing_value", arg));
         }

         nodeName = returnVals[0].toString();
      }
      else if (isArg(arg, "--anchor", "-a", returnVals))
      {
         if (targetRefName != null)
         {
            throw new InvalidSyntaxException(
              getMessage("error.clisyntax.only_one", arg));
         }

         if (returnVals[0] == null)
         {
            throw new InvalidSyntaxException(
               getMessage("error.clisyntax.missing_value", arg));
         }

         targetRefName = returnVals[0].toString();
      }
      else
      {
         return false;
      }

      return true;
   }

   @Override
   protected void postCLIProcess() throws InvalidSyntaxException
   {
      if (inFileName == null)
      {
         throw new InvalidSyntaxException(
            getMessage("error.clisyntax.missing_in"));
      }
   }

   @Override 
   protected void loadDictionaries(MessageSystem msgSys) throws IOException
   {  
      msgSys.loadDictionary(
       "/com/dickimawbooks/tjhviewer/dictionaries/",
       "tjhviewer");
   }  
      
   protected void run()
    throws IOException,SAXException
   {
      File file = new File(inFileName);

      if (!file.exists())
      {
         throw new FileNotFoundException(
           getMessage("error.file_not_found", file));
      }

      TeXJavaHelpLib helpLib = getHelpLib();

      helpLib.setHelpSetZipFile(file);

      helpLib.initHelpSet();

      HelpFrame helpFrame = helpLib.getHelpFrame();
      helpFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      ArrayList<Image> imageList = new ArrayList<Image>();

      for (int size : TeXJavaHelpLib.HELP_LIB_ICON_SIZES )
      {
         ImageIcon ic = helpLib.getHelpIcon("manual", "-" +size);

         if (ic != null)
         {
            imageList.add(ic.getImage());
         }
      }

      if (!imageList.isEmpty())
      {
         helpFrame.setIconImages(imageList);
      }

      helpFrame.setVisible(true);

      TargetRef targetRef = null;
      NavigationNode node = null;

      if (targetRefName != null)
      {
         targetRef = helpLib.getTargetRef(targetRefName);

         if (targetRef == null)
         {
            helpLib.error(getMessage("error.node_id_not_found", targetRefName));
         }
      }

      if (targetRef == null && nodeName != null)
      {
         node = helpLib.getNavigationNodeById(nodeName);

         if (node == null)
         {
            helpLib.error(getMessage("error.node_id_not_found", nodeName));
         }
      }

      try
      {
         if (targetRef != null)
         {
            helpFrame.setPage(targetRef);
         }
         else if (node != null)
         {
            helpFrame.setPage(node);
         }
      }
      catch (IOException e)
      {
         helpLib.error(e);
      }
   }

   public static void main(String[] args)
   {
      final TeXJavaHelpViewer viewer = new TeXJavaHelpViewer();

      try
      {  
         viewer.initialiseHelpAndParse(args);

         SwingUtilities.invokeAndWait(new Runnable()
         {
            public void run()
            {
               try
               {
                  viewer.run();
               }
               catch (Throwable e)
               {
                  viewer.error(e.getMessage(), null);

                  System.exit(viewer.getExitCode());
               }
            }
         });
      }     
      catch (InvalidSyntaxException e)
      {     
         viewer.error(e.getMessage(), null);

         System.exit(viewer.getExitCode());
      }
      catch (Throwable e)
      {
         viewer.error(null, e);

         System.exit(viewer.getExitCode());
      }
   }

   String inFileName = null;
   String nodeName, targetRefName;

   public static final String NAME = "tjhviewer";
}
