/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import java.util.List;

import polyglot.types.Flags;
import polyglot.types.Package;
import polyglot.types.Type;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

/**
 * A <code>NodeFactory</code> constructs AST nodes.  All node construction
 * should go through this factory or by done with the <code>copy()</code>
 * method of <code>Node</code>.
 */
public class NodeFactory_c extends AbstractNodeFactory_c {
    private final ExtFactory extFactory;
    private final DelFactory delFactory;

    // use an empty implementation of AbstractExtFactory_c and
    // AbstractDelFactory_c, so we don't need to do null checks
    protected static class EmptyExtFactory extends AbstractExtFactory_c {
    }

    protected static class EmptyDelFactory extends AbstractDelFactory_c {
    }

    public NodeFactory_c() {
        this(new EmptyExtFactory(), new EmptyDelFactory());
    }

    public NodeFactory_c(ExtFactory extFactory) {
        this(extFactory, new EmptyDelFactory());
    }

    public NodeFactory_c(ExtFactory extFactory, DelFactory delFactory) {
        this.extFactory = extFactory;
        this.delFactory = delFactory;
        initEnums();
    }

    /**
     * Ensure the enums in the AST are initialized and interned before any 
     * deserialization occurs.
     */
    protected void initEnums() {
        // Just force the static initializers of these classes to run.
        @SuppressWarnings("unused")
        Object o;
        o = Branch.BREAK;
        o = ConstructorCall.SUPER;
        o = FloatLit.FLOAT;
        o = Import.CLASS;
        o = IntLit.INT;
        o = Special.SUPER;
    }

    protected ExtFactory extFactory() {
        return this.extFactory;
    }

    protected DelFactory delFactory() {
        return this.delFactory;
    }

