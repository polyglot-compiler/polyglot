package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.ast.Assert;
import polyglot.types.Flags;
import polyglot.types.Package;
import polyglot.types.Type;
import polyglot.types.Qualifier;
import polyglot.util.*;
import java.util.*;

/**
 * A <code>NodeFactory</code> constructs AST nodes.  All node construction
 * should go through this factory or by done with the <code>copy()</code>
 * method of <code>Node</code>.
 */
public class NodeFactory_c extends AbstractNodeFactory_c
{
    private ExtFactory extFactory;
    public NodeFactory_c() {
        // use an empty implementation of AbstractExtFactory_c, so we
        // don't need to do null checks
        this(new AbstractExtFactory_c() {}); 
    }
    protected NodeFactory_c(ExtFactory extFactory ) {
        this.extFactory = extFactory; 
    }
    
    protected ExtFactory extFactory() {
        return this.extFactory;
    }
    
    /**
     * Utility method to find an instance of an Extension Factory
     */
    protected final ExtFactory findExtFactInstance(Class c) {
        ExtFactory e = extFactory();
        while (e != null) {
            if (c.isInstance(e)) {
                // the factory e is an instance of the class c
                return e;
            }
            e = e.nextExtFactory();
        }
        return null;
    }

    public AmbPrefix AmbPrefix(Position pos, Prefix prefix, String name) {
        AmbPrefix n = new AmbPrefix_c(pos, prefix, name);
        n = (AmbPrefix)n.ext(extFactory.extAmbPrefix());
        return n;
    }

    public AmbReceiver AmbReceiver(Position pos, Prefix prefix, String name) {
        AmbReceiver n = new AmbReceiver_c(pos, prefix, name);
        n = (AmbReceiver)n.ext(extFactory.extAmbReceiver());
        return n;
    }

    public AmbQualifierNode AmbQualifierNode(Position pos, QualifierNode qualifier, String name) {
        AmbQualifierNode n = new AmbQualifierNode_c(pos, qualifier, name);
        n = (AmbQualifierNode)n.ext(extFactory.extAmbQualifierNode());
        return n;
    }

    public AmbExpr AmbExpr(Position pos, String name) {
        AmbExpr n = new AmbExpr_c(pos, name);
        n = (AmbExpr)n.ext(extFactory.extAmbExpr());
        return n;
    }

    public AmbTypeNode AmbTypeNode(Position pos, QualifierNode qualifier, String name) {
        AmbTypeNode n = new AmbTypeNode_c(pos, qualifier, name);
        n = (AmbTypeNode)n.ext(extFactory.extAmbTypeNode());
        return n;
    }

    public ArrayAccess ArrayAccess(Position pos, Expr base, Expr index) {
        ArrayAccess n = new ArrayAccess_c(pos, base, index);
        n = (ArrayAccess)n.ext(extFactory.extArrayAccess());
        return n;
    }

    public ArrayInit ArrayInit(Position pos, List elements) {
        ArrayInit n = new ArrayInit_c(pos, elements);
        n = (ArrayInit)n.ext(extFactory.extArrayInit());
        return n;
    }

    public Assert Assert(Position pos, Expr cond, Expr errorMessage) {
        Assert n = new Assert_c(pos, cond, errorMessage);
        n = (Assert)n.ext(extFactory.extAssert());
        return n;
    }

    public Assign Assign(Position pos, Expr left, Assign.Operator op, Expr right) {
        Assign n;
        if (left instanceof Local) {
            return LocalAssign(pos, (Local)left, op, right);
        }
        else if (left instanceof Field) {
            return FieldAssign(pos, (Field)left, op, right);
        }
        else if (left instanceof ArrayAccess) {
            return ArrayAccessAssign(pos, (ArrayAccess)left, op, right);
        }
        return AmbAssign(pos, left, op, right);
    }

    public LocalAssign LocalAssign(Position pos, Local left, Assign.Operator op, Expr right) {
        LocalAssign n = new LocalAssign_c(pos, left, op, right);
        n = (LocalAssign)n.ext(extFactory.extLocalAssign());
        return n;
    }
    public FieldAssign FieldAssign(Position pos, Field left, Assign.Operator op, Expr right) {
        FieldAssign n = new FieldAssign_c(pos, left, op, right);
        n = (FieldAssign)n.ext(extFactory.extFieldAssign());
        return n;
    }
    public ArrayAccessAssign ArrayAccessAssign(Position pos, ArrayAccess left, Assign.Operator op, Expr right) {
        ArrayAccessAssign n = new ArrayAccessAssign_c(pos, left, op, right);
        n = (ArrayAccessAssign)n.ext(extFactory.extArrayAccessAssign());
        return n;
    }
    public AmbAssign AmbAssign(Position pos, Expr left, Assign.Operator op, Expr right) {
        AmbAssign n = new AmbAssign_c(pos, left, op, right);
        n = (AmbAssign)n.ext(extFactory.extAmbAssign());
        return n;
    }


    public Binary Binary(Position pos, Expr left, Binary.Operator op, Expr right) {
        Binary n = new Binary_c(pos, left, op, right);
        n = (Binary)n.ext(extFactory.extBinary());
        return n;
    }

    public Block Block(Position pos, List statements) {
        Block n = new Block_c(pos, statements);
        n = (Block)n.ext(extFactory.extBlock());
        return n;
    }

    public SwitchBlock SwitchBlock(Position pos, List statements) {
        SwitchBlock n = new SwitchBlock_c(pos, statements);
        n = (SwitchBlock)n.ext(extFactory.extSwitchBlock());
        return n;
    }

    public BooleanLit BooleanLit(Position pos, boolean value) {
        BooleanLit n = new BooleanLit_c(pos, value);
        n = (BooleanLit)n.ext(extFactory.extBooleanLit());
        return n;
    }

    public Branch Branch(Position pos, Branch.Kind kind, String label) {
        Branch n = new Branch_c(pos, kind, label);
        n = (Branch)n.ext(extFactory.extBranch());
        return n;
    }

    public Call Call(Position pos, Receiver target, String name, List args) {
        Call n = new Call_c(pos, target, name, args);
        n = (Call)n.ext(extFactory.extCall());
        return n;
    }

    public Case Case(Position pos, Expr expr) {
        Case n = new Case_c(pos, expr);
        n = (Case)n.ext(extFactory.extCase());
        return n;
    }

    public Cast Cast(Position pos, TypeNode type, Expr expr) {
        Cast n = new Cast_c(pos, type, expr);
        n = (Cast)n.ext(extFactory.extCast());
        return n;
    }

    public Catch Catch(Position pos, Formal formal, Block body) {
        Catch n = new Catch_c(pos, formal, body);
        n = (Catch)n.ext(extFactory.extCatch());
        return n;
    }

    public CharLit CharLit(Position pos, char value) {
        CharLit n = new CharLit_c(pos, value);
        n = (CharLit)n.ext(extFactory.extCharLit());
        return n;
    }

    public ClassBody ClassBody(Position pos, List members) {
        ClassBody n = new ClassBody_c(pos, members);
        n = (ClassBody)n.ext(extFactory.extClassBody());
        return n;
    }

    public ClassDecl ClassDecl(Position pos, Flags flags, String name, TypeNode superClass, List interfaces, ClassBody body) {
        ClassDecl n = new ClassDecl_c(pos, flags, name, superClass, interfaces, body);
        n = (ClassDecl)n.ext(extFactory.extClassDecl());
        return n;
    }

    public Conditional Conditional(Position pos, Expr cond, Expr consequent, Expr alternative) {
        Conditional n = new Conditional_c(pos, cond, consequent, alternative);
        n = (Conditional)n.ext(extFactory.extConditional());
        return n;
    }

    public ConstructorCall ConstructorCall(Position pos, ConstructorCall.Kind kind, Expr outer, List args) {
        ConstructorCall n = new ConstructorCall_c(pos, kind, outer, args);
        n = (ConstructorCall)n.ext(extFactory.extConstructorCall());
        return n;
    }

