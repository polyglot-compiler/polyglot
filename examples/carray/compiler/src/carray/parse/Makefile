SOURCE = ../../../..
PACKAGE = jltools/ext/carray/parse
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	Lexer.java \
	Grm.java \
	sym.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

Grm.java sym.java: carray_jlgen.cup
	$(yacc)

Lexer.java: carray.flex sym.java
	$(flex)

carray_jlgen.cup: carray.jlg $(SOURCE)/jltools/ext/jl/parse/java12.cup
	$(jlgen) carray.jlg > carray_jlgen.cup

clean: cleanclasses
	$(subdirs)

clobber: cleanclasses
	$(subdirs)
	-rm -f Grm.java sym.java Lexer.java carray_jlgen.cup

REL_SOURCES = \
	carray.jlg \
	carray.flex \
	sym.java \
	JLParserWrapper.java \
	JLUtil.java \
	Name.java \


