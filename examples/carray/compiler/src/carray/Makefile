SOURCE = ../../..
PACKAGE = polyglot/ext/carray
SUBDIRS = parse ast types
JAR_FILE = carray.jar

include $(SOURCE)/Rules.mk

SOURCES = \
	ExtensionInfo.java \
	Version.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: parse $(CLASSES)
	$(subdirs)

parse: FORCE
	$(MAKE) -C parse all

release: release_src
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

FORCE:
