
vs_package	= jltools/visit
vs_codebase	= ../..
vs_files 	= AmbiguityRemover.java \
		  DumpAst.java \
		  TypeChecker.java \
		  SymbolReader.java

VISIT_TARGET = $(patsubst %.java, $(vs_package)/%.class, $(vs_files))

ifndef CALLED_FROM_PARENT
all:	
	cd ../.. && $(MAKE) visit
%:
	cd ../.. && $(MAKE) $@
endif
