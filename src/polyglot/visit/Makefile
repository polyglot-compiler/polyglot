SOURCE = ../..
PACKAGE = jltools/visit
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	AddMemberVisitor.java \
	AmbiguityRemover.java \
	BlockFlattener.java \
	ClassSerializer.java \
	CodeCleaner.java \
	ConstantFolder.java \
	DumpAst.java \
	ExceptionChecker.java \
	ExpectedTypeVisitor.java \
	FlattenVisitor.java \
	FlowChecker.java \
	NodeScrambler.java \
	NodeVisitor.java \
	PrettyPrinter.java \
	SemanticVisitor.java \
	StringPrettyPrinter.java \
	Translator.java \
	TypeBuilder.java \
	TypeChecker.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

release: release_src

clean clobber: cleanclasses
