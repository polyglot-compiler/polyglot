SOURCE = ../../../..
PACKAGE = polyglot/ext/pao/extension
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	PaoBinaryDel_c.java \
	PaoCastDel_c.java \
	PaoDel_c.java \
	PaoDel.java \
	PaoInstanceofDel_c.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = pao.jar

all: $(CLASSES)

clean clobber: cleanclasses

