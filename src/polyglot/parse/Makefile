SOURCE = ../..
PACKAGE = jltools/parse
SUBDIRS =

SOURCES = \
	SyntaxException.java \
	Grm.java \
	sym.java

CLASSES = $(patsubst %.java, %.class, $(SOURCES))

include $(SOURCE)/Rules.mk

all: $(CLASSES)

clean: cleanclasses

clobber: cleanclasses
	-rm -f Grm.java sym.java

Grm.java sym.java: java12.cup
	$(yacc)
