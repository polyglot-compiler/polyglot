SOURCE = ../../../..
PACKAGE = polyglot/ext/jl/qq
SUBDIRS = 

include $(SOURCE)/Rules.mk

SOURCES = \
	Lexer_c.java \
	Grm.java \
	sym.java \
	QQ.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES) qq.ppg qq.flex
JAR_FILE = polyglot.jar

all: $(CLASSES)
	$(subdirs)

Grm.java sym.java: qq_ppg.cup
	$(yacc)
	 
qq_ppg.cup: qq.ppg $(SOURCE)/polyglot/ext/jl/parse/java12.cup
	$(ppg) qq.ppg > qq_ppg.cup

Lexer_c.java: qq.flex sym.java
	$(flex)

clean: cleanclasses
	$(subdirs)

clobber: cleanclasses
	$(subdirs)
	-rm -f Grm.java sym.java Lexer_c.java qq_ppg.cup
