SOURCE = ../../../..
PACKAGE = polyglot/ext/pao/extension
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES =

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

clean clobber: cleanclasses
