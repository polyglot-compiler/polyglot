SOURCE = ../..
PACKAGE = jltools.ast
SUBDIRS =

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
	ConstructorCallStatement.java \
	DoStatement.java \
	EmptyStatement.java \
	Expression.java \
	ExpressionStatement.java \
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
	ReturnStatement.java \
	SourceFileNode.java \
	SpecialExpression.java \
	Statement.java \
	StringLiteral.java \
	SwitchStatement.java \
	SynchronizedStatement.java \
	TernaryExpression.java \
	ThrowStatement.java \
	TryStatement.java \
	TypeNode.java \
	UnaryExpression.java \
	VariableDeclarationStatement.java \
	WhileStatement.java

CLASSES = $(patsubst %.java, %.class, $(SOURCES))

include $(SOURCE)/Rules.mk

all: $(CLASSES)

clean clobber:
	-rm -f *.class
