SOURCE = ../../..
PACKAGE = jltools/util/typedump
SUBDIRS = 

SOURCES = \
	Main.java \
	TypeDumper.java

CLASSES = $(patsubst %.java, %.class, $(SOURCES))

include $(SOURCE)/Rules.mk

all: $(CLASSES)

clean clobber: cleanclasses
