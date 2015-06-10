/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
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
 * A {@code NodeFactory} constructs AST nodes.  All node construction
 * should go through this factory or by done with the {@code copy()}
 * method of {@code Node}.
 */
// XXX Backward compatible with Polyglot 2.5.3
@SuppressWarnings("deprecation")
public class NodeFactory_c extends AbstractNodeFactory_c {
    private final JLang lang;
    private final ExtFactory extFactory;

    protected static final DelFactory emptyDelFactory =
            new AbstractDelFactory_c() {
            };

    @Deprecated
    private static class JLangToJLDelWithFactory extends JLangToJLDel {
        protected final DelFactory delFactory;

        public JLangToJLDelWithFactory(DelFactory delFactory) {
            this.delFactory = delFactory;
        }
    }

    @Deprecated
    public NodeFactory_c() {
        this(AbstractExtFactory_c.emptyExtFactory, emptyDelFactory);
    }

    @Deprecated
    public NodeFactory_c(ExtFactory extFactory) {
        this(extFactory, emptyDelFactory);
    }

    @Deprecated
    public NodeFactory_c(ExtFactory extFactory, DelFactory delFactory) {
        this(new JLangToJLDelWithFactory(delFactory), extFactory);
    }

    public NodeFactory_c(JLang lang) {
        this(lang, AbstractExtFactory_c.emptyExtFactory);
    }

    public NodeFactory_c(JLang lang, ExtFactory extFactory) {
        this.lang = lang;
        this.extFactory = extFactory;
        initEnums();
    }

    @Override
    public JLang lang() {
        return this.lang;
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
        o = Import.SINGLE_TYPE;
        o = IntLit.INT;
        o = Special.SUPER;
    }

    protected ExtFactory extFactory() {
        return this.extFactory;
    }

    @SuppressWarnings("unchecked")
    protected <T extends Node> T ext(T n, Ext ext) {
        if (ext != null) return (T) n.ext(ext);
        return n;
    }

    /**
     * Compose two extensions together. Order is important: e1 gets added
     * at the end of e2's chain of extensions.
     * @param e1 the {@code Ext} object to add to the end of e2's 
     *             chain of extensions. 
     * @param e2 the second {@code Ext} object that will have e1 added to 
     *             its chain of extensions.
     * @return the result of adding e1 to the end of e2's chain of extensions.
     */
    protected Ext composeExts(Ext e1, Ext e2) {
        if (e1 == null) return e2;
        if (e2 == null) return e1;
        // add e1 as e2's last extension, by recursing...
        return e2.ext(composeExts(e1, e2.ext()));
    }

    @Deprecated
    protected DelFactory delFactory() {
        if (lang instanceof JLangToJLDelWithFactory)
            return ((JLangToJLDelWithFactory) lang).delFactory;
        return emptyDelFactory;
    }

    @SuppressWarnings("unchecked")
    protected <T extends Node> T del(T n, JLDel del) {
        if (del != null) return (T) n.del(del);
        return n;
    }

    /**
     * Utility method to find an instance of an Extension Factory
     */
    protected final ExtFactory findExtFactInstance(Class<? extends ExtFactory> c) {
        for (ExtFactory e : extFactory()) {
            if (c.isInstance(e)) {
                // the factory e is an instance of the class c
                return e;
            }
        }
        return null;
    }

    @Override
    public Id Id(Position pos, String name) {
        Id n = new Id_c(pos, name);
        n = ext(n, extFactory().extId());
        n = del(n, delFactory().delId());
        return n;
    }

    @Override
    public AmbPrefix AmbPrefix(Position pos, Prefix prefix, Id name) {
        AmbPrefix n = new AmbPrefix_c(pos, prefix, name);
        n = ext(n, extFactory().extAmbPrefix());
        n = del(n, delFactory().delAmbPrefix());
        return n;
    }

    @Override
    public AmbReceiver AmbReceiver(Position pos, Prefix prefix, Id name) {
        AmbReceiver n = new AmbReceiver_c(pos, prefix, name);
        n = ext(n, extFactory().extAmbReceiver());
        n = del(n, delFactory().delAmbReceiver());
        return n;
    }

    @Override
    public AmbQualifierNode AmbQualifierNode(Position pos,
            QualifierNode qualifier, Id name) {
        AmbQualifierNode n = new AmbQualifierNode_c(pos, qualifier, name);
        n = ext(n, extFactory().extAmbQualifierNode());
        n = del(n, delFactory().delAmbQualifierNode());
        return n;
    }

    @Override
    public AmbExpr AmbExpr(Position pos, Id name) {
        AmbExpr n = new AmbExpr_c(pos, name);
        n = ext(n, extFactory().extAmbExpr());
        n = del(n, delFactory().delAmbExpr());
        return n;
    }

    @Override
    public AmbTypeNode AmbTypeNode(Position pos, QualifierNode qualifier,
            Id name) {
        AmbTypeNode n = new AmbTypeNode_c(pos, qualifier, name);
        n = ext(n, extFactory().extAmbTypeNode());
        n = del(n, delFactory().delAmbTypeNode());
        return n;
    }

    @Override
    public ArrayAccess ArrayAccess(Position pos, Expr base, Expr index) {
        ArrayAccess n = new ArrayAccess_c(pos, base, index);
        n = ext(n, extFactory().extArrayAccess());
        n = del(n, delFactory().delArrayAccess());
        return n;
    }

    @Override
    public ArrayInit ArrayInit(Position pos, List<Expr> elements) {
        ArrayInit n =
                new ArrayInit_c(pos, CollectionUtil.nonNullList(elements));
        n = ext(n, extFactory().extArrayInit());
        n = del(n, delFactory().delArrayInit());
        return n;
    }

    @Override
    public Assert Assert(Position pos, Expr cond, Expr errorMessage) {
        Assert n = new Assert_c(pos, cond, errorMessage);
        n = ext(n, extFactory().extAssert());
        n = del(n, delFactory().delAssert());
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
        n = ext(n, extFactory().extLocalAssign());
        n = del(n, delFactory().delLocalAssign());
        return n;
    }

    @Override
    public FieldAssign FieldAssign(Position pos, Field left,
            Assign.Operator op, Expr right) {
        FieldAssign n = new FieldAssign_c(pos, left, op, right);
        n = ext(n, extFactory().extFieldAssign());
        n = del(n, delFactory().delFieldAssign());
        return n;
    }

    @Override
    public ArrayAccessAssign ArrayAccessAssign(Position pos, ArrayAccess left,
            Assign.Operator op, Expr right) {
        ArrayAccessAssign n = new ArrayAccessAssign_c(pos, left, op, right);
        n = ext(n, extFactory().extArrayAccessAssign());
        n = del(n, delFactory().delArrayAccessAssign());
        return n;
    }

    @Override
    public AmbAssign AmbAssign(Position pos, Expr left, Assign.Operator op,
            Expr right) {
        AmbAssign n = new AmbAssign_c(pos, left, op, right);
        n = ext(n, extFactory().extAmbAssign());
        n = del(n, delFactory().delAmbAssign());
        return n;
    }

    @Override
    public Binary Binary(Position pos, Expr left, Binary.Operator op, Expr right) {
        Binary n = new Binary_c(pos, left, op, right);
        n = ext(n, extFactory().extBinary());
        n = del(n, delFactory().delBinary());
        return n;
    }

    @Override
    public Block Block(Position pos, List<Stmt> statements) {
        Block n = new Block_c(pos, CollectionUtil.nonNullList(statements));
        n = ext(n, extFactory().extBlock());
        n = del(n, delFactory().delBlock());
        return n;
    }

    @Override
    public SwitchBlock SwitchBlock(Position pos, List<Stmt> statements) {
        SwitchBlock n =
                new SwitchBlock_c(pos, CollectionUtil.nonNullList(statements));
        n = ext(n, extFactory().extSwitchBlock());
        n = del(n, delFactory().delSwitchBlock());
        return n;
    }

    @Override
    public BooleanLit BooleanLit(Position pos, boolean value) {
        BooleanLit n = new BooleanLit_c(pos, value);
        n = ext(n, extFactory().extBooleanLit());
        n = del(n, delFactory().delBooleanLit());
        return n;
    }

    @Override
    public Branch Branch(Position pos, Branch.Kind kind, Id label) {
        Branch n = new Branch_c(pos, kind, label);
        n = ext(n, extFactory().extBranch());
        n = del(n, delFactory().delBranch());
        return n;
    }

    @Override
    public Call Call(Position pos, Receiver target, Id name, List<Expr> args) {
        Call n =
                new Call_c(pos, target, name, CollectionUtil.nonNullList(args));
        n = ext(n, extFactory().extCall());
        n = del(n, delFactory().delCall());
        return n;
    }

