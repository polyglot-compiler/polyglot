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
	    super(new JL5ExtFactory_c() { }, new JL5DelFactory_c());
    	this.qq = qq;
    }
    
    public JL5NodeFactory_c(QQ qq, JL5ExtFactory extFactory) {
        super(extFactory, new JL5DelFactory_c()); 
        this.qq = qq;
    }

    public JL5NodeFactory_c(QQ qq, JL5ExtFactory extFactory, JL5DelFactory delFactory) {
        super(extFactory, new JL5DelFactory_c(delFactory)); 
        this.qq = qq;
    }

    public QQ qq() {
        return qq;
    }
    
    @Override
    public JL5ExtFactory extFactory() {
        return (JL5ExtFactory)super.extFactory();
    }

    @Override
    public JL5DelFactory delFactory() {
        return (JL5DelFactory)super.delFactory();
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
        n = (JL5EnumDecl)n.ext(extFactory().extEnumDecl());
        n = (JL5EnumDecl)n.del(delFactory().delEnumDecl());
        return n;

    }
    
    @Override
	public JL5ClassDecl ClassDecl(Position pos, Flags flags, Id name, TypeNode superType,  List interfaces, ClassBody body, List paramTypes ){
        JL5ClassDecl n = new JL5ClassDecl_c(pos, flags, name, superType, interfaces, body, paramTypes);
        n = (JL5ClassDecl)n.ext(extFactory().extClassDecl());
        n = (JL5ClassDecl)n.del(delFactory().delClassDecl());
        return n;
    }
    
    
    @Override
    public ClassBody ClassBody(Position pos, List members) {
        ClassBody n = new JL5ClassBody_c(pos, CollectionUtil.nonNullList(members));
        n = (ClassBody)n.ext(extFactory().extClassBody());
        n = (ClassBody)n.del(delFactory().delClassBody());
        return n;
    }

    @Override
	public ExtendedFor ExtendedFor(Position pos, LocalDecl decl, Expr expr, Stmt stmt){
    	ExtendedFor n = new ExtendedFor_c(pos, decl, expr, stmt);
        n = (ExtendedFor)n.ext(extFactory().extExtendedFor());
        n = (ExtendedFor)n.del(delFactory().delExtendedFor());
        return n;
    }
    
    @Override
	public EnumConstantDecl EnumConstantDecl(Position pos, Flags flags, Id name, List args, ClassBody body){
        EnumConstantDecl n = new EnumConstantDecl_c(pos, flags, name, args, body);
        n = (EnumConstantDecl)n.ext(extFactory().extEnumConstantDecl());
        n = (EnumConstantDecl)n.del(delFactory().delEnumConstantDecl());
        return n;
    }
    
    @Override
	public EnumConstantDecl EnumConstantDecl(Position pos, Flags flags, Id name, List args){
        EnumConstantDecl n = new EnumConstantDecl_c(pos, flags, name, args, null);
        n = (EnumConstantDecl)n.ext(extFactory().extEnumConstantDecl());
        n = (EnumConstantDecl)n.del(delFactory().delEnumConstantDecl());
        return n;
    }
    
    @Override
	public EnumConstant EnumConstant(Position pos, Receiver target, Id name){
        EnumConstant n = new EnumConstant_c(pos, target, name);
        n = (EnumConstant)n.ext(extFactory().extEnumConstant());
        n = (EnumConstant)n.del(delFactory().delEnumConstant());
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
        return ConstructorCall(pos, kind, outer, args, false);
    }
    @Override
    public ConstructorCall ConstructorCall(Position pos, Kind kind, Expr outer,
            List args, boolean isEnumConstructorCall) {

        ConstructorCall n = new JL5ConstructorCall_c(pos, kind, outer, args, isEnumConstructorCall);
        n = (ConstructorCall)n.ext(extFactory().extConstructorCall());
        n = (ConstructorCall)n.del(delFactory().delConstructorCall());
		return n;
	}

	@Override
	public JL5ConstructorDecl ConstructorDecl(Position pos, Flags flags, Id name, List formals, List throwTypes, Block body){
	    JL5ConstructorDecl n = ConstructorDecl(pos, flags, name, formals, throwTypes, body, null);
        n = (JL5ConstructorDecl)n.ext(extFactory().extConstructorDecl());
        n = (JL5ConstructorDecl)n.del(delFactory().delConstructorDecl());
        return n;
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
        n = (JL5ConstructorDecl)n.ext(extFactory().extConstructorDecl());
        n = (JL5ConstructorDecl)n.del(delFactory().delConstructorDecl());
        return n;
    }
    
    @Override
	public Disamb disamb(){
        Disamb n = new JL5Disamb_c();
        return n;
    }
    

    @Override
	public JL5MethodDecl MethodDecl(Position pos, Flags flags, TypeNode returnType, Id name, List formals, List throwTypes, Block body){
        JL5MethodDecl n = MethodDecl(pos, flags, returnType, name, formals, throwTypes, body, null);
        n = (JL5MethodDecl)n.ext(extFactory().extMethodDecl());
        n = (JL5MethodDecl)n.del(delFactory().delMethodDecl());
        return n;
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
        n = (JL5MethodDecl)n.ext(extFactory().extMethodDecl());
        n = (JL5MethodDecl)n.del(delFactory().delMethodDecl());
        return n;
    }    

    @Override
	public ParamTypeNode ParamTypeNode(Position pos, List bounds, Id id){
        ParamTypeNode n = new ParamTypeNode_c(pos, bounds, id);
        n = (polyglot.ext.jl5.ast.ParamTypeNode)n.ext(extFactory().extParamTypeNode());
        n = (polyglot.ext.jl5.ast.ParamTypeNode)n.del(delFactory().delParamTypeNode());
        return n;
    }

    @Override
    public AmbTypeInstantiation AmbTypeInstantiation(Position pos, TypeNode base, List typeArguments) {
        AmbTypeInstantiation n = new AmbTypeInstantiation(pos, base, typeArguments);
        return n;
    }

    
    @Override
	public JL5Import Import(Position pos, Import.Kind kind, String name){
        JL5Import n = new JL5Import_c(pos, kind, name);
        n = (JL5Import)n.ext(extFactory().extImport());
        n = (JL5Import)n.del(delFactory().delImport());
        return n;
    }
	@Override
	public Formal Formal(Position pos, Flags flags, TypeNode type,
			Id name) {
		Formal n = Formal(pos, flags, type, name, false);
        n = (Formal)n.ext(extFactory().extFormal());
        n = (Formal)n.del(delFactory().delFormal());
        return n;
	}
	@Override
	public Formal Formal(Position pos, Flags flags, TypeNode type,
			Id name, boolean varArgs) {
		Formal n = new JL5Formal_c(pos, flags, type, name, varArgs);
        n = (Formal)n.ext(extFactory().extFormal());
        n = (Formal)n.del(delFactory().delFormal());
        return n;
	}

	@Override
	public Switch Switch(Position pos, Expr expr, List elements) {
		Switch n = new JL5Switch_c(pos, expr, elements);
        n = (Switch)n.ext(extFactory().extSwitch());
        n = (Switch)n.del(delFactory().delSwitch());
        return n;
	}

	@Override
	public Case Case(Position pos, Expr expr) {
		Case n = new JL5Case_c(pos, expr);
        n = (Case)n.ext(extFactory().extCase());
        n = (Case)n.del(delFactory().delCase());
        return n;
	}

    @Override
    public AmbWildCard AmbWildCard(Position pos) {
        AmbWildCard n = new AmbWildCard(pos);
        return n;
    }

    @Override
    public AmbWildCard AmbWildCardExtends(Position pos,
            TypeNode extendsNode) {
        AmbWildCard n = new AmbWildCard(pos, extendsNode, true);
        return n;
    }

    @Override
    public AmbWildCard AmbWildCardSuper(Position pos,
            TypeNode superNode) {
        AmbWildCard n = new AmbWildCard(pos, superNode, false);
        return n;
    }

    @Override
    public Call Call(Position pos, Receiver target, Id name, List args) {
        Call n = Call(pos, target, null, name, args);
        n = (Call)n.ext(extFactory().extCall());
        n = (Call)n.del(delFactory().delCall());
        return n;
    }
    @Override
    public Call Call(Position pos, Receiver target, List typeArgs, Id name, List args) {
        Call n = new JL5Call_c(pos, target, CollectionUtil.nonNullList(typeArgs), name, CollectionUtil.nonNullList(args));
        n = (Call)n.ext(extFactory().extCall());
        n = (Call)n.del(delFactory().delCall());
        return n;
    }

    @Override
    public New New(Position pos, List<TypeNode> typeArgs, TypeNode type, List args, ClassBody body) {
        New n = this.New(pos, null, typeArgs, type, args, body);
        n = (New)n.ext(extFactory().extNew());
        n = (New)n.del(delFactory().delNew());
        return n;
    }

    @Override
    public New New(Position pos, 
            Expr outer, List<TypeNode> typeArgs, TypeNode objectType, List args,
            ClassBody body) {
        New n = new JL5New_c(pos, outer, CollectionUtil.nonNullList(typeArgs), objectType, CollectionUtil.nonNullList(args), body);
        n = (New)n.ext(extFactory().extNew());
        n = (New)n.del(delFactory().delNew());
        return n;
    }	
    @Override
    public New New(Position pos, Expr outer, TypeNode objectType, List args, ClassBody body) {
        New n = this.New(pos, outer, null, objectType, args, body);
        n = (New)n.ext(extFactory().extNew());
        n = (New)n.del(delFactory().delNew());
        return n;
    }

    @Override
    public Field Field(Position pos, Receiver target, Id name) {
        Field n = new JL5Field_c(pos, target, name);
        n = (Field)n.ext(extFactory().extField());
        n = (Field)n.del(delFactory().delField());
        return n;
    }

    
}
