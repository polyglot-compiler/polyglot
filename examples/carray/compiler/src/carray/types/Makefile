SOURCE = ../../../..
PACKAGE = polyglot/ext/carray/types
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	CarrayTypeSystem.java \
	ConstArrayType.java \
	ConstArrayType_c.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = carray.jar

all: $(CLASSES)
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)
