SOURCE = ../../..
PACKAGE = polyglot/util/ppg
SUBDIRS = atoms cmds code lex parse spec util
JAR_FILE = polyglot.jar

include $(SOURCE)/Rules.mk

SOURCES = PPG.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

$(SOURCE)/classes:
	mkdir $(SOURCE)/classes

all: parse $(CLASSES) $(SOURCE)/classes
	$(subdirs)

parse: FORCE
	$(MAKE) -C parse all

release: release_src
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

FORCE:
