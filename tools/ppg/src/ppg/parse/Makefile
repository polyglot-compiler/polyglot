SOURCE = ../../../..
PACKAGE = jltools/util/jlgen/parse
SUBDIRS = 

include $(SOURCE)/Rules.mk

SOURCES = \
	Constant.java \
	ParserError.java \
	Parser.java \
	ParseTest.java \
	Unparse.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

Parser.java Constant.java:	jlgen.cup
	java -cp $(CUP_RUNTIME) java_cup.Main -parser Parser -symbols Constant < jlgen.cup

all: $(CLASSES)
	$(subdirs)

release: release_src
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

