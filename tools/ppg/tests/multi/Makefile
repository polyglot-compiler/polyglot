SOURCE = ../../../../..
PACKAGE = polyglot/util/ppg/test/multi
SUBDIRS = 

include $(SOURCE)/Rules.mk

Lexer_c.java: multi.flex
	java -cp $(SOURCE)/jlex.jar JFlex.Main multi.flex

Parser.java Constant.java: out.cup
	java -cp $(SOURCE)/java_cup.jar java_cup.Main -parser Parser -symbols Constant < out.cup

out.cup: multi.ppg multi.cup
	$(SOURCE)/bin/ppg -symbols Constant multi.ppg > out.cup


SOURCES = \
	Constant.java \
	LexerResult.java \
	LexicalError.java \
	Token.java \
	Expr.java \
	Lexer_c.java \
	ParenExpr.java \
	Parser.java \
	Tester.java \
	
CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)
	$(subdirs)

test:
	$(JAVA) -classpath $(CLASSPATH) polyglot.util.ppg.test.multi.Tester expr.txt

clean clobber: cleanclasses
	$(subdirs)
	rm -f out.cup Parser.java Constant.java Lexer_c.java Lexer_c.java~


MANIFEST = Makefile $(SOURCES)
