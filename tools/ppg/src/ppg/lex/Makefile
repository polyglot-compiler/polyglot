SOURCE = ../../../..
PACKAGE = polyglot/util/ppg/lex
SUBDIRS = 

include $(SOURCE)/Rules.mk

SOURCES = \
	Lexer.java \
	LexerResult.java \
	LexicalError.java \
	Token.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES) ppg.flex
JAR_FILE = polyglot.jar

Lexer.java: ppg.flex
	$(flex)

all: $(CLASSES)
	$(subdirs)

clean: cleanclasses

clobber: cleanclasses
	-rm -f Lexer.java

