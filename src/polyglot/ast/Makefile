SOURCE = ../..
PACKAGE = jltools/ast
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	AmbiguousExpression.java \
	AmbiguousName.java \
	AmbiguousNameExpression.java \
	AmbiguousNode.java \
	ArrayIndexExpression.java \
	ArrayInitializerExpression.java \
	BinaryExpression.java \
	BlockStatement.java \
	BooleanLiteral.java \
	BranchStatement.java \
	CastExpression.java \
	CatchBlock.java \
	CharacterLiteral.java \
	ClassDeclarationStatement.java \
	ClassMember.java \
	ClassNode.java \
	CleanupSignaturesOverride.java \
	ConstructorCallStatement.java \
	DoStatement.java \
	EmptyStatement.java \
	Expression.java \
	ExpressionStatement.java \
	ExtensionFactory.java \
	FieldExpression.java \
	FieldNode.java \
	FloatLiteral.java \
	ForStatement.java \
	FormalParameter.java \
	GlobalDeclaration.java \
	IfStatement.java \
	ImportNode.java \
	InitializerBlock.java \
	InstanceofExpression.java \
	IntLiteral.java \
	LabelledStatement.java \
	Literal.java \
	LocalVariableExpression.java \
	MethodExpression.java \
	MethodNode.java \
	NewArrayExpression.java \
	NewObjectExpression.java \
	Node.java \
	NodeVisitor.java \
	NullLiteral.java \
	NumericalLiteral.java \
	ReadSymbolsOverride.java \
	RemoveAmbiguitiesOverride.java \
	ReturnStatement.java \
	SourceFileNode.java \
	SpecialExpression.java \
	StandardExtensionFactory.java \
	Statement.java \
	StringLiteral.java \
	SwitchStatement.java \
	SynchronizedStatement.java \
	TernaryExpression.java \
	ThrowStatement.java \
	TranslateBodyOverride.java \
	TranslateOverride.java \
	TryStatement.java \
	TypeCheckOverride.java \
	TypeNode.java \
	UnaryExpression.java \
	VariableDeclarationStatement.java \
	WhileStatement.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

clean clobber: cleanclasses
