
fe_package	= jltools/frontend
fe_codebase	= ../..
fe_files 	= Main.java Compiler.java

FRONTEND_TARGET = $(patsubst %.java, $(fe_package)/%.class, $(fe_files))

ifndef CALLED_FROM_PARENT
all:	
	cd ../.. && $(MAKE) frontend
%:
	cd ../.. && $(MAKE) $@
endif
