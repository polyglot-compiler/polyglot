SOURCE = ../../../..
PACKAGE = polyglot/util/ppg/atoms
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

all: $(CLASSES)
	$(subdirs)

release: release_src
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)
