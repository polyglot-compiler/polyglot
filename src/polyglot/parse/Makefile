SOURCE = ../..
PACKAGE = polyglot/parse
SUBDIRS =
JAR_FILE = polyglot.jar

include $(SOURCE)/Rules.mk

SOURCES = \
	ParserWrapper.java \
	Util.java \
	VarDeclarator.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

release: release_src

clean clobber: cleanclasses