    public ConstructorDecl ConstructorDecl(Position pos, Flags flags, String name, List formals, List throwTypes, Block body) {
        ConstructorDecl n = new ConstructorDecl_c(pos, flags, name, formals, throwTypes, body);
        n = (ConstructorDecl)n.ext(extFactory.extConstructorDecl());
        return n;
    }

    public FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type, String name, Expr init) {
        FieldDecl n = new FieldDecl_c(pos, flags, type, name, init);
        n = (FieldDecl)n.ext(extFactory.extFieldDecl());
        return n;
    }

    public Do Do(Position pos, Stmt body, Expr cond) {
        Do n = new Do_c(pos, body, cond);
        n = (Do)n.ext(extFactory.extDo());
        return n;
    }

    public Empty Empty(Position pos) {
        Empty n = new Empty_c(pos);
        n = (Empty)n.ext(extFactory.extEmpty());
        return n;
    }

    public Eval Eval(Position pos, Expr expr) {
        Eval n = new Eval_c(pos, expr);
        n = (Eval)n.ext(extFactory.extEval());
        return n;
    }

    public Field Field(Position pos, Receiver target, String name) {
        Field n = new Field_c(pos, target, name);
        n = (Field)n.ext(extFactory.extField());
        return n;
    }

    public FloatLit FloatLit(Position pos, FloatLit.Kind kind, double value) {
        FloatLit n = new FloatLit_c(pos, kind, value);
        n = (FloatLit)n.ext(extFactory.extFloatLit());
        return n;
    }

    public For For(Position pos, List inits, Expr cond, List iters, Stmt body) {
        For n = new For_c(pos, inits, cond, iters, body);
        n = (For)n.ext(extFactory.extFor());
        return n;
    }

    public Formal Formal(Position pos, Flags flags, TypeNode type, String name) {
        Formal n = new Formal_c(pos, flags, type, name);
        n = (Formal)n.ext(extFactory.extFormal());
        return n;
    }

    public If If(Position pos, Expr cond, Stmt consequent, Stmt alternative) {
        If n = new If_c(pos, cond, consequent, alternative);
        n = (If)n.ext(extFactory.extIf());
        return n;
    }

    public Import Import(Position pos, Import.Kind kind, String name) {
        Import n = new Import_c(pos, kind, name);
        n = (Import)n.ext(extFactory.extImport());
        return n;
    }

    public Initializer Initializer(Position pos, Flags flags, Block body) {
        Initializer n = new Initializer_c(pos, flags, body);
        n = (Initializer)n.ext(extFactory.extInitializer());
        return n;
    }

    public Instanceof Instanceof(Position pos, Expr expr, TypeNode type) {
        Instanceof n = new Instanceof_c(pos, expr, type);
        n = (Instanceof)n.ext(extFactory.extInstanceof());
        return n;
    }

    public IntLit IntLit(Position pos, IntLit.Kind kind, long value) {
        IntLit n = new IntLit_c(pos, kind, value);
        n = (IntLit)n.ext(extFactory.extIntLit());
        return n;
    }

    public Labeled Labeled(Position pos, String label, Stmt body) {
        Labeled n = new Labeled_c(pos, label, body);
        n = (Labeled)n.ext(extFactory.extLabeled());
        return n;
    }

    public Local Local(Position pos, String name) {
        Local n = new Local_c(pos, name);
        n = (Local)n.ext(extFactory.extLocal());
        return n;
    }

    public LocalClassDecl LocalClassDecl(Position pos, ClassDecl decl) {
        LocalClassDecl n = new LocalClassDecl_c(pos, decl);
        n = (LocalClassDecl)n.ext(extFactory.extLocalClassDecl());
        return n;
    }

    public LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type, String name, Expr init) {
        LocalDecl n = new LocalDecl_c(pos, flags, type, name, init);
        n = (LocalDecl)n.ext(extFactory.extLocalDecl());
        return n;
    }

    public MethodDecl MethodDecl(Position pos, Flags flags, TypeNode returnType, String name, List formals, List throwTypes, Block body) {
        MethodDecl n = new MethodDecl_c(pos, flags, returnType, name, formals, throwTypes, body);
        n = (MethodDecl)n.ext(extFactory.extMethodDecl());
        return n;
    }

    public New New(Position pos, Expr outer, TypeNode objectType, List args, ClassBody body) {
        New n = new New_c(pos, outer, objectType, args, body);
        n = (New)n.ext(extFactory.extNew());
        return n;
    }

    public NewArray NewArray(Position pos, TypeNode base, List dims, int addDims, ArrayInit init) {
        NewArray n = new NewArray_c(pos, base, dims, addDims, init);
        n = (NewArray)n.ext(extFactory.extNewArray());
        return n;
    }

    public NullLit NullLit(Position pos) {
        NullLit n = new NullLit_c(pos);
        n = (NullLit)n.ext(extFactory.extNullLit());
        return n;
    }

    public Return Return(Position pos, Expr expr) {
        Return n = new Return_c(pos, expr);
        n = (Return)n.ext(extFactory.extReturn());
        return n;
    }

    public SourceCollection SourceCollection(Position pos, List sources) {
        SourceCollection n = new SourceCollection_c(pos,  sources);
        n = (SourceCollection)n.ext(extFactory.extSourceCollection());
        return n;
    }

    public SourceFile SourceFile(Position pos, PackageNode packageName, List imports, List decls) {
        SourceFile n = new SourceFile_c(pos, packageName, imports, decls);
        n = (SourceFile)n.ext(extFactory.extSourceFile());
        return n;
    }

    public Special Special(Position pos, Special.Kind kind, TypeNode outer) {
        Special n = new Special_c(pos, kind, outer);
        n = (Special)n.ext(extFactory.extSpecial());
        return n;
    }

    public StringLit StringLit(Position pos, String value) {
        StringLit n = new StringLit_c(pos, value);
        n = (StringLit)n.ext(extFactory.extStringLit());
        return n;
    }

    public Switch Switch(Position pos, Expr expr, List elements) {
        Switch n = new Switch_c(pos, expr, elements);
        n = (Switch)n.ext(extFactory.extSwitch());
        return n;
    }

    public Synchronized Synchronized(Position pos, Expr expr, Block body) {
        Synchronized n = new Synchronized_c(pos, expr, body);
        n = (Synchronized)n.ext(extFactory.extSynchronized());
        return n;
    }

    public Throw Throw(Position pos, Expr expr) {
        Throw n = new Throw_c(pos, expr);
        n = (Throw)n.ext(extFactory.extThrow());
        return n;
    }

    public Try Try(Position pos, Block tryBlock, List catchBlocks, Block finallyBlock) {
        Try n = new Try_c(pos, tryBlock, catchBlocks, finallyBlock);
        n = (Try)n.ext(extFactory.extTry());
        return n;
    }

    public ArrayTypeNode ArrayTypeNode(Position pos, TypeNode base) {
        ArrayTypeNode n = new ArrayTypeNode_c(pos, base);
        n = (ArrayTypeNode)n.ext(extFactory.extArrayTypeNode());
        return n;
    }

    public CanonicalTypeNode CanonicalTypeNode(Position pos, Type type) {
        if (! type.isCanonical()) {
            throw new InternalCompilerError("Cannot construct a canonical " +
                "type node for a non-canonical type.");
        }

        CanonicalTypeNode n = new CanonicalTypeNode_c(pos, type);
        n = (CanonicalTypeNode)n.ext(extFactory.extCanonicalTypeNode());
        return n;
    }

    public PackageNode PackageNode(Position pos, Package p) {
        PackageNode n = new PackageNode_c(pos, p);
        n = (PackageNode)n.ext(extFactory.extPackageNode());
        return n;
    }

    public Unary Unary(Position pos, Unary.Operator op, Expr expr) {
        Unary n = new Unary_c(pos, op, expr);
        n = (Unary)n.ext(extFactory.extUnary());
        return n;
    }

    public While While(Position pos, Expr cond, Stmt body) {
        While n = new While_c(pos, cond, body);
        n = (While)n.ext(extFactory.extWhile());
        return n;
    }
}
