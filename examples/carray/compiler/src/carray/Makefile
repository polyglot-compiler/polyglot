SOURCE = ../../..
PACKAGE = polyglot/ext/carray
SUBDIRS = parse ast types

include $(SOURCE)/Rules.mk

SOURCES = \
	ExtensionInfo.java \
	Version.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES) tests
JAR_FILE = carray.jar

all: parse $(CLASSES)
	$(subdirs)

parse: FORCE
	$(MAKE) -C parse all

clean clobber: cleanclasses
	$(subdirs)

FORCE:

