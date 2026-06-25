# INSTALLATION

The TeX Java Help (TJH) system requires the TeX Java Parser (TJP) library to be
installed. If `TEXMF` is the TEXMF root, for example, `~/texmf`, then
TJP needs to be installed as:
```
TEXMF/scripts/texjavaparser/texjavaparserlib.jar
```
The `jar` file includes the localisation support files. Language
support can be added via pull request at https://github.com/nlct/texparser
(The dictionary file source is in the `src/dictionaries`
sub-directory.)

TJH needs to be installed as:

```
TEXMF/scripts/texjavahelp/texjavahelplib.jar
TEXMF/scripts/texjavahelp/texjavahelpmk.jar
TEXMF/scripts/texjavahelp/texjavahelpmk.tlu
TEXMF/scripts/texjavahelp/tjhcreateiconpdf.jar
TEXMF/scripts/texjavahelp/tjhcreateiconpdf.tlu
TEXMF/scripts/texjavahelp/tjhflattendocsrc.jar
TEXMF/scripts/texjavahelp/tjhflattendocsrc.tlu
TEXMF/scripts/texjavahelp/tjhviewer.jar
TEXMF/scripts/texjavahelp/tjhviewer.sh
TEXMF/scripts/texjavahelp/tjhxml2bib.jar
TEXMF/scripts/texjavahelp/tjhxml2bib.sh
TEXMF/scripts/texjavahelp/tjhziphelpset.jar
TEXMF/scripts/texjavahelp/tjhziphelpset.tlu

TEXMF/tex/latex/texjavahelp/texjavahelplibicons-16.def
TEXMF/tex/latex/texjavahelp/texjavahelplibicons-16.pdf
TEXMF/tex/latex/texjavahelp/texjavahelplibicons-20.def
TEXMF/tex/latex/texjavahelp/texjavahelplibicons-20.pdf
TEXMF/tex/latex/texjavahelp/texjavahelplibicons-24.def
TEXMF/tex/latex/texjavahelp/texjavahelplibicons-24.pdf
TEXMF/tex/latex/texjavahelp/texjavahelplibicons-32.def
TEXMF/tex/latex/texjavahelp/texjavahelplibicons-32.pdf
TEXMF/tex/latex/texjavahelp/texjavahelplibicons-64.def
TEXMF/tex/latex/texjavahelp/texjavahelplibicons-64.pdf
```
The `texjavahelplibicons-`_size_`.pdf` files contain an icon for the
given size on each page. These correspond to icons embedded in
`texjavahelplib.jar` and provide a way of including an icon image
in the documentation pdf. The corresponding `def` file provides a
mapping from the icon name to the applicable page number.

For Unix-like systems, make symbolic links in a directory that's on
your path (for example, `~/bin`) to the `.tlu` and `.sh` files
without the extension. For example, assuming that TEXMF is `~/texmf` 
and `~/bin` is on your path:

```bash
cd ~/bin
ln -s ~/texmf/scripts/texjavahelp/texjavahelpmk.tlu texjavahelpmk
ln -s ~/texmf/scripts/texjavahelp/tjhcreateiconpdf.tlu tjhcreateiconpdf
ln -s ~/texmf/scripts/texjavahelp/tjhflattendocsrc.tlu tjhflattendocsrc
ln -s ~/texmf/scripts/texjavahelp/tjhviewer.sh tjhviewer
ln -s ~/texmf/scripts/texjavahelp/tjhxml2bib.sh tjhxml2bib
ln -s ~/texmf/scripts/texjavahelp/tjhziphelpset.tlu tjhziphelpset
```

For Windows, find the file `runscript.exe` and copy it to
_basename_`.exe` where _basename_ is the basename of the `.tlu` or
`.sh` file. (Untested.)
