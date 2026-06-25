arara = arara --verbose
texjavahelpmk=texjavahelpmk #--debug-mode sty-data --log texparser.log
tjhziphelpset=tjhziphelpset
tjhflattendocsrc=tjhflattendocsrc
srcdir=../..
libdir=$(srcdir)/lib

all	: helpset/index.xml test-helpset.tjh html/index.shtml epub/test.epub

test.pdf	: test.glstex
		lualatex test
		lualatex test

test.glstex	: test.tex test.bib
		lualatex test
		bib2gls test

helpset/index.xml	: $(libdir)/texjavahelpmk.jar test.pdf
		$(texjavahelpmk) --log test-helpset.log \
		test.tex -o helpset

test-helpset.tjh	: $(libdir)/tjhziphelpset.jar helpset/index.xml
		$(tjhziphelpset) -L gpl-3.0-standalone.html en . -o test-helpset.tjh

html/index.shtml	: $(libdir)/texjavahelpmk.jar test.pdf
		$(texjavahelpmk) --log test-html.log \
		--nohelpset \
		--html-ext shtml \
		--image-dest images \
		--root-name index \
		--head-from-file head.html \
		--body-preamble-from-file bodypreamble.html \
		--body-postamble-from-file bodypostamble.html \
		--minitoc-div-id sidebar \
		--minitoc-preamble-from-file minitocpreamble.html \
		test.tex -o html

epub/test.epub	: $(libdir)/texjavahelpmk.jar test.pdf
		$(texjavahelpmk) --log test-epub.log \
		--epub \
		--image-dest images \
		--isbn "0123456789012" \
		--author "Ann Author" \
		--author-file-as "Author, Ann" \
		test.tex -o epub

$(libdir)/texjavahelpmk.jar	:
		make -C $(srcdir)

$(libdir)/tjhflattendocsrc.jar	:
		make -C $(srcdir)

clean	:
	\rm -f test*.{aux,glstex,log,toc,pdf,glg}
	\rm -f -r helpset
	\rm -f -r html
	\rm -f -r epub
	\rm -f test-helpset.tjh