    @Override
    public Case Case(Position pos, Expr expr) {
        Case n = new Case_c(pos, expr);
        n = ext(n, extFactory().extCase());
        n = del(n, delFactory().delCase());
        return n;
    }

    @Override
    public Cast Cast(Position pos, TypeNode type, Expr expr) {
        Cast n = new Cast_c(pos, type, expr);
        n = ext(n, extFactory().extCast());
        n = del(n, delFactory().delCast());
        return n;
    }

    @Override
    public Catch Catch(Position pos, Formal formal, Block body) {
        Catch n = new Catch_c(pos, formal, body);
        n = ext(n, extFactory().extCatch());
        n = del(n, delFactory().delCatch());
        return n;
    }

    @Override
    public CharLit CharLit(Position pos, char value) {
        CharLit n = new CharLit_c(pos, value);
        n = ext(n, extFactory().extCharLit());
        n = del(n, delFactory().delCharLit());
        return n;
    }

    @Override
    public ClassBody ClassBody(Position pos, List<ClassMember> members) {
        ClassBody n = new ClassBody_c(pos, CollectionUtil.nonNullList(members));
        n = ext(n, extFactory().extClassBody());
        n = del(n, delFactory().delClassBody());
        return n;
    }

    /**
     * @deprecated Use the method that takes in Javadoc.
     */
    @Deprecated
    @Override
    public ClassDecl ClassDecl(Position pos, Flags flags, Id name,
            TypeNode superClass, List<TypeNode> interfaces, ClassBody body) {
        return ClassDecl(pos, flags, name, superClass, interfaces, body, null);
    }

    @Override
    public ClassDecl ClassDecl(Position pos, Flags flags, Id name,
            TypeNode superClass, List<TypeNode> interfaces, ClassBody body,
            Javadoc javadoc) {
        ClassDecl n =
                new ClassDecl_c(pos,
                                flags,
                                name,
                                superClass,
                                CollectionUtil.nonNullList(interfaces),
                                body,
                                javadoc);
        n = ext(n, extFactory().extClassDecl());
        n = del(n, delFactory().delClassDecl());
        return n;
    }

    @Override
    public ClassLit ClassLit(Position pos, TypeNode typeNode) {
        ClassLit n = new ClassLit_c(pos, typeNode);
        n = ext(n, extFactory().extClassLit());
        n = del(n, delFactory().delClassLit());
        return n;
    }

    @Override
    public Conditional Conditional(Position pos, Expr cond, Expr consequent,
            Expr alternative) {
        Conditional n = new Conditional_c(pos, cond, consequent, alternative);
        n = ext(n, extFactory().extConditional());
        n = del(n, delFactory().delConditional());
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
        n = ext(n, extFactory().extConstructorCall());
        n = del(n, delFactory().delConstructorCall());
        return n;
    }

    /**
     * @deprecated Use the method that takes in Javadoc.
     */
    @Deprecated
    @Override
    public ConstructorDecl ConstructorDecl(Position pos, Flags flags, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body) {
        return ConstructorDecl(pos,
                               flags,
                               name,
                               formals,
                               throwTypes,
                               body,
                               null);
    }

    @Override
    public ConstructorDecl ConstructorDecl(Position pos, Flags flags, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body,
            Javadoc javadoc) {
        ConstructorDecl n =
                new ConstructorDecl_c(pos,
                                      flags,
                                      name,
                                      CollectionUtil.nonNullList(formals),
                                      CollectionUtil.nonNullList(throwTypes),
                                      body,
                                      javadoc);
        n = ext(n, extFactory().extConstructorDecl());
        n = del(n, delFactory().delConstructorDecl());
        return n;
    }

