SOURCE = ../..
PACKAGE = jltools/parse
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	SyntaxException.java \
	Grm.java \
	sym.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

clean: cleanclasses

clobber: cleanclasses
	-rm -f Grm.java sym.java

Grm.java sym.java: java12.cup
	$(yacc)
