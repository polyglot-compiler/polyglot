SOURCE = ../../..
PACKAGE = polyglot/ext/coffer
SUBDIRS = parse ast extension visit types

include $(SOURCE)/Rules.mk

SOURCES = \
	ExtensionInfo.java \
	Topics.java \
	Version.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = coffer.jar

all: parse $(CLASSES)
	$(subdirs)

parse: FORCE
	$(MAKE) -C parse all

clean clobber: cleanclasses
	$(subdirs)

FORCE:

