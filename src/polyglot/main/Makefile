SOURCE = ../..
PACKAGE = jltools.main
SUBDIRS =

SOURCES = \
	Main.java \
	Options.java \
	MainTargetFactory.java

CLASSES = $(patsubst %.java, %.class, $(SOURCES))

include $(SOURCE)/Rules.mk

all: $(CLASSES)

clean clobber: cleanclasses
