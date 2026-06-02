ifeq ($(shell javac -version 2>&1 | grep 1.8),)
# Java 8 extended support until December 2030
  target_version := --release 8 -Xlint:-options
else
  target_version =
endif
arara = arara --verbose
texjavahelpmk=bin/texjavahelpmk.tlu #--debug-mode sty-data --log texparser.log
tjhflattendocsrc=bin/tjhflattendocsrc.tlu 
tjhziphelpset=bin/tjhziphelpset.tlu 

texjavaparserlib=$(shell kpsewhich --format=texmfscripts texjavaparserlib.jar)

ifeq ($(texjavaparserlib),)
  texjavaparserlib = $(HOME)/programming/java/texparser/src/lib/texjavaparserlib.jar
endif

all:	lib/texjavahelplib.jar \
	lib/tjhziphelpset.jar \
	lib/tjhxml2bib.jar \
	lib/texjavahelpmk.jar \
	lib/tjhflattendocsrc.jar \
	lib/texjavahelpdemo.jar

lib/texjavahelpdemo.jar	: java/texjavahelpdemo/Manifest.txt \
	classes/com/dickimawbooks/texjavahelpdemo/TeXJavaHelpDemo.class \
	lib/helpset.zip
	cp dictionaries/texjavahelpdemo-*.xml classes/com/dickimawbooks/texjavahelpdemo/
	cd classes; \
	jar cmf ../java/texjavahelpdemo/Manifest.txt ../lib/texjavahelpdemo.jar \
	com/dickimawbooks/texjavahelpdemo/*.class \
	com/dickimawbooks/texjavahelpdemo/*.xml

classes/com/dickimawbooks/texjavahelpdemo/TeXJavaHelpDemo.class	: classes/com/dickimawbooks/texjavahelpdemo \
	java/texjavahelpdemo/*.java lib/texjavahelplib.jar 
	cd java/texjavahelpdemo; \
	javac $(target_version) -d ../../classes \
	 -Xlint:unchecked -Xlint:deprecation \
	-cp ../../lib/texjavahelplib.jar \
	*.java

demo:	lib/texjavahelpdemo.jar
	bin/texjavahelpdemo

lib/helpset.zip	: lib/helpset/index.xml lib/tjhziphelpset.jar
	$(tjhziphelpset) -L doc/gpl-3.0-standalone.html en lib

classes/com/dickimawbooks/texjavahelpdemo	:
	mkdir -p classes/com/dickimawbooks/texjavahelpdemo

lib/helpset/index.xml	: lib/texjavahelpmk.jar doc/texjavahelp.pdf
	$(texjavahelpmk) doc/texjavahelp.tex lib/helpset
	@grep -E 'Package glossaries(-extra)? Warning' doc/texjavahelp.log || true

doc/texjavahelp.pdf	: doc/texjavahelplib.bib doc/texjavahelp.tex doc/helpinterface.tex doc/texjavahelp.sty doc/texjavahelp.bib doc/images/*.tex doc/images/*.png
	$(arara) -d doc texjavahelp

doc/texjavahelplib.bib	: lib/tjhxml2bib.jar \
			dictionaries/texjavahelplib-en.xml \
			dictionaries/texjavahelpdemo-en.xml
	bin/tjhxml2bib \
		dictionaries/texjavahelplib-en.xml \
		dictionaries/texjavahelpdemo-en.xml \
		-o doc/texjavahelplib.bib

lib/texjavahelpmk.jar	: java/texjavahelpmk/Manifest.txt \
	classes/com/dickimawbooks/texjavahelpmk/TeXJavaHelpMk.class
	cp dictionaries/texjavahelpmk-*.xml classes/com/dickimawbooks/texjavahelpmk/
	cd classes; \
	jar cmf ../java/texjavahelpmk/Manifest.txt ../lib/texjavahelpmk.jar \
	com/dickimawbooks/texjavahelpmk/*.class \
	com/dickimawbooks/texjavahelpmk/*.xml

classes/com/dickimawbooks/texjavahelpmk/TeXJavaHelpMk.class	: classes/com/dickimawbooks/texjavahelpmk \
	java/texjavahelpmk/*.java lib/texjavahelplib.jar 
	cd java/texjavahelpmk; \
	javac $(target_version) -d ../../classes \
	 -Xlint:unchecked -Xlint:deprecation \
	-cp $(texjavaparserlib):../../lib/texjavahelplib.jar \
	*.java

classes/com/dickimawbooks/texjavahelpmk	:
	mkdir -p classes/com/dickimawbooks/texjavahelpmk

lib/texjavahelplib.jar	: classes/com/dickimawbooks/texjavahelplib/TeXJavaHelpLib.class
	cp -u -r java/texjavahelplib/icons classes/com/dickimawbooks/texjavahelplib/
	cp dictionaries/texjavahelplib-*.xml classes/com/dickimawbooks/texjavahelplib/ 
	cd classes; \
	jar cf ../lib/texjavahelplib.jar \
	com/dickimawbooks/texjavahelplib/*.class \
	com/dickimawbooks/texjavahelplib/*.xml \
	com/dickimawbooks/texjavahelplib/icons/*

classes/com/dickimawbooks/texjavahelplib/TeXJavaHelpLib.class	: classes/com/dickimawbooks/texjavahelplib \
	java/texjavahelplib/*.java java/texjavahelplib/icons/*.png
	cd java/texjavahelplib; \
	javac $(target_version) -d ../../classes \
	 -Xlint:unchecked -Xlint:deprecation \
	-cp $(texjavaparserlib) \
	*.java

classes/com/dickimawbooks/texjavahelplib	:
	mkdir -p classes/com/dickimawbooks/texjavahelplib

lib/tjhxml2bib.jar	: java/tjhxml2bib/Manifest.txt \
	classes/com/dickimawbooks/tjhxml2bib/Xml2Bib.class
	cp dictionaries/tjhxml2bib-*.xml classes/com/dickimawbooks/tjhxml2bib/
	cd classes; \
	jar cmf ../java/tjhxml2bib/Manifest.txt ../lib/tjhxml2bib.jar \
	com/dickimawbooks/tjhxml2bib/*.class \
	com/dickimawbooks/tjhxml2bib/*.xml

classes/com/dickimawbooks/tjhxml2bib/Xml2Bib.class	: classes/com/dickimawbooks/tjhxml2bib \
	java/tjhxml2bib/*.java lib/texjavahelplib.jar
	cd java/tjhxml2bib; \
	javac $(target_version) -d ../../classes \
	 -Xlint:unchecked -Xlint:deprecation \
	-cp ../../lib/texjavahelplib.jar \
	*.java

classes/com/dickimawbooks/tjhxml2bib	:
	mkdir -p classes/com/dickimawbooks/tjhxml2bib

lib/tjhflattendocsrc.jar	: java/tjhflattendocsrc/Manifest.txt \
	classes/com/dickimawbooks/tjhflattendocsrc/FlattenDocSrc.class
	cp dictionaries/tjhflattendocsrc-*.xml classes/com/dickimawbooks/tjhflattendocsrc/
	cd classes; \
	jar cmf ../java/tjhflattendocsrc/Manifest.txt ../lib/tjhflattendocsrc.jar \
	com/dickimawbooks/tjhflattendocsrc/*.class \
	com/dickimawbooks/tjhflattendocsrc/*.xml

classes/com/dickimawbooks/tjhflattendocsrc/FlattenDocSrc.class	: classes/com/dickimawbooks/tjhflattendocsrc \
	java/tjhflattendocsrc/*.java lib/texjavahelplib.jar
	cd java/tjhflattendocsrc; \
	javac $(target_version) -d ../../classes \
	 -Xlint:unchecked -Xlint:deprecation \
	-cp $(texjavaparserlib):../../lib/texjavahelplib.jar \
	*.java

classes/com/dickimawbooks/tjhflattendocsrc	:
	mkdir -p classes/com/dickimawbooks/tjhflattendocsrc

lib/tjhziphelpset.jar	: java/tjhziphelpset/Manifest.txt \
	classes/com/dickimawbooks/tjhziphelpset/ZipHelpset.class
	cp dictionaries/tjhziphelpset-*.xml classes/com/dickimawbooks/tjhziphelpset/
	cd classes; \
	jar cmf ../java/tjhziphelpset/Manifest.txt ../lib/tjhziphelpset.jar \
	com/dickimawbooks/tjhziphelpset/*.class \
	com/dickimawbooks/tjhziphelpset/*.xml

classes/com/dickimawbooks/tjhziphelpset/ZipHelpset.class	: classes/com/dickimawbooks/tjhziphelpset \
	java/tjhziphelpset/*.java lib/texjavahelplib.jar
	cd java/tjhziphelpset; \
	javac $(target_version) -d ../../classes \
	 -Xlint:unchecked -Xlint:deprecation \
	-cp $(texjavaparserlib):../../lib/texjavahelplib.jar \
	*.java

classes/com/dickimawbooks/tjhziphelpset	:
	mkdir -p classes/com/dickimawbooks/tjhziphelpset

../unstable/texjavahelp.zip	: lib/texjavahelplib.jar \
	lib/texjavahelpmk.jar \
	lib/tjhxml2bib.jar \
	lib/tjhflattendocsrc.jar \
	lib/tjhziphelpset.jar \
	doc/texjavahelp.sty doc/texjavahelp.pdf ../LICENSE ../README.md
	mkdir -p ../unstable/texjavahelp
	cp lib/texjavahelplib.jar ../unstable/texjavahelp/
	cp lib/texjavahelpmk.jar ../unstable/texjavahelp/
	cp lib/tjhxml2bib.jar ../unstable/texjavahelp/
	cp lib/tjhflattendocsrc.jar ../unstable/texjavahelp/
	cp lib/tjhziphelpset.jar ../unstable/texjavahelp/
	cp bin/*.{sh,tlu} ../unstable/texjavahelp/
	cp doc/texjavahelp.sty ../unstable/texjavahelp/
	cp doc/texjavahelp.pdf ../unstable/texjavahelp/
	$(tjhflattendocsrc) doc/texjavahelp.tex ../unstable/
	cp ../LICENSE ../unstable/texjavahelp
	cp ../README.md ../unstable/texjavahelp
	cd ../unstable ; zip -r texjavahelp.zip texjavahelp

../unstable/html/index.shtml	: lib/texjavahelpmk.jar \
	doc/texjavahelp.pdf
	$(texjavahelpmk) --nohelpset \
	--html-ext shtml \
	--image-dest images \
	--root-name index \
	--head-from-file doc/head.html \
	--body-preamble-from-file doc/bodypreamble.html \
	--body-postamble-from-file doc/bodypostamble.html \
	--minitoc-div-id sidebar \
	--minitoc-preamble-from-file doc/minitocpreamble.html \
	doc/texjavahelp.tex -o ../unstable/html

../unstable/epub/texjavahelp.epub	: lib/texjavahelpmk.jar \
	doc/texjavahelp.pdf
	$(texjavahelpmk) --epub \
	--image-dest images \
	--isbn "xxxxxxxxxxxxx" \
	--author "Nicola L. C. Talbot" \
	--author-file-as "Talbot, Nicola L. C." \
	doc/texjavahelp.tex -o ../unstable/epub

clean	:
	\rm -rf classes
	\rm -f doc/*.glstex
	\rm -f doc/*.aux
	\rm -f doc/*.toc
	\rm -f doc/*.log
	\rm -f doc/*.glg
	\rm -f doc/*.out
	\rm -f doc/*.pdf
	\rm -f lib/texjavahelplib.jar
	\rm -f lib/texjavahelpmk.jar
	\rm -f lib/tjhxml2bib.jar
	\rm -f lib/texjavahelpdemo.jar
	\rm -f lib/tjhflattendocsrc.jar
	\rm -f lib/tjhziphelpset.jar
	\rm -rf lib/helpset
	\rm -f lib/helpset.zip
