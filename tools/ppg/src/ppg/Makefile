SOURCE = ../../..
PACKAGE = jltools/util/jlgen
SUBDIRS = atoms cmds code lex parse spec util

include $(SOURCE)/Rules.mk

SOURCES = JLgen.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

$(SOURCE)/classes:
	mkdir $(SOURCE)/classes

all: parse $(CLASSES) $(SOURCE)/classes
	$(subdirs)

parse: FORCE
	$(MAKE) -C parse all

release: release_src
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

FORCE:
