SOURCE = ../../../..
PACKAGE = polyglot/ext/pao/ast
SUBDIRS =
JAR_FILE = pao.jar

include $(SOURCE)/Rules.mk

SOURCES = \
	PaoInstanceof_c.java \
	PaoNodeFactory_c.java \
	PaoNodeFactory.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

clean clobber: cleanclasses
