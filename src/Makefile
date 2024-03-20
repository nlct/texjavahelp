ifeq ($(shell javac -version 2>&1 | grep 1.8),)
  target_version := --release 8
else
  target_version =
endif

lib/resources/helpset/index.xml	: lib/texjavahelpmk.jar doc/texjavahelp.pdf
	bin/texjavahelpmk doc/texjavahelp.tex lib/resources/helpset

doc/texjavahelp.pdf	: doc/texjavahelp.tex doc/texjavahelp.sty doc/texjavahelp.bib
	arara -d doc texjavahelp

lib/texjavahelpmk.jar	: java/texjavahelpmk/Manifest.txt \
	classes/com/dickimawbooks/texjavahelpmk/TeXJavaHelpMk.class
	cd classes; \
	jar cmf ../java/texjavahelpmk/Manifest.txt ../lib/texjavahelpmk.jar \
	com/dickimawbooks/texjavahelpmk/*.class

classes/com/dickimawbooks/texjavahelpmk/TeXJavaHelpMk.class	: classes/com/dickimawbooks/texjavahelpmk \
	java/texjavahelpmk/*.java
	cd java/texjavahelpmk; \
	javac $(target_version) -d ../../classes \
	 -Xlint:unchecked -Xlint:deprecation \
	-cp ../../lib/texparserlib.jar \
	*.java

classes/com/dickimawbooks/texjavahelpmk	:
	mkdir -p classes/com/dickimawbooks/texjavahelpmk

clean	:
	\rm -rf classes
	\rm -f doc/*.glstex
	\rm -f doc/*.aux
	\rm -f doc/*.toc
