package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.Flags;
import jltools.types.Package;
import jltools.types.Type;
import jltools.types.Qualifier;
import jltools.util.*;
import java.util.*;

/**
 * A <code>NodeFactory</code> constructs AST nodes.  All node construction
 * should go through this factory or by done with the <code>copy()</code>
 * method of <code>Node</code>.
 */
public class NodeFactory_c implements NodeFactory
{
    public Disamb disamb() {
        return new Disamb_c();
    }

    public AmbPrefix AmbPrefix(Position pos, String name) {
	return AmbPrefix(pos, null, name);
    }

    public AmbPrefix AmbPrefix(Position pos, Prefix prefix, String name) {
	return new AmbPrefix_c(new Del_c(), pos, prefix, name);
    }

    public AmbReceiver AmbReceiver(Position pos, String name) {
	return AmbReceiver(pos, null, name);
    }

    public AmbReceiver AmbReceiver(Position pos, Prefix prefix, String name) {
	return new AmbReceiver_c(new Del_c(), pos, prefix, name);
    }

    public AmbQualifierNode AmbQualifierNode(Position pos, String name) {
	return AmbQualifierNode(pos, null, name);
    }

    public AmbQualifierNode AmbQualifierNode(Position pos, QualifierNode qualifier, String name) {
	return new AmbQualifierNode_c(new Del_c(), pos, qualifier, name);
    }

    public AmbExpr AmbExpr(Position pos, String name) {
	return new AmbExpr_c(new Del_c(), pos, name);
    }

    public AmbTypeNode AmbTypeNode(Position pos, String name) {
        return AmbTypeNode(pos, null, name);
    }

    public AmbTypeNode AmbTypeNode(Position pos, QualifierNode qualifier, String name) {
	return new AmbTypeNode_c(new Del_c(), pos, qualifier, name);
    }

    public ArrayAccess ArrayAccess(Position pos, Expr base, Expr index) {
	return new ArrayAccess_c(new Del_c(), pos, base, index);
    }

    public ArrayInit ArrayInit(Position pos) {
	return new ArrayInit_c(new Del_c(), pos, Collections.EMPTY_LIST);
    }

    public ArrayInit ArrayInit(Position pos, List elements) {
	return new ArrayInit_c(new Del_c(), pos, elements);
    }

    public Assign Assign(Position pos, Expr left, Assign.Operator op, Expr right) {
	return new Assign_c(new Del_c(), pos, left, op, right);
    }

    public Binary Binary(Position pos, Expr left, Binary.Operator op, Expr right) {
	return new Binary_c(new Del_c(), pos, left, op, right);
    }

    public Block Block(Position pos) {
	return new Block_c(new Del_c(), pos, Collections.EMPTY_LIST);
    }

    public Block Block(Position pos, Stmt s1) {
        List l = new ArrayList(1);
	l.add(s1);
	return Block(pos, l);
    }

    public Block Block(Position pos, Stmt s1, Stmt s2) {
        List l = new ArrayList(2);
	l.add(s1);
	l.add(s2);
	return Block(pos, l);
    }

    public Block Block(Position pos, Stmt s1, Stmt s2, Stmt s3) {
        List l = new ArrayList(3);
	l.add(s1);
	l.add(s2);
	l.add(s3);
	return Block(pos, l);
    }

    public Block Block(Position pos, Stmt s1, Stmt s2, Stmt s3, Stmt s4) {
        List l = new ArrayList(4);
	l.add(s1);
	l.add(s2);
	l.add(s3);
	l.add(s4);
	return Block(pos, l);
    }

    public Block Block(Position pos, List statements) {
	return new Block_c(new Del_c(), pos, statements);
    }

    public SwitchBlock SwitchBlock(Position pos, List statements) {
	return new SwitchBlock_c(new Del_c(), pos, statements);
    }

    public BooleanLit BooleanLit(Position pos, boolean value) {
	return new BooleanLit_c(new Del_c(), pos, value);
    }

    public Branch Break(Position pos) {
	return Branch(pos, Branch.BREAK, null);
    }

    public Branch Break(Position pos, String label) {
	return Branch(pos, Branch.BREAK, label);
    }

    public Branch Continue(Position pos) {
	return Branch(pos, Branch.CONTINUE, null);
    }

    public Branch Continue(Position pos, String label) {
	return Branch(pos, Branch.CONTINUE, label);
    }

    public Branch Branch(Position pos, Branch.Kind kind) {
	return Branch(pos, kind, null);
    }

    public Branch Branch(Position pos, Branch.Kind kind, String label) {
	return new Branch_c(new Del_c(), pos, kind, label);
    }

    public Call Call(Position pos, String name) {
	return Call(pos, null, name, Collections.EMPTY_LIST);
    }

    public Call Call(Position pos, String name, Expr a1) {
        List l = new ArrayList(1);
	l.add(a1);
	return Call(pos, null, name, l);
    }

    public Call Call(Position pos, String name, Expr a1, Expr a2) {
        List l = new ArrayList(2);
	l.add(a1);
	l.add(a2);
	return Call(pos, null, name, l);
    }

    public Call Call(Position pos, String name, Expr a1, Expr a2, Expr a3) {
        List l = new ArrayList(3);
	l.add(a1);
	l.add(a2);
	l.add(a3);
	return Call(pos, null, name, l);
    }

    public Call Call(Position pos, String name, Expr a1, Expr a2, Expr a3, Expr a4) {
        List l = new ArrayList(4);
	l.add(a1);
	l.add(a2);
	l.add(a3);
	l.add(a4);
	return Call(pos, null, name, l);
    }

    public Call Call(Position pos, String name, List args) {
        return Call(pos, null, name, args);
    }

    public Call Call(Position pos, Receiver target, String name) {
	return Call(pos, target, name, Collections.EMPTY_LIST);
    }

    public Call Call(Position pos, Receiver target, String name, Expr a1) {
        List l = new ArrayList(1);
	l.add(a1);
	return Call(pos, target, name, l);
    }

    public Call Call(Position pos, Receiver target, String name, Expr a1, Expr a2) {
        List l = new ArrayList(2);
	l.add(a1);
	l.add(a2);
	return Call(pos, target, name, l);
    }

    public Call Call(Position pos, Receiver target, String name, Expr a1, Expr a2, Expr a3) {
        List l = new ArrayList(3);
	l.add(a1);
	l.add(a2);
	l.add(a3);
	return Call(pos, target, name, l);
    }

    public Call Call(Position pos, Receiver target, String name, Expr a1, Expr a2, Expr a3, Expr a4) {
	List l = new ArrayList(4);
	l.add(a1);
	l.add(a2);
	l.add(a3);
	l.add(a4);
	return Call(pos, target, name, l);
    }

    public Call Call(Position pos, Receiver target, String name, List args) {
	return new Call_c(new Del_c(), pos, target, name, args);
    }

    public Case Default(Position pos) {
	return Case(pos, null);
    }

    public Case Case(Position pos, Expr expr) {
	return new Case_c(new Del_c(), pos, expr);
    }

    public Cast Cast(Position pos, TypeNode type, Expr expr) {
	return new Cast_c(new Del_c(), pos, type, expr);
    }

    public Catch Catch(Position pos, Formal formal, Block body) {
	return new Catch_c(new Del_c(), pos, formal, body);
    }

    public CharLit CharLit(Position pos, char value) {
	return new CharLit_c(new Del_c(), pos, value);
    }

    public ClassBody ClassBody(Position pos, List members) {
	return new ClassBody_c(new Del_c(), pos, members);
    }

    public ClassDecl ClassDecl(Position pos, Flags flags, String name, TypeNode superClass, List interfaces, ClassBody body) {
	return new ClassDecl_c(new Del_c(), pos, flags, name, superClass, interfaces, body);
    }

    public Conditional Conditional(Position pos, Expr cond, Expr consequent, Expr alternative) {
	return new Conditional_c(new Del_c(), pos, cond, consequent, alternative);
    }

    public ConstructorCall ThisCall(Position pos, List args) {
	return ConstructorCall(pos, ConstructorCall.THIS, null, args);
    }

    public ConstructorCall ThisCall(Position pos, Expr outer, List args) {
	return ConstructorCall(pos, ConstructorCall.THIS, outer, args);
    }

    public ConstructorCall SuperCall(Position pos, List args) {
	return ConstructorCall(pos, ConstructorCall.SUPER, null, args);
    }

    public ConstructorCall SuperCall(Position pos, Expr outer, List args) {
	return ConstructorCall(pos, ConstructorCall.SUPER, outer, args);
    }

    public ConstructorCall ConstructorCall(Position pos, ConstructorCall.Kind kind, List args) {
	return ConstructorCall(pos, kind, null, args);
    }

