
ast_package = jltools/ast
ast_codebase= ../..
ast_files   =  AmbiguousExpression.java \
		   AmbiguousNameExpression.java \
			ArrayIndexExpression.java \
			ArrayInitializerExpression.java \
			BinaryExpression.java \
			BlockStatement.java \
			BranchStatement.java \
			CharacterLiteral.java \
			CastExpression.java \
			CatchBlock.java\
			ClassDeclarationStatement.java \
			ClassMember.java \
			ClassNode.java \
			ConstructorCallStatement.java \
			DoStatement.java \
			Expression.java \
			ExpressionStatement.java \
			FieldExpression.java \
			FieldNode.java \
			FloatLiteral.java \
			ForStatement.java \
			FormalParameter.java \
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
			ObjectPrimitiveCastExpression.java \
			ObjectPrimitiveInstanceofExpression.java \
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

AST_TARGET = $(patsubst %.java, $(ast_package)/%.class, $(ast_files))


ifndef CALLED_FROM_PARENT
all:	
	cd ../.. && $(MAKE) ast
%:
	cd ../.. && $(MAKE) $@
endif


