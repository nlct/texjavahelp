# texjavahelp
Help library for Java GUI applications with LaTeX source

**Work in progress.**

The aim is to produce an alternative to JavaHelp for my Java GUI applications.
The documentation source will be in LaTeX, but it needs to be simple enough for
the [TeX Parser Library](https://github.com/nlct/texparser) to parse.
This means that the same source can be used to create both the PDF documentation
and the in-application help.

To compile the documentation you need to first create `texjavahelplib.bib`:
```bash
bin/tjhxml2bib lib/resources/texjavahelplib-en.xml lib/resources/texjavahelpdemo-en.xml -o doc/texjavahelplib.bib
```
The above creates a Bib2Gls file with entry information obtained
from the application's XML dictionary files. The entry tags have a
particular system that helps `tjhxml2bib` to determine the glossary
entry hierarchy and entry fields. This allows the documentation
system to reproduce menu hierarchies, mnemonics, keystrokes and button labels.

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
