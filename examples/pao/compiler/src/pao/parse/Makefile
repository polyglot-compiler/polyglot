SOURCE = ../../../..
PACKAGE = polyglot/ext/pao/parse
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	Lexer.java \
	Grm.java \
	sym.java \
	PaoParserWrapper.java \
	PaoUtil.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

Grm.java sym.java: pao_ppg.cup
	$(yacc)

pao_ppg.cup: pao.ppg $(SOURCE)/polyglot/ext/jl/parse/java12.cup
	$(ppg) pao.ppg > pao_ppg.cup

Lexer.java: pao.flex sym.java
	$(flex)

clean: cleanclasses
	$(subdirs)

clobber: cleanclasses
	$(subdirs)
	-rm -f Grm.java sym.java Lexer.java pao_ppg.cup




