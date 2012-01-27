package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.*;
import polyglot.qq.QQ;
import polyglot.types.Flags;
import polyglot.util.Position;

/**
 * NodeFactory for jl5 extension.
 */
public interface JL5NodeFactory extends NodeFactory {
	QQ qq();
	JL5EnumDecl EnumDecl(Position pos, Flags flags, Id name,
			TypeNode superType, List interfaces, ClassBody body);

    EnumConstantDecl EnumConstantDecl(Position pos, Flags flags, Id name, List args, ClassBody body);
    
    EnumConstantDecl EnumConstantDecl(Position pos, Flags flags, Id name, List args);

    ParamTypeNode ParamTypeNode(Position pos, List bounds, Id id);
            
	JL5ClassDecl ClassDecl(Position pos, Flags flags, Id name,
			TypeNode superType, List interfaces, ClassBody body, List paramTypes);

	JL5ConstructorDecl ConstructorDecl(Position pos,
			Flags flags, Id name, List formals,
			List throwTypes, Block body, List typeParams);

	JL5MethodDecl MethodDecl(Position pos, Flags flags, TypeNode returnType,
			Id name, List formals, List throwTypes, Block body, List typeParams);
	    
    Formal Formal(Position pos, Flags flags, TypeNode type, Id name, boolean var_args);
 
	EnumConstant EnumConstant(Position pos, Receiver r, Id name);

	ExtendedFor ExtendedFor(Position pos, LocalDecl decl, Expr expr, Stmt stmt);
	
	AmbTypeInstantiation AmbTypeInstantiation(Position pos, TypeNode base, List typeArguments);
	
    AmbWildCard AmbWildCard(Position pos);
    AmbWildCard AmbWildCardExtends(Position pos, TypeNode extendsNode);
    AmbWildCard AmbWildCardSuper(Position pos, TypeNode superNode);
    
    Call Call(Position pos, Receiver target, List typeArgs, Id name, List args);
    New New(Position pos, List<TypeNode> typeArgs, TypeNode type, List args, ClassBody body);
    New New(Position pos, Expr outer, List<TypeNode> typeArgs, TypeNode objectType, List args, ClassBody body);

}

