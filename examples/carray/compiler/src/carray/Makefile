SOURCE = ../../..
PACKAGE = jltools/ext/carray
#SUBDIRS = parse ast extension visit types
# runtime util wrappers

include $(SOURCE)/Rules.mk

SOURCES = \
	ExtensionInfo.java \
	Version.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))


all: parse $(CLASSES)
	$(subdirs)

parse: FORCE
	$(MAKE) -C parse all

FORCE:












