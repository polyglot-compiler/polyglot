#
# Makefile to build the jltools source to source compiler
# includes a makefile in each package to handle building of respective 
# packages
#

SOURCE = .
SUBDIRS = jltools splitter

include Rules.mk

all clean:
	$(subdirs)

clobber:
	-rm -rf $(JAVADOC_OUTPUT)
	-rm -f $(JAR_FILE)
	$(subdirs)

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
	jltools.ext.jif.visit

javadoc: FORCE
	$(javadoc)

jar: all
	$(JAR) $(JAR_FLAGS) $(JAR_FILE) `find jltools -name \*.class`

FORCE:

