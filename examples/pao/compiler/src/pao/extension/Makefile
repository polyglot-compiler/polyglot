SOURCE = ../../../..
PACKAGE = polyglot/ext/pao/extension
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES =

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = pao.jar

all: $(CLASSES)

clean clobber: cleanclasses