    /**
     * Utility method to find an instance of an Extension Factory
     */
    protected final ExtFactory findExtFactInstance(Class<? extends ExtFactory> c) {
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

    @Override
    public Id Id(Position pos, String name) {
        Id n = new Id_c(pos, name);
        n = (Id) n.ext(extFactory.extId());
        n = (Id) n.del(delFactory.delId());
        return n;
    }

    @Override
    public AmbPrefix AmbPrefix(Position pos, Prefix prefix, Id name) {
        AmbPrefix n = new AmbPrefix_c(pos, prefix, name);
        n = (AmbPrefix) n.ext(extFactory.extAmbPrefix());
        n = (AmbPrefix) n.del(delFactory.delAmbPrefix());
        return n;
    }

    @Override
    public AmbReceiver AmbReceiver(Position pos, Prefix prefix, Id name) {
        AmbReceiver n = new AmbReceiver_c(pos, prefix, name);
        n = (AmbReceiver) n.ext(extFactory.extAmbReceiver());
        n = (AmbReceiver) n.del(delFactory.delAmbReceiver());
        return n;
    }

    @Override
    public AmbQualifierNode AmbQualifierNode(Position pos,
            QualifierNode qualifier, Id name) {
        AmbQualifierNode n = new AmbQualifierNode_c(pos, qualifier, name);
        n = (AmbQualifierNode) n.ext(extFactory.extAmbQualifierNode());
        n = (AmbQualifierNode) n.del(delFactory.delAmbQualifierNode());
        return n;
    }

    @Override
    public AmbExpr AmbExpr(Position pos, Id name) {
        AmbExpr n = new AmbExpr_c(pos, name);
        n = (AmbExpr) n.ext(extFactory.extAmbExpr());
        n = (AmbExpr) n.del(delFactory.delAmbExpr());
        return n;
    }

    @Override
    public AmbTypeNode AmbTypeNode(Position pos, QualifierNode qualifier,
            Id name) {
        AmbTypeNode n = new AmbTypeNode_c(pos, qualifier, name);
        n = (AmbTypeNode) n.ext(extFactory.extAmbTypeNode());
        n = (AmbTypeNode) n.del(delFactory.delAmbTypeNode());
        return n;
    }

    @Override
    public ArrayAccess ArrayAccess(Position pos, Expr base, Expr index) {
        ArrayAccess n = new ArrayAccess_c(pos, base, index);
        n = (ArrayAccess) n.ext(extFactory.extArrayAccess());
        n = (ArrayAccess) n.del(delFactory.delArrayAccess());
        return n;
    }

    @Override
    public ArrayInit ArrayInit(Position pos, List<Expr> elements) {
        ArrayInit n =
                new ArrayInit_c(pos, CollectionUtil.nonNullList(elements));
        n = (ArrayInit) n.ext(extFactory.extArrayInit());
        n = (ArrayInit) n.del(delFactory.delArrayInit());
        return n;
    }

    @Override
    public Assert Assert(Position pos, Expr cond, Expr errorMessage) {
        Assert n = new Assert_c(pos, cond, errorMessage);
        n = (Assert) n.ext(extFactory.extAssert());
        n = (Assert) n.del(delFactory.delAssert());
        return n;
    }

    @Override
    public Assign Assign(Position pos, Expr left, Assign.Operator op, Expr right) {
        if (left instanceof Local) {
            return LocalAssign(pos, (Local) left, op, right);
        }
        else if (left instanceof Field) {
            return FieldAssign(pos, (Field) left, op, right);
        }
        else if (left instanceof ArrayAccess) {
            return ArrayAccessAssign(pos, (ArrayAccess) left, op, right);
        }
        return AmbAssign(pos, left, op, right);
    }

    @Override
    public LocalAssign LocalAssign(Position pos, Local left,
            Assign.Operator op, Expr right) {
        LocalAssign n = new LocalAssign_c(pos, left, op, right);
        n = (LocalAssign) n.ext(extFactory.extLocalAssign());
        n = (LocalAssign) n.del(delFactory.delLocalAssign());
        return n;
    }

    @Override
    public FieldAssign FieldAssign(Position pos, Field left,
            Assign.Operator op, Expr right) {
        FieldAssign n = new FieldAssign_c(pos, left, op, right);
        n = (FieldAssign) n.ext(extFactory.extFieldAssign());
        n = (FieldAssign) n.del(delFactory.delFieldAssign());
        return n;
    }

    @Override
    public ArrayAccessAssign ArrayAccessAssign(Position pos, ArrayAccess left,
            Assign.Operator op, Expr right) {
        ArrayAccessAssign n = new ArrayAccessAssign_c(pos, left, op, right);
        n = (ArrayAccessAssign) n.ext(extFactory.extArrayAccessAssign());
        n = (ArrayAccessAssign) n.del(delFactory.delArrayAccessAssign());
        return n;
    }

    @Override
    public AmbAssign AmbAssign(Position pos, Expr left, Assign.Operator op,
            Expr right) {
        AmbAssign n = new AmbAssign_c(pos, left, op, right);
        n = (AmbAssign) n.ext(extFactory.extAmbAssign());
        n = (AmbAssign) n.del(delFactory.delAmbAssign());
        return n;
    }

    @Override
    public Binary Binary(Position pos, Expr left, Binary.Operator op, Expr right) {
        Binary n = new Binary_c(pos, left, op, right);
        n = (Binary) n.ext(extFactory.extBinary());
        n = (Binary) n.del(delFactory.delBinary());
        return n;
    }

    @Override
    public Block Block(Position pos, List<Stmt> statements) {
        Block n = new Block_c(pos, CollectionUtil.nonNullList(statements));
        n = (Block) n.ext(extFactory.extBlock());
        n = (Block) n.del(delFactory.delBlock());
        return n;
    }

    @Override
    public SwitchBlock SwitchBlock(Position pos, List<Stmt> statements) {
        SwitchBlock n =
                new SwitchBlock_c(pos, CollectionUtil.nonNullList(statements));
        n = (SwitchBlock) n.ext(extFactory.extSwitchBlock());
        n = (SwitchBlock) n.del(delFactory.delSwitchBlock());
        return n;
    }

    @Override
    public BooleanLit BooleanLit(Position pos, boolean value) {
        BooleanLit n = new BooleanLit_c(pos, value);
        n = (BooleanLit) n.ext(extFactory.extBooleanLit());
        n = (BooleanLit) n.del(delFactory.delBooleanLit());
        return n;
    }

    @Override
    public Branch Branch(Position pos, Branch.Kind kind, Id label) {
        Branch n = new Branch_c(pos, kind, label);
        n = (Branch) n.ext(extFactory.extBranch());
        n = (Branch) n.del(delFactory.delBranch());
        return n;
    }

    @Override
    public Call Call(Position pos, Receiver target, Id name, List<Expr> args) {
        Call n =
                new Call_c(pos, target, name, CollectionUtil.nonNullList(args));
        n = (Call) n.ext(extFactory.extCall());
        n = (Call) n.del(delFactory.delCall());
        return n;
    }

    @Override
    public Case Case(Position pos, Expr expr) {
        Case n = new Case_c(pos, expr);
        n = (Case) n.ext(extFactory.extCase());
        n = (Case) n.del(delFactory.delCase());
        return n;
    }

    @Override
    public Cast Cast(Position pos, TypeNode type, Expr expr) {
        Cast n = new Cast_c(pos, type, expr);
        n = (Cast) n.ext(extFactory.extCast());
        n = (Cast) n.del(delFactory.delCast());
        return n;
    }

    @Override
    public Catch Catch(Position pos, Formal formal, Block body) {
        Catch n = new Catch_c(pos, formal, body);
        n = (Catch) n.ext(extFactory.extCatch());
        n = (Catch) n.del(delFactory.delCatch());
        return n;
    }

    @Override
    public CharLit CharLit(Position pos, char value) {
        CharLit n = new CharLit_c(pos, value);
        n = (CharLit) n.ext(extFactory.extCharLit());
        n = (CharLit) n.del(delFactory.delCharLit());
        return n;
    }

    @Override
    public ClassBody ClassBody(Position pos, List<ClassMember> members) {
        ClassBody n = new ClassBody_c(pos, CollectionUtil.nonNullList(members));
        n = (ClassBody) n.ext(extFactory.extClassBody());
        n = (ClassBody) n.del(delFactory.delClassBody());
        return n;
    }

    @Override
    public ClassDecl ClassDecl(Position pos, Flags flags, Id name,
            TypeNode superClass, List<TypeNode> interfaces, ClassBody body) {
        ClassDecl n =
                new ClassDecl_c(pos,
                                flags,
                                name,
                                superClass,
                                CollectionUtil.nonNullList(interfaces),
                                body);
        n = (ClassDecl) n.ext(extFactory.extClassDecl());
        n = (ClassDecl) n.del(delFactory.delClassDecl());
        return n;
    }

    @Override
    public ClassLit ClassLit(Position pos, TypeNode typeNode) {
        ClassLit n = new ClassLit_c(pos, typeNode);
        n = (ClassLit) n.ext(extFactory.extClassLit());
        n = (ClassLit) n.del(delFactory.delClassLit());
        return n;
    }

    @Override
    public Conditional Conditional(Position pos, Expr cond, Expr consequent,
            Expr alternative) {
        Conditional n = new Conditional_c(pos, cond, consequent, alternative);
        n = (Conditional) n.ext(extFactory.extConditional());
        n = (Conditional) n.del(delFactory.delConditional());
        return n;
    }

    @Override
    public ConstructorCall ConstructorCall(Position pos,
            ConstructorCall.Kind kind, Expr outer, List<Expr> args) {
        ConstructorCall n =
                new ConstructorCall_c(pos,
                                      kind,
                                      outer,
                                      CollectionUtil.nonNullList(args));
        n = (ConstructorCall) n.ext(extFactory.extConstructorCall());
        n = (ConstructorCall) n.del(delFactory.delConstructorCall());
        return n;
    }

    @Override
    public ConstructorDecl ConstructorDecl(Position pos, Flags flags, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body) {
        ConstructorDecl n =
                new ConstructorDecl_c(pos,
                                      flags,
                                      name,
                                      CollectionUtil.nonNullList(formals),
                                      CollectionUtil.nonNullList(throwTypes),
                                      body);
        n = (ConstructorDecl) n.ext(extFactory.extConstructorDecl());
        n = (ConstructorDecl) n.del(delFactory.delConstructorDecl());
        return n;
    }

    @Override
    public FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type,
            Id name, Expr init) {
        FieldDecl n = new FieldDecl_c(pos, flags, type, name, init);
        n = (FieldDecl) n.ext(extFactory.extFieldDecl());
        n = (FieldDecl) n.del(delFactory.delFieldDecl());
        return n;
    }

