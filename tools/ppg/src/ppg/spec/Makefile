SOURCE = ../../../..
PACKAGE = jltools/util/jlgen/spec
SUBDIRS = 

include $(SOURCE)/Rules.mk

SOURCES = \
	CupSpec.java \
	JLgenSpec.java \
	Spec.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)
	$(subdirs)

release: release_src
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)
