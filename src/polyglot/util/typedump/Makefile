SOURCE = ../../..
PACKAGE = polyglot/util/typedump
SUBDIRS = 

include $(SOURCE)/Rules.mk

SOURCES = \
	Main.java \
	TypeDumper.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

release: release_src

clean clobber: cleanclasses