    @Override
    public Do Do(Position pos, Stmt body, Expr cond) {
        Do n = new Do_c(pos, body, cond);
        n = (Do) n.ext(extFactory.extDo());
        n = (Do) n.del(delFactory.delDo());
        return n;
    }

    @Override
    public Empty Empty(Position pos) {
        Empty n = new Empty_c(pos);
        n = (Empty) n.ext(extFactory.extEmpty());
        n = (Empty) n.del(delFactory.delEmpty());
        return n;
    }

    @Override
    public Eval Eval(Position pos, Expr expr) {
        Eval n = new Eval_c(pos, expr);
        n = (Eval) n.ext(extFactory.extEval());
        n = (Eval) n.del(delFactory.delEval());
        return n;
    }

    @Override
    public Field Field(Position pos, Receiver target, Id name) {
        Field n = new Field_c(pos, target, name);
        n = (Field) n.ext(extFactory.extField());
        n = (Field) n.del(delFactory.delField());
        return n;
    }

    @Override
    public FloatLit FloatLit(Position pos, FloatLit.Kind kind, double value) {
        FloatLit n = new FloatLit_c(pos, kind, value);
        n = (FloatLit) n.ext(extFactory.extFloatLit());
        n = (FloatLit) n.del(delFactory.delFloatLit());
        return n;
    }

