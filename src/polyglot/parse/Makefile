
PACKAGE		= jltools/parse
CODEBASE	= ../..
LEAF		= .

all: Grm.java

include	$(CODEBASE)/Makefile


Grm.java: java12.cup
	java java_cup.Main -parser Grm < java12.cup
