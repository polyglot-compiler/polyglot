SOURCE = ../..
PACKAGE = jltools/ast
SUBDIRS =

include $(SOURCE)/Rules.mk

SOURCES = \
	AmbExpr.java \
	Ambiguous.java \
	AmbPrefix.java \
	AmbQualifierNode.java \
	AmbReceiver.java \
	AmbTypeNode.java \
	ArrayAccess.java \
	ArrayInit.java \
	ArrayTypeNode.java \
	Assign.java \
	Binary.java \
	Block.java \
	BooleanLit.java \
	Branch.java \
	Call.java \
	CanonicalTypeNode.java \
	Case.java \
	Cast.java \
	Catch.java \
	CharLit.java \
	ClassBody.java \
	ClassDecl.java \
	ClassMember.java \
	Conditional.java \
	ConstructorCall.java \
	ConstructorDecl.java \
	Declarator.java \
    Disamb.java \
	Do.java \
	Empty.java \
	Eval.java \
	Expr.java \
	Ext.java \
	FieldDecl.java \
	Field.java \
	FloatLit.java \
	ForInit.java \
	For.java \
	Formal.java \
	ForUpdate.java \
	If.java \
	Import.java \
	Initializer.java \
	Instanceof.java \
	IntLit.java \
	Labeled.java \
	Lit.java \
	LocalClassDecl.java \
	LocalDecl.java \
	Local.java \
	Makefile \
	MethodDecl.java \
	NewArray.java \
	New.java \
	NodeFactory.java \
	Node.java \
	NodeVisitor.java \
	NullLit.java \
	NumLit.java \
	PackageNode.java \
	Precedence.java \
	Prefix.java \
	ProcedureDecl.java \
	QualifiedNew.java \
	QualifierNode.java \
	Receiver.java \
	Return.java \
	SourceFile.java \
	Special.java \
	Stmt.java \
	StringLit.java \
	SwitchBlock.java \
	SwitchElement.java \
	Switch.java \
	Synchronized.java \
	Throw.java \
	TopLevelDecl.java \
	Try.java \
	Typed.java \
	TypeNode.java \
	Unary.java \
	While.java

CLASSES = $(patsubst %.java, $(PACKAGEPATH)/%.class, $(SOURCES))

all: $(CLASSES)

clean clobber: cleanclasses

release: release_src
