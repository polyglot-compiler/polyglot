SOURCE = ../../../..
PACKAGE = polyglot/ext/pao/extension
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	PaoBinaryExt_c.java \
	PaoCastExt_c.java \
	PaoExt_c.java \
	PaoExt.java \
	PaoInstanceofDel_c.java \
	PaoInstanceofExt_c.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = pao.jar

all: $(CLASSES)

clean clobber: cleanclasses

