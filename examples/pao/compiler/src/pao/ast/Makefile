SOURCE = ../../../..
PACKAGE = polyglot/ext/pao/ast
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	PaoInstanceof_c.java \
	PaoNodeFactory_c.java \
	PaoNodeFactory.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

clean clobber: cleanclasses
