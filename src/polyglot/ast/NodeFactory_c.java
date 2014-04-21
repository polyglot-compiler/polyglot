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

    // use an empty implementation of AbstractExtFactory_c,
    // so we don't need to do null checks
    protected static final ExtFactory emptyExtFactory =
            new AbstractExtFactory_c() {
            };

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
        this(emptyExtFactory, emptyDelFactory);
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
        this(lang, emptyExtFactory);
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
        Id n = Id(pos, name, null, extFactory());
        n = del(n, delFactory().delId());
        return n;
    }

    protected final Id Id(Position pos, String name, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extId());
        return new Id_c(pos, name, ext);
    }

    @Override
    public AmbPrefix AmbPrefix(Position pos, Prefix prefix, Id name) {
        AmbPrefix n = AmbPrefix(pos, prefix, name, null, extFactory());
        n = del(n, delFactory().delAmbPrefix());
        return n;
    }

    protected final AmbPrefix AmbPrefix(Position pos, Prefix prefix, Id name,
            Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extAmbPrefix());
        return new AmbPrefix_c(pos, prefix, name, ext);
    }

    @Override
    public AmbReceiver AmbReceiver(Position pos, Prefix prefix, Id name) {
        AmbReceiver n = AmbReceiver(pos, prefix, name, null, extFactory());
        n = del(n, delFactory().delAmbReceiver());
        return n;
    }

    protected final AmbReceiver AmbReceiver(Position pos, Prefix prefix,
            Id name, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extAmbReceiver());
        return new AmbReceiver_c(pos, prefix, name, ext);
    }

    @Override
    public AmbQualifierNode AmbQualifierNode(Position pos,
            QualifierNode qualifier, Id name) {
        AmbQualifierNode n =
                AmbQualifierNode(pos, qualifier, name, null, extFactory());
        n = del(n, delFactory().delAmbQualifierNode());
        return n;
    }

    protected final AmbQualifierNode AmbQualifierNode(Position pos,
            QualifierNode qualifier, Id name, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extAmbQualifierNode());
        return new AmbQualifierNode_c(pos, qualifier, name, ext);
    }

    @Override
    public AmbExpr AmbExpr(Position pos, Id name) {
        AmbExpr n = AmbExpr(pos, name, null, extFactory());
        n = del(n, delFactory().delAmbExpr());
        return n;
    }

    protected final AmbExpr AmbExpr(Position pos, Id name, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extAmbExpr());
        return new AmbExpr_c(pos, name, ext);
    }

    @Override
    public AmbTypeNode AmbTypeNode(Position pos, QualifierNode qualifier,
            Id name) {
        AmbTypeNode n = AmbTypeNode(pos, qualifier, name, null, extFactory());
        n = del(n, delFactory().delAmbTypeNode());
        return n;
    }

    protected final AmbTypeNode AmbTypeNode(Position pos,
            QualifierNode qualifier, Id name, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extAmbTypeNode());
        return new AmbTypeNode_c(pos, qualifier, name, ext);
    }

    @Override
    public ArrayAccess ArrayAccess(Position pos, Expr base, Expr index) {
        ArrayAccess n = ArrayAccess(pos, base, index, null, extFactory());
        n = del(n, delFactory().delArrayAccess());
        return n;
    }

    protected final ArrayAccess ArrayAccess(Position pos, Expr base,
            Expr index, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extArrayAccess());
        return new ArrayAccess_c(pos, base, index, ext);
    }

    @Override
    public ArrayInit ArrayInit(Position pos, List<Expr> elements) {
        ArrayInit n = ArrayInit(pos, elements, null, extFactory());
        n = del(n, delFactory().delArrayInit());
        return n;
    }

    protected final ArrayInit ArrayInit(Position pos, List<Expr> elements,
            Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extArrayInit());
        return new ArrayInit_c(pos, CollectionUtil.nonNullList(elements), ext);
    }

    @Override
    public Assert Assert(Position pos, Expr cond, Expr errorMessage) {
        Assert n = Assert(pos, cond, errorMessage, null, extFactory());
        n = del(n, delFactory().delAssert());
        return n;
    }

    protected final Assert Assert(Position pos, Expr cond, Expr errorMessage,
            Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extAssert());
        return new Assert_c(pos, cond, errorMessage, ext);
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
        LocalAssign n = LocalAssign(pos, left, op, right, null, extFactory());
        n = del(n, delFactory().delLocalAssign());
        return n;
    }

    protected final LocalAssign LocalAssign(Position pos, Local left,
            Assign.Operator op, Expr right, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extLocalAssign());
        return new LocalAssign_c(pos, left, op, right, ext);
    }

    @Override
    public FieldAssign FieldAssign(Position pos, Field left,
            Assign.Operator op, Expr right) {
        FieldAssign n = FieldAssign(pos, left, op, right, null, extFactory());
        n = del(n, delFactory().delFieldAssign());
        return n;
    }

    protected final FieldAssign FieldAssign(Position pos, Field left,
            Assign.Operator op, Expr right, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extFieldAssign());
        return new FieldAssign_c(pos, left, op, right, ext);
    }

    @Override
    public ArrayAccessAssign ArrayAccessAssign(Position pos, ArrayAccess left,
            Assign.Operator op, Expr right) {
        ArrayAccessAssign n =
                ArrayAccessAssign(pos, left, op, right, null, extFactory());
        n = del(n, delFactory().delArrayAccessAssign());
        return n;
    }

    protected final ArrayAccessAssign ArrayAccessAssign(Position pos,
            ArrayAccess left, Assign.Operator op, Expr right, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extArrayAccessAssign());
        return new ArrayAccessAssign_c(pos, left, op, right, ext);
    }

    @Override
    public AmbAssign AmbAssign(Position pos, Expr left, Assign.Operator op,
            Expr right) {
        AmbAssign n = AmbAssign(pos, left, op, right, null, extFactory());
        n = del(n, delFactory().delAmbAssign());
        return n;
    }

    protected final AmbAssign AmbAssign(Position pos, Expr left,
            Assign.Operator op, Expr right, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extAmbAssign());
        return new AmbAssign_c(pos, left, op, right, ext);
    }

    @Override
    public Binary Binary(Position pos, Expr left, Binary.Operator op, Expr right) {
        Binary n = Binary(pos, left, op, right, null, extFactory());
        n = del(n, delFactory().delBinary());
        return n;
    }

    protected final Binary Binary(Position pos, Expr left, Binary.Operator op,
            Expr right, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extBinary());
        return new Binary_c(pos, left, op, right, ext);
    }

    @Override
    public Block Block(Position pos, List<Stmt> statements) {
        Block n = Block(pos, statements, null, extFactory());
        n = del(n, delFactory().delBlock());
        return n;
    }

    protected final Block Block(Position pos, List<Stmt> statements, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extBlock());
        return new Block_c(pos, CollectionUtil.nonNullList(statements), ext);
    }

    @Override
    public SwitchBlock SwitchBlock(Position pos, List<Stmt> statements) {
        SwitchBlock n = SwitchBlock(pos, statements, null, extFactory());
        n = del(n, delFactory().delSwitchBlock());
        return n;
    }

    protected final SwitchBlock SwitchBlock(Position pos,
            List<Stmt> statements, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extSwitchBlock());
        return new SwitchBlock_c(pos,
                                 CollectionUtil.nonNullList(statements),
                                 ext);
    }

    @Override
    public BooleanLit BooleanLit(Position pos, boolean value) {
        BooleanLit n = BooleanLit(pos, value, null, extFactory());
        n = del(n, delFactory().delBooleanLit());
        return n;
    }

    protected final BooleanLit BooleanLit(Position pos, boolean value, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extBooleanLit());
        return new BooleanLit_c(pos, value, ext);
    }

    @Override
    public Branch Branch(Position pos, Branch.Kind kind, Id label) {
        Branch n = Branch(pos, kind, label, null, extFactory());
        n = del(n, delFactory().delBranch());
        return n;
    }

    protected final Branch Branch(Position pos, Branch.Kind kind, Id label,
            Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extBranch());
        return new Branch_c(pos, kind, label, ext);
    }

    @Override
    public Call Call(Position pos, Receiver target, Id name, List<Expr> args) {
        Call n = Call(pos, target, name, args, null, extFactory());
        n = del(n, delFactory().delCall());
        return n;
    }

    protected final Call Call(Position pos, Receiver target, Id name,
            List<Expr> args, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extCall());
        return new Call_c(pos,
                          target,
                          name,
                          CollectionUtil.nonNullList(args),
                          ext);
    }

    @Override
    public Case Case(Position pos, Expr expr) {
        Case n = Case(pos, expr, null, extFactory());
        n = del(n, delFactory().delCase());
        return n;
    }

    protected final Case Case(Position pos, Expr expr, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extCase());
        return new Case_c(pos, expr, ext);
    }

    @Override
    public Cast Cast(Position pos, TypeNode type, Expr expr) {
        Cast n = Cast(pos, type, expr, null, extFactory());
        n = del(n, delFactory().delCast());
        return n;
    }

    protected final Cast Cast(Position pos, TypeNode type, Expr expr, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extCast());
        return new Cast_c(pos, type, expr, ext);
    }

    @Override
    public Catch Catch(Position pos, Formal formal, Block body) {
        Catch n = Catch(pos, formal, body, null, extFactory());
        n = del(n, delFactory().delCatch());
        return n;
    }

    protected final Catch Catch(Position pos, Formal formal, Block body,
            Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extCatch());
        return new Catch_c(pos, formal, body, ext);
    }

    @Override
    public CharLit CharLit(Position pos, char value) {
        CharLit n = CharLit(pos, value, null, extFactory());
        n = del(n, delFactory().delCharLit());
        return n;
    }

    protected final CharLit CharLit(Position pos, char value, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extCharLit());
        return new CharLit_c(pos, value, ext);
    }

    @Override
    public ClassBody ClassBody(Position pos, List<ClassMember> members) {
        ClassBody n = ClassBody(pos, members, null, extFactory());
        n = del(n, delFactory().delClassBody());
        return n;
    }

    protected final ClassBody ClassBody(Position pos,
            List<ClassMember> members, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extClassBody());
        return new ClassBody_c(pos, CollectionUtil.nonNullList(members), ext);
    }

    @Override
    public ClassDecl ClassDecl(Position pos, Flags flags, Id name,
            TypeNode superClass, List<TypeNode> interfaces, ClassBody body) {
        ClassDecl n =
                ClassDecl(pos,
                          flags,
                          name,
                          superClass,
                          interfaces,
                          body,
                          null,
                          extFactory());
        n = del(n, delFactory().delClassDecl());
        return n;
    }

    protected final ClassDecl ClassDecl(Position pos, Flags flags, Id name,
            TypeNode superClass, List<TypeNode> interfaces, ClassBody body,
            Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extClassDecl());
        return new ClassDecl_c(pos,
                               flags,
                               name,
                               superClass,
                               CollectionUtil.nonNullList(interfaces),
                               body,
                               ext);
    }

    @Override
    public ClassLit ClassLit(Position pos, TypeNode typeNode) {
        ClassLit n = ClassLit(pos, typeNode, null, extFactory());
        n = del(n, delFactory().delClassLit());
        return n;
    }

    protected final ClassLit ClassLit(Position pos, TypeNode typeNode, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extClassLit());
        return new ClassLit_c(pos, typeNode, ext);
    }

    @Override
    public Conditional Conditional(Position pos, Expr cond, Expr consequent,
            Expr alternative) {
        Conditional n =
                Conditional(pos,
                            cond,
                            consequent,
                            alternative,
                            null,
                            extFactory());
        n = del(n, delFactory().delConditional());
        return n;
    }

    protected final Conditional Conditional(Position pos, Expr cond,
            Expr consequent, Expr alternative, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extConditional());
        return new Conditional_c(pos, cond, consequent, alternative, ext);
    }

    @Override
    public ConstructorCall ConstructorCall(Position pos,
            ConstructorCall.Kind kind, Expr outer, List<Expr> args) {
        ConstructorCall n =
                ConstructorCall(pos, kind, outer, args, null, extFactory());
        n = del(n, delFactory().delConstructorCall());
        return n;
    }

    protected final ConstructorCall ConstructorCall(Position pos,
            ConstructorCall.Kind kind, Expr outer, List<Expr> args, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extConstructorCall());
        return new ConstructorCall_c(pos,
                                     kind,
                                     outer,
                                     CollectionUtil.nonNullList(args),
                                     ext);
    }

    @Override
    public ConstructorDecl ConstructorDecl(Position pos, Flags flags, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body) {
        ConstructorDecl n =
                ConstructorDecl(pos,
                                flags,
                                name,
                                formals,
                                throwTypes,
                                body,
                                null,
                                extFactory());
        n = del(n, delFactory().delConstructorDecl());
        return n;
    }

    protected final ConstructorDecl ConstructorDecl(Position pos, Flags flags,
            Id name, List<Formal> formals, List<TypeNode> throwTypes,
            Block body, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extConstructorDecl());
        return new ConstructorDecl_c(pos,
                                     flags,
                                     name,
                                     CollectionUtil.nonNullList(formals),
                                     CollectionUtil.nonNullList(throwTypes),
                                     body,
                                     ext);
    }

    @Override
    public FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type,
            Id name, Expr init) {
        FieldDecl n =
                FieldDecl(pos, flags, type, name, init, null, extFactory());
        n = del(n, delFactory().delFieldDecl());
        return n;
    }

    protected final FieldDecl FieldDecl(Position pos, Flags flags,
            TypeNode type, Id name, Expr init, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extFieldDecl());
        return new FieldDecl_c(pos, flags, type, name, init, ext);
    }

    @Override
    public Do Do(Position pos, Stmt body, Expr cond) {
        Do n = Do(pos, body, cond, null, extFactory());
        n = del(n, delFactory().delDo());
        return n;
    }

    protected final Do Do(Position pos, Stmt body, Expr cond, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extDo());
        return new Do_c(pos, body, cond, ext);
    }

    @Override
    public Empty Empty(Position pos) {
        Empty n = Empty(pos, null, extFactory());
        n = del(n, delFactory().delEmpty());
        return n;
    }

    protected final Empty Empty(Position pos, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extEmpty());
        return new Empty_c(pos, ext);
    }

    @Override
    public Eval Eval(Position pos, Expr expr) {
        Eval n = Eval(pos, expr, null, extFactory());
        n = del(n, delFactory().delEval());
        return n;
    }

    protected final Eval Eval(Position pos, Expr expr, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extEval());
        return new Eval_c(pos, expr, ext);
    }

    @Override
    public Field Field(Position pos, Receiver target, Id name) {
        Field n = Field(pos, target, name, null, extFactory());
        n = del(n, delFactory().delField());
        return n;
    }

    protected final Field Field(Position pos, Receiver target, Id name,
            Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extField());
        return new Field_c(pos, target, name, ext);
    }

    @Override
    public FloatLit FloatLit(Position pos, FloatLit.Kind kind, double value) {
        FloatLit n = FloatLit(pos, kind, value, null, extFactory());
        n = del(n, delFactory().delFloatLit());
        return n;
    }

    protected final FloatLit FloatLit(Position pos, FloatLit.Kind kind,
            double value, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extFloatLit());
        return new FloatLit_c(pos, kind, value, ext);
    }

    @Override
    public For For(Position pos, List<ForInit> inits, Expr cond,
            List<ForUpdate> iters, Stmt body) {
        For n = For(pos, inits, cond, iters, body, null, extFactory());
        n = del(n, delFactory().delFor());
        return n;
    }

    protected final For For(Position pos, List<ForInit> inits, Expr cond,
            List<ForUpdate> iters, Stmt body, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extFor());
        return new For_c(pos,
                         CollectionUtil.nonNullList(inits),
                         cond,
                         CollectionUtil.nonNullList(iters),
                         body,
                         ext);
    }

    @Override
    public Formal Formal(Position pos, Flags flags, TypeNode type, Id name) {
        Formal n = Formal(pos, flags, type, name, null, extFactory());
        n = del(n, delFactory().delFormal());
        return n;
    }

    protected final Formal Formal(Position pos, Flags flags, TypeNode type,
            Id name, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extFormal());
        return new Formal_c(pos, flags, type, name, ext);
    }

    @Override
    public If If(Position pos, Expr cond, Stmt consequent, Stmt alternative) {
        If n = If(pos, cond, consequent, alternative, null, extFactory());
        n = del(n, delFactory().delIf());
        return n;
    }

    protected final If If(Position pos, Expr cond, Stmt consequent,
            Stmt alternative, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extIf());
        return new If_c(pos, cond, consequent, alternative, ext);
    }

    @Override
    public Import Import(Position pos, Import.Kind kind, String name) {
        Import n = Import(pos, kind, name, null, extFactory());
        n = del(n, delFactory().delImport());
        return n;
    }

    protected final Import Import(Position pos, Import.Kind kind, String name,
            Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extImport());
        return new Import_c(pos, kind, name, ext);
    }

    @Override
    public Initializer Initializer(Position pos, Flags flags, Block body) {
        Initializer n = Initializer(pos, flags, body, null, extFactory());
        n = del(n, delFactory().delInitializer());
        return n;
    }

    protected final Initializer Initializer(Position pos, Flags flags,
            Block body, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extInitializer());
        return new Initializer_c(pos, flags, body, ext);
    }

    @Override
    public Instanceof Instanceof(Position pos, Expr expr, TypeNode type) {
        Instanceof n = Instanceof(pos, expr, type, null, extFactory());
        n = del(n, delFactory().delInstanceof());
        return n;
    }

    protected final Instanceof Instanceof(Position pos, Expr expr,
            TypeNode type, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extInstanceof());
        return new Instanceof_c(pos, expr, type, ext);
    }

    @Override
    public IntLit IntLit(Position pos, IntLit.Kind kind, long value) {
        IntLit n = IntLit(pos, kind, value, null, extFactory());
        n = del(n, delFactory().delIntLit());
        return n;
    }

    protected final IntLit IntLit(Position pos, IntLit.Kind kind, long value,
            Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extIntLit());
        return new IntLit_c(pos, kind, value, ext);
    }

    @Override
    public Labeled Labeled(Position pos, Id label, Stmt body) {
        Labeled n = Labeled(pos, label, body, null, extFactory());
        n = del(n, delFactory().delLabeled());
        return n;
    }

    protected final Labeled Labeled(Position pos, Id label, Stmt body, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extLabeled());
        return new Labeled_c(pos, label, body, ext);
    }

    @Override
    public Local Local(Position pos, Id name) {
        Local n = Local(pos, name, null, extFactory());
        n = del(n, delFactory().delLocal());
        return n;
    }

    protected final Local Local(Position pos, Id name, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extLocal());
        return new Local_c(pos, name, ext);
    }

    @Override
    public LocalClassDecl LocalClassDecl(Position pos, ClassDecl decl) {
        LocalClassDecl n = LocalClassDecl(pos, decl, null, extFactory());
        n = del(n, delFactory().delLocalClassDecl());
        return n;
    }

    protected final LocalClassDecl LocalClassDecl(Position pos, ClassDecl decl,
            Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extLocalClassDecl());
        return new LocalClassDecl_c(pos, decl, ext);
    }

    @Override
    public LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type,
            Id name, Expr init) {
        LocalDecl n =
                LocalDecl(pos, flags, type, name, init, null, extFactory());
        n = del(n, delFactory().delLocalDecl());
        return n;
    }

    protected final LocalDecl LocalDecl(Position pos, Flags flags,
            TypeNode type, Id name, Expr init, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extLocalDecl());
        return new LocalDecl_c(pos, flags, type, name, init, ext);
    }

    protected final Loop Loop(Position pos, Expr cond, Stmt body, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extLoop());
        return new Loop.Instance(pos, cond, body, ext);
    }

    @Override
    public MethodDecl MethodDecl(Position pos, Flags flags,
            TypeNode returnType, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body) {
        MethodDecl n =
                MethodDecl(pos,
                           flags,
                           returnType,
                           name,
                           formals,
                           throwTypes,
                           body,
                           null,
                           extFactory());
        n = del(n, delFactory().delMethodDecl());
        return n;
    }

    protected final MethodDecl MethodDecl(Position pos, Flags flags,
            TypeNode returnType, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extMethodDecl());
        return new MethodDecl_c(pos,
                                flags,
                                returnType,
                                name,
                                CollectionUtil.nonNullList(formals),
                                CollectionUtil.nonNullList(throwTypes),
                                body,
                                ext);
    }

    @Override
    public New New(Position pos, Expr outer, TypeNode objectType,
            List<Expr> args, ClassBody body) {
        New n = New(pos, outer, objectType, args, body, null, extFactory());
        n = del(n, delFactory().delNew());
        return n;
    }

    protected final New New(Position pos, Expr outer, TypeNode objectType,
            List<Expr> args, ClassBody body, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extNew());
        return new New_c(pos,
                         outer,
                         objectType,
                         CollectionUtil.nonNullList(args),
                         body,
                         ext);
    }

    @Override
    public NewArray NewArray(Position pos, TypeNode base, List<Expr> dims,
            int addDims, ArrayInit init) {
        NewArray n =
                NewArray(pos, base, dims, addDims, init, null, extFactory());
        n = del(n, delFactory().delNewArray());
        return n;
    }

    protected final NewArray NewArray(Position pos, TypeNode base,
            List<Expr> dims, int addDims, ArrayInit init, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extNewArray());
        return new NewArray_c(pos,
                              base,
                              CollectionUtil.nonNullList(dims),
                              addDims,
                              init,
                              ext);
    }

    @Override
    public NullLit NullLit(Position pos) {
        NullLit n = NullLit(pos, null, extFactory());
        n = del(n, delFactory().delNullLit());
        return n;
    }

    protected final NullLit NullLit(Position pos, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extNullLit());
        return new NullLit_c(pos, ext);
    }

    @Override
    public Return Return(Position pos, Expr expr) {
        Return n = Return(pos, expr, null, extFactory());
        n = del(n, delFactory().delReturn());
        return n;
    }

    protected final Return Return(Position pos, Expr expr, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extReturn());
        return new Return_c(pos, expr, ext);
    }

    @Override
    public SourceCollection SourceCollection(Position pos,
            List<SourceFile> sources) {
        SourceCollection n = SourceCollection(pos, sources, null, extFactory());
        n = del(n, delFactory().delSourceCollection());
        return n;
    }

    protected final SourceCollection SourceCollection(Position pos,
            List<SourceFile> sources, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extSourceCollection());
        return new SourceCollection_c(pos,
                                      CollectionUtil.nonNullList(sources),
                                      ext);
    }

    @Override
    public SourceFile SourceFile(Position pos, PackageNode packageName,
            List<Import> imports, List<TopLevelDecl> decls) {
        SourceFile n =
                SourceFile(pos, packageName, imports, decls, null, extFactory());
        n = del(n, delFactory().delSourceFile());
        return n;
    }

    protected final SourceFile SourceFile(Position pos,
            PackageNode packageName, List<Import> imports,
            List<TopLevelDecl> decls, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extSourceFile());
        return new SourceFile_c(pos,
                                packageName,
                                CollectionUtil.nonNullList(imports),
                                CollectionUtil.nonNullList(decls),
                                ext);
    }

    @Override
    public Special Special(Position pos, Special.Kind kind, TypeNode outer) {
        Special n = Special(pos, kind, outer, null, extFactory());
        n = del(n, delFactory().delSpecial());
        return n;
    }

    protected final Special Special(Position pos, Special.Kind kind,
            TypeNode outer, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extSpecial());
        return new Special_c(pos, kind, outer, ext);
    }

    @Override
    public StringLit StringLit(Position pos, String value) {
        StringLit n = StringLit(pos, value, null, extFactory());
        n = del(n, delFactory().delStringLit());
        return n;
    }

    protected final StringLit StringLit(Position pos, String value, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extStringLit());
        return new StringLit_c(pos, value, ext);
    }

    @Override
    public Switch Switch(Position pos, Expr expr, List<SwitchElement> elements) {
        Switch n = Switch(pos, expr, elements, null, extFactory());
        n = del(n, delFactory().delSwitch());
        return n;
    }

    protected final Switch Switch(Position pos, Expr expr,
            List<SwitchElement> elements, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extSwitch());
        return new Switch_c(pos,
                            expr,
                            CollectionUtil.nonNullList(elements),
                            ext);
    }

    @Override
    public Synchronized Synchronized(Position pos, Expr expr, Block body) {
        Synchronized n = Synchronized(pos, expr, body, null, extFactory());
        n = del(n, delFactory().delSynchronized());
        return n;
    }

    protected final Synchronized Synchronized(Position pos, Expr expr,
            Block body, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extSynchronized());
        return new Synchronized_c(pos, expr, body, ext);
    }

    protected final Term Term(Position pos, Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extTerm());
        return new Term.Instance(pos, ext);
    }

    @Override
    public Throw Throw(Position pos, Expr expr) {
        Throw n = Throw(pos, expr, null, extFactory());
        n = del(n, delFactory().delThrow());
        return n;
    }

    protected final Throw Throw(Position pos, Expr expr, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extThrow());
        return new Throw_c(pos, expr, ext);
    }

    @Override
    public Try Try(Position pos, Block tryBlock, List<Catch> catchBlocks,
            Block finallyBlock) {
        Try n =
                Try(pos,
                    tryBlock,
                    catchBlocks,
                    finallyBlock,
                    null,
                    extFactory());
        n = del(n, delFactory().delTry());
        return n;
    }

    protected final Try Try(Position pos, Block tryBlock,
            List<Catch> catchBlocks, Block finallyBlock, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extTry());
        return new Try_c(pos,
                         tryBlock,
                         CollectionUtil.nonNullList(catchBlocks),
                         finallyBlock,
                         ext);
    }

    protected final TypeNode TypeNode(Position pos, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extTypeNode());
        return new TypeNode.Instance(pos, ext);
    }

    @Override
    public ArrayTypeNode ArrayTypeNode(Position pos, TypeNode base) {
        ArrayTypeNode n = ArrayTypeNode(pos, base, null, extFactory());
        n = del(n, delFactory().delArrayTypeNode());
        return n;
    }

    protected final ArrayTypeNode ArrayTypeNode(Position pos, TypeNode base,
            Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extArrayTypeNode());
        return new ArrayTypeNode_c(pos, base, ext);
    }

    @Override
    public CanonicalTypeNode CanonicalTypeNode(Position pos, Type type) {
        CanonicalTypeNode n = CanonicalTypeNode(pos, type, null, extFactory());
        n = del(n, delFactory().delCanonicalTypeNode());
        return n;
    }

    protected final CanonicalTypeNode CanonicalTypeNode(Position pos,
            Type type, Ext ext, ExtFactory extFactory) {
        if (!type.isCanonical()) {
            throw new InternalCompilerError("Cannot construct a canonical "
                    + "type node for a non-canonical type.");
        }

        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extCanonicalTypeNode());
        return new CanonicalTypeNode_c(pos, type, ext);
    }

    @Override
    public PackageNode PackageNode(Position pos, Package p) {
        PackageNode n = PackageNode(pos, p, null, extFactory());
        n = del(n, delFactory().delPackageNode());
        return n;
    }

    protected final PackageNode PackageNode(Position pos, Package p, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extPackageNode());
        return new PackageNode_c(pos, p, ext);
    }

    @Override
    public Unary Unary(Position pos, Unary.Operator op, Expr expr) {
        Unary n = Unary(pos, op, expr, null, extFactory());
        n = del(n, delFactory().delUnary());
        return n;
    }

    protected final Unary Unary(Position pos, Unary.Operator op, Expr expr,
            Ext ext, ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extUnary());
        return new Unary_c(pos, op, expr, ext);
    }

    @Override
    public While While(Position pos, Expr cond, Stmt body) {
        While n = While(pos, cond, body, null, extFactory());
        n = del(n, delFactory().delWhile());
        return n;
    }

    protected final While While(Position pos, Expr cond, Stmt body, Ext ext,
            ExtFactory extFactory) {
        for (ExtFactory ef : extFactory)
            ext = composeExts(ext, ef.extWhile());
        return new While_c(pos, cond, body, ext);
    }
}
