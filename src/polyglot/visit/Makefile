
vs_package	= jltools/visit
vs_codebase	= ../..
vs_files 	= SymbolReader.java \
		  AmbiguityRemover.java \
		  TypeChecker.java \
		  ExceptionChecker.java \
		  FlowChecker.java \
		  DumpAst.java \
		  NodeScrambler.java

VISIT_TARGET = $(patsubst %.java, $(vs_package)/%.class, $(vs_files))

ifndef CALLED_FROM_PARENT
all:	
	cd ../.. && $(MAKE) visit
%:
	cd ../.. && $(MAKE) $@
endif
