SOURCE = ../..
PACKAGE = jltools/lex
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	BooleanLiteral.java \
	CharacterLiteral.java \
	DoubleLiteral.java \
	EOF.java \
	EscapedUnicodeReader.java \
	FloatLiteral.java \
	Identifier.java \
	IntegerLiteral.java \
	Keyword.java \
	Literal.java \
	LongLiteral.java \
	NullLiteral.java \
	NumericLiteral.java \
	Operator.java \
	StringLiteral.java \
	Token.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

clean: cleanclasses cleanclasses
