SOURCE = ../../..
PACKAGE = jltools/util/jlgen/parse
SUBDIRS = 

include $(SOURCE)/Rules.mk

SOURCES = \
	Lexer.java \
	LexerResult.java \
	LexicalError.java \
	Token.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

parser:	jlgen.cup
	java -cp $(SOURCE)/java_cup.jar java_cup.Main -parser Parser -symbols Constant < jlgen.cup

all: parser $(CLASSES)
	$(subdirs)

release: release_src
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

