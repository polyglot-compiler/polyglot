SOURCE = ../../..
PACKAGE = polyglot/ext/param
SUBDIRS = types

include $(SOURCE)/Rules.mk

SOURCES = \
	ExtensionInfo.java \
	Topics.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = param.jar

all: $(CLASSES)
	$(subdirs)

parse: FORCE
	$(MAKE) -C parse all

clean clobber: cleanclasses
	$(subdirs)

FORCE:

