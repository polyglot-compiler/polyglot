SOURCE = ../..
PACKAGE = polyglot/parse
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	ParserWrapper.java \
	Util.java \
	VarDeclarator.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = polyglot.jar

all: $(CLASSES)

clean clobber: cleanclasses
