SOURCE = ../../../..
PACKAGE = polyglot/ext/pao/types
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	PaoTypeSystem_c.java \
	PaoTypeSystem.java \
	PaoParsedTopLevelClassType_c.java \
	PaoPrimitiveType_c.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)
