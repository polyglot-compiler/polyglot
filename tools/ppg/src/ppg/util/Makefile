SOURCE = ../../../..
PACKAGE = jltools/util/jlgen/util
SUBDIRS = 

include $(SOURCE)/Rules.mk

SOURCES = \
	CodeWriter.java \
	Equatable.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)
	$(subdirs)

release: release_src
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)
