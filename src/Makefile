ifeq ($(shell javac -version 2>&1 | grep 1.8),)
  target_version := --release 8
else
  target_version =
endif
arara = arara --verbose

lib/texjavahelpdemo.jar	: java/texjavahelpdemo/Manifest.txt \
	classes/com/dickimawbooks/texjavahelpdemo/TeXJavaHelpDemo.class
	cd classes; \
	jar cmf ../java/texjavahelpdemo/Manifest.txt ../lib/texjavahelpdemo.jar \
	com/dickimawbooks/texjavahelpdemo/*.class

classes/com/dickimawbooks/texjavahelpdemo/TeXJavaHelpDemo.class	: classes/com/dickimawbooks/texjavahelpdemo \
	java/texjavahelpdemo/*.java lib/texjavahelplib.jar \
	lib/resources/helpset/index.xml
	cd java/texjavahelpdemo; \
	javac $(target_version) -d ../../classes \
	 -Xlint:unchecked -Xlint:deprecation \
	-cp ../../lib/texjavahelplib.jar \
	*.java

demo:	lib/texjavahelpdemo.jar
	bin/texjavahelpdemo

classes/com/dickimawbooks/texjavahelpdemo	:
	mkdir -p classes/com/dickimawbooks/texjavahelpdemo

lib/resources/helpset/index.xml	: lib/texjavahelpmk.jar doc/texjavahelp.pdf
	bin/texjavahelpmk doc/texjavahelp.tex lib/resources/helpset
	@grep -E 'Package glossaries(-extra)? Warning' doc/texjavahelp.log || true

doc/texjavahelp.pdf	: doc/texjavahelplib.bib doc/texjavahelp.tex doc/texjavahelp.sty doc/texjavahelp.bib doc/images/*.tex doc/images/*.png
	$(arara) -d doc texjavahelp

doc/texjavahelplib.bib	: lib/xml2bib.jar lib/resources/texjavahelplib-en.xml
	bin/xml2bib lib/resources/texjavahelplib-en.xml -o doc/texjavahelplib.bib

lib/texjavahelpmk.jar	: java/texjavahelpmk/Manifest.txt \
	classes/com/dickimawbooks/texjavahelpmk/TeXJavaHelpMk.class
	cd classes; \
	jar cmf ../java/texjavahelpmk/Manifest.txt ../lib/texjavahelpmk.jar \
	com/dickimawbooks/texjavahelpmk/*.class

classes/com/dickimawbooks/texjavahelpmk/TeXJavaHelpMk.class	: classes/com/dickimawbooks/texjavahelpmk \
	java/texjavahelpmk/*.java lib/texjavahelplib.jar lib/texparserlib.jar
	cd java/texjavahelpmk; \
	javac $(target_version) -d ../../classes \
	 -Xlint:unchecked -Xlint:deprecation \
	-cp ../../lib/texparserlib.jar:../../lib/texjavahelplib.jar \
	*.java

classes/com/dickimawbooks/texjavahelpmk	:
	mkdir -p classes/com/dickimawbooks/texjavahelpmk

lib/texjavahelplib.jar	: classes/com/dickimawbooks/texjavahelplib/TeXJavaHelpLib.class
	cp -r java/texjavahelplib/icons classes/com/dickimawbooks/texjavahelplib/
	cd classes; \
	jar cf ../lib/texjavahelplib.jar \
	com/dickimawbooks/texjavahelplib/*.class \
	com/dickimawbooks/texjavahelplib/icons/*

classes/com/dickimawbooks/texjavahelplib/TeXJavaHelpLib.class	: classes/com/dickimawbooks/texjavahelplib \
	java/texjavahelplib/*.java
	cd java/texjavahelplib; \
	javac $(target_version) -d ../../classes \
	 -Xlint:unchecked -Xlint:deprecation \
	-cp ../../lib/texparserlib.jar \
	*.java

classes/com/dickimawbooks/texjavahelplib	:
	mkdir -p classes/com/dickimawbooks/texjavahelplib

lib/xml2bib.jar	: java/xml2bib/Manifest.txt \
	classes/com/dickimawbooks/xml2bib/Xml2Bib.class
	cd classes; \
	jar cmf ../java/xml2bib/Manifest.txt ../lib/xml2bib.jar \
	com/dickimawbooks/xml2bib/*.class

classes/com/dickimawbooks/xml2bib/Xml2Bib.class	: classes/com/dickimawbooks/xml2bib \
	java/xml2bib/*.java lib/texjavahelplib.jar
	cd java/xml2bib; \
	javac $(target_version) -d ../../classes \
	 -Xlint:unchecked -Xlint:deprecation \
	-cp ../../lib/texjavahelplib.jar \
	*.java

classes/com/dickimawbooks/xml2bib	:
	mkdir -p classes/com/dickimawbooks/xml2bib

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
	\rm -rf lib/resources/helpset
