
lex_package	= jltools/lex
lex_codebase	= ../..
lex_files 	=  BooleanLiteral.java \
		   	CharacterLiteral.java \
		   	Comment.java	\
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
			Token.java \
			TraditionalComment.java \
			WhiteSpace.java \

LEX_TARGET = $(patsubst %.java, $(lex_package)/%.class, $(lex_files))

ifndef CALLED_FROM_PARENT
all:	
	cd ../.. && $(MAKE) lex
%:
	cd ../.. && $(MAKE) $@
endif
