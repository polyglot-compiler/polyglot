SOURCE = ../../../..
PACKAGE = polyglot/ext/param/types
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	ParametricClassType.java \
	ParametricType.java \
	Param.java \
	ParamTypeSystem_c.java \
	ParamTypeSystem.java \
	SubstClassType_c.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = param.jar

all: $(CLASSES)
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

