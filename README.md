# texjavahelp
Help library for Java GUI applications with LaTeX source

This bundle is provided because the TeX Java Help (TJH) library
(`texjavahelplib.jar`) is used by a number of my Java applications.
The accompanying TJH command line applications and the
`texjavahelp.sty` package are used to create the documentation and
in-application help.

The library also provides methods to implement localisation support
for both GUI and CLI applications, and a TeXApp class
for applications that use the TeX Java Parser library.

The aim is to provide an alternative to JavaHelp where the documentation 
for the both the PDF and GUI in-application help (for a particular
language) can be provided by a single LaTeX source.

If there are translations available, each translation can be
provided but the chapter/section units and labels must be the same
for each file. The label can be used by the application to provide
context-sensitive help to open the applicable chapter or section for
the chosen language.

Note that the LaTeX code needs to be simple enough for the 
[TeX Java Parser Library](https://github.com/nlct/texparser) to parse
(via `texjavahelpmk`).  This means that the same source can be used
to create both the PDF documentation and the in-application help.
Additionally, `texjavahelpmk` creates the navigation tree, search
database and index while it creates the HTML files.

The `bib2gls` application is used to generate the index information
(as well as any glossary terms). The application's language `.xml`
files can be converted to a `bib2gls` file with `tjhxml2bib`.
This requires a specific format for the entry labels in order to
convert the flat properties file to hierarchical entries.

The PDF must be compiled before `texjavahelpmk` is run as the
`.aux`, `.toc`, `.glstex` (and, where applicable, `.lot` or `.lof`)
files created during the document build are needed by
`texjavahelpmk`.

## Installing

The files need to be arranged as follows, where *TEXMF* denotes the
TEXMF tree root:

*TEXMF*`/scripts/texjavahelp/texjavahelplib.jar`    
*TEXMF*`/scripts/texjavahelp/texjavahelpmk.jar`    
*TEXMF*`/scripts/texjavahelp/texjavahelpmk.tlu`    
*TEXMF*`/scripts/texjavahelp/tjhflattendocsrc.jar`    
*TEXMF*`/scripts/texjavahelp/tjhflattendocsrc.tlu`    
*TEXMF*`/scripts/texjavahelp/tjhxml2bib.jar`    
*TEXMF*`/scripts/texjavahelp/tjhxml2bib.sh`    
*TEXMF*`/scripts/texjavahelp/tjhziphelpset.jar`    
*TEXMF*`/scripts/texjavahelp/tjhziphelpset.tlu`    

*TEXMF*`/tex/latex/texjavahelp/texjavahelp.sty`    

*TEXMF*`/doc/latex/texjavahelp/texjavahelp.pdf`    
*TEXMF*`/doc/latex/texjavahelp/texjavahelp.tex`    
*TEXMF*`/doc/latex/texjavahelp/texjavahelp.bib`  
*TEXMF*`/doc/latex/texjavahelp/texjavahelplib.bib`  
*TEXMF*`/doc/latex/texjavahelp/images/*`

The `texjavahelplib.jar` file is a library. The other jar files are
command line applications.

The `texjavahelpmk`, `tjhflattendocsrc` and `tjhziphelpset` applications require the
TeX Java Parser Library, which should be installed separately:

*TEXMF*`/scripts/texjavaparser/texjavaparserlib.jar`

The `.tlu` texlua scripts search for `texjavaparserlib.jar` and add
it to the Java class path. 

```bash
texjavahelpmk.tlu [<options>] <in-tex> <out-dir>
tjhziphelpset.tlu [<options>] <in-dir>
tjhflattendocsrc.tlu [<options>] <in-tex> <out-dir>
```

The `tjhxml2bib` application doesn't require `texjavaparserlib.jar`
so the invocation is simpler (`path/to/` is the path to
*TEXMF*`/scripts/texjavahelp/`): 

```bash
java -jar path/to/tjhxml2bib.jar [<options>] <xml>... -o <bib>
```

Localisation support is contained within the applicable `.jar` file.
The `texjavahelplib.jar` file contains common icons for use with GUI
applications.

## Example

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

The default name for the helpset archive is `helpset.zip` but if a
different name is required this must first be set. For example, if
the file is called `demo-helpset.zip`:

```java
helpLib.setHelpSetZipName("demo-helpset.zip");
```

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

If the licence file has been included in the helpset (via
`tjhziphelpset`'s `--license` or `-l` switch) then a `JDialog` can
be created that contains the document:

```java
JFrame parentFrame = ...;

JDialog licenseDialog = helpLib.createLicenseDialog(parentFrame,
             helpLib.getMessage("license.title"));
```

The title text identified by `license.title` must be defined in the
localisation file. (The `.title` suffix is used for window titles.)
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

It can be referenced in the document text with `\dialog`:

```latex
the \dialog{license} dialog ...
```

(or you can just use `\gls{license.title}`).

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

The above creates a Bib2Gls file with entry information obtained
from the application's XML dictionary files. The entry tags have a
particular system that helps `tjhxml2bib` to determine the glossary
entry hierarchy and entry fields. This allows the documentation
system to reproduce menu hierarchies, mnemonics, keystrokes and button labels.

For example:

```xml
<entry key="menu.file">File</entry>
<entry key="menu.file.mnemonic">F</entry>

<entry key="menu.file.quit">Quit</entry>
<entry key="menu.file.quit.mnemonic">Q</entry>
<entry key="menu.file.quit.tooltip">Exit application</entry>
<entry key="menu.file.quit.description">This menu item quits the application</entry>
```

The above provides the text for a menu (File) and an item in that
menu (Quit). The `menu.` prefix indicates a menu or menu item. The
suffix `.mnemonic`, `.tooltip` and `.description` identify fields.
This corresponds to two bib2gls entries:

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

The `texjavahelp.sty` package provides the custom entry types (such
as `@menu`) and fields (such as `tooltip` and `mnemonic`).
It also sets global options with `\BibGlsOptions` so you don't need
to include `--group` when you run `bib2gls` as that's automatically
switched on.

Then compile the LaTeX source:

```bash
lualatex texjavahelpdoc
bib2gls texjavahelpdoc
lualatex texjavahelpdoc
lualatex texjavahelpdoc
```

**NOTE** The temporary files (`aux`, `toc`, `glstex` etc) created 
during the build process are required for the next step.

The helpset files can then be created with:

```bash
texjavahelpmk doc/texjavahelp.tex lib/helpset
```

This can then be bundled into an archive called `helpset.zip` that contains both the
helpset files and the licence file which is identified as being in English:

```bash
tjhziphelpset -L doc/gpl-3.0-standalone.html en lib
```

If the application is to be distributed on TeX Live then a more specific
name is required for the helpset. For example, FlowframTk has
`flowframtk-helpset.zip` which is created using:

```bash
tjhziphelpset --helpset flowframtk-helpset \
--locales 'en,en-GB' \
-L path/to/doc/gpl-3.0-standalone.html en \ 
--in-dir . \
--output path/to/lib/flowframtk-helpset.zip 
```

But first the helpset files for `en` and `en-GB` must be created with `texjavahelpmk`:

```java
texjavahelpmk flowframtk-en.tex flowframtk-helpset/en
texjavahelpmk flowframtk-en-GB.tex flowframtk-helpset/en-GB
```

Since texjavahelpmk only has a limited knowledge of LaTeX commands
and packages, it's best to minimise the number of packages used.
The user-level commands and environments provided by `texjavahelp.sty` are
recognised but as `JEditorPane` only has limited CSS support, some
effects from the PDF can't be replicated in the HTML files.

## TODO

Finish documentation.

Home: https://github.com/nlct/texjavahelp
