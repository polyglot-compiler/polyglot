SOURCE = ../../../../..
PACKAGE = jltools/util/jlgen/test/multi
SUBDIRS = 

include $(SOURCE)/Rules.mk

Lexer.java: multi.flex
	java -cp $(SOURCE)/jlex.jar JFlex.Main multi.flex

Parser.java Constant.java: out.cup
	java -cp $(SOURCE)/java_cup.jar java_cup.Main -parser Parser -symbols Constant < out.cup

out.cup: multi.jlg multi.cup
	$(SOURCE)/bin/jlgen multi.jlg > out.cup


SOURCES = \
	Constant.java \
	LexerResult.java \
	LexicalError.java \
	Token.java \
	Expr.java \
	Lexer.java \
	ParenExpr.java \
	Parser.java \
	Tester.java \
	
CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)
	$(subdirs)

test:
	java -classpath $(SOURCE)/java_cup.jar:$(OUTPUT) jltools.util.jlgen.test.multi.Tester expr.txt


release: release_src
	$(subdirs)

clean clobber: cleanclasses
	$(subdirs)
	rm -f out.cup Parser.java Constant.java Lexer.java Lexer.java~