SOURCE = ../../../..
PACKAGE = jltools/ext/jl/qq
SUBDIRS = 

include $(SOURCE)/Rules.mk

SOURCES = \
	Lexer.java \
	Grm.java \
	sym.java \
	QQ.java \
	QQParserWrapper.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)
	$(subdirs)

Grm.java sym.java: qq_jlgen.cup
	$(yacc)
	 
qq_jlgen.cup: qq.jlg $(SOURCE)/jltools/ext/jl/parse/java12.cup
	$(jlgen) qq.jlg > qq_jlgen.cup

Lexer.java: qq.flex sym.java
	$(flex)

clean: cleanclasses
	$(subdirs)

clobber: cleanclasses
	$(subdirs)
	-rm -f Grm.java sym.java Lexer.java qq_jlgen.cup

release: release_src
