SOURCE = ../..
PACKAGE = jltools/visit
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	AmbiguityRemover.java \
	BaseVisitor.java \
	BlockFlattener.java \
	ClassSerializer.java \
	CodeCleaner.java \
	ConstantFolder.java \
	DumpAst.java \
	ExceptionChecker.java \
	FlattenVisitor.java \
	FlowChecker.java \
	NodeScrambler.java \
	SemanticVisitor.java \
	TypeBuilder.java \
	AddMemberVisitor.java \
	Translator.java \
	TypeChecker.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

release: release_src

clean clobber: cleanclasses
