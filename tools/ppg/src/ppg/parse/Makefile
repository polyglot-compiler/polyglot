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

parser:	jlgen.cup
	java -cp $(CUP_RUNTIME) java_cup.Main -parser Parser -symbols Constant < jlgen.cup

all: parser $(CLASSES)
	$(subdirs)

release: release_src
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

