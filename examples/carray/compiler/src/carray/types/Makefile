SOURCE = ../../../..
PACKAGE = jltools/ext/carray/types
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	CarrayTypeSystem.java \
	ConstArrayType.java \
	ConstArrayType_c.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

release: release_src
