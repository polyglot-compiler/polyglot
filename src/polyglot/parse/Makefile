SOURCE = ../..
PACKAGE = polyglot/parse
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	ParserWrapper.java \
	Util.java \
	VarDeclarator.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

release: release_src

clean clobber: cleanclasses


