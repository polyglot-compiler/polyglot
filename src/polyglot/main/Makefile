SOURCE = ../..
PACKAGE = jltools/main
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	Main.java \
	Options.java \
	Report.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

clean clobber: cleanclasses
