SOURCE = ../..
PACKAGE = jltools/main
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	Main.java \
	Options.java \
	Report.java \
	UsageError.java \
	Version.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

release: release_src

clean clobber: cleanclasses
