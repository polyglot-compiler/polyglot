SOURCE = ../../../..
PACKAGE = polyglot/util/ppg/util
SUBDIRS = 
JAR_FILE = polyglot.jar

include $(SOURCE)/Rules.mk

SOURCES = \
	CodeWriter.java \
	Equatable.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)
	$(subdirs)

release: release_src
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)
