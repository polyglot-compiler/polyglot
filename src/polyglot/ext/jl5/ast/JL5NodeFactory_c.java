package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.*;
import polyglot.ast.ConstructorCall.Kind;
import polyglot.qq.QQ;
import polyglot.types.Flags;
import polyglot.types.Type;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

/**
 * NodeFactory for jl5 extension.
 */
public class JL5NodeFactory_c extends NodeFactory_c implements JL5NodeFactory {
    // TODO:  Implement factory methods for new AST nodes.
	QQ qq;
	public JL5NodeFactory_c(QQ qq) {
	    super(new AbstractExtFactory_c() { }, new JL5DelFactory_c());
    	this.qq = qq;
    }
    
    public JL5NodeFactory_c(QQ qq, ExtFactory extFactory) {
        super(extFactory, new JL5DelFactory_c()); 
        this.qq = qq;
    }

    public JL5NodeFactory_c(QQ qq, ExtFactory extFactory, DelFactory delFactory) {
        super(extFactory, new JL5DelFactory_c(delFactory)); 
        this.qq = qq;
    }

    public QQ qq() {
        return qq;
    }

    @Override
    public CanonicalTypeNode CanonicalTypeNode(Position pos, Type type) {
        if (! type.isCanonical()) {
            throw new InternalCompilerError("Cannot construct a canonical " +
            "type node for a non-canonical type.");
        }

        CanonicalTypeNode n = new JL5CanonicalTypeNode_c(pos, type);
        n = (CanonicalTypeNode)n.ext(extFactory().extCanonicalTypeNode());
        n = (CanonicalTypeNode)n.del(delFactory().delCanonicalTypeNode());
        return n;
    }

    @Override
    public JL5EnumDecl EnumDecl(Position pos, Flags flags, Id name,
			TypeNode superType, List interfaces, ClassBody body) {
    	JL5EnumDecl n = new JL5EnumDecl_c(pos, flags, name, superType, interfaces, body);
        return n;

    }
    
    @Override
	public JL5ClassDecl ClassDecl(Position pos, Flags flags, Id name, TypeNode superType,  List interfaces, ClassBody body, List paramTypes ){
        JL5ClassDecl n = new JL5ClassDecl_c(pos, flags, name, superType, interfaces, body, paramTypes);
        return n;
    }

    @Override
	public ExtendedFor ExtendedFor(Position pos, LocalDecl decl, Expr expr, Stmt stmt){
    	return new ExtendedFor_c(pos, decl, expr, stmt);
    }
    
    @Override
	public EnumConstantDecl EnumConstantDecl(Position pos, Flags flags, Id name, List args, ClassBody body){
        EnumConstantDecl n = new EnumConstantDecl_c(pos, flags, name, args, body);
        return n;
    }
    
    @Override
	public EnumConstantDecl EnumConstantDecl(Position pos, Flags flags, Id name, List args){
        EnumConstantDecl n = new EnumConstantDecl_c(pos, flags, name, args, null);
        return n;
    }
    
    @Override
	public EnumConstant EnumConstant(Position pos, Receiver target, Id name){
        EnumConstant n = new EnumConstant_c(pos, target, name);
        return n;
    }

    @Override
    public Binary Binary(Position pos, Expr left, Binary.Operator op, Expr right) {
        Binary n = new JL5Binary_c(pos, left, op, right);
        n = (Binary)n.ext(extFactory().extBinary());
        n = (Binary)n.del(delFactory().delBinary());
        return n;
    }
    
    @Override
    public Unary Unary(Position pos, Unary.Operator op, Expr expr) {
        Unary n = new JL5Unary_c(pos, op, expr);
        n = (Unary)n.ext(extFactory().extUnary());
        n = (Unary)n.del(delFactory().delUnary());
        return n;
    }
    
    @Override
	public ConstructorCall ConstructorCall(Position pos, Kind kind, Expr outer,
			List args) {		
		return new JL5ConstructorCall_c(pos, kind, outer, args);
	}

