#
# Makefile to build the jltools source to source compiler
# includes a makefile in each package to handle building of respective 
# packages
#

SOURCE = .
SUBDIRS = jltools

include Rules.mk

all: classes
	$(subdirs)

clean:
	rm -rf classes
	$(subdirs)

classes:
	mkdir classes

clobber:
	-rm -rf $(JAVADOC_OUTPUT)
	-rm -f $(JAR_FILE)
	$(subdirs)

norecurse: classes jif polyj split op
	$(JC) $(JC_FLAGS) jltools/main/Main.java

jif:
	$(JC) $(JC_FLAGS) jltools/ext/jif/ExtensionInfo.java
polyj:
	$(JC) $(JC_FLAGS) jltools/ext/polyj/ExtensionInfo.java
op:
	$(JC) $(JC_FLAGS) jltools/ext/op/ExtensionInfo.java
split:
	$(JC) $(JC_FLAGS) jltools/ext/split/ExtensionInfo.java
	

PACKAGES = \
	jltools.ast \
	jltools.frontend \
	jltools.lex \
	jltools.main \
	jltools.parse \
	jltools.types \
	jltools.util \
	jltools.visit \
	jltools.ext.op \
	jltools.ext.op.runtime \
	jltools.ext.polyj \
	jltools.ext.polyj.ast \
	jltools.ext.polyj.lex \
	jltools.ext.polyj.extension \
	jltools.ext.polyj.parse \
	jltools.ext.polyj.types \
	jltools.ext.polyj.visit \
	jltools.ext.jif \
	jltools.ext.jif.ast \
	jltools.ext.jif.lex \
	jltools.ext.jif.extension \
	jltools.ext.jif.parse \
	jltools.ext.jif.types \
	jltools.ext.jif.visit \
	jif.lang

javadoc: FORCE
	$(javadoc)

jar: all
	cd classes ; \
	$(JAR) $(JAR_FLAGS) ../$(JAR_FILE) `find jltools -name \*.class`

FORCE:

