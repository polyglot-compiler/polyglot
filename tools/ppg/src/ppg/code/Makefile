SOURCE = ../../../..
PACKAGE = polyglot/util/ppg/code
SUBDIRS = 

include $(SOURCE)/Rules.mk

SOURCES = \
	ActionCode.java \
	Code.java \
	InitCode.java \
	ParserCode.java \
	ScanCode.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

MANIFEST = Makefile $(SOURCES)
JAR_FILE = polyglot.jar

all: $(CLASSES)
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

