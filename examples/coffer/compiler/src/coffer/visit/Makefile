SOURCE = ../../../..
PACKAGE = polyglot/ext/coffer/visit
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	KeyChecker.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = coffer.jar

all: $(CLASSES)

clean clobber: cleanclasses

