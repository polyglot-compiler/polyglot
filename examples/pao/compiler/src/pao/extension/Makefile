SOURCE = ../../../..
PACKAGE = polyglot/ext/pao/extension
SUBDIRS =
JAR_FILE = pao.jar

include $(SOURCE)/Rules.mk

SOURCES =

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

clean clobber: cleanclasses
