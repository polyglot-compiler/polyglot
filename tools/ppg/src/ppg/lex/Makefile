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

Lexer.java: ppg.flex
	$(flex)

all: $(CLASSES)
	$(subdirs)

release: release_src
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)
