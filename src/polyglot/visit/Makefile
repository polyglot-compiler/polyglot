SOURCE = ../..
PACKAGE = jltools.visit
SUBDIRS =k

SOURCES = \
	AmbiguityRemover.java \
	ClassSerializer.java \
	ConstantFolder.java \
	DumpAst.java \
	ExceptionChecker.java \
	FlowChecker.java \
	NodeScrambler.java \
	SignatureCleaner.java \
	SymbolReader.java \
	TranslationVisitor.java \
	TypeChecker.java

CLASSES = $(patsubst %.java, %.class, $(SOURCES))

include $(SOURCE)/Rules.mk

all: $(CLASSES)

clean clobber: cleanclasses
