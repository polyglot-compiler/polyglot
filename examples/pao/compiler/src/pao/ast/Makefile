SOURCE = ../../../..
PACKAGE = polyglot/ext/pao/ast
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	PaoNodeFactory_c.java \
	PaoNodeFactory.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = pao.jar

all: $(CLASSES)

clean clobber: cleanclasses