	@Override
	public JL5ConstructorDecl ConstructorDecl(Position pos, Flags flags, Id name, List formals, List throwTypes, Block body){
    	return ConstructorDecl(pos, flags, name, formals, throwTypes, body, null);
    }
    @Override
	public JL5ConstructorDecl ConstructorDecl(Position pos, Flags flags, Id name, List formals, List throwTypes, Block body, List typeParams){
        JL5ConstructorDecl n;
        if (typeParams == null){
            n = new JL5ConstructorDecl_c(pos, flags, name, formals, throwTypes, body);
        }
        else {
            n = new JL5ConstructorDecl_c(pos, flags, name, formals, throwTypes, body, typeParams);
        }
        return n;
    }
    
    @Override
	public Disamb disamb(){
        return new JL5Disamb_c();
    }
    

    @Override
	public JL5MethodDecl MethodDecl(Position pos, Flags flags, TypeNode returnType, Id name, List formals, List throwTypes, Block body){
    	return MethodDecl(pos, flags, returnType, name, formals, throwTypes, body, null);
    }
    
    @Override
	public JL5MethodDecl MethodDecl(Position pos, Flags flags, TypeNode returnType, Id name, List formals, List throwTypes, Block body, List typeParams){
        JL5MethodDecl n;
        if (typeParams == null){
            n = new JL5MethodDecl_c(pos, flags, returnType, name, formals, throwTypes, body);
        }
        else {
            n = new JL5MethodDecl_c(pos, flags, returnType, name, formals, throwTypes, body, typeParams);
        }
        return n;
    }    

    @Override
	public ParamTypeNode ParamTypeNode(Position pos, List bounds, Id id){
        ParamTypeNode n = new ParamTypeNode_c(pos, bounds, id);
        return n;
    }

    @Override
    public AmbTypeInstantiation AmbTypeInstantiation(Position pos, TypeNode base, List typeArguments) {
        return new AmbTypeInstantiation(pos, base, typeArguments);
    }

    
    @Override
	public JL5Import Import(Position pos, Import.Kind kind, String name){
        JL5Import n = new JL5Import_c(pos, kind, name);
        return n;
    }
	@Override
	public Formal Formal(Position pos, Flags flags, TypeNode type,
			Id name) {
		return Formal(pos, flags, type, name, false);
	}
	@Override
	public Formal Formal(Position pos, Flags flags, TypeNode type,
			Id name, boolean varArgs) {
		return new JL5Formal_c(pos, flags, type, name, varArgs);
	}

	@Override
	public Switch Switch(Position pos, Expr expr, List elements) {
		return new JL5Switch_c(pos, expr, elements);
	}

	@Override
	public Case Case(Position pos, Expr expr) {
		return new JL5Case_c(pos, expr);
	}

    @Override
    public AmbWildCard AmbWildCard(Position pos) {
        return new AmbWildCard(pos);
    }

    @Override
    public AmbWildCard AmbWildCardExtends(Position pos,
            TypeNode extendsNode) {
        return new AmbWildCard(pos, extendsNode, true);
    }

    @Override
    public AmbWildCard AmbWildCardSuper(Position pos,
            TypeNode superNode) {
        return new AmbWildCard(pos, superNode, false);
    }

    @Override
    public Call Call(Position pos, Receiver target, Id name, List args) {
        return Call(pos, target, null, name, args);
    }
    @Override
    public Call Call(Position pos, Receiver target, List typeArgs, Id name, List args) {
        Call n = new JL5Call_c(pos, target, CollectionUtil.nonNullList(typeArgs), name, CollectionUtil.nonNullList(args));
        n = (Call)n.ext(extFactory().extCall());
        n = (Call)n.del(delFactory().delCall());
        return n;
    }

    @Override
    public polyglot.ast.New New(Position pos, List<TypeNode> typeArgs, TypeNode type, List args, polyglot.ast.ClassBody body) {
        return this.New(pos, null, typeArgs, type, args, body);
    }

    @Override
    public polyglot.ast.New New(Position pos, 
            Expr outer, List<TypeNode> typeArgs, TypeNode objectType, List args,
            polyglot.ast.ClassBody body) {
        New n = new JL5New_c(pos, outer, CollectionUtil.nonNullList(typeArgs), objectType, CollectionUtil.nonNullList(args), body);
        n = (New)n.ext(extFactory().extNew());
        n = (New)n.del(delFactory().delNew());
        return n;
    }	
    @Override
    public New New(Position pos, Expr outer, TypeNode objectType, List args, ClassBody body) {
        return this.New(pos, outer, null, objectType, args, body);
    }

}
