SOURCE = ../../..
PACKAGE = jltools/util/typedump
SUBDIRS = 

include $(SOURCE)/Rules.mk

SOURCES = \
	Main.java \
	TypeDumper.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

clean clobber: cleanclasses
