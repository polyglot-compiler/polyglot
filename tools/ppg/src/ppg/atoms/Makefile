SOURCE = ../../../..
PACKAGE = ppg/atoms
SUBDIRS = 

include $(SOURCE)/Rules.mk

SOURCES = \
	GrammarPart.java \
	GrammarSymbol.java \
	Nonterminal.java \
	Precedence.java \
	Production.java \
	SemanticAction.java \
	SymbolList.java \
	Terminal.java \
	TerminalList.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

MANIFEST = Makefile $(SOURCES)
JAR_FILE = polyglot.jar

all: $(CLASSES)
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)