    @Override
    public For For(Position pos, List<ForInit> inits, Expr cond,
            List<ForUpdate> iters, Stmt body) {
        For n =
                new For_c(pos,
                          CollectionUtil.nonNullList(inits),
                          cond,
                          CollectionUtil.nonNullList(iters),
                          body);
        n = (For) n.ext(extFactory.extFor());
        n = (For) n.del(delFactory.delFor());
        return n;
    }

    @Override
    public Formal Formal(Position pos, Flags flags, TypeNode type, Id name) {
        Formal n = new Formal_c(pos, flags, type, name);
        n = (Formal) n.ext(extFactory.extFormal());
        n = (Formal) n.del(delFactory.delFormal());
        return n;
    }

    @Override
    public If If(Position pos, Expr cond, Stmt consequent, Stmt alternative) {
        If n = new If_c(pos, cond, consequent, alternative);
        n = (If) n.ext(extFactory.extIf());
        n = (If) n.del(delFactory.delIf());
        return n;
    }

    @Override
    public Import Import(Position pos, Import.Kind kind, String name) {
        Import n = new Import_c(pos, kind, name);
        n = (Import) n.ext(extFactory.extImport());
        n = (Import) n.del(delFactory.delImport());
        return n;
    }

    @Override
    public Initializer Initializer(Position pos, Flags flags, Block body) {
        Initializer n = new Initializer_c(pos, flags, body);
        n = (Initializer) n.ext(extFactory.extInitializer());
        n = (Initializer) n.del(delFactory.delInitializer());
        return n;
    }

    @Override
    public Instanceof Instanceof(Position pos, Expr expr, TypeNode type) {
        Instanceof n = new Instanceof_c(pos, expr, type);
        n = (Instanceof) n.ext(extFactory.extInstanceof());
        n = (Instanceof) n.del(delFactory.delInstanceof());
        return n;
    }

