SOURCE = ../..
PACKAGE = jltools.lex
SUBDIRS =

SOURCES = \
	BooleanLiteral.java \
	CharacterLiteral.java \
	Comment.java \
	DocumentationComment.java \
	DoubleLiteral.java \
	EOF.java \
	EndOfLineComment.java \
	EscapedUnicodeReader.java \
	FloatLiteral.java \
	Identifier.java \
	InputElement.java \
	IntegerLiteral.java \
	Keyword.java \
	Lexer.java \
	Literal.java \
	LongLiteral.java \
	NullLiteral.java \
	NumericLiteral.java \
	Operator.java \
	Separator.java \
	StringLiteral.java \
	Sym.java \
	Token.java \
	TraditionalComment.java \
	WhiteSpace.java

CLASSES = $(patsubst %.java, %.class, $(SOURCES))

include $(SOURCE)/Rules.mk

all: $(CLASSES)

clean clobber: cleanclasses
