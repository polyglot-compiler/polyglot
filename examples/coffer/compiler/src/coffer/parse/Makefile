SOURCE = ../../../..
PACKAGE = polyglot/ext/coffer/parse
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	Lexer.java \
	Grm.java \
	sym.java \
	$(SRC) \

SRC = \
	CofferParserWrapper.java \
	CofferUtil.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SRC) coffer.ppg coffer.flex
JAR_FILE = coffer.jar

all: $(CLASSES)

Grm.java sym.java: coffer_ppg.cup
	$(yacc)

coffer_ppg.cup: coffer.ppg $(SOURCE)/polyglot/ext/jl/parse/java12.cup
	$(ppg) coffer.ppg > coffer_ppg.cup

Lexer.java: coffer.flex sym.java
	$(flex)

clean: cleanclasses
	$(subdirs)

clobber: cleanclasses
	$(subdirs)
	-rm -f Grm.java sym.java Lexer.java coffer_ppg.cup

