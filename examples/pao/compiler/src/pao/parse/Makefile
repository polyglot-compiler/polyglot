SOURCE = ../../../..
PACKAGE = polyglot/ext/pao/parse
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	Lexer_c.java \
	Grm.java \
	sym.java \
	$(SRC)

SRC = \
	PaoParserWrapper.java \
	PaoUtil.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SRC) pao.ppg pao.flex
JAR_FILE = pao.jar

all: $(CLASSES)

Grm.java sym.java: pao_ppg.cup
	$(yacc)

pao_ppg.cup: pao.ppg $(SOURCE)/polyglot/ext/jl/parse/java12.cup
	$(ppg) pao.ppg > pao_ppg.cup

Lexer_c.java: pao.flex sym.java
	$(flex)

clean: cleanclasses
	$(subdirs)

clobber: cleanclasses
	$(subdirs)
	-rm -f Grm.java sym.java Lexer_c.java pao_ppg.cup

