SOURCE = ../..
PACKAGE = polyglot/visit
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	AddMemberVisitor.java \
	AlphaRenamer.java \
	AmbiguityRemover.java \
	AscriptionVisitor.java \
	CFGBuilder.java \
	ClassSerializer.java \
	CodeCleaner.java \
	ConstantFolder.java \
	ContextVisitor.java \
	DataFlow.java \
	DumpAst.java \
	ErrorHandlingVisitor.java \
	ExceptionChecker.java \
	ExitChecker.java \
	FlattenVisitor.java \
	FlowGraph.java \
	HaltingVisitor.java \
	InitChecker.java \
	NodeScrambler.java \
	NodeVisitor.java \
	PrettyPrinter.java \
	ReachChecker.java \
	StringPrettyPrinter.java \
	Translator.java \
	TypeBuilder.java \
	TypeChecker.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES)
JAR_FILE = polyglot.jar

all: $(CLASSES)

clean clobber: cleanclasses