    public ConstructorCall ConstructorCall(Position pos, ConstructorCall.Kind kind, Expr outer, List args) {
	return new ConstructorCall_c(new Del_c(), pos, kind, outer, args);
    }

    public ConstructorDecl ConstructorDecl(Position pos, Flags flags, String name, List formals, List exceptionTypes, Block body) {
	return new ConstructorDecl_c(new Del_c(), pos, flags, name, formals, exceptionTypes, body);
    }

    public FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type, String name) {
	return FieldDecl(pos, flags, type, name, null);
    }

    public FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type, String name, Expr init) {
	return new FieldDecl_c(new Del_c(), pos, flags, type, name, init);
    }

    public Do Do(Position pos, Stmt body, Expr cond) {
	return new Do_c(new Del_c(), pos, body, cond);
    }

    public Empty Empty(Position pos) {
	return new Empty_c(new Del_c(), pos);
    }

    public Eval Eval(Position pos, Expr expr) {
	return new Eval_c(new Del_c(), pos, expr);
    }

    public Field Field(Position pos, String name) {
	return Field(pos, null, name);
    }

    public Field Field(Position pos, Receiver target, String name) {
	return new Field_c(new Del_c(), pos, target, name);
    }

    public FloatLit FloatLit(Position pos, FloatLit.Kind kind, double value) {
	return new FloatLit_c(new Del_c(), pos, kind, value);
    }

    public For For(Position pos, List inits, Expr cond, List iters, Stmt body) {
	return new For_c(new Del_c(), pos, inits, cond, iters, body);
    }

    public Formal Formal(Position pos, Flags flags, TypeNode type, String name) {
	return new Formal_c(new Del_c(), pos, flags, type, name);
    }

    public If If(Position pos, Expr cond, Stmt consequent) {
	return If(pos, cond, consequent, null);
    }

    public If If(Position pos, Expr cond, Stmt consequent, Stmt alternative) {
	return new If_c(new Del_c(), pos, cond, consequent, alternative);
    }

    public Import Import(Position pos, Import.Kind kind, String name) {
	return new Import_c(new Del_c(), pos, kind, name);
    }

    public Initializer Initializer(Position pos, Flags flags, Block body) {
	return new Initializer_c(new Del_c(), pos, flags, body);
    }

    public Instanceof Instanceof(Position pos, Expr expr, TypeNode type) {
	return new Instanceof_c(new Del_c(), pos, expr, type);
    }

    public IntLit IntLit(Position pos, long value) {
	return new IntLit_c(new Del_c(), pos, value);
    }

    public Labeled Labeled(Position pos, String label, Stmt body) {
	return new Labeled_c(new Del_c(), pos, label, body);
    }

    public Local Local(Position pos, String name) {
	return new Local_c(new Del_c(), pos, name);
    }

    public LocalClassDecl LocalClassDecl(Position pos, ClassDecl decl) {
	return new LocalClassDecl_c(new Del_c(), pos, decl);
    }

    public LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type, String name) {
        return LocalDecl(pos, flags, type, name, null);
    }

    public LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type, String name, Expr init) {
	return new LocalDecl_c(new Del_c(), pos, flags, type, name, init);
    }

    public MethodDecl MethodDecl(Position pos, Flags flags, TypeNode returnType, String name, List formals, List exceptionTypes, Block body) {
	return new MethodDecl_c(new Del_c(), pos, flags, returnType, name, formals, exceptionTypes, body);
    }

    public New New(Position pos, TypeNode type, List args) {
        return New(pos, null, type, args, null);
    }

    public New New(Position pos, TypeNode type, List args, ClassBody body) {
	return New(pos, null, type, args, body);
    }

    public New New(Position pos, Expr outer, TypeNode objectType, List args) {
        return New(pos, outer, objectType, args, null);
    }

    public New New(Position pos, Expr outer, TypeNode objectType, List args, ClassBody body) {
        return new New_c(new Del_c(), pos, outer, objectType, args, body);
    }

    public NewArray NewArray(Position pos, TypeNode base, List dims) {
	return NewArray(pos, base, dims, 0);
    }

    public NewArray NewArray(Position pos, TypeNode base, List dims, int addDims) {
	return new NewArray_c(new Del_c(), pos, base, dims, addDims, null);
    }

    public NewArray NewArray(Position pos, TypeNode base, int addDims, ArrayInit init) {
	return new NewArray_c(new Del_c(), pos, base, Collections.EMPTY_LIST, addDims, init);
    }

    public NullLit NullLit(Position pos) {
	return new NullLit_c(new Del_c(), pos);
    }

    public Return Return(Position pos) {
	return Return(pos, null);
    }

    public Return Return(Position pos, Expr expr) {
	return new Return_c(new Del_c(), pos, expr);
    }

    public SourceCollection SourceCollection(Position pos, List sources) {
        return new SourceCollection_c(new Del_c(), pos,  sources);
    }

    public SourceFile SourceFile(Position pos, List decls) {
        return SourceFile(pos, null, Collections.EMPTY_LIST, decls);
    }

    public SourceFile SourceFile(Position pos, List imports, List decls) {
        return SourceFile(pos, null, imports, decls);
    }

    public SourceFile SourceFile(Position pos, PackageNode packageName, List imports, List decls) {
	return new SourceFile_c(new Del_c(), pos, packageName, imports, decls);
    }

    public Special This(Position pos) {
        return Special(pos, Special.THIS, null);
    }

    public Special This(Position pos, TypeNode outer) {
        return Special(pos, Special.THIS, outer);
    }

    public Special Super(Position pos) {
        return Special(pos, Special.SUPER, null);
    }

    public Special Super(Position pos, TypeNode outer) {
        return Special(pos, Special.SUPER, outer);
    }

    public Special Special(Position pos, Special.Kind kind) {
        return Special(pos, kind, null);
    }

    public Special Special(Position pos, Special.Kind kind, TypeNode outer) {
	return new Special_c(new Del_c(), pos, kind, outer);
    }

    public StringLit StringLit(Position pos, String value) {
	return new StringLit_c(new Del_c(), pos, value);
    }

    public Switch Switch(Position pos, Expr expr, List elements) {
	return new Switch_c(new Del_c(), pos, expr, elements);
    }

    public Synchronized Synchronized(Position pos, Expr expr, Block body) {
	return new Synchronized_c(new Del_c(), pos, expr, body);
    }

    public Throw Throw(Position pos, Expr expr) {
	return new Throw_c(new Del_c(), pos, expr);
    }

    public Try Try(Position pos, Block tryBlock, List catchBlocks) {
        return Try(pos, tryBlock, catchBlocks, null);
    }

    public Try Try(Position pos, Block tryBlock, List catchBlocks, Block finallyBlock) {
	return new Try_c(new Del_c(), pos, tryBlock, catchBlocks, finallyBlock);
    }

    public ArrayTypeNode ArrayTypeNode(Position pos, TypeNode base) {
        return new ArrayTypeNode_c(new Del_c(), pos, base);
    }

    public CanonicalTypeNode CanonicalTypeNode(Position pos, Type type) {
        if (! type.isCanonical()) {
	    throw new InternalCompilerError("Cannot construct a canonical " +
		"type node for a non-canonical type.");
	}

        return new CanonicalTypeNode_c(new Del_c(), pos, type);
    }

    /*
    public TypeNode TypeNode(Position pos, Type type) {
        if (type.isCanonical()) {
	    return CanonicalTypeNode(pos, type);
	}

	if (type.isArray()) {
	    return ArrayTypeNode(pos, TypeNode(pos, type.toArray().base()));
	}

	throw new InternalCompilerError(pos, "Could not construct node " +
	    				"for unrecognized type " + type);
    }
    */

    /*
    protected QualifierNode QualifierNode(Position pos, Qualifier qualifier) {
        if (qualifier.isType()) {
	    return TypeNode(pos, qualifier.toType());
	}
	else if (qualifier.isPackage()) {
	    return PackageNode(pos, qualifier.toPackage());
	}

	throw new InternalCompilerError(pos, "Could not construct node " +
	    				"for unrecognized type qualifier " +
					qualifier);
    }
    */

    public PackageNode PackageNode(Position pos, Package p) {
	return new PackageNode_c(new Del_c(), pos, p);
    }

    public Unary Unary(Position pos, Unary.Operator op, Expr expr) {
	return new Unary_c(new Del_c(), pos, op, expr);
    }

    public Unary Unary(Position pos, Expr expr, Unary.Operator op) {
	return new Unary_c(new Del_c(), pos, op, expr);
    }

    public While While(Position pos, Expr cond, Stmt body) {
	return new While_c(new Del_c(), pos, cond, body);
    }
}
