SOURCE = ../..
PACKAGE = polyglot/main
SUBDIRS =
JAR_FILE = polyglot.jar

include $(SOURCE)/Rules.mk

SOURCES = \
	Main.java \
	Options.java \
	Report.java \
	UsageError.java \
	Version.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

release: release_src

clean clobber: cleanclasses
