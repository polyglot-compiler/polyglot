SOURCE = ../..
PACKAGE = polyglot/visit
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	AddMemberVisitor.java \
	AmbiguityRemover.java \
	AscriptionVisitor.java \
	BlockFlattener.java \
	ClassSerializer.java \
	CodeCleaner.java \
	ConstantFolder.java \
	ContextVisitor.java \
	DumpAst.java \
	ExceptionChecker.java \
	FlattenVisitor.java \
	HaltingVisitor.java \
	FlowChecker.java \
	NodeScrambler.java \
	NodeVisitor.java \
	PrettyPrinter.java \
	StringPrettyPrinter.java \
	Translator.java \
	TypeBuilder.java \
	TypeChecker.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

release: release_src

clean clobber: cleanclasses
