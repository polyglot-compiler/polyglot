SOURCE = ../../..
PACKAGE = ppg
SUBDIRS = atoms cmds code lex parse spec util

include $(SOURCE)/Rules.mk

SOURCES = \
	PPG.java \
	PPGError.java 

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

MANIFEST = Makefile $(SOURCES) tests
JAR_FILE = polyglot.jar

$(SOURCE)/classes:
	mkdir $(SOURCE)/classes

all: parse $(CLASSES) $(SOURCE)/classes
	$(subdirs)

parse: FORCE
	$(MAKE) -C parse all

clean clobber: cleanclasses
	$(subdirs)

FORCE:

