SOURCE = ../../..
PACKAGE = polyglot/ext/pao
SUBDIRS = parse ast extension visit types
JAR_FILE = pao.jar

include $(SOURCE)/Rules.mk

SOURCES = \
	ExtensionInfo.java \
	Version.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: parse $(CLASSES)
	$(subdirs)

parse: FORCE
	$(MAKE) -C parse all

clean clobber: cleanclasses
	$(subdirs)

FORCE:
