SOURCE = ../..
PACKAGE = jltools/visit
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	AmbiguityRemover.java \
	ClassSerializer.java \
	ConstantFolder.java \
	DumpAst.java \
	ExceptionChecker.java \
	FlowChecker.java \
	FlattenVisitor.java \
	NodeScrambler.java \
	SignatureCleaner.java \
	SymbolReader.java \
	TranslationVisitor.java \
	TypeChecker.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

clean clobber: cleanclasses
