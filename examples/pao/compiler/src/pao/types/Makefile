SOURCE = ../../../..
PACKAGE = polyglot/ext/pao/types
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	PaoTypeSystem_c.java \
	PaoTypeSystem.java \
	PaoParsedClassType_c.java \
	PaoPrimitiveType_c.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = pao.jar

all: $(CLASSES)
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

