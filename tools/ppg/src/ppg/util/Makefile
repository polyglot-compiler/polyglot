SOURCE = ../../../..
PACKAGE = polyglot/util/ppg/util
SUBDIRS = 

include $(SOURCE)/Rules.mk

SOURCES = \
	CodeWriter.java \
	Equatable.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = polyglot.jar

all: $(CLASSES)
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

