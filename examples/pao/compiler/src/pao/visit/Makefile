SOURCE = ../../../..
PACKAGE = polyglot/ext/pao/visit
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	PaoBoxer.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

clean clobber: cleanclasses
