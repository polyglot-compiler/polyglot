#
# Makefile to build the polyglot source to source compiler
# includes a makefile in each package to handle building of respective 
# packages
#

SOURCE = .
SUBDIRS = polyglot
TAG = POLYGLOT_0_90

include Rules.mk

all: init
	$(subdirs)

init: classes lib
	@chmod +x bin/*

classes:
	mkdir classes

lib:
	mkdir lib

clean:
	-rm -rf classes
	$(subdirs)

clobber:
	-rm -rf $(JAVADOC_OUTPUT)
	-rm -f $(JAR_FILE)
	$(subdirs)

javadoc: FORCE
	$(javadoc)

norecurse: classes jif polyj split op jmatch
	$(JC) $(JC_FLAGS) polyglot/main/Main.java

jif:
	$(JC) $(JC_FLAGS) polyglot/ext/jif/ExtensionInfo.java
polyj:
	$(JC) $(JC_FLAGS) polyglot/ext/polyj/ExtensionInfo.java
op:
	$(JC) $(JC_FLAGS) polyglot/ext/op/ExtensionInfo.java
split:
	$(JC) $(JC_FLAGS) polyglot/ext/split/ExtensionInfo.java
jmatch:
	$(JC) $(JC_FLAGS) polyglot/ext/jmatch/ExtensionInfo.java

jar: all
	cd classes ; \
	$(JAR) $(JAR_FLAGS) ../$(JAR_FILE) `find polyglot -name \*.class`; \
	$(JAR) $(JAR_FLAGS) ../jif.jar `find jif -name \*.class`

export: javadoc
	rm -rf release
	mkdir release
	cvs rtag $(TAG) polyglot
	cd release; cvs export -r $(TAG) -f polyglot
	cvs rtag -d $(TAG) polyglot
	for i in $(EXT) skel; do \
		mv release/polyglot/polyglot/ext/$$i release; \
	done
	rm -rf release/polyglot/polyglot/ext/*/
	for i in $(EXT) skel; do \
		mv release/$$i release/polyglot/polyglot/ext; \
	done
	mv javadoc release/polyglot
	rm release/polyglot/jltools2polyglot.sh
	rm release/polyglot/iDoclet.jar
	rm release/polyglot/jsse.jar
	rm release/polyglot/jnet.jar
	rm release/polyglot/jcert.jar
	rm release/polyglot/cryptix32.jar
	rm -rf release/polyglot/bugs
	rm -rf release/polyglot/example
	rm -rf release/polyglot/splitter
	rm -rf release/polyglot/test
	rm release/polyglot/bin/polyjc
	rm release/polyglot/README-JIF.txt
	rm -rf release/polyglot/classes
	bin/jlc > release/polyglot/README-JLC.txt
	cd release; jar cf polyglot-src.jar polyglot

REL_SOURCES = \
	Rules.mk \
	java_cup.jar \
	jlex.jar \
	iDoclet.jar \

REL_LIBS = \
	polyglot.jar \
	java_cup.jar \
	jif.jar \

release_clean: FORCE
	rm -rf $(RELPATH)
	mkdir -p $(RELPATH)

release_doc: FORCE
	cp LICENSE Readme.html $(RELPATH)
	mkdir -p $(REL_DOC)
	mkdir -p $(REL_SRC)
	mkdir -p $(REL_IMG)
	cp -f images/*.gif $(REL_IMG)
	$(MAKE) -C doc release

release: jar release_clean release_doc release_src
	$(MAKE) -C polyglot/ext/jif/tests release
	cp -f configure $(RELPATH)/configure
	$(subdirs)
	mkdir -p $(REL_LIB)
	cp $(REL_LIBS) $(REL_LIB)
	cp lib/*fs.* $(REL_LIB)
	chmod a+x $(RELPATH)/configure
	rm polyglot.jar jif.jar
	
FORCE:
