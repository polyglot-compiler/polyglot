
ma_package	= jltools/main
ma_codebase	= ../..
ma_files 	= MainErrorQueueFactory.java \
		  MainTargetFactory.java \
		  Main.java

MAIN_TARGET = $(patsubst %.java, $(ma_package)/%.class, $(ma_files))

ifndef CALLED_FROM_PARENT
all:	
	cd ../.. && $(MAKE) main
%:
	cd ../.. && $(MAKE) $@
endif
