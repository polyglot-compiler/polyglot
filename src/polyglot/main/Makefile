SOURCE = ../..
PACKAGE = polyglot/main
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	Main.java \
	Options.java \
	Report.java \
	UsageError.java \
	Version.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = polyglot.jar

all: $(CLASSES)

clean clobber: cleanclasses