    /**
     * @deprecated Use the method that takes in Javadoc.
     */
    @Deprecated
    @Override
    public FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type,
            Id name, Expr init) {
        return FieldDecl(pos, flags, type, name, init, null);
    }

    @Override
    public FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type,
            Id name, Expr init, Javadoc javadoc) {
        FieldDecl n = new FieldDecl_c(pos, flags, type, name, init, javadoc);
        n = ext(n, extFactory().extFieldDecl());
        n = del(n, delFactory().delFieldDecl());
        return n;
    }

    @Override
    public Do Do(Position pos, Stmt body, Expr cond) {
        Do n = new Do_c(pos, body, cond);
        n = ext(n, extFactory().extDo());
        n = del(n, delFactory().delDo());
        return n;
    }

    @Override
    public Empty Empty(Position pos) {
        Empty n = new Empty_c(pos);
        n = ext(n, extFactory().extEmpty());
        n = del(n, delFactory().delEmpty());
        return n;
    }

    @Override
    public Eval Eval(Position pos, Expr expr) {
        Eval n = new Eval_c(pos, expr);
        n = ext(n, extFactory().extEval());
        n = del(n, delFactory().delEval());
        return n;
    }

    @Override
    public Field Field(Position pos, Receiver target, Id name) {
        Field n = new Field_c(pos, target, name);
        n = ext(n, extFactory().extField());
        n = del(n, delFactory().delField());
        return n;
    }

    @Override
    public FloatLit FloatLit(Position pos, FloatLit.Kind kind, double value) {
        FloatLit n = new FloatLit_c(pos, kind, value);
        n = ext(n, extFactory().extFloatLit());
        n = del(n, delFactory().delFloatLit());
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
        n = ext(n, extFactory().extFor());
        n = del(n, delFactory().delFor());
        return n;
    }

    @Override
    public Formal Formal(Position pos, Flags flags, TypeNode type, Id name) {
        Formal n = new Formal_c(pos, flags, type, name);
        n = ext(n, extFactory().extFormal());
        n = del(n, delFactory().delFormal());
        return n;
    }

    @Override
    public If If(Position pos, Expr cond, Stmt consequent, Stmt alternative) {
        If n = new If_c(pos, cond, consequent, alternative);
        n = ext(n, extFactory().extIf());
        n = del(n, delFactory().delIf());
        return n;
    }

    @Override
    public Import Import(Position pos, Import.Kind kind, String name) {
        Import n = new Import_c(pos, kind, name);
        n = ext(n, extFactory().extImport());
        n = del(n, delFactory().delImport());
        return n;
    }

    @Override
    public Initializer Initializer(Position pos, Flags flags, Block body) {
        Initializer n = new Initializer_c(pos, flags, body);
        n = ext(n, extFactory().extInitializer());
        n = del(n, delFactory().delInitializer());
        return n;
    }

    @Override
    public Instanceof Instanceof(Position pos, Expr expr, TypeNode type) {
        Instanceof n = new Instanceof_c(pos, expr, type);
        n = ext(n, extFactory().extInstanceof());
        n = del(n, delFactory().delInstanceof());
        return n;
    }

    @Override
    public IntLit IntLit(Position pos, IntLit.Kind kind, long value) {
        IntLit n = new IntLit_c(pos, kind, value);
        n = ext(n, extFactory().extIntLit());
        n = del(n, delFactory().delIntLit());
        return n;
    }

    @Override
    public Labeled Labeled(Position pos, Id label, Stmt body) {
        Labeled n = new Labeled_c(pos, label, body);
        n = ext(n, extFactory().extLabeled());
        n = del(n, delFactory().delLabeled());
        return n;
    }

    @Override
    public Local Local(Position pos, Id name) {
        Local n = new Local_c(pos, name);
        n = ext(n, extFactory().extLocal());
        n = del(n, delFactory().delLocal());
        return n;
    }

    @Override
    public LocalClassDecl LocalClassDecl(Position pos, ClassDecl decl) {
        LocalClassDecl n = new LocalClassDecl_c(pos, decl);
        n = ext(n, extFactory().extLocalClassDecl());
        n = del(n, delFactory().delLocalClassDecl());
        return n;
    }

    @Override
    public LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type,
            Id name, Expr init) {
        LocalDecl n = new LocalDecl_c(pos, flags, type, name, init);
        n = ext(n, extFactory().extLocalDecl());
        n = del(n, delFactory().delLocalDecl());
        return n;
    }

    /**
     * @deprecated Use the method that takes in Javadoc.
     */
    @Deprecated
    @Override
    public MethodDecl MethodDecl(Position pos, Flags flags,
            TypeNode returnType, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body) {
        return MethodDecl(pos,
                          flags,
                          returnType,
                          name,
                          formals,
                          throwTypes,
                          body,
                          null);
    }

    @Override
    public MethodDecl MethodDecl(Position pos, Flags flags,
            TypeNode returnType, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body, Javadoc javadoc) {
        MethodDecl n =
                new MethodDecl_c(pos,
                                 flags,
                                 returnType,
                                 name,
                                 CollectionUtil.nonNullList(formals),
                                 CollectionUtil.nonNullList(throwTypes),
                                 body,
                                 javadoc);
        n = ext(n, extFactory().extMethodDecl());
        n = del(n, delFactory().delMethodDecl());
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
        n = ext(n, extFactory().extNew());
        n = del(n, delFactory().delNew());
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
        n = ext(n, extFactory().extNewArray());
        n = del(n, delFactory().delNewArray());
        return n;
    }

    @Override
    public NullLit NullLit(Position pos) {
        NullLit n = new NullLit_c(pos);
        n = ext(n, extFactory().extNullLit());
        n = del(n, delFactory().delNullLit());
        return n;
    }

    @Override
    public Return Return(Position pos, Expr expr) {
        Return n = new Return_c(pos, expr);
        n = ext(n, extFactory().extReturn());
        n = del(n, delFactory().delReturn());
        return n;
    }

    @Override
    public SourceCollection SourceCollection(Position pos,
            List<SourceFile> sources) {
        SourceCollection n =
                new SourceCollection_c(pos, CollectionUtil.nonNullList(sources));
        n = ext(n, extFactory().extSourceCollection());
        n = del(n, delFactory().delSourceCollection());
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
        n = ext(n, extFactory().extSourceFile());
        n = del(n, delFactory().delSourceFile());
        return n;
    }

    @Override
    public Special Special(Position pos, Special.Kind kind, TypeNode outer) {
        Special n = new Special_c(pos, kind, outer);
        n = ext(n, extFactory().extSpecial());
        n = del(n, delFactory().delSpecial());
        return n;
    }

    @Override
    public StringLit StringLit(Position pos, String value) {
        StringLit n = new StringLit_c(pos, value);
        n = ext(n, extFactory().extStringLit());
        n = del(n, delFactory().delStringLit());
        return n;
    }

    @Override
    public Switch Switch(Position pos, Expr expr, List<SwitchElement> elements) {
        Switch n =
                new Switch_c(pos, expr, CollectionUtil.nonNullList(elements));
        n = ext(n, extFactory().extSwitch());
        n = del(n, delFactory().delSwitch());
        return n;
    }

    @Override
    public Synchronized Synchronized(Position pos, Expr expr, Block body) {
        Synchronized n = new Synchronized_c(pos, expr, body);
        n = ext(n, extFactory().extSynchronized());
        n = del(n, delFactory().delSynchronized());
        return n;
    }

    @Override
    public Throw Throw(Position pos, Expr expr) {
        Throw n = new Throw_c(pos, expr);
        n = ext(n, extFactory().extThrow());
        n = del(n, delFactory().delThrow());
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
        n = ext(n, extFactory().extTry());
        n = del(n, delFactory().delTry());
        return n;
    }

    @Override
    public ArrayTypeNode ArrayTypeNode(Position pos, TypeNode base) {
        ArrayTypeNode n = new ArrayTypeNode_c(pos, base);
        n = ext(n, extFactory().extArrayTypeNode());
        n = del(n, delFactory().delArrayTypeNode());
        return n;
    }

    @Override
    public CanonicalTypeNode CanonicalTypeNode(Position pos, Type type) {
        if (!type.isCanonical()) {
            throw new InternalCompilerError("Cannot construct a canonical "
                    + "type node for a non-canonical type.");
        }

        CanonicalTypeNode n = new CanonicalTypeNode_c(pos, type);
        n = ext(n, extFactory().extCanonicalTypeNode());
        n = del(n, delFactory().delCanonicalTypeNode());
        return n;
    }

    @Override
    public PackageNode PackageNode(Position pos, Package p) {
        PackageNode n = new PackageNode_c(pos, p);
        n = ext(n, extFactory().extPackageNode());
        n = del(n, delFactory().delPackageNode());
        return n;
    }

    @Override
    public Unary Unary(Position pos, Unary.Operator op, Expr expr) {
        Unary n = new Unary_c(pos, op, expr);
        n = ext(n, extFactory().extUnary());
        n = del(n, delFactory().delUnary());
        return n;
    }

    @Override
    public While While(Position pos, Expr cond, Stmt body) {
        While n = new While_c(pos, cond, body);
        n = ext(n, extFactory().extWhile());
        n = del(n, delFactory().delWhile());
        return n;
    }
}
