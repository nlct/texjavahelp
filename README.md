# texjavahelp
Help library for Java GUI applications with LaTeX source

This bundle is provided because the TeX Java Help (TJH) library
(`texjavahelplib.jar`) is used by a number of my Java applications.
The accompanying TJH command line applications and the
`texjavahelp.sty` package are used to create the documentation and
in-application help.

The library also provides methods to implement localisation support
for both GUI and CLI command line applications, and a TeXApp class
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

## INSTALLING

The files need to be arranged as follows, where *TEXMF* denotes the
TEXMF tree root:

*TEXMF*`/scripts/texjavahelp/texjavahelplib.jar`
*TEXMF*`/scripts/texjavahelp/texjavahelpmk.jar`
*TEXMF*`/scripts/texjavahelp/texjavahelpmk.tlu`
*TEXMF*`/scripts/texjavahelp/tjhxml2bib.jar`
*TEXMF*`/scripts/texjavahelp/tjhxml2bib.sh`
*TEXMF*`/scripts/texjavahelp/tjhflattendocsrc.jar`
*TEXMF*`/scripts/texjavahelp/tjhflattendocsrc.tlu`
*TEXMF*`/scripts/texjavahelp/tjhziphelpset.jar`
*TEXMF*`/scripts/texjavahelp/tjhziphelpset.sh`

*TEXMF*`/tex/latex/texjavahelp/texjavahelp.sty`

*TEXMF*`/doc/latex/texjavahelp/texjavahelp.pdf`
*TEXMF*`/doc/latex/texjavahelp/texjavahelp.tex`
*TEXMF*`/doc/latex/texjavahelp/texjavahelp.bib`

The `texjavahelplib.jar` file is a library. The other jar files are
command line applications.

The `texjavahelpmk` and `tjhflattendocsrc` applications require the
TeX Java Parser Library, which should be installed separately:

*TEXMF*`/scripts/texjavaparser/texjavaparserlib.jar`

The `.tlu` texlua scripts search for `texjavaparserlib.jar` and add
it to the Java class path. 

```bash
texjavahelpmk.tlu [options] <in-tex> <out-dir>
tjhflattendocsrc.tlu [options] <in-tex> <out-dir>
```

The `tjhxml2bib` and `tjhziphelpset`
applications don't require `texjavaparserlib.jar` so the invocation
is simpler (`path/to/` is the path to *TEXMF*`/scripts/texjavahelp/`): 

```bash
java -jar path/to/tjhxml2bib.jar [options] <xml>... -o <bib>
java -jar path/to/tjhziphelpset.jar [options] <dir>
```

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
bin/tjhxml2bib dictionaries/texjavahelplib-en.xml dictionaries/texjavahelpdemo-en.xml -o doc/texjavahelplib.bib
```
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

Then compile the LaTeX source:
```bash
lualatex texjavahelpdoc
bib2gls -g --no-warn-unknown-entry-types texjavahelpdoc
lualatex texjavahelpdoc
lualatex texjavahelpdoc
```

**NOTE** The temporary files (`aux`, `toc`, `glstex` etc) created 
during the build process are required for the next step.

The helpset files can then be created with:
```bash
texjavahelpmk doc/texjavahelp.tex lib/resources/helpset
```

Since texjavahelpmk only has a limited knowledge of LaTeX commands
and packages, it's best to minimise the number of packages used.
The user-level commands and environments provided by `texjavahelp.sty` are
recognised but as `JEditorPane` only has limited CSS support, some
effects from the PDF can't be replicated in the HTML files.

## TODO

Finish documentation.

Home: https://github.com/nlct/texjavahelp