    @Override
    public IntLit IntLit(Position pos, IntLit.Kind kind, long value) {
        IntLit n = new IntLit_c(pos, kind, value);
        n = (IntLit) n.ext(extFactory.extIntLit());
        n = (IntLit) n.del(delFactory.delIntLit());
        return n;
    }

    @Override
    public Labeled Labeled(Position pos, Id label, Stmt body) {
        Labeled n = new Labeled_c(pos, label, body);
        n = (Labeled) n.ext(extFactory.extLabeled());
        n = (Labeled) n.del(delFactory.delLabeled());
        return n;
    }

    @Override
    public Local Local(Position pos, Id name) {
        Local n = new Local_c(pos, name);
        n = (Local) n.ext(extFactory.extLocal());
        n = (Local) n.del(delFactory.delLocal());
        return n;
    }

    @Override
    public LocalClassDecl LocalClassDecl(Position pos, ClassDecl decl) {
        LocalClassDecl n = new LocalClassDecl_c(pos, decl);
        n = (LocalClassDecl) n.ext(extFactory.extLocalClassDecl());
        n = (LocalClassDecl) n.del(delFactory.delLocalClassDecl());
        return n;
    }

    @Override
    public LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type,
            Id name, Expr init) {
        LocalDecl n = new LocalDecl_c(pos, flags, type, name, init);
        n = (LocalDecl) n.ext(extFactory.extLocalDecl());
        n = (LocalDecl) n.del(delFactory.delLocalDecl());
        return n;
    }

    @Override
    public MethodDecl MethodDecl(Position pos, Flags flags,
            TypeNode returnType, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body) {
        MethodDecl n =
                new MethodDecl_c(pos,
                                 flags,
                                 returnType,
                                 name,
                                 CollectionUtil.nonNullList(formals),
                                 CollectionUtil.nonNullList(throwTypes),
                                 body);
        n = (MethodDecl) n.ext(extFactory.extMethodDecl());
        n = (MethodDecl) n.del(delFactory.delMethodDecl());
        return n;
    }

    @Override
    public New New(Position pos, Expr outer, TypeNode objectType,
            List<Expr> args, ClassBody body) {
        New n =
                new New_c(pos,
                          outer,
                          objectType,
                          CollectionUtil.nonNullList(args),
                          body);
        n = (New) n.ext(extFactory.extNew());
        n = (New) n.del(delFactory.delNew());
        return n;
    }

    @Override
    public NewArray NewArray(Position pos, TypeNode base, List<Expr> dims,
            int addDims, ArrayInit init) {
        NewArray n =
                new NewArray_c(pos,
                               base,
                               CollectionUtil.nonNullList(dims),
                               addDims,
                               init);
        n = (NewArray) n.ext(extFactory.extNewArray());
        n = (NewArray) n.del(delFactory.delNewArray());
        return n;
    }

    @Override
    public NullLit NullLit(Position pos) {
        NullLit n = new NullLit_c(pos);
        n = (NullLit) n.ext(extFactory.extNullLit());
        n = (NullLit) n.del(delFactory.delNullLit());
        return n;
    }

    @Override
    public Return Return(Position pos, Expr expr) {
        Return n = new Return_c(pos, expr);
        n = (Return) n.ext(extFactory.extReturn());
        n = (Return) n.del(delFactory.delReturn());
        return n;
    }

    @Override
    public SourceCollection SourceCollection(Position pos,
            List<SourceFile> sources) {
        SourceCollection n =
                new SourceCollection_c(pos, CollectionUtil.nonNullList(sources));
        n = (SourceCollection) n.ext(extFactory.extSourceCollection());
        n = (SourceCollection) n.del(delFactory.delSourceCollection());
        return n;
    }

    @Override
    public SourceFile SourceFile(Position pos, PackageNode packageName,
            List<Import> imports, List<TopLevelDecl> decls) {
        SourceFile n =
                new SourceFile_c(pos,
                                 packageName,
                                 CollectionUtil.nonNullList(imports),
                                 CollectionUtil.nonNullList(decls));
        n = (SourceFile) n.ext(extFactory.extSourceFile());
        n = (SourceFile) n.del(delFactory.delSourceFile());
        return n;
    }

    @Override
    public Special Special(Position pos, Special.Kind kind, TypeNode outer) {
        Special n = new Special_c(pos, kind, outer);
        n = (Special) n.ext(extFactory.extSpecial());
        n = (Special) n.del(delFactory.delSpecial());
        return n;
    }

    @Override
    public StringLit StringLit(Position pos, String value) {
        StringLit n = new StringLit_c(pos, value);
        n = (StringLit) n.ext(extFactory.extStringLit());
        n = (StringLit) n.del(delFactory.delStringLit());
        return n;
    }

    @Override
    public Switch Switch(Position pos, Expr expr, List<SwitchElement> elements) {
        Switch n =
                new Switch_c(pos, expr, CollectionUtil.nonNullList(elements));
        n = (Switch) n.ext(extFactory.extSwitch());
        n = (Switch) n.del(delFactory.delSwitch());
        return n;
    }

    @Override
    public Synchronized Synchronized(Position pos, Expr expr, Block body) {
        Synchronized n = new Synchronized_c(pos, expr, body);
        n = (Synchronized) n.ext(extFactory.extSynchronized());
        n = (Synchronized) n.del(delFactory.delSynchronized());
        return n;
    }

    @Override
    public Throw Throw(Position pos, Expr expr) {
        Throw n = new Throw_c(pos, expr);
        n = (Throw) n.ext(extFactory.extThrow());
        n = (Throw) n.del(delFactory.delThrow());
        return n;
    }

    @Override
    public Try Try(Position pos, Block tryBlock, List<Catch> catchBlocks,
            Block finallyBlock) {
        Try n =
                new Try_c(pos,
                          tryBlock,
                          CollectionUtil.nonNullList(catchBlocks),
                          finallyBlock);
        n = (Try) n.ext(extFactory.extTry());
        n = (Try) n.del(delFactory.delTry());
        return n;
    }

    @Override
    public ArrayTypeNode ArrayTypeNode(Position pos, TypeNode base) {
        ArrayTypeNode n = new ArrayTypeNode_c(pos, base);
        n = (ArrayTypeNode) n.ext(extFactory.extArrayTypeNode());
        n = (ArrayTypeNode) n.del(delFactory.delArrayTypeNode());
        return n;
    }

    @Override
    public CanonicalTypeNode CanonicalTypeNode(Position pos, Type type) {
        if (!type.isCanonical()) {
            throw new InternalCompilerError("Cannot construct a canonical "
                    + "type node for a non-canonical type.");
        }

        CanonicalTypeNode n = new CanonicalTypeNode_c(pos, type);
        n = (CanonicalTypeNode) n.ext(extFactory.extCanonicalTypeNode());
        n = (CanonicalTypeNode) n.del(delFactory.delCanonicalTypeNode());
        return n;
    }

    @Override
    public PackageNode PackageNode(Position pos, Package p) {
        PackageNode n = new PackageNode_c(pos, p);
        n = (PackageNode) n.ext(extFactory.extPackageNode());
        n = (PackageNode) n.del(delFactory.delPackageNode());
        return n;
    }

    @Override
    public Unary Unary(Position pos, Unary.Operator op, Expr expr) {
        Unary n = new Unary_c(pos, op, expr);
        n = (Unary) n.ext(extFactory.extUnary());
        n = (Unary) n.del(delFactory.delUnary());
        return n;
    }

    @Override
    public While While(Position pos, Expr cond, Stmt body) {
        While n = new While_c(pos, cond, body);
        n = (While) n.ext(extFactory.extWhile());
        n = (While) n.del(delFactory.delWhile());
        return n;
    }
}
