SOURCE = ../../../..
PACKAGE = polyglot/util/ppg/code
SUBDIRS = 
JAR_FILE = polyglot.jar

include $(SOURCE)/Rules.mk

SOURCES = \
	ActionCode.java \
	Code.java \
	InitCode.java \
	ParserCode.java \
	ScanCode.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)
	$(subdirs)

release: release_src
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)
