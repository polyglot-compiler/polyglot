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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import polyglot.types.Flags;
import polyglot.types.Package;
import polyglot.types.Type;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

/**
 * A {@code NodeFactory} constructs AST nodes.  All node construction
 * should go through this factory or be done with the {@code copy()}
 * method of {@code Node}.
 */
public class JLNodeFactory_c extends AbstractNodeFactory_c {
    private final JLang lang;
    private final Map<Lang, Lang> superLangMap;
    private final ExtFactory extFactory;

    public JLNodeFactory_c(JLang lang) {
        this(lang, JLAbstractExtFactory_c.emptyExtFactory);
    }

    public JLNodeFactory_c(JLang lang, ExtFactory extFactory) {
        this.lang = lang;
        superLangMap = new HashMap<>();
        this.extFactory = extFactory;
        try {
            Lang prevLang = null;
            for (ExtFactory ef : extFactory) {
                Lang curLang = ef.lang();
                if (prevLang != null) superLangMap.put(prevLang, curLang);
                prevLang = curLang;
            }
            if (prevLang != null) superLangMap.put(prevLang, JLang_c.instance);
        }
        catch (InternalCompilerError e) {
        }
        initEnums();
    }

    @Override
    public JLang lang() {
        return lang;
    }

    @Override
    public Map<Lang, Lang> superLangMap() {
        return superLangMap;
    }

    /**
     * Ensure the enums in the AST are initialized and interned before any 
     * deserialization occurs.
     */
    protected static void initEnums() {
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
        return extFactory;
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

    private static final Lang primaryLang = JLang_c.instance;

    protected static interface NodeExt {
        Ext ext(ExtFactory extFactory);
    }

    private <N extends Node> void ext(N n, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory, NodeExt ne) {
        for (; extFactory != JLAbstractExtFactory_c.emptyExtFactory; extFactory =
                extFactory.nextExtFactory())
            nodeMap.put(extFactory.lang(), ne.ext(extFactory));
        nodeMap.put(JLang_c.instance, n);
    }

    protected final <N extends Node> void finalize(N n, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory, NodeExt ne) {
        ext(n, nodeMap, extFactory, ne);
        for (NodeOps o : nodeMap.values()) {
            o.initPrimaryLang(primaryLang);
            o.initNodeMap(nodeMap);
        }
    }

    @Override
    public Id Id(Position pos, String name) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Id(pos, name, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extId = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extId();
        }
    };

    protected final Id Id(Position pos, String name, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Id n = new Id_c(pos, name);
        finalize(n, primaryLang, nodeMap, extFactory, extId);
        return n;
    }

