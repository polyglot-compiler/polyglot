SOURCE = ../../../..
PACKAGE = polyglot/util/ppg/spec
SUBDIRS = 

include $(SOURCE)/Rules.mk

SOURCES = \
	CUPSpec.java \
	PPGSpec.java \
	Spec.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = polyglot.jar

all: $(CLASSES)
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

