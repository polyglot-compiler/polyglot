SOURCE = ../..
PACKAGE = jltools/visit
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	AmbiguityRemover.java \
	BaseVisitor.java \
	ClassSerializer.java \
	ConstantFolder.java \
	DumpAst.java \
	ExceptionChecker.java \
	FlattenVisitor.java \
	FlowChecker.java \
	NodeScrambler.java \
	SemanticVisitor.java \
	TypeAmbiguityRemover.java \
	TypeBuilder.java \
	Translator.java \
	TypeChecker.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

clean clobber: cleanclasses
