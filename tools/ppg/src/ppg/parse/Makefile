SOURCE = ../../../..
PACKAGE = polyglot/util/ppg/parse
SUBDIRS = 

include $(SOURCE)/Rules.mk

SOURCES = \
	Parser.java \
	Constant.java \
	ParserError.java \
	Unparse.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))
MANIFEST = Makefile $(SOURCES) ppg.cup
JAR_FILE = polyglot.jar

Parser.java Constant.java: ppg.cup
	java -cp $(CUP_RUNTIME) java_cup.Main -parser Parser -symbols Constant < ppg.cup
	$(MAKE) -C ../lex Lexer.java

all: $(CLASSES)
	$(subdirs)

clean clobber: cleanclasses
	-rm -f Constant.java Parser.java
	$(subdirs)


