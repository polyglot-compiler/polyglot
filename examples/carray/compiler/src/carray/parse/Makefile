SOURCE = ../../../..
PACKAGE = polyglot/ext/carray/parse
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	Lexer.java \
	Grm.java \
	sym.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

Grm.java sym.java: carray_ppg.cup
	$(yacc)

Lexer.java: carray.flex sym.java
	$(flex)

carray_ppg.cup: carray.ppg $(SOURCE)/polyglot/ext/jl/parse/java12.cup
	$(ppg) carray.ppg > carray_ppg.cup

clean: cleanclasses
	$(subdirs)

clobber: cleanclasses
	$(subdirs)
	-rm -f Grm.java sym.java Lexer.java carray_ppg.cup

REL_SOURCES = \
	carray.ppg \
	carray.flex \
	sym.java \
	JLParserWrapper.java \
	JLUtil.java \
	Name.java \


