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
	$(MAKE) -C ../parse Parser.java

all: $(CLASSES)
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

