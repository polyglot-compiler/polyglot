SOURCE = ../../../..
PACKAGE = polyglot/util/ppg/spec
SUBDIRS = 
JAR_FILE = polyglot.jar

include $(SOURCE)/Rules.mk

SOURCES = \
	CUPSpec.java \
	PPGSpec.java \
	Spec.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)
	$(subdirs)

release: release_src
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)
