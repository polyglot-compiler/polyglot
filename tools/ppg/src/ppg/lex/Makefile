SOURCE = ../../../..
PACKAGE = jltools/util/jlgen/lex
SUBDIRS = 

include $(SOURCE)/Rules.mk

SOURCES = \
	Lexer.java \
	LexerResult.java \
	LexicalError.java \
	Token.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

lexer: jlgen.lex
	java -classpath $(LEX_RUNTIME) JFlex.Main jlgen.lex
	mv jlgen.lex.java Lexer.java

all: lexer $(CLASSES)
	$(subdirs)

release: release_src
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)
