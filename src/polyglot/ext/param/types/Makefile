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
	InstType.java \
	Subst.java \
	Subst_c.java \
	SubstClassType_c.java \
	SubstType.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = param.jar

all: $(CLASSES)
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

