# texjavahelp
Help library for Java GUI applications with LaTeX source

This bundle is provided because the TeX Java Help (TJH) library
(`texjavahelplib.jar`) is used by a number of my Java applications.
The accompanying TJH command line applications and the
`texjavahelp.sty` package are used to create the documentation and
in-application help.

 - `texjavahelplib.jar` : Java library to manage localisation and
   helpset files.
 - `tjhxml2bib` : converts localisation `xml` files to Bib2Gls `bib`
   files.
 - `texjavahelp.sty` : LaTeX package to integrate with `tjhxml2bib`
   and `texjavahelpmk`.
 - `texjavahelpmk` : creates HTML and XML helpset files from the
   LaTeX `.tex` source and the `.toc`, `.aux`, and `.glstex`
   files created during the document build.
 - `tjhziphelpset` : bundles the HTML and XML helpset files into a
   `tjh` file (which is a special type of zip file).
 - `tjhcreateiconpdf` creates a PDF file and def file containing all icon images
   for a particular size to allow LaTeX to access icon images that
   are bundled in a jar file.
 - `tjhviewer` : a `tjh` file viewer. (This allows the help to be
    viewed on its own without the burden of loading the application
    the helpset was created for.)
 - `tjhflattendocsrc` : may optionally be used to create a
   flattened document source for easier distribution. (That is, 
   a copy will be made with any instance of `\input` replaced with
   the referenced file's content.) Referenced bib and image files
   will be copied over to the destination directory as well.

The aim is to provide an alternative to JavaHelp where the documentation 
for the both the PDF and GUI in-application help (for a particular
language) can be provided by a single LaTeX source.

If there are translations available, each translation can be
provided but the chapter/section units and labels must be the same
for each file. The label can be used by the application to provide
context-sensitive help to open the applicable chapter or section for
the chosen language.

Note that the LaTeX code needs to be simple enough for the 
[TeX Java Parser Library (TJP)](https://github.com/nlct/texparser) to parse
(via `texjavahelpmk`).  This means that the same source can be used
to create both the PDF documentation and the in-application help.
Additionally, `texjavahelpmk` creates the navigation tree, search
database and index XML files while it creates the HTML files, so
it's not just a LaTeX to HTML generator.

The `bib2gls` application is used to generate the index information
(as well as any glossary terms). The application's language `.xml`
files can be converted to a `bib2gls` file with `tjhxml2bib`.
This requires a specific format for the entry labels in order to
convert the flat properties file to hierarchical entries.

The `texjavahelp.sty` package has a TJP implementation within
`texjavahelpmk` designed to assist with the generation of the
helpset files and integration with `bib2gls` (“standalone” glossary
entries, glossary lists, and index).

The PDF must be compiled before `texjavahelpmk` is run as the
`.aux`, `.toc`, `.glstex` (and, where applicable, `.lot` or `.lof`)
files created during the document build are needed by
`texjavahelpmk`.

The accompanying `tjhziphelpset` command line application is for use
after `texjavahelpmk` to bundle the helpset files into 
a single `tjh` file for compact distribution and to comply with 
TeX Live requirements (no generic filenames, no non-unique filenames).

The library also provides methods to implement localisation support
for both GUI and CLI applications, and a class that implements
`com.dickimawbooks.texparserlib.TeXApp` 
for applications that use the TeX Java Parser library.

Localisation support is contained within the applicable `.jar` file.
The `texjavahelplib.jar` file contains common icons for use with GUI
applications.

## Installing

See [`INSTALL.md`](INSTALL.md)

## Example

(For a complete but small example application, see 
[`jmakepdfx`](https://github.com/nlct/jmakepdfx).)

Suppose the class `com.example.demo.Demo` has class variables:

```java
TeXJavaHelpLibAppAdapter helpLibApp;
TeXJavaHelpLib helpLib;
```

Then these variables can be initialised as follows:

```java
helpLibApp = new TeXJavaHelpLibAppAdapter()
 {
    @Override
    public String getApplicationName()
    {
       return "Demo GUI Application";
    }

    @Override
    public boolean isGUI() { return true; }
 };

helpLib = new TeXJavaHelpLib(helpLibApp,
   Locale.getDefault(), // Locale for messages, menus, buttons etc
   Locale.getDefault() // Locale for helpset
);

helpLibApp.setHelpLib(helpLib);
```

If the application's dictionary files are named `demo-`_langtag_`.xml`
(such as `demo-en.xml`) and are bundled in the application's jar file in
a sub-directory called `dictionaries` then the applicable file can be loaded with:

```java
helpLib.getMessageSystem().loadDictionary(
  "/com/example/demo/dictionaries/", "demo");
```

**Note that the `dictionaries` sub-directory itself must be included
in the jar file.**

The default name for the helpset `tjh` file is `helpset.tjh`, but
bear in mind that TeX Live has strict requirements that don't allow generic 
names, so typically the default `helpset.tjh` will need to be
changed. For example, an application called `example` might
have `example-helpset.tjh`:

```java
helpLib.setHelpSetZipName("example-helpset.tjh");
```
This file is expected to be on the resource path.
That is, put it in the same directory as your application's jar file.

The helpset can then be initialised.

```java
helpLib.initHelpSet();
```

If there is no localisation support for a particular locale, the
fallback locale is used. The fallback locale is given by
`TeXJavaHelpLib.getFallbackLocale()` which defaults to English.
(The fallback locale files must be provided with the application.)

The main help window (created by `initHelpSet()`) can be obtained with:

```java
HelpFrame helpFrame = helpLib.getHelpFrame();
```

The `HelpFrame` is a sub-class of `JFrame`. It contains a
`JSplitPane` with a navigation tree on the left and the selected
page on the right.

If a `JDialog` requires a help button to open a particular page,
then a `HelpDialog` is required. The main `HelpFrame` can't be used
with a blocking `JDialog`.

Suppose the documentation source has:

```latex
\chapter{Help Windows}
\label{sec:helpwindows}
```

Then a `JButton` can be created with an associated `HelpDialog`
where the button action opens the `HelpDialog` at the appropriate
page.

```java
JDialog parentDialog = ...;

JButton helpButton = helpLib.createHelpDialogButton(parentDialog, "sec:helpwindows");
```

In this case, the navigation tree will just contain the application
section.

The label `sec:helpfontdialog` is reserved for the Help button in
the `HelpFrame`'s font dialog. If the label is not present in the
LaTeX documentation, the Help button will be omitted. If you want to
include the section on how to use the help system in your
documentation, you can copy the `helpinterface.tex` file and `\input` 
it in the appropriate place. (You will also need to copy over the
image files as well.)

If an HTML licence file has been included in the helpset (via
`tjhziphelpset`'s `--license` or `-l` switch) then a `JDialog` can
be created that contains the document:

```java
JFrame parentFrame = ...;

JDialog licenseDialog = helpLib.createLicenseDialog(parentFrame,
             helpLib.getMessage("license.title"));
```

The title text identified by `license.title` must be defined in the
localisation file. (The `.title` suffix is used for container titles.)
For example:

```xml
<entry key="license.title">License</entry>
```

This will be converted by `tjhxml2bib` into the entry:

```bib
@dialog{license.title,
  name={\dialogfmt{License}}
}
```

The `texjavahelp.sty` package provides `bib2gls` entry aliases,
including `@dialog`.

The entry can be referenced in the document text with `\dialog`:

```latex
the \dialog{license} dialog ...
```

(or you can just use `\gls{license.title}`).

The prefix `menu.` indicates a menu and menu item. For example:

```xml
<entry key="menu.file">File</entry>
<entry key="menu.file.mnemonic">F</entry>

<entry key="menu.file.quit">Quit</entry>
<entry key="menu.file.quit.mnemonic">Q</entry>
<entry key="menu.file.quit.tooltip">Exit application</entry>
<entry key="menu.file.quit.description">This menu item quits the application</entry>
```

The above provides the text for a menu (File) and an item in that
menu (Quit). The suffix `.mnemonic`, `.tooltip` and `.description` identify fields.
The above corresponds to two bib2gls entries:

```
@menu{menu.file,
  name={\menufmt{File}},
  mnemonic={F}
} 
@menu{menu.file.quit,
  parent={menu.file},
  name={\menufmt{Quit}},
  tooltip={Exit application},
  mnemonic={Q},
  description={This menu item quits the application}
}
```

The `.iconimage` suffix is used to identify an icon image for a
widget. For example, `texjavahelplib.jar` contains the files:
```
com/dickimawbooks/texjavahelplib/icons/okay-16.png
com/dickimawbooks/texjavahelplib/icons/okay-20.png
com/dickimawbooks/texjavahelplib/icons/okay-24.png
com/dickimawbooks/texjavahelplib/icons/okay-32.png
com/dickimawbooks/texjavahelplib/icons/okay-64.png
```

Common buttons such as Okay and Cancel are provided in
the dictionary file `texjavahelplib-en.xml`:

```xml
<entry key="button.okay">Okay</entry>
<entry key="button.okay.mnemonic">O</entry>
<entry key="button.okay.keystroke">shift ENTER</entry>
<entry key="button.okay.defaultkeys">format=glsignore</entry>
<entry key="button.okay.tooltip">Accept changes and close window</entry>
<entry key="button.okay.iconimage">okay</entry>
```

`tjhxml2bib` will convert this to:

```
@commonbutton{button.okay,
  name={\buttonfmt{Okay}},
  tooltip={Accept changes and close window},
  mnemonic={O},
  defaultkeys={format=glsignore},
  keystroke={\keys{\keyref{shift}+\keyref{return}}},
  iconimage={okay}
}
```

(The `defaultkeys` value `format=glsignore` indicates a common entry
that is referenced too frequently to index every mention.)

An Okay button can be created with:

```java
TeXJavaHelpLib.createOkayButton(OkayAction, JComponent)
```

The default icon size prefix is
`-16` for small icons and `-32` for large icons. So the Okay button
created with the `createOkayButton` method will look for the image `okay-16.png`.

The button can be referenced in the LaTeX file with `\btn{okay}`
which will display the text “Okay”.  Since the `iconimage` field is
also set, `\btn` will try to include the associated small icon
image.  However, `texjavahelp.sty` can't access any files within the
`jar` file so you also need to provide a way for `\includegraphics`
to access the image:

```latex
\TJHRequireIcons{texjavahelplibicons}{16}{24}
```

(This indicates the files `texjavahelplibicons-16.pdf` and
`texjavahelplibicons-24.pdf` with corresponding mapping files
`texjavahelplibicons-16.def` and `texjavahelplibicons-24.def`, which
supply the small and large icons that are embedded in
`texjavahelplib.jar`. These files will need to be on TeX's input path.)
The small and large suffix don't need to match those used by the
Java application, but should instead be chosen to best fit the PDF
document.


## Building from Source

The `https://github.com/nlct/texjavahelp` repository additionally
provides a GUI demonstration called `texjavahelpdemo`. It just has a
window, menus and buttons that open various parts of the TJH
documentation that has been converted into a helpset.

The dictionary `.xml` files are bundled in the associated `.jar`
file but in the source they can be found in the `dictionaries`
subdirectory. (If you want to add translations, you can do so via a
pull request.)

To compile the documentation you need to first create `texjavahelplib.bib`:

```bash
tjhxml2bib dictionaries/texjavahelplib-en.xml dictionaries/texjavahelpdemo-en.xml -o doc/texjavahelplib.bib
```

If any of the dictionary files are embedded in a jar file, you will
need to use the `--resource` (or `-r`) switch. For example:

```bash
tjhxml2bib --resource /com/dickimawbooks/texjavahelplib/dictionaries/texjavahelplib-en.xml dictionaries/texjavahelpdemo-en.xml -o doc/texjavahelplib.bib
```

(In the case of `texjavahelpdemo` both the above methods are
available if building from a clone of the repository.)

The above creates a Bib2Gls `.bib` file with entry information obtained
from the application's XML dictionary files. The entry tags have a
particular system that helps `tjhxml2bib` to determine the glossary
entry hierarchy and entry fields. This allows the documentation
system to reproduce menu hierarchies, mnemonics, keystrokes and button labels
from the flat XML format.

The `texjavahelp.sty` package provides the custom entry types (such
as `@menu`) and fields (such as `tooltip` and `mnemonic`).
It also sets global options with `\BibGlsOptions` so you don't need
to include `--group` when you run `bib2gls` as that's automatically
switched on.

Then compile the LaTeX source:

```bash
lualatex texjavahelp
bib2gls texjavahelp
lualatex texjavahelp
lualatex texjavahelp
```

**NOTE** The temporary files (`aux`, `toc`, `glstex` etc) created 
during the build process are required for the next step.

The helpset files can then be created with:

```bash
texjavahelpmk doc/texjavahelp.tex lib/helpset
```

This creates a sub-directory `lib/helpset` which contains all the
HTML, CSS and XML files required for the helpset.
These can then be bundled into a file called `helpset.tjh` (a special
type of zip file) that contains both the helpset files and the
licence file which is identified as being in English:

```bash
tjhziphelpset -L doc/gpl-3.0-standalone.html en lib
```

(The `lib/helpset` sub-directory can then be deleted afterwards.)
If the application is to be distributed on TeX Live then a more specific
name is required for the helpset. For example:

```bash
tjhziphelpset -L doc/gpl-3.0-standalone.html en lib -o example-helpset.tjh
```

If the `.tjh` file is missing, TJH will search for the `helpset`
sub-directory on the resource path. However, it works better with
the `.tjh` file, which includes a manifest and the file contents are
cached.

Since texjavahelpmk only has a limited knowledge of LaTeX commands
and packages, it's best to minimise the number of packages used.
The user-level commands and environments provided by `texjavahelp.sty` are
recognised but as `JEditorPane` only has limited CSS support, some
effects from the PDF can't be replicated in the HTML files.

If you use one of the KOMA-Script classes, use the `toc=listof`
class option. If you have a list of figures or list of tables, use a
hook to insert a label. For example:

```latex
\AfterTOCHead[lof]{%
 \label{listoffigures}%
}
```

It's best to label all sections, even if you don't need to reference
them. The label is used to form the basename of the corresponding
HTML file.

Instead of using `\GlsXtrLoadResources` directly, use
`\TeXJavaHelpLoadResources{`__bib list__`}` which sets up all the
TJH resource options.

## TODO

Finish documentation.

Repository: https://github.com/nlct/texjavahelp  
License: GPL-3  
Author: Nicola L. C. Talbot [dickimaw-books.com](https://www.dickimaw-books.com/)
