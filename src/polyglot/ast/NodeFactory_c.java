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
        this(null); 
    }
    public NodeFactory_c(ExtFactory extFactory ) {
        this.extFactory = extFactory; 
    }
    
    protected ExtFactory extFactory() {
        return this.extFactory;
    }
    
    public AmbPrefix AmbPrefix(Position pos, Prefix prefix, String name) {
        AmbPrefix n = new AmbPrefix_c(pos, prefix, name);
        if (extFactory != null) {
            n = (AmbPrefix)n.ext(extFactory.extAmbPrefix());
        }
        return n;
    }

    public AmbReceiver AmbReceiver(Position pos, Prefix prefix, String name) {
        AmbReceiver n = new AmbReceiver_c(pos, prefix, name);
        if (extFactory != null) {
            n = (AmbReceiver)n.ext(extFactory.extAmbReceiver());
        }
        return n;
    }

    public AmbQualifierNode AmbQualifierNode(Position pos, QualifierNode qualifier, String name) {
        AmbQualifierNode n = new AmbQualifierNode_c(pos, qualifier, name);
        if (extFactory != null) {
            n = (AmbQualifierNode)n.ext(extFactory.extAmbQualifierNode());
        }
        return n;
    }

    public AmbExpr AmbExpr(Position pos, String name) {
        AmbExpr n = new AmbExpr_c(pos, name);
        if (extFactory != null) {
            n = (AmbExpr)n.ext(extFactory.extAmbExpr());
        }
        return n;
    }

    public AmbTypeNode AmbTypeNode(Position pos, QualifierNode qualifier, String name) {
        AmbTypeNode n = new AmbTypeNode_c(pos, qualifier, name);
        if (extFactory != null) {
            n = (AmbTypeNode)n.ext(extFactory.extAmbTypeNode());
        }
        return n;
    }

    public ArrayAccess ArrayAccess(Position pos, Expr base, Expr index) {
        ArrayAccess n = new ArrayAccess_c(pos, base, index);
        if (extFactory != null) {
            n = (ArrayAccess)n.ext(extFactory.extArrayAccess());
        }
        return n;
    }

    public ArrayInit ArrayInit(Position pos, List elements) {
        ArrayInit n = new ArrayInit_c(pos, elements);
        if (extFactory != null) {
            n = (ArrayInit)n.ext(extFactory.extArrayInit());
        }
        return n;
    }

    public Assert Assert(Position pos, Expr cond, Expr errorMessage) {
        Assert n = new Assert_c(pos, cond, errorMessage);
        if (extFactory != null) {
            n = (Assert)n.ext(extFactory.extAssert());
        }
        return n;
    }

    public Assign Assign(Position pos, Expr left, Assign.Operator op, Expr right) {
        Assign n = new Assign_c(pos, left, op, right);
        if (extFactory != null) {
            n = (Assign)n.ext(extFactory.extAssign());
        }
        return n;
    }

    public Binary Binary(Position pos, Expr left, Binary.Operator op, Expr right) {
        Binary n = new Binary_c(pos, left, op, right);
        if (extFactory != null) {
            n = (Binary)n.ext(extFactory.extBinary());
        }
        return n;
    }

    public Block Block(Position pos, List statements) {
        Block n = new Block_c(pos, statements);
        if (extFactory != null) {
            n = (Block)n.ext(extFactory.extBlock());
        }
        return n;
    }

    public SwitchBlock SwitchBlock(Position pos, List statements) {
        SwitchBlock n = new SwitchBlock_c(pos, statements);
        if (extFactory != null) {
            n = (SwitchBlock)n.ext(extFactory.extSwitchBlock());
        }
        return n;
    }

    public BooleanLit BooleanLit(Position pos, boolean value) {
        BooleanLit n = new BooleanLit_c(pos, value);
        if (extFactory != null) {
            n = (BooleanLit)n.ext(extFactory.extBooleanLit());
        }
        return n;
    }

    public Branch Branch(Position pos, Branch.Kind kind, String label) {
        Branch n = new Branch_c(pos, kind, label);
        if (extFactory != null) {
            n = (Branch)n.ext(extFactory.extBranch());
        }
        return n;
    }

    public Call Call(Position pos, Receiver target, String name, List args) {
        Call n = new Call_c(pos, target, name, args);
        if (extFactory != null) {
            n = (Call)n.ext(extFactory.extCall());
        }
        return n;
    }

    public Case Case(Position pos, Expr expr) {
        Case n = new Case_c(pos, expr);
        if (extFactory != null) {
            n = (Case)n.ext(extFactory.extCase());
        }
        return n;
    }

    public Cast Cast(Position pos, TypeNode type, Expr expr) {
        Cast n = new Cast_c(pos, type, expr);
        if (extFactory != null) {
            n = (Cast)n.ext(extFactory.extCast());
        }
        return n;
    }

    public Catch Catch(Position pos, Formal formal, Block body) {
        Catch n = new Catch_c(pos, formal, body);
        if (extFactory != null) {
            n = (Catch)n.ext(extFactory.extCatch());
        }
        return n;
    }

    public CharLit CharLit(Position pos, char value) {
        CharLit n = new CharLit_c(pos, value);
        if (extFactory != null) {
            n = (CharLit)n.ext(extFactory.extCharLit());
        }
        return n;
    }

    public ClassBody ClassBody(Position pos, List members) {
        ClassBody n = new ClassBody_c(pos, members);
        if (extFactory != null) {
            n = (ClassBody)n.ext(extFactory.extClassBody());
        }
        return n;
    }

    public ClassDecl ClassDecl(Position pos, Flags flags, String name, TypeNode superClass, List interfaces, ClassBody body) {
        ClassDecl n = new ClassDecl_c(pos, flags, name, superClass, interfaces, body);
        if (extFactory != null) {
            n = (ClassDecl)n.ext(extFactory.extClassDecl());
        }
        return n;
    }

    public Conditional Conditional(Position pos, Expr cond, Expr consequent, Expr alternative) {
        Conditional n = new Conditional_c(pos, cond, consequent, alternative);
        if (extFactory != null) {
            n = (Conditional)n.ext(extFactory.extConditional());
        }
        return n;
    }

    public ConstructorCall ConstructorCall(Position pos, ConstructorCall.Kind kind, Expr outer, List args) {
        ConstructorCall n = new ConstructorCall_c(pos, kind, outer, args);
        if (extFactory != null) {
            n = (ConstructorCall)n.ext(extFactory.extConstructorCall());
        }
        return n;
    }

    public ConstructorDecl ConstructorDecl(Position pos, Flags flags, String name, List formals, List throwTypes, Block body) {
        ConstructorDecl n = new ConstructorDecl_c(pos, flags, name, formals, throwTypes, body);
        if (extFactory != null) {
            n = (ConstructorDecl)n.ext(extFactory.extConstructorDecl());
        }
        return n;
    }

    public FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type, String name, Expr init) {
        FieldDecl n = new FieldDecl_c(pos, flags, type, name, init);
        if (extFactory != null) {
            n = (FieldDecl)n.ext(extFactory.extFieldDecl());
        }
        return n;
    }

    public Do Do(Position pos, Stmt body, Expr cond) {
        Do n = new Do_c(pos, body, cond);
        if (extFactory != null) {
            n = (Do)n.ext(extFactory.extDo());
        }
        return n;
    }

    public Empty Empty(Position pos) {
        Empty n = new Empty_c(pos);
        if (extFactory != null) {
            n = (Empty)n.ext(extFactory.extEmpty());
        }
        return n;
    }

    public Eval Eval(Position pos, Expr expr) {
        Eval n = new Eval_c(pos, expr);
        if (extFactory != null) {
            n = (Eval)n.ext(extFactory.extEval());
        }
        return n;
    }

    public Field Field(Position pos, Receiver target, String name) {
        Field n = new Field_c(pos, target, name);
        if (extFactory != null) {
            n = (Field)n.ext(extFactory.extField());
        }
        return n;
    }

    public FloatLit FloatLit(Position pos, FloatLit.Kind kind, double value) {
        FloatLit n = new FloatLit_c(pos, kind, value);
        if (extFactory != null) {
            n = (FloatLit)n.ext(extFactory.extFloatLit());
        }
        return n;
    }

    public For For(Position pos, List inits, Expr cond, List iters, Stmt body) {
        For n = new For_c(pos, inits, cond, iters, body);
        if (extFactory != null) {
            n = (For)n.ext(extFactory.extFor());
        }
        return n;
    }

    public Formal Formal(Position pos, Flags flags, TypeNode type, String name) {
        Formal n = new Formal_c(pos, flags, type, name);
        if (extFactory != null) {
            n = (Formal)n.ext(extFactory.extFormal());
        }
        return n;
    }

    public If If(Position pos, Expr cond, Stmt consequent, Stmt alternative) {
        If n = new If_c(pos, cond, consequent, alternative);
        if (extFactory != null) {
            n = (If)n.ext(extFactory.extIf());
        }
        return n;
    }

    public Import Import(Position pos, Import.Kind kind, String name) {
        Import n = new Import_c(pos, kind, name);
        if (extFactory != null) {
            n = (Import)n.ext(extFactory.extImport());
        }
        return n;
    }

    public Initializer Initializer(Position pos, Flags flags, Block body) {
        Initializer n = new Initializer_c(pos, flags, body);
        if (extFactory != null) {
            n = (Initializer)n.ext(extFactory.extInitializer());
        }
        return n;
    }

    public Instanceof Instanceof(Position pos, Expr expr, TypeNode type) {
        Instanceof n = new Instanceof_c(pos, expr, type);
        if (extFactory != null) {
            n = (Instanceof)n.ext(extFactory.extInstanceof());
        }
        return n;
    }

    public IntLit IntLit(Position pos, IntLit.Kind kind, long value) {
        IntLit n = new IntLit_c(pos, kind, value);
        if (extFactory != null) {
            n = (IntLit)n.ext(extFactory.extIntLit());
        }
        return n;
    }

    public Labeled Labeled(Position pos, String label, Stmt body) {
        Labeled n = new Labeled_c(pos, label, body);
        if (extFactory != null) {
            n = (Labeled)n.ext(extFactory.extLabeled());
        }
        return n;
    }

    public Local Local(Position pos, String name) {
        Local n = new Local_c(pos, name);
        if (extFactory != null) {
            n = (Local)n.ext(extFactory.extLocal());
        }
        return n;
    }

    public LocalClassDecl LocalClassDecl(Position pos, ClassDecl decl) {
        LocalClassDecl n = new LocalClassDecl_c(pos, decl);
        if (extFactory != null) {
            n = (LocalClassDecl)n.ext(extFactory.extLocalClassDecl());
        }
        return n;
    }

    public LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type, String name, Expr init) {
        LocalDecl n = new LocalDecl_c(pos, flags, type, name, init);
        if (extFactory != null) {
            n = (LocalDecl)n.ext(extFactory.extLocalDecl());
        }
        return n;
    }

    public MethodDecl MethodDecl(Position pos, Flags flags, TypeNode returnType, String name, List formals, List throwTypes, Block body) {
        MethodDecl n = new MethodDecl_c(pos, flags, returnType, name, formals, throwTypes, body);
        if (extFactory != null) {
            n = (MethodDecl)n.ext(extFactory.extMethodDecl());
        }
        return n;
    }

    public New New(Position pos, Expr outer, TypeNode objectType, List args, ClassBody body) {
        New n = new New_c(pos, outer, objectType, args, body);
        if (extFactory != null) {
            n = (New)n.ext(extFactory.extNew());
        }
        return n;
    }

    public NewArray NewArray(Position pos, TypeNode base, List dims, int addDims, ArrayInit init) {
        NewArray n = new NewArray_c(pos, base, dims, addDims, init);
        if (extFactory != null) {
            n = (NewArray)n.ext(extFactory.extNewArray());
        }
        return n;
    }

    public NullLit NullLit(Position pos) {
        NullLit n = new NullLit_c(pos);
        if (extFactory != null) {
            n = (NullLit)n.ext(extFactory.extNullLit());
        }
        return n;
    }

    public Return Return(Position pos, Expr expr) {
        Return n = new Return_c(pos, expr);
        if (extFactory != null) {
            n = (Return)n.ext(extFactory.extReturn());
        }
        return n;
    }

    public SourceCollection SourceCollection(Position pos, List sources) {
        SourceCollection n = new SourceCollection_c(pos,  sources);
        if (extFactory != null) {
            n = (SourceCollection)n.ext(extFactory.extSourceCollection());
        }
        return n;
    }

    public SourceFile SourceFile(Position pos, PackageNode packageName, List imports, List decls) {
        SourceFile n = new SourceFile_c(pos, packageName, imports, decls);
        if (extFactory != null) {
            n = (SourceFile)n.ext(extFactory.extSourceFile());
        }
        return n;
    }

    public Special Special(Position pos, Special.Kind kind, TypeNode outer) {
        Special n = new Special_c(pos, kind, outer);
        if (extFactory != null) {
            n = (Special)n.ext(extFactory.extSpecial());
        }
        return n;
    }

    public StringLit StringLit(Position pos, String value) {
        StringLit n = new StringLit_c(pos, value);
        if (extFactory != null) {
            n = (StringLit)n.ext(extFactory.extStringLit());
        }
        return n;
    }

    public Switch Switch(Position pos, Expr expr, List elements) {
        Switch n = new Switch_c(pos, expr, elements);
        if (extFactory != null) {
            n = (Switch)n.ext(extFactory.extSwitch());
        }
        return n;
    }

    public Synchronized Synchronized(Position pos, Expr expr, Block body) {
        Synchronized n = new Synchronized_c(pos, expr, body);
        if (extFactory != null) {
            n = (Synchronized)n.ext(extFactory.extSynchronized());
        }
        return n;
    }

    public Throw Throw(Position pos, Expr expr) {
        Throw n = new Throw_c(pos, expr);
        if (extFactory != null) {
            n = (Throw)n.ext(extFactory.extThrow());
        }
        return n;
    }

    public Try Try(Position pos, Block tryBlock, List catchBlocks, Block finallyBlock) {
        Try n = new Try_c(pos, tryBlock, catchBlocks, finallyBlock);
        if (extFactory != null) {
            n = (Try)n.ext(extFactory.extTry());
        }
        return n;
    }

    public ArrayTypeNode ArrayTypeNode(Position pos, TypeNode base) {
        ArrayTypeNode n = new ArrayTypeNode_c(pos, base);
        if (extFactory != null) {
            n = (ArrayTypeNode)n.ext(extFactory.extArrayTypeNode());
        }
        return n;
    }

    public CanonicalTypeNode CanonicalTypeNode(Position pos, Type type) {
        if (! type.isCanonical()) {
            throw new InternalCompilerError("Cannot construct a canonical " +
                "type node for a non-canonical type.");
        }

        CanonicalTypeNode n = new CanonicalTypeNode_c(pos, type);
        if (extFactory != null) {
            n = (CanonicalTypeNode)n.ext(extFactory.extCanonicalTypeNode());
        }
        return n;
    }

    public PackageNode PackageNode(Position pos, Package p) {
        PackageNode n = new PackageNode_c(pos, p);
        if (extFactory != null) {
            n = (PackageNode)n.ext(extFactory.extPackageNode());
        }
        return n;
    }

    public Unary Unary(Position pos, Unary.Operator op, Expr expr) {
        Unary n = new Unary_c(pos, op, expr);
        if (extFactory != null) {
            n = (Unary)n.ext(extFactory.extUnary());
        }
        return n;
    }

    public While While(Position pos, Expr cond, Stmt body) {
        While n = new While_c(pos, cond, body);
        if (extFactory != null) {
            n = (While)n.ext(extFactory.extWhile());
        }
        return n;
    }
}