    @Override
    public AmbPrefix AmbPrefix(Position pos, Prefix prefix, Id name) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return AmbPrefix(pos, prefix, name, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extAmbPrefix = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extAmbPrefix();
        }
    };

    protected final AmbPrefix AmbPrefix(Position pos, Prefix prefix, Id name,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        AmbPrefix n = new AmbPrefix_c(pos, prefix, name);
        finalize(n, primaryLang, nodeMap, extFactory, extAmbPrefix);
        return n;
    }

    @Override
    public AmbReceiver AmbReceiver(Position pos, Prefix prefix, Id name) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return AmbReceiver(pos,
                           prefix,
                           name,
                           primaryLang,
                           nodeMap,
                           extFactory());
    }

    protected static final NodeExt extAmbReceiver = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extAmbReceiver();
        }
    };

    protected final AmbReceiver AmbReceiver(Position pos, Prefix prefix,
            Id name, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        AmbReceiver n = new AmbReceiver_c(pos, prefix, name);
        finalize(n, primaryLang, nodeMap, extFactory, extAmbReceiver);
        return n;
    }

    @Override
    public AmbQualifierNode AmbQualifierNode(Position pos,
            QualifierNode qualifier, Id name) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return AmbQualifierNode(pos,
                                qualifier,
                                name,
                                primaryLang,
                                nodeMap,
                                extFactory());
    }

    protected static final NodeExt extAmbQualifierNode = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extAmbQualifierNode();
        }
    };

    protected final AmbQualifierNode AmbQualifierNode(Position pos,
            QualifierNode qualifier, Id name, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        AmbQualifierNode n = new AmbQualifierNode_c(pos, qualifier, name);
        finalize(n, primaryLang, nodeMap, extFactory, extAmbQualifierNode);
        return n;
    }

    @Override
    public AmbExpr AmbExpr(Position pos, Id name) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return AmbExpr(pos, name, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extAmbExpr = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extAmbExpr();
        }
    };

    protected final AmbExpr AmbExpr(Position pos, Id name, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        AmbExpr n = new AmbExpr_c(pos, name);
        finalize(n, primaryLang, nodeMap, extFactory, extAmbExpr);
        return n;
    }

    @Override
    public AmbTypeNode AmbTypeNode(Position pos, QualifierNode qualifier,
            Id name) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return AmbTypeNode(pos,
                           qualifier,
                           name,
                           primaryLang,
                           nodeMap,
                           extFactory());
    }

    protected static final NodeExt extAmbTypeNode = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extAmbTypeNode();
        }
    };

    protected final AmbTypeNode AmbTypeNode(Position pos,
            QualifierNode qualifier, Id name, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        AmbTypeNode n = new AmbTypeNode_c(pos, qualifier, name);
        finalize(n, primaryLang, nodeMap, extFactory, extAmbTypeNode);
        return n;
    }

    @Override
    public ArrayAccess ArrayAccess(Position pos, Expr base, Expr index) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return ArrayAccess(pos, base, index, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extArrayAccess = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extArrayAccess();
        }
    };

    protected final ArrayAccess ArrayAccess(Position pos, Expr base,
            Expr index, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        ArrayAccess n = new ArrayAccess_c(pos, base, index);
        finalize(n, primaryLang, nodeMap, extFactory, extArrayAccess);
        return n;
    }

    @Override
    public ArrayInit ArrayInit(Position pos, List<Expr> elements) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return ArrayInit(pos, elements, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extArrayInit = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extArrayInit();
        }
    };

    protected final ArrayInit ArrayInit(Position pos, List<Expr> elements,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        ArrayInit n =
                new ArrayInit_c(pos, CollectionUtil.nonNullList(elements));
        finalize(n, primaryLang, nodeMap, extFactory, extArrayInit);
        return n;
    }

    @Override
    public Assert Assert(Position pos, Expr cond, Expr errorMessage) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Assert(pos,
                      cond,
                      errorMessage,
                      primaryLang,
                      nodeMap,
                      extFactory());
    }

    protected static final NodeExt extAssert = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extAssert();
        }
    };

    protected final Assert Assert(Position pos, Expr cond, Expr errorMessage,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Assert n = new Assert_c(pos, cond, errorMessage);
        finalize(n, primaryLang, nodeMap, extFactory, extAssert);
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
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return LocalAssign(pos,
                           left,
                           op,
                           right,
                           primaryLang,
                           nodeMap,
                           extFactory());
    }

    protected static final NodeExt extLocalAssign = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extLocalAssign();
        }
    };

    protected final LocalAssign LocalAssign(Position pos, Local left,
            Assign.Operator op, Expr right, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        LocalAssign n = new LocalAssign_c(pos, left, op, right);
        finalize(n, primaryLang, nodeMap, extFactory, extLocalAssign);
        return n;
    }

    @Override
    public FieldAssign FieldAssign(Position pos, Field left,
            Assign.Operator op, Expr right) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return FieldAssign(pos,
                           left,
                           op,
                           right,
                           primaryLang,
                           nodeMap,
                           extFactory());
    }

    protected static final NodeExt extFieldAssign = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extFieldAssign();
        }
    };

    protected final FieldAssign FieldAssign(Position pos, Field left,
            Assign.Operator op, Expr right, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        FieldAssign n = new FieldAssign_c(pos, left, op, right);
        finalize(n, primaryLang, nodeMap, extFactory, extFieldAssign);
        return n;
    }

    @Override
    public ArrayAccessAssign ArrayAccessAssign(Position pos, ArrayAccess left,
            Assign.Operator op, Expr right) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return ArrayAccessAssign(pos,
                                 left,
                                 op,
                                 right,
                                 primaryLang,
                                 nodeMap,
                                 extFactory());
    }

    protected static final NodeExt extArrayAccessAssign = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extArrayAccessAssign();
        }
    };

    protected final ArrayAccessAssign ArrayAccessAssign(Position pos,
            ArrayAccess left, Assign.Operator op, Expr right, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        ArrayAccessAssign n = new ArrayAccessAssign_c(pos, left, op, right);
        finalize(n, primaryLang, nodeMap, extFactory, extArrayAccessAssign);
        return n;
    }

    @Override
    public AmbAssign AmbAssign(Position pos, Expr left, Assign.Operator op,
            Expr right) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return AmbAssign(pos,
                         left,
                         op,
                         right,
                         primaryLang,
                         nodeMap,
                         extFactory());
    }

    protected static final NodeExt extAmbAssign = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extAmbAssign();
        }
    };

    protected final AmbAssign AmbAssign(Position pos, Expr left,
            Assign.Operator op, Expr right, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        AmbAssign n = new AmbAssign_c(pos, left, op, right);
        finalize(n, primaryLang, nodeMap, extFactory, extAmbAssign);
        return n;
    }

    @Override
    public Binary Binary(Position pos, Expr left, Binary.Operator op, Expr right) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Binary(pos, left, op, right, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extBinary = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extBinary();
        }
    };

    protected final Binary Binary(Position pos, Expr left, Binary.Operator op,
            Expr right, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        Binary n = new Binary_c(pos, left, op, right);
        finalize(n, primaryLang, nodeMap, extFactory, extBinary);
        return n;
    }

    @Override
    public Block Block(Position pos, List<Stmt> statements) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Block(pos, statements, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extBlock = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extBlock();
        }
    };

    protected final Block Block(Position pos, List<Stmt> statements,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Block n = new Block_c(pos, CollectionUtil.nonNullList(statements));
        finalize(n, primaryLang, nodeMap, extFactory, extBlock);
        return n;
    }

    @Override
    public SwitchBlock SwitchBlock(Position pos, List<Stmt> statements) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return SwitchBlock(pos, statements, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extSwitchBlock = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extSwitchBlock();
        }
    };

    protected final SwitchBlock SwitchBlock(Position pos,
            List<Stmt> statements, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        SwitchBlock n =
                new SwitchBlock_c(pos, CollectionUtil.nonNullList(statements));
        finalize(n, primaryLang, nodeMap, extFactory, extSwitchBlock);
        return n;
    }

    @Override
    public BooleanLit BooleanLit(Position pos, boolean value) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return BooleanLit(pos, value, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extBooleanLit = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extBooleanLit();
        }
    };

    protected final BooleanLit BooleanLit(Position pos, boolean value,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        BooleanLit n = new BooleanLit_c(pos, value);
        finalize(n, primaryLang, nodeMap, extFactory, extBooleanLit);
        return n;
    }

    @Override
    public Branch Branch(Position pos, Branch.Kind kind, Id label) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Branch(pos, kind, label, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extBranch = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extBranch();
        }
    };

    protected final Branch Branch(Position pos, Branch.Kind kind, Id label,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Branch n = new Branch_c(pos, kind, label);
        finalize(n, primaryLang, nodeMap, extFactory, extBranch);
        return n;
    }

    @Override
    public Call Call(Position pos, Receiver target, Id name, List<Expr> args) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Call(pos, target, name, args, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extCall = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extCall();
        }
    };

    protected final Call Call(Position pos, Receiver target, Id name,
            List<Expr> args, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        Call n =
                new Call_c(pos, target, name, CollectionUtil.nonNullList(args));
        finalize(n, primaryLang, nodeMap, extFactory, extCall);
        return n;
    }

    @Override
    public Case Case(Position pos, Expr expr) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Case(pos, expr, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extCase = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extCase();
        }
    };

    protected final Case Case(Position pos, Expr expr, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Case n = new Case_c(pos, expr);
        finalize(n, primaryLang, nodeMap, extFactory, extCase);
        return n;
    }

    @Override
    public Cast Cast(Position pos, TypeNode type, Expr expr) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Cast(pos, type, expr, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extCast = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extCast();
        }
    };

    protected final Cast Cast(Position pos, TypeNode type, Expr expr,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Cast n = new Cast_c(pos, type, expr);
        finalize(n, primaryLang, nodeMap, extFactory, extCast);
        return n;
    }

    @Override
    public Catch Catch(Position pos, Formal formal, Block body) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Catch(pos, formal, body, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extCatch = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extCatch();
        }
    };

    protected final Catch Catch(Position pos, Formal formal, Block body,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Catch n = new Catch_c(pos, formal, body);
        finalize(n, primaryLang, nodeMap, extFactory, extCatch);
        return n;
    }

    @Override
    public CharLit CharLit(Position pos, char value) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return CharLit(pos, value, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extCharLit = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extCharLit();
        }
    };

    protected final CharLit CharLit(Position pos, char value, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        CharLit n = new CharLit_c(pos, value);
        finalize(n, primaryLang, nodeMap, extFactory, extCharLit);
        return n;
    }

    @Override
    public ClassBody ClassBody(Position pos, List<ClassMember> members) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return ClassBody(pos, members, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extClassBody = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extClassBody();
        }
    };

    protected final ClassBody ClassBody(Position pos,
            List<ClassMember> members, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        ClassBody n = new ClassBody_c(pos, CollectionUtil.nonNullList(members));
        finalize(n, primaryLang, nodeMap, extFactory, extClassBody);
        return n;
    }

    @Override
    public ClassDecl ClassDecl(Position pos, Flags flags, Id name,
            TypeNode superClass, List<TypeNode> interfaces, ClassBody body) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return ClassDecl(pos,
                         flags,
                         name,
                         superClass,
                         interfaces,
                         body,
                         primaryLang,
                         nodeMap,
                         extFactory());
    }

    protected static final NodeExt extClassDecl = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extClassDecl();
        }
    };

    protected final ClassDecl ClassDecl(Position pos, Flags flags, Id name,
            TypeNode superClass, List<TypeNode> interfaces, ClassBody body,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        ClassDecl n =
                new ClassDecl_c(pos,
                                flags,
                                name,
                                superClass,
                                CollectionUtil.nonNullList(interfaces),
                                body);
        finalize(n, primaryLang, nodeMap, extFactory, extClassDecl);
        return n;
    }

    @Override
    public ClassLit ClassLit(Position pos, TypeNode typeNode) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return ClassLit(pos, typeNode, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extClassLit = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extClassLit();
        }
    };

    protected final ClassLit ClassLit(Position pos, TypeNode typeNode,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        ClassLit n = new ClassLit_c(pos, typeNode);
        finalize(n, primaryLang, nodeMap, extFactory, extClassLit);
        return n;
    }

    @Override
    public Conditional Conditional(Position pos, Expr cond, Expr consequent,
            Expr alternative) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Conditional(pos,
                           cond,
                           consequent,
                           alternative,
                           primaryLang,
                           nodeMap,
                           extFactory());
    }

    protected static final NodeExt extConditional = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extConditional();
        }
    };

    protected final Conditional Conditional(Position pos, Expr cond,
            Expr consequent, Expr alternative, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Conditional n = new Conditional_c(pos, cond, consequent, alternative);
        finalize(n, primaryLang, nodeMap, extFactory, extConditional);
        return n;
    }

    @Override
    public ConstructorCall ConstructorCall(Position pos,
            ConstructorCall.Kind kind, Expr outer, List<Expr> args) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return ConstructorCall(pos,
                               kind,
                               outer,
                               args,
                               primaryLang,
                               nodeMap,
                               extFactory());
    }

    protected static final NodeExt extConstructorCall = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extConstructorCall();
        }
    };

    protected final ConstructorCall ConstructorCall(Position pos,
            ConstructorCall.Kind kind, Expr outer, List<Expr> args,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        ConstructorCall n =
                new ConstructorCall_c(pos,
                                      kind,
                                      outer,
                                      CollectionUtil.nonNullList(args));
        finalize(n, primaryLang, nodeMap, extFactory, extConstructorCall);
        return n;
    }

    @Override
    public ConstructorDecl ConstructorDecl(Position pos, Flags flags, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return ConstructorDecl(pos,
                               flags,
                               name,
                               formals,
                               throwTypes,
                               body,
                               primaryLang,
                               nodeMap,
                               extFactory());
    }

    protected static final NodeExt extConstructorDecl = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extConstructorDecl();
        }
    };

    protected final ConstructorDecl ConstructorDecl(Position pos, Flags flags,
            Id name, List<Formal> formals, List<TypeNode> throwTypes,
            Block body, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        ConstructorDecl n =
                new ConstructorDecl_c(pos,
                                      flags,
                                      name,
                                      CollectionUtil.nonNullList(formals),
                                      CollectionUtil.nonNullList(throwTypes),
                                      body);
        finalize(n, primaryLang, nodeMap, extFactory, extConstructorDecl);
        return n;
    }

    @Override
    public FieldDecl FieldDecl(Position pos, Flags flags, TypeNode type,
            Id name, Expr init) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return FieldDecl(pos,
                         flags,
                         type,
                         name,
                         init,
                         primaryLang,
                         nodeMap,
                         extFactory());
    }

    protected static final NodeExt extFieldDecl = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extFieldDecl();
        }
    };

    protected final FieldDecl FieldDecl(Position pos, Flags flags,
            TypeNode type, Id name, Expr init, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        FieldDecl n = new FieldDecl_c(pos, flags, type, name, init);
        finalize(n, primaryLang, nodeMap, extFactory, extFieldDecl);
        return n;
    }

    @Override
    public Do Do(Position pos, Stmt body, Expr cond) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Do(pos, body, cond, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extDo = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extDo();
        }
    };

    protected final Do Do(Position pos, Stmt body, Expr cond, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Do n = new Do_c(pos, body, cond);
        finalize(n, primaryLang, nodeMap, extFactory, extDo);
        return n;
    }

    @Override
    public Empty Empty(Position pos) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Empty(pos, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extEmpty = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extEmpty();
        }
    };

    protected final Empty Empty(Position pos, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Empty n = new Empty_c(pos);
        finalize(n, primaryLang, nodeMap, extFactory, extEmpty);
        return n;
    }

    @Override
    public Eval Eval(Position pos, Expr expr) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Eval(pos, expr, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extEval = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extEval();
        }
    };

    protected final Eval Eval(Position pos, Expr expr, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Eval n = new Eval_c(pos, expr);
        finalize(n, primaryLang, nodeMap, extFactory, extEval);
        return n;
    }

    @Override
    public Field Field(Position pos, Receiver target, Id name) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Field(pos, target, name, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extField = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extField();
        }
    };

    protected final Field Field(Position pos, Receiver target, Id name,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Field n = new Field_c(pos, target, name);
        finalize(n, primaryLang, nodeMap, extFactory, extField);
        return n;
    }

    @Override
    public FloatLit FloatLit(Position pos, FloatLit.Kind kind, double value) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return FloatLit(pos, kind, value, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extFloatLit = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extFloatLit();
        }
    };

    protected final FloatLit FloatLit(Position pos, FloatLit.Kind kind,
            double value, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        FloatLit n = new FloatLit_c(pos, kind, value);
        finalize(n, primaryLang, nodeMap, extFactory, extFloatLit);
        return n;
    }

    @Override
    public For For(Position pos, List<ForInit> inits, Expr cond,
            List<ForUpdate> iters, Stmt body) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return For(pos,
                   inits,
                   cond,
                   iters,
                   body,
                   primaryLang,
                   nodeMap,
                   extFactory());
    }

    protected static final NodeExt extFor = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extFor();
        }
    };

    protected final For For(Position pos, List<ForInit> inits, Expr cond,
            List<ForUpdate> iters, Stmt body, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        For n =
                new For_c(pos,
                          CollectionUtil.nonNullList(inits),
                          cond,
                          CollectionUtil.nonNullList(iters),
                          body);
        finalize(n, primaryLang, nodeMap, extFactory, extFor);
        return n;
    }

    @Override
    public Formal Formal(Position pos, Flags flags, TypeNode type, Id name) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Formal(pos,
                      flags,
                      type,
                      name,
                      primaryLang,
                      nodeMap,
                      extFactory());
    }

    protected static final NodeExt extFormal = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extFormal();
        }
    };

    protected final Formal Formal(Position pos, Flags flags, TypeNode type,
            Id name, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        Formal n = new Formal_c(pos, flags, type, name);
        finalize(n, primaryLang, nodeMap, extFactory, extFormal);
        return n;
    }

    @Override
    public If If(Position pos, Expr cond, Stmt consequent, Stmt alternative) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return If(pos,
                  cond,
                  consequent,
                  alternative,
                  primaryLang,
                  nodeMap,
                  extFactory());
    }

    protected static final NodeExt extIf = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extIf();
        }
    };

    protected final If If(Position pos, Expr cond, Stmt consequent,
            Stmt alternative, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        If n = new If_c(pos, cond, consequent, alternative);
        finalize(n, primaryLang, nodeMap, extFactory, extIf);
        return n;
    }

    @Override
    public Import Import(Position pos, Import.Kind kind, String name) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Import(pos, kind, name, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extImport = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extImport();
        }
    };

    protected final Import Import(Position pos, Import.Kind kind, String name,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Import n = new Import_c(pos, kind, name);
        finalize(n, primaryLang, nodeMap, extFactory, extImport);
        return n;
    }

    @Override
    public Initializer Initializer(Position pos, Flags flags, Block body) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Initializer(pos, flags, body, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extInitializer = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extInitializer();
        }
    };

    protected final Initializer Initializer(Position pos, Flags flags,
            Block body, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        Initializer n = new Initializer_c(pos, flags, body);
        finalize(n, primaryLang, nodeMap, extFactory, extInitializer);
        return n;
    }

    @Override
    public Instanceof Instanceof(Position pos, Expr expr, TypeNode type) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Instanceof(pos, expr, type, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extInstanceof = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extInstanceof();
        }
    };

    protected final Instanceof Instanceof(Position pos, Expr expr,
            TypeNode type, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        Instanceof n = new Instanceof_c(pos, expr, type);
        finalize(n, primaryLang, nodeMap, extFactory, extInstanceof);
        return n;
    }

    @Override
    public IntLit IntLit(Position pos, IntLit.Kind kind, long value) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return IntLit(pos, kind, value, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extIntLit = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extIntLit();
        }
    };

    protected final IntLit IntLit(Position pos, IntLit.Kind kind, long value,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        IntLit n = new IntLit_c(pos, kind, value);
        finalize(n, primaryLang, nodeMap, extFactory, extIntLit);
        return n;
    }

    @Override
    public Labeled Labeled(Position pos, Id label, Stmt body) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Labeled(pos, label, body, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extLabeled = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extLabeled();
        }
    };

    protected final Labeled Labeled(Position pos, Id label, Stmt body,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Labeled n = new Labeled_c(pos, label, body);
        finalize(n, primaryLang, nodeMap, extFactory, extLabeled);
        return n;
    }

    @Override
    public Local Local(Position pos, Id name) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Local(pos, name, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extLocal = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extLocal();
        }
    };

    protected final Local Local(Position pos, Id name, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Local n = new Local_c(pos, name);
        finalize(n, primaryLang, nodeMap, extFactory, extLocal);
        return n;
    }

    @Override
    public LocalClassDecl LocalClassDecl(Position pos, ClassDecl decl) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return LocalClassDecl(pos, decl, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extLocalClassDecl = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extLocalClassDecl();
        }
    };

    protected final LocalClassDecl LocalClassDecl(Position pos, ClassDecl decl,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        LocalClassDecl n = new LocalClassDecl_c(pos, decl);
        finalize(n, primaryLang, nodeMap, extFactory, extLocalClassDecl);
        return n;
    }

    @Override
    public LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type,
            Id name, Expr init) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return LocalDecl(pos,
                         flags,
                         type,
                         name,
                         init,
                         primaryLang,
                         nodeMap,
                         extFactory());
    }

    protected static final NodeExt extLocalDecl = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extLocalDecl();
        }
    };

    protected final LocalDecl LocalDecl(Position pos, Flags flags,
            TypeNode type, Id name, Expr init, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        LocalDecl n = new LocalDecl_c(pos, flags, type, name, init);
        finalize(n, primaryLang, nodeMap, extFactory, extLocalDecl);
        return n;
    }

    @Override
    public MethodDecl MethodDecl(Position pos, Flags flags,
            TypeNode returnType, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return MethodDecl(pos,
                          flags,
                          returnType,
                          name,
                          formals,
                          throwTypes,
                          body,
                          primaryLang,
                          nodeMap,
                          extFactory());
    }

    protected static final NodeExt extMethodDecl = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extMethodDecl();
        }
    };

    protected final MethodDecl MethodDecl(Position pos, Flags flags,
            TypeNode returnType, Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        MethodDecl n =
                new MethodDecl_c(pos,
                                 flags,
                                 returnType,
                                 name,
                                 CollectionUtil.nonNullList(formals),
                                 CollectionUtil.nonNullList(throwTypes),
                                 body);
        finalize(n, primaryLang, nodeMap, extFactory, extMethodDecl);
        return n;
    }

    @Override
    public New New(Position pos, Expr outer, TypeNode objectType,
            List<Expr> args, ClassBody body) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return New(pos,
                   outer,
                   objectType,
                   args,
                   body,
                   primaryLang,
                   nodeMap,
                   extFactory());
    }

    protected static final NodeExt extNew = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extNew();
        }
    };

    protected final New New(Position pos, Expr outer, TypeNode objectType,
            List<Expr> args, ClassBody body, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        New n =
                new New_c(pos,
                          outer,
                          objectType,
                          CollectionUtil.nonNullList(args),
                          body);
        finalize(n, primaryLang, nodeMap, extFactory, extNew);
        return n;
    }

    @Override
    public NewArray NewArray(Position pos, TypeNode base, List<Expr> dims,
            int addDims, ArrayInit init) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return NewArray(pos,
                        base,
                        dims,
                        addDims,
                        init,
                        primaryLang,
                        nodeMap,
                        extFactory());
    }

    protected static final NodeExt extNewArray = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extNewArray();
        }
    };

    protected final NewArray NewArray(Position pos, TypeNode base,
            List<Expr> dims, int addDims, ArrayInit init, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        NewArray n =
                new NewArray_c(pos,
                               base,
                               CollectionUtil.nonNullList(dims),
                               addDims,
                               init);
        finalize(n, primaryLang, nodeMap, extFactory, extNewArray);
        return n;
    }

    @Override
    public NullLit NullLit(Position pos) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return NullLit(pos, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extNullLit = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extNullLit();
        }
    };

    protected final NullLit NullLit(Position pos, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        NullLit n = new NullLit_c(pos);
        finalize(n, primaryLang, nodeMap, extFactory, extNullLit);
        return n;
    }

    @Override
    public Return Return(Position pos, Expr expr) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Return(pos, expr, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extReturn = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extReturn();
        }
    };

    protected final Return Return(Position pos, Expr expr, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Return n = new Return_c(pos, expr);
        finalize(n, primaryLang, nodeMap, extFactory, extReturn);
        return n;
    }

    @Override
    public SourceCollection SourceCollection(Position pos,
            List<SourceFile> sources) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return SourceCollection(pos,
                                sources,
                                primaryLang,
                                nodeMap,
                                extFactory());
    }

    protected static final NodeExt extSourceCollection = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extSourceCollection();
        }
    };

    protected final SourceCollection SourceCollection(Position pos,
            List<SourceFile> sources, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        SourceCollection n =
                new SourceCollection_c(pos, CollectionUtil.nonNullList(sources));
        finalize(n, primaryLang, nodeMap, extFactory, extSourceCollection);
        return n;
    }

    @Override
    public SourceFile SourceFile(Position pos, PackageNode packageName,
            List<Import> imports, List<TopLevelDecl> decls) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return SourceFile(pos,
                          packageName,
                          imports,
                          decls,
                          primaryLang,
                          nodeMap,
                          extFactory());
    }

    protected static final NodeExt extSourceFile = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extSourceFile();
        }
    };

    protected final SourceFile SourceFile(Position pos,
            PackageNode packageName, List<Import> imports,
            List<TopLevelDecl> decls, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        SourceFile n =
                new SourceFile_c(pos,
                                 packageName,
                                 CollectionUtil.nonNullList(imports),
                                 CollectionUtil.nonNullList(decls));
        finalize(n, primaryLang, nodeMap, extFactory, extSourceFile);
        return n;
    }

    @Override
    public Special Special(Position pos, Special.Kind kind, TypeNode outer) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Special(pos, kind, outer, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extSpecial = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extSpecial();
        }
    };

    protected final Special Special(Position pos, Special.Kind kind,
            TypeNode outer, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        Special n = new Special_c(pos, kind, outer);
        finalize(n, primaryLang, nodeMap, extFactory, extSpecial);
        return n;
    }

    @Override
    public StringLit StringLit(Position pos, String value) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return StringLit(pos, value, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extStringLit = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extStringLit();
        }
    };

    protected final StringLit StringLit(Position pos, String value,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        StringLit n = new StringLit_c(pos, value);
        finalize(n, primaryLang, nodeMap, extFactory, extStringLit);
        return n;
    }

    @Override
    public Switch Switch(Position pos, Expr expr, List<SwitchElement> elements) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Switch(pos, expr, elements, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extSwitch = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extSwitch();
        }
    };

    protected final Switch Switch(Position pos, Expr expr,
            List<SwitchElement> elements, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Switch n =
                new Switch_c(pos, expr, CollectionUtil.nonNullList(elements));
        finalize(n, primaryLang, nodeMap, extFactory, extSwitch);
        return n;
    }

    @Override
    public Synchronized Synchronized(Position pos, Expr expr, Block body) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Synchronized(pos, expr, body, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extSynchronized = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extSynchronized();
        }
    };

    protected final Synchronized Synchronized(Position pos, Expr expr,
            Block body, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        Synchronized n = new Synchronized_c(pos, expr, body);
        finalize(n, primaryLang, nodeMap, extFactory, extSynchronized);
        return n;
    }

    @Override
    public Throw Throw(Position pos, Expr expr) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Throw(pos, expr, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extThrow = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extThrow();
        }
    };

    protected final Throw Throw(Position pos, Expr expr, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Throw n = new Throw_c(pos, expr);
        finalize(n, primaryLang, nodeMap, extFactory, extThrow);
        return n;
    }

    @Override
    public Try Try(Position pos, Block tryBlock, List<Catch> catchBlocks,
            Block finallyBlock) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Try(pos,
                   tryBlock,
                   catchBlocks,
                   finallyBlock,
                   primaryLang,
                   nodeMap,
                   extFactory());
    }

    protected static final NodeExt extTry = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extTry();
        }
    };

    protected final Try Try(Position pos, Block tryBlock,
            List<Catch> catchBlocks, Block finallyBlock, Lang primaryLang,
            Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Try n =
                new Try_c(pos,
                          tryBlock,
                          CollectionUtil.nonNullList(catchBlocks),
                          finallyBlock);
        finalize(n, primaryLang, nodeMap, extFactory, extTry);
        return n;
    }

    @Override
    public ArrayTypeNode ArrayTypeNode(Position pos, TypeNode base) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return ArrayTypeNode(pos, base, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extArrayTypeNode = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extArrayTypeNode();
        }
    };

    protected final ArrayTypeNode ArrayTypeNode(Position pos, TypeNode base,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        ArrayTypeNode n = new ArrayTypeNode_c(pos, base);
        finalize(n, primaryLang, nodeMap, extFactory, extArrayTypeNode);
        return n;
    }

    @Override
    public CanonicalTypeNode CanonicalTypeNode(Position pos, Type type) {
        if (!type.isCanonical()) {
            throw new InternalCompilerError("Cannot construct a canonical "
                    + "type node for a non-canonical type.");
        }

        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return CanonicalTypeNode(pos, type, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extCanonicalTypeNode = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extCanonicalTypeNode();
        }
    };

    protected final CanonicalTypeNode CanonicalTypeNode(Position pos,
            Type type, Lang primaryLang, Map<Lang, NodeOps> nodeMap,
            ExtFactory extFactory) {
        CanonicalTypeNode n = new CanonicalTypeNode_c(pos, type);
        finalize(n, primaryLang, nodeMap, extFactory, extCanonicalTypeNode);
        return n;
    }

    @Override
    public PackageNode PackageNode(Position pos, Package p) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return PackageNode(pos, p, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extPackageNode = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extPackageNode();
        }
    };

    protected final PackageNode PackageNode(Position pos, Package p,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        PackageNode n = new PackageNode_c(pos, p);
        finalize(n, primaryLang, nodeMap, extFactory, extPackageNode);
        return n;
    }

    @Override
    public Unary Unary(Position pos, Unary.Operator op, Expr expr) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return Unary(pos, op, expr, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extUnary = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extUnary();
        }
    };

    protected final Unary Unary(Position pos, Unary.Operator op, Expr expr,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        Unary n = new Unary_c(pos, op, expr);
        finalize(n, primaryLang, nodeMap, extFactory, extUnary);
        return n;
    }

    @Override
    public While While(Position pos, Expr cond, Stmt body) {
        Map<Lang, NodeOps> nodeMap = new HashMap<>();
        return While(pos, cond, body, primaryLang, nodeMap, extFactory());
    }

    protected static final NodeExt extWhile = new NodeExt() {
        @Override
        public Ext ext(ExtFactory extFactory) {
            return extFactory.extWhile();
        }
    };

    protected final While While(Position pos, Expr cond, Stmt body,
            Lang primaryLang, Map<Lang, NodeOps> nodeMap, ExtFactory extFactory) {
        While n = new While_c(pos, cond, body);
        finalize(n, primaryLang, nodeMap, extFactory, extWhile);
        return n;
    }
}
