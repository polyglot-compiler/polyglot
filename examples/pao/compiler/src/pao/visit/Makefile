SOURCE = ../../../..
PACKAGE = polyglot/ext/pao/visit
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	PaoBoxer.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = pao.jar

all: $(CLASSES)

clean clobber: cleanclasses

