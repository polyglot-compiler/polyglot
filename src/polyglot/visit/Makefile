
fe_package	= jltools/visit
fe_codebase	= ../..
fe_files 	= TypeChecker.java SymbolReader.java

FRONTEND_TARGET = $(patsubst %.java, $(fe_package)/%.class, $(fe_files))

ifndef CALLED_FROM_PARENT
all:	
	cd ../.. && $(MAKE) frontend
%:
	cd ../.. && $(MAKE) $@
endif
