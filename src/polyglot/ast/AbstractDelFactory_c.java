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

/**
 * This abstract implementation of {@code DelFactory} provides
 * a way of chaining together DelFactories, and default implementations
 * of factory methods for each node.
 * 
 * <p>
 * For a given type of AST node {@code N}, there are three methods:
 * {@code delN()},  {@code delNImpl()} and {@code postDelN(JL)}. 
 * The method {@code delN()} calls {@code delNImpl()} to create
 * an appropriate delegate object, and if other {@code DelFactory}s are 
 * chained onto this one, it will also call {@code delN()} on the next 
 * {@code DelFactory}. The method {@code delN()} will then 
 * call {@code postDelN}, passing in the newly created extension object.
 * 
 * <p>
 * The default implementation of {@code delNImpl()} is to simply call
 * {@code delMImpl()}, where {@code M} is the immediate 
 * superclass of {@code N}. Similarly, the default implementation of
 * {@code postDelN(JL)} is to call {@code postDelM(JL)}.
 * 
 * @see polyglot.ast.AbstractExtFactory_c has a very similar structure. 
 */
@Deprecated
public abstract class AbstractDelFactory_c implements DelFactory {
    protected AbstractDelFactory_c() {
        this(null);
    }

    protected AbstractDelFactory_c(DelFactory nextDelFactory) {
        this.nextDelFactory = nextDelFactory;
    }

    /**
     * The next delFactory in the chain. Whenever an extension is instantiated,
     * the next delFactory should be called to see if it also has an extension,
     * and if so, the extensions should be joined together using the method
     * {@code composeDels}
     */
    private DelFactory nextDelFactory;

    public DelFactory nextDelFactory() {
        return nextDelFactory;
    }

    /**
     * Compose two delegates together. This operation is extension specific,
     * and so in this class it throws an UnsupportedOperationException. 
     * A typical use of this method would be to add e2 as the "superclass" delegate
     * to e1.
     * 
     * @param e1 a {@code JL} object created by this DelFactory.
     * @param e2 a  {@code JL} object created by this.nextDelFactory
     * @return the result of composing e1 and e2.
     */
    protected JLDel composeDels(JLDel e1, JLDel e2) {
        throw new UnsupportedOperationException("Composition of delegates unimplemented.");
    }

    // ******************************************
    // Final methods that call the Impl methods to construct 
    // extensions, and then check with nextDelFactory to see if it
    // also has an extension. Finally, call an appropriate post method,
    // to allow subclasses to perform operations on the construction Exts
    // ******************************************
    @Override
    public final JLDel delId() {
        JLDel e = delIdImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delId();
            e = composeDels(e, e2);
        }
        return postDelId(e);
    }

    @Override
    public final JLDel delAmbAssign() {
        JLDel e = delAmbAssignImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delAmbAssign();
            e = composeDels(e, e2);
        }
        return postDelAmbAssign(e);
    }

    @Override
    public final JLDel delAmbExpr() {
        JLDel e = delAmbExprImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delAmbExpr();
            e = composeDels(e, e2);
        }
        return postDelAmbExpr(e);
    }

    @Override
    public final JLDel delAmbPrefix() {
        JLDel e = delAmbPrefixImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delAmbPrefix();
            e = composeDels(e, e2);
        }
        return postDelAmbPrefix(e);
    }

    @Override
    public final JLDel delAmbQualifierNode() {
        JLDel e = delAmbQualifierNodeImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delAmbQualifierNode();
            e = composeDels(e, e2);
        }
        return postDelAmbQualifierNode(e);
    }

    @Override
    public final JLDel delAmbReceiver() {
        JLDel e = delAmbReceiverImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delAmbReceiver();
            e = composeDels(e, e2);
        }
        return postDelAmbReceiver(e);
    }

    @Override
    public final JLDel delAmbTypeNode() {
        JLDel e = delAmbTypeNodeImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delAmbTypeNode();
            e = composeDels(e, e2);
        }
        return postDelAmbTypeNode(e);
    }

    @Override
    public final JLDel delArrayAccess() {
        JLDel e = delArrayAccessImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delArrayAccess();
            e = composeDels(e, e2);
        }
        return postDelArrayAccess(e);
    }

    @Override
    public final JLDel delArrayInit() {
        JLDel e = delArrayInitImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delArrayInit();
            e = composeDels(e, e2);
        }
        return postDelArrayInit(e);
    }

    @Override
    public final JLDel delArrayTypeNode() {
        JLDel e = delArrayTypeNodeImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delArrayTypeNode();
            e = composeDels(e, e2);
        }
        return postDelArrayTypeNode(e);
    }

    @Override
    public final JLDel delAssert() {
        JLDel e = delAssertImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delAssert();
            e = composeDels(e, e2);
        }
        return postDelAssert(e);
    }

    @Override
    public final JLDel delAssign() {
        JLDel e = delAssignImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delAssign();
            e = composeDels(e, e2);
        }
        return postDelAssign(e);
    }

    @Override
    public final JLDel delLocalAssign() {
        JLDel e = delLocalAssignImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delLocalAssign();
            e = composeDels(e, e2);
        }
        return postDelLocalAssign(e);
    }

    @Override
    public final JLDel delFieldAssign() {
        JLDel e = delFieldAssignImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delFieldAssign();
            e = composeDels(e, e2);
        }
        return postDelFieldAssign(e);
    }

    @Override
    public final JLDel delArrayAccessAssign() {
        JLDel e = delArrayAccessAssignImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delArrayAccessAssign();
            e = composeDels(e, e2);
        }
        return postDelArrayAccessAssign(e);
    }

    @Override
    public final JLDel delBinary() {
        JLDel e = delBinaryImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delBinary();
            e = composeDels(e, e2);
        }
        return postDelBinary(e);
    }

    @Override
    public final JLDel delBlock() {
        JLDel e = delBlockImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delBlock();
            e = composeDels(e, e2);
        }
        return postDelBlock(e);
    }

    @Override
    public final JLDel delBooleanLit() {
        JLDel e = delBooleanLitImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delBooleanLit();
            e = composeDels(e, e2);
        }
        return postDelBooleanLit(e);
    }

    @Override
    public final JLDel delBranch() {
        JLDel e = delBranchImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delBranch();
            e = composeDels(e, e2);
        }
        return postDelBranch(e);
    }

    @Override
    public final JLDel delCall() {
        JLDel e = delCallImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delCall();
            e = composeDels(e, e2);
        }
        return postDelCall(e);
    }

    @Override
    public final JLDel delCanonicalTypeNode() {
        JLDel e = delCanonicalTypeNodeImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delCanonicalTypeNode();
            e = composeDels(e, e2);
        }
        return postDelCanonicalTypeNode(e);
    }

    @Override
    public final JLDel delCase() {
        JLDel e = delCaseImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delCase();
            e = composeDels(e, e2);
        }
        return postDelCase(e);
    }

    @Override
    public final JLDel delCast() {
        JLDel e = delCastImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delCast();
            e = composeDels(e, e2);
        }
        return postDelCast(e);
    }

    @Override
    public final JLDel delCatch() {
        JLDel e = delCatchImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delCatch();
            e = composeDels(e, e2);
        }
        return postDelCatch(e);
    }

    @Override
    public final JLDel delCharLit() {
        JLDel e = delCharLitImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delCharLit();
            e = composeDels(e, e2);
        }
        return postDelCharLit(e);
    }

    @Override
    public final JLDel delClassBody() {
        JLDel e = delClassBodyImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delClassBody();
            e = composeDels(e, e2);
        }
        return postDelClassBody(e);
    }

    @Override
    public final JLDel delClassDecl() {
        JLDel e = delClassDeclImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delClassDecl();
            e = composeDels(e, e2);
        }
        return postDelClassDecl(e);
    }

    @Override
    public final JLDel delClassLit() {
        JLDel e = delClassLitImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delClassLit();
            e = composeDels(e, e2);
        }
        return postDelClassLit(e);
    }

    @Override
    public final JLDel delClassMember() {
        JLDel e = delClassMemberImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delClassMember();
            e = composeDels(e, e2);
        }
        return postDelClassMember(e);
    }

    @Override
    public final JLDel delCodeDecl() {
        JLDel e = delCodeDeclImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delCodeDecl();
            e = composeDels(e, e2);
        }
        return postDelCodeDecl(e);
    }

    @Override
    public final JLDel delCompoundStmt() {
        JLDel e = delCompoundStmtImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delCompoundStmt();
            e = composeDels(e, e2);
        }
        return postDelCompoundStmt(e);
    }

    @Override
    public final JLDel delConditional() {
        JLDel e = delConditionalImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delConditional();
            e = composeDels(e, e2);
        }
        return postDelConditional(e);
    }

    @Override
    public final JLDel delConstructorCall() {
        JLDel e = delConstructorCallImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delConstructorCall();
            e = composeDels(e, e2);
        }
        return postDelConstructorCall(e);
    }

    @Override
    public final JLDel delConstructorDecl() {
        JLDel e = delConstructorDeclImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delConstructorDecl();
            e = composeDels(e, e2);
        }
        return postDelConstructorDecl(e);
    }

    @Override
    public final JLDel delDo() {
        JLDel e = delDoImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delDo();
            e = composeDels(e, e2);
        }
        return postDelDo(e);
    }

    @Override
    public final JLDel delEmpty() {
        JLDel e = delEmptyImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delEmpty();
            e = composeDels(e, e2);
        }
        return postDelEmpty(e);
    }

    @Override
    public final JLDel delEval() {
        JLDel e = delEvalImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delEval();
            e = composeDels(e, e2);
        }
        return postDelEval(e);
    }

    @Override
    public final JLDel delExpr() {
        JLDel e = delExprImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delExpr();
            e = composeDels(e, e2);
        }
        return postDelExpr(e);
    }

    @Override
    public final JLDel delField() {
        JLDel e = delFieldImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delField();
            e = composeDels(e, e2);
        }
        return postDelField(e);
    }

    @Override
    public final JLDel delFieldDecl() {
        JLDel e = delFieldDeclImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delFieldDecl();
            e = composeDels(e, e2);
        }
        return postDelFieldDecl(e);
    }

    @Override
    public final JLDel delFloatLit() {
        JLDel e = delFloatLitImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delFloatLit();
            e = composeDels(e, e2);
        }
        return postDelFloatLit(e);
    }

    @Override
    public final JLDel delFor() {
        JLDel e = delForImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delFor();
            e = composeDels(e, e2);
        }
        return postDelFor(e);
    }

    @Override
    public final JLDel delFormal() {
        JLDel e = delFormalImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delFormal();
            e = composeDels(e, e2);
        }
        return postDelFormal(e);
    }

    @Override
    public final JLDel delIf() {
        JLDel e = delIfImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delIf();
            e = composeDels(e, e2);
        }
        return postDelIf(e);
    }

    @Override
    public final JLDel delImport() {
        JLDel e = delImportImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delImport();
            e = composeDels(e, e2);
        }
        return postDelImport(e);
    }

    @Override
    public final JLDel delInitializer() {
        JLDel e = delInitializerImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delInitializer();
            e = composeDels(e, e2);
        }
        return postDelInitializer(e);
    }

    @Override
    public final JLDel delInstanceof() {
        JLDel e = delInstanceofImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delInstanceof();
            e = composeDels(e, e2);
        }
        return postDelInstanceof(e);
    }

    @Override
    public final JLDel delIntLit() {
        JLDel e = delIntLitImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delIntLit();
            e = composeDels(e, e2);
        }
        return postDelIntLit(e);
    }

    @Override
    public final JLDel delLabeled() {
        JLDel e = delLabeledImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delLabeled();
            e = composeDels(e, e2);
        }
        return postDelLabeled(e);
    }

    @Override
    public final JLDel delLit() {
        JLDel e = delLitImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delLit();
            e = composeDels(e, e2);
        }
        return postDelLit(e);
    }

    @Override
    public final JLDel delLocal() {
        JLDel e = delLocalImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delLocal();
            e = composeDels(e, e2);
        }
        return postDelLocal(e);
    }

    @Override
    public final JLDel delLocalClassDecl() {
        JLDel e = delLocalClassDeclImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delLocalClassDecl();
            e = composeDels(e, e2);
        }
        return postDelLocalClassDecl(e);
    }

    @Override
    public final JLDel delLocalDecl() {
        JLDel e = delLocalDeclImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delLocalDecl();
            e = composeDels(e, e2);
        }
        return postDelLocalDecl(e);
    }

    @Override
    public final JLDel delLoop() {
        JLDel e = delLoopImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delLoop();
            e = composeDels(e, e2);
        }
        return postDelLoop(e);
    }

    @Override
    public final JLDel delMethodDecl() {
        JLDel e = delMethodDeclImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delMethodDecl();
            e = composeDels(e, e2);
        }
        return postDelMethodDecl(e);
    }

    @Override
    public final JLDel delNewArray() {
        JLDel e = delNewArrayImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delNewArray();
            e = composeDels(e, e2);
        }
        return postDelNewArray(e);
    }

    @Override
    public final JLDel delNode() {
        JLDel e = delNodeImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delNode();
            e = composeDels(e, e2);
        }
        return postDelNode(e);
    }

    @Override
    public final JLDel delNodeList() {
        JLDel e = delNodeListImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delNode();
            e = composeDels(e, e2);
        }
        return postDelNodeList(e);
    }

    @Override
    public final JLDel delNew() {
        JLDel e = delNewImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delNew();
            e = composeDels(e, e2);
        }
        return postDelNew(e);
    }

    @Override
    public final JLDel delNullLit() {
        JLDel e = delNullLitImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delNullLit();
            e = composeDels(e, e2);
        }
        return postDelNullLit(e);
    }

    @Override
    public final JLDel delNumLit() {
        JLDel e = delNumLitImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delNumLit();
            e = composeDels(e, e2);
        }
        return postDelNumLit(e);
    }

    @Override
    public final JLDel delPackageNode() {
        JLDel e = delPackageNodeImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delPackageNode();
            e = composeDels(e, e2);
        }
        return postDelPackageNode(e);
    }

    @Override
    public final JLDel delProcedureDecl() {
        JLDel e = delProcedureDeclImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delProcedureDecl();
            e = composeDels(e, e2);
        }
        return postDelProcedureDecl(e);
    }

    @Override
    public final JLDel delReturn() {
        JLDel e = delReturnImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delReturn();
            e = composeDels(e, e2);
        }
        return postDelReturn(e);
    }

    @Override
    public final JLDel delSourceCollection() {
        JLDel e = delSourceCollectionImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delSourceCollection();
            e = composeDels(e, e2);
        }
        return postDelSourceCollection(e);
    }

    @Override
    public final JLDel delSourceFile() {
        JLDel e = delSourceFileImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delSourceFile();
            e = composeDels(e, e2);
        }
        return postDelSourceFile(e);
    }

    @Override
    public final JLDel delSpecial() {
        JLDel e = delSpecialImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delSpecial();
            e = composeDels(e, e2);
        }
        return postDelSpecial(e);
    }

    @Override
    public final JLDel delStmt() {
        JLDel e = delStmtImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delStmt();
            e = composeDels(e, e2);
        }
        return postDelStmt(e);
    }

    @Override
    public final JLDel delStringLit() {
        JLDel e = delStringLitImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delStringLit();
            e = composeDels(e, e2);
        }
        return postDelStringLit(e);
    }

    @Override
    public final JLDel delSwitchBlock() {
        JLDel e = delSwitchBlockImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delSwitchBlock();
            e = composeDels(e, e2);
        }
        return postDelSwitchBlock(e);
    }

    @Override
    public final JLDel delSwitchElement() {
        JLDel e = delSwitchElementImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delSwitchElement();
            e = composeDels(e, e2);
        }
        return postDelSwitchElement(e);
    }

    @Override
    public final JLDel delSwitch() {
        JLDel e = delSwitchImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delSwitch();
            e = composeDels(e, e2);
        }
        return postDelSwitch(e);
    }

    @Override
    public final JLDel delSynchronized() {
        JLDel e = delSynchronizedImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delSynchronized();
            e = composeDels(e, e2);
        }
        return postDelSynchronized(e);
    }

    @Override
    public final JLDel delTerm() {
        JLDel e = delTermImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delTerm();
            e = composeDels(e, e2);
        }
        return postDelTerm(e);
    }

    @Override
    public final JLDel delThrow() {
        JLDel e = delThrowImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delThrow();
            e = composeDels(e, e2);
        }
        return postDelThrow(e);
    }

    @Override
    public final JLDel delTry() {
        JLDel e = delTryImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delTry();
            e = composeDels(e, e2);
        }
        return postDelTry(e);
    }

    @Override
    public final JLDel delTypeNode() {
        JLDel e = delTypeNodeImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delTypeNode();
            e = composeDels(e, e2);
        }
        return postDelTypeNode(e);
    }

    @Override
    public final JLDel delUnary() {
        JLDel e = delUnaryImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delUnary();
            e = composeDels(e, e2);
        }
        return postDelUnary(e);
    }

    @Override
    public final JLDel delWhile() {
        JLDel e = delWhileImpl();

        if (nextDelFactory != null) {
            JLDel e2 = nextDelFactory.delWhile();
            e = composeDels(e, e2);
        }
        return postDelWhile(e);
    }

    // ********************************************
    // Impl methods
    // ********************************************

    /**
     * Create the delegate for a {@code Name} AST node.
     * @return the delegate for a {@code Name} AST node.
     */
    protected JLDel delIdImpl() {
        return delNodeImpl();
    }

    /**
     * Create the delegate for a {@code AmbAssign} AST node.
     * @return the delegate for a {@code AmbAssign} AST node.
     */
    protected JLDel delAmbAssignImpl() {
        return delAssignImpl();
    }

    /**
     * Create the delegate for a {@code AmbExpr} AST node.
     * @return the delegate for a {@code AmbExpr} AST node.
     */
    protected JLDel delAmbExprImpl() {
        return delExprImpl();
    }

    /**
     * Create the delegate for a {@code AmbPrefix} AST node.
     * @return the delegate for a {@code AmbPrefix} AST node.
     */
    protected JLDel delAmbPrefixImpl() {
        return delNodeImpl();
    }

    /**
     * Create the delegate for a {@code AmbQualifierNode} AST node.
     * @return the delegate for a {@code AmbQualifierNode} AST node.
     */
    protected JLDel delAmbQualifierNodeImpl() {
        return delNodeImpl();
    }

    /**
     * Create the delegate for a {@code AmbReceiver} AST node.
     * @return the delegate for a {@code AmbReceiver} AST node.
     */
    protected JLDel delAmbReceiverImpl() {
        return delNodeImpl();
    }

    /**
     * Create the delegate for a {@code AmbTypeNode} AST node.
     * @return the delegate for a {@code AmbTypeNode} AST node.
     */
    protected JLDel delAmbTypeNodeImpl() {
        return delTypeNodeImpl();
    }

    /**
     * Create the delegate for a {@code ArrayAccess} AST node.
     * @return the delegate for a {@code ArrayAccess} AST node.
     */
    protected JLDel delArrayAccessImpl() {
        return delExprImpl();
    }

    /**
     * Create the delegate for a {@code ArrayInit} AST node.
     * @return the delegate for a {@code ArrayInit} AST node.
     */
    protected JLDel delArrayInitImpl() {
        return delExprImpl();
    }

    /**
     * Create the delegate for a {@code ArrayTypeNode} AST node.
     * @return the delegate for a {@code ArrayTypeNode} AST node.
     */
    protected JLDel delArrayTypeNodeImpl() {
        return delTypeNodeImpl();
    }

    /**
     * Create the delegate for a {@code Assert} AST node.
     * @return the delegate for a {@code Assert} AST node.
     */
    protected JLDel delAssertImpl() {
        return delStmtImpl();
    }

    /**
     * Create the delegate for a {@code Assign} AST node.
     * @return the delegate for a {@code Assign} AST node.
     */
    protected JLDel delAssignImpl() {
        return delExprImpl();
    }

    /**
     * Create the delegate for a {@code LocalAssign} AST node.
     * @return the delegate for a {@code LocalAssign} AST node.
     */
    protected JLDel delLocalAssignImpl() {
        return delAssignImpl();
    }

    /**
     * Create the delegate for a {@code FieldAssign} AST node.
     * @return the delegate for a {@code FieldAssign} AST node.
     */
    protected JLDel delFieldAssignImpl() {
        return delAssignImpl();
    }

    /**
     * Create the delegate for a {@code ArrayAccessAssign} AST node.
     * @return the delegate for a {@code ArrayAccessAssign} AST node.
     */
    protected JLDel delArrayAccessAssignImpl() {
        return delAssignImpl();
    }

    protected JLDel delBinaryImpl() {
        return delExprImpl();
    }

    /**
     * Create the delegate for a {@code Block} AST node.
     * @return the delegate for a {@code Block} AST node.
     */
    protected JLDel delBlockImpl() {
        return delCompoundStmtImpl();
    }

    /**
     * Create the delegate for a {@code BooleanLit} AST node.
     * @return the delegate for a {@code BooleanLit} AST node.
     */
    protected JLDel delBooleanLitImpl() {
        return delLitImpl();
    }

    /**
     * Create the delegate for a {@code Branch} AST node.
     * @return the delegate for a {@code Branch} AST node.
     */
    protected JLDel delBranchImpl() {
        return delStmtImpl();
    }

    /**
     * Create the delegate for a {@code Call} AST node.
     * @return the delegate for a {@code Call} AST node.
     */
    protected JLDel delCallImpl() {
        return delExprImpl();
    }

    /**
     * Create the delegate for a {@code CanonicalTypeNode} AST node.
     * @return the delegate for a {@code CanonicalTypeNode} AST node.
     */
    protected JLDel delCanonicalTypeNodeImpl() {
        return delTypeNodeImpl();
    }

    /**
     * Create the delegate for a {@code Case} AST node.
     * @return the delegate for a {@code Case} AST node.
     */
    protected JLDel delCaseImpl() {
        return delSwitchElementImpl();
    }

    /**
     * Create the delegate for a {@code Cast} AST node.
     * @return the delegate for a {@code Cast} AST node.
     */
    protected JLDel delCastImpl() {
        return delExprImpl();
    }

    /**
     * Create the delegate for a {@code Catch} AST node.
     * @return the delegate for a {@code Catch} AST node.
     */
    protected JLDel delCatchImpl() {
        return delCompoundStmtImpl();
    }

    /**
     * Create the delegate for a {@code CharLit} AST node.
     * @return the delegate for a {@code CharLit} AST node.
     */
    protected JLDel delCharLitImpl() {
        return delNumLitImpl();
    }

    /**
     * Create the delegate for a {@code ClassBody} AST node.
     * @return the delegate for a {@code ClassBody} AST node.
     */
    protected JLDel delClassBodyImpl() {
        return delTermImpl();
    }

    /**
     * Create the delegate for a {@code ClassDecl} AST node.
     * @return the delegate for a {@code ClassDecl} AST node.
     */
    protected JLDel delClassDeclImpl() {
        return delTermImpl();
    }

    /**
     * Create the delegate for a {@code ClassLit} AST node.
     * @return the delegate for a {@code ClassLit} AST node.
     */
    protected JLDel delClassLitImpl() {
        return delLitImpl();
    }

    /**
     * Create the delegate for a {@code ClassMember} AST node.
     * @return the delegate for a {@code ClassMember} AST node.
     */
    protected JLDel delClassMemberImpl() {
        return delTermImpl();
    }

    /**
     * Create the delegate for a {@code CodeDecl} AST node.
     * @return the delegate for a {@code CodeDecl} AST node.
     */
    protected JLDel delCodeDeclImpl() {
        return delClassMemberImpl();
    }

    /**
     * Create the delegate for a {@code CompoundStmt} AST node.
     * @return the delegate for a {@code CompoundStmt} AST node.
     */
    protected JLDel delCompoundStmtImpl() {
        return delStmtImpl();
    }

    /**
     * Create the delegate for a {@code Conditional} AST node.
     * @return the delegate for a {@code Conditional} AST node.
     */
    protected JLDel delConditionalImpl() {
        return delExprImpl();
    }

    /**
     * Create the delegate for a {@code ConstructorCall} AST node.
     * @return the delegate for a {@code ConstructorCall} AST node.
     */
    protected JLDel delConstructorCallImpl() {
        return delStmtImpl();
    }

    /**
     * Create the delegate for a {@code ConstructorDecl} AST node.
     * @return the delegate for a {@code ConstructorDecl} AST node.
     */
    protected JLDel delConstructorDeclImpl() {
        return delProcedureDeclImpl();
    }

    /**
     * Create the delegate for a {@code Do} AST node.
     * @return the delegate for a {@code Do} AST node.
     */
    protected JLDel delDoImpl() {
        return delLoopImpl();
    }

    /**
     * Create the delegate for a {@code Empty} AST node.
     * @return the delegate for a {@code Empty} AST node.
     */
    protected JLDel delEmptyImpl() {
        return delStmtImpl();
    }

    /**
     * Create the delegate for a {@code Eval} AST node.
     * @return the delegate for a {@code Eval} AST node.
     */
    protected JLDel delEvalImpl() {
        return delStmtImpl();
    }

    /**
     * Create the delegate for a {@code Expr} AST node.
     * @return the delegate for a {@code Expr} AST node.
     */
    protected JLDel delExprImpl() {
        return delTermImpl();
    }

    /**
     * Create the delegate for a {@code Field} AST node.
     * @return the delegate for a {@code Field} AST node.
     */
    protected JLDel delFieldImpl() {
        return delExprImpl();
    }

    /**
     * Create the delegate for a {@code FieldDecl} AST node.
     * @return the delegate for a {@code FieldDecl} AST node.
     */
    protected JLDel delFieldDeclImpl() {
        return delClassMemberImpl();
    }

    /**
     * Create the delegate for a {@code FloatLit} AST node.
     * @return the delegate for a {@code FloatLit} AST node.
     */
    protected JLDel delFloatLitImpl() {
        return delLitImpl();
    }

    /**
     * Create the delegate for a {@code For} AST node.
     * @return the delegate for a {@code For} AST node.
     */
    protected JLDel delForImpl() {
        return delLoopImpl();
    }

    /**
     * Create the delegate for a {@code Formal} AST node.
     * @return the delegate for a {@code Formal} AST node.
     */
    protected JLDel delFormalImpl() {
        return delTermImpl();
    }

    /**
     * Create the delegate for a {@code If} AST node.
     * @return the delegate for a {@code If} AST node.
     */
    protected JLDel delIfImpl() {
        return delCompoundStmtImpl();
    }

    /**
     * Create the delegate for a {@code Import} AST node.
     * @return the delegate for a {@code Import} AST node.
     */
    protected JLDel delImportImpl() {
        return delNodeImpl();
    }

    /**
     * Create the delegate for a {@code Initializer} AST node.
     * @return the delegate for a {@code Initializer} AST node.
     */
    protected JLDel delInitializerImpl() {
        return delCodeDeclImpl();
    }

    /**
     * Create the delegate for a {@code Instanceof} AST node.
     * @return the delegate for a {@code Instanceof} AST node.
     */
    protected JLDel delInstanceofImpl() {
        return delExprImpl();
    }

    /**
     * Create the delegate for a {@code IntLit} AST node.
     * @return the delegate for a {@code IntLit} AST node.
     */
    protected JLDel delIntLitImpl() {
        return delNumLitImpl();
    }

    /**
     * Create the delegate for a {@code Labeled} AST node.
     * @return the delegate for a {@code Labeled} AST node.
     */
    protected JLDel delLabeledImpl() {
        return delCompoundStmtImpl();
    }

    /**
     * Create the delegate for a {@code Lit} AST node.
     * @return the delegate for a {@code Lit} AST node.
     */
    protected JLDel delLitImpl() {
        return delExprImpl();
    }

    /**
     * Create the delegate for a {@code Local} AST node.
     * @return the delegate for a {@code Local} AST node.
     */
    protected JLDel delLocalImpl() {
        return delExprImpl();
    }

    /**
     * Create the delegate for a {@code LocalClassDecl} AST node.
     * @return the delegate for a {@code LocalClassDecl} AST node.
     */
    protected JLDel delLocalClassDeclImpl() {
        return delCompoundStmtImpl();
    }

    /**
     * Create the delegate for a {@code LocalDecl} AST node.
     * @return the delegate for a {@code LocalDecl} AST node.
     */
    protected JLDel delLocalDeclImpl() {
        return delStmtImpl();
    }

    /**
     * Create the delegate for a {@code Loop} AST node.
     * @return the delegate for a {@code Loop} AST node.
     */
    protected JLDel delLoopImpl() {
        return delCompoundStmtImpl();
    }

    /**
     * Create the delegate for a {@code MethodDecl} AST node.
     * @return the delegate for a {@code MethodDecl} AST node.
     */
    protected JLDel delMethodDeclImpl() {
        return delProcedureDeclImpl();
    }

    /**
     * Create the delegate for a {@code NewArray} AST node.
     * @return the delegate for a {@code NewArray} AST node.
     */
    protected JLDel delNewArrayImpl() {
        return delExprImpl();
    }

    /**
     * Create the delegate for a {@code Node} AST node.
     * @return the delegate for a {@code Node} AST node.
     */
    protected JLDel delNodeImpl() {
        return null;
    }

    /**
     * Create the delegate for a {@code NodeList} AST node.
     * @return the delegate for a {@code NodeList} AST node.
     */
    protected JLDel delNodeListImpl() {
        return delNodeImpl();
    }

    /**
     * Create the delegate for a {@code New} AST node.
     * @return the delegate for a {@code New} AST node.
     */
    protected JLDel delNewImpl() {
        return delExprImpl();
    }

    /**
     * Create the delegate for a {@code NullLit} AST node.
     * @return the delegate for a {@code NullLit} AST node.
     */
    protected JLDel delNullLitImpl() {
        return delLitImpl();
    }

    /**
     * Create the delegate for a {@code NumLit} AST node.
     * @return the delegate for a {@code NumLit} AST node.
     */
    protected JLDel delNumLitImpl() {
        return delLitImpl();
    }

    /**
     * Create the delegate for a {@code PackageNode} AST node.
     * @return the delegate for a {@code PackageNode} AST node.
     */
    protected JLDel delPackageNodeImpl() {
        return delNodeImpl();
    }

    /**
     * Create the delegate for a {@code ProcedureDecl} AST node.
     * @return the delegate for a {@code ProcedureDecl} AST node.
     */
    protected JLDel delProcedureDeclImpl() {
        return delCodeDeclImpl();
    }

    /**
     * Create the delegate for a {@code Return} AST node.
     * @return the delegate for a {@code Return} AST node.
     */
    protected JLDel delReturnImpl() {
        return delStmtImpl();
    }

    /**
     * Create the delegate for a {@code SourceCollection} AST node.
     * @return the delegate for a {@code SourceCollection} AST node.
     */
    protected JLDel delSourceCollectionImpl() {
        return delNodeImpl();
    }

    /**
     * Create the delegate for a {@code SourceFile} AST node.
     * @return the delegate for a {@code SourceFile} AST node.
     */
    protected JLDel delSourceFileImpl() {
        return delNodeImpl();
    }

    /**
     * Create the delegate for a {@code Special} AST node.
     * @return the delegate for a {@code Special} AST node.
     */
    protected JLDel delSpecialImpl() {
        return delExprImpl();
    }

    /**
     * Create the delegate for a {@code Stmt} AST node.
     * @return the delegate for a {@code Stmt} AST node.
     */
    protected JLDel delStmtImpl() {
        return delTermImpl();
    }

    /**
     * Create the delegate for a {@code StringLit} AST node.
     * @return the delegate for a {@code StringLit} AST node.
     */
    protected JLDel delStringLitImpl() {
        return delLitImpl();
    }

    /**
     * Create the delegate for a {@code SwitchBlock} AST node.
     * @return the delegate for a {@code SwitchBlock} AST node.
     */
    protected JLDel delSwitchBlockImpl() {
        return delSwitchElementImpl();
    }

    /**
     * Create the delegate for a {@code SwitchElement} AST node.
     * @return the delegate for a {@code SwitchElement} AST node.
     */
    protected JLDel delSwitchElementImpl() {
        return delStmtImpl();
    }

    /**
     * Create the delegate for a {@code Switch} AST node.
     * @return the delegate for a {@code Switch} AST node.
     */
    protected JLDel delSwitchImpl() {
        return delCompoundStmtImpl();
    }

    /**
     * Create the delegate for a {@code Synchronized} AST node.
     * @return the delegate for a {@code Synchronized} AST node.
     */
    protected JLDel delSynchronizedImpl() {
        return delCompoundStmtImpl();
    }

    /**
     * Create the delegate for a {@code Term} AST node.
     * @return the delegate for a {@code Term} AST node.
     */
    protected JLDel delTermImpl() {
        return delNodeImpl();
    }

    /**
     * Create the delegate for a {@code Throw} AST node.
     * @return the delegate for a {@code Throw} AST node.
     */
    protected JLDel delThrowImpl() {
        return delStmtImpl();
    }

    /**
     * Create the delegate for a {@code Try} AST node.
     * @return the delegate for a {@code Try} AST node.
     */
    protected JLDel delTryImpl() {
        return delCompoundStmtImpl();
    }

    /**
     * Create the delegate for a {@code TypeNode} AST node.
     * @return the delegate for a {@code TypeNode} AST node.
     */
    protected JLDel delTypeNodeImpl() {
        return delTermImpl();
    }

    /**
     * Create the delegate for a {@code Unary} AST node.
     * @return the delegate for a {@code Unary} AST node.
     */
    protected JLDel delUnaryImpl() {
        return delExprImpl();
    }

    /**
     * Create the delegate for a {@code While} AST node.
     * @return the delegate for a {@code While} AST node.
     */
    protected JLDel delWhileImpl() {
        return delLoopImpl();
    }

    // ********************************************
    // Post methods
    // ********************************************

    protected JLDel postDelId(JLDel del) {
        return postDelNode(del);
    }

    protected JLDel postDelAmbAssign(JLDel del) {
        return postDelAssign(del);
    }

    protected JLDel postDelAmbExpr(JLDel del) {
        return postDelExpr(del);
    }

    protected JLDel postDelAmbPrefix(JLDel del) {
        return postDelNode(del);
    }

    protected JLDel postDelAmbQualifierNode(JLDel del) {
        return postDelNode(del);
    }

    protected JLDel postDelAmbReceiver(JLDel del) {
        return postDelNode(del);
    }

    protected JLDel postDelAmbTypeNode(JLDel del) {
        return postDelTypeNode(del);
    }

    protected JLDel postDelArrayAccess(JLDel del) {
        return postDelExpr(del);
    }

    protected JLDel postDelArrayInit(JLDel del) {
        return postDelExpr(del);
    }

    protected JLDel postDelArrayTypeNode(JLDel del) {
        return postDelTypeNode(del);
    }

    protected JLDel postDelAssert(JLDel del) {
        return postDelStmt(del);
    }

    protected JLDel postDelAssign(JLDel del) {
        return postDelExpr(del);
    }

    protected JLDel postDelLocalAssign(JLDel del) {
        return postDelAssign(del);
    }

    protected JLDel postDelFieldAssign(JLDel del) {
        return postDelAssign(del);
    }

    protected JLDel postDelArrayAccessAssign(JLDel del) {
        return postDelAssign(del);
    }

    protected JLDel postDelBinary(JLDel del) {
        return postDelExpr(del);
    }

    protected JLDel postDelBlock(JLDel del) {
        return postDelCompoundStmt(del);
    }

    protected JLDel postDelBooleanLit(JLDel del) {
        return postDelLit(del);
    }

    protected JLDel postDelBranch(JLDel del) {
        return postDelStmt(del);
    }

    protected JLDel postDelCall(JLDel del) {
        return postDelExpr(del);
    }

    protected JLDel postDelCanonicalTypeNode(JLDel del) {
        return postDelTypeNode(del);
    }

    protected JLDel postDelCase(JLDel del) {
        return postDelSwitchElement(del);
    }

    protected JLDel postDelCast(JLDel del) {
        return postDelExpr(del);
    }

    protected JLDel postDelCatch(JLDel del) {
        return postDelCompoundStmt(del);
    }

    protected JLDel postDelCharLit(JLDel del) {
        return postDelNumLit(del);
    }

    protected JLDel postDelClassBody(JLDel del) {
        return postDelTerm(del);
    }

    protected JLDel postDelClassDecl(JLDel del) {
        return postDelTerm(del);
    }

    protected JLDel postDelClassLit(JLDel del) {
        return postDelLit(del);
    }

    protected JLDel postDelClassMember(JLDel del) {
        return postDelTerm(del);
    }

    protected JLDel postDelCodeDecl(JLDel del) {
        return postDelClassMember(del);
    }

    protected JLDel postDelCompoundStmt(JLDel del) {
        return postDelStmt(del);
    }

    protected JLDel postDelConditional(JLDel del) {
        return postDelExpr(del);
    }

    protected JLDel postDelConstructorCall(JLDel del) {
        return postDelStmt(del);
    }

    protected JLDel postDelConstructorDecl(JLDel del) {
        return postDelProcedureDecl(del);
    }

    protected JLDel postDelDo(JLDel del) {
        return postDelLoop(del);
    }

    protected JLDel postDelEmpty(JLDel del) {
        return postDelStmt(del);
    }

    protected JLDel postDelEval(JLDel del) {
        return postDelStmt(del);
    }

    protected JLDel postDelExpr(JLDel del) {
        return postDelTerm(del);
    }

    protected JLDel postDelField(JLDel del) {
        return postDelExpr(del);
    }

    protected JLDel postDelFieldDecl(JLDel del) {
        return postDelClassMember(del);
    }

    protected JLDel postDelFloatLit(JLDel del) {
        return postDelLit(del);
    }

    protected JLDel postDelFor(JLDel del) {
        return postDelLoop(del);
    }

    protected JLDel postDelFormal(JLDel del) {
        return postDelTerm(del);
    }

    protected JLDel postDelIf(JLDel del) {
        return postDelCompoundStmt(del);
    }

    protected JLDel postDelImport(JLDel del) {
        return postDelNode(del);
    }

    protected JLDel postDelInitializer(JLDel del) {
        return postDelCodeDecl(del);
    }

    protected JLDel postDelInstanceof(JLDel del) {
        return postDelExpr(del);
    }

    protected JLDel postDelIntLit(JLDel del) {
        return postDelNumLit(del);
    }

    protected JLDel postDelLabeled(JLDel del) {
        return postDelCompoundStmt(del);
    }

    protected JLDel postDelLit(JLDel del) {
        return postDelExpr(del);
    }

    protected JLDel postDelLocal(JLDel del) {
        return postDelExpr(del);
    }

    protected JLDel postDelLocalClassDecl(JLDel del) {
        return postDelCompoundStmt(del);
    }

    protected JLDel postDelLocalDecl(JLDel del) {
        return postDelStmt(del);
    }

    protected JLDel postDelLoop(JLDel del) {
        return postDelCompoundStmt(del);
    }

    protected JLDel postDelMethodDecl(JLDel del) {
        return postDelProcedureDecl(del);
    }

    protected JLDel postDelNewArray(JLDel del) {
        return postDelExpr(del);
    }

    protected JLDel postDelNode(JLDel del) {
        return del;
    }

    protected JLDel postDelNodeList(JLDel del) {
        return postDelNode(del);
    }

    protected JLDel postDelNew(JLDel del) {
        return postDelExpr(del);
    }

    protected JLDel postDelNullLit(JLDel del) {
        return postDelLit(del);
    }

    protected JLDel postDelNumLit(JLDel del) {
        return postDelLit(del);
    }

    protected JLDel postDelPackageNode(JLDel del) {
        return postDelNode(del);
    }

    protected JLDel postDelProcedureDecl(JLDel del) {
        return postDelCodeDecl(del);
    }

    protected JLDel postDelReturn(JLDel del) {
        return postDelStmt(del);
    }

    protected JLDel postDelSourceCollection(JLDel del) {
        return postDelNode(del);
    }

    protected JLDel postDelSourceFile(JLDel del) {
        return postDelNode(del);
    }

    protected JLDel postDelSpecial(JLDel del) {
        return postDelExpr(del);
    }

    protected JLDel postDelStmt(JLDel del) {
        return postDelTerm(del);
    }

    protected JLDel postDelStringLit(JLDel del) {
        return postDelLit(del);
    }

    protected JLDel postDelSwitchBlock(JLDel del) {
        return postDelSwitchElement(del);
    }

    protected JLDel postDelSwitchElement(JLDel del) {
        return postDelStmt(del);
    }

    protected JLDel postDelSwitch(JLDel del) {
        return postDelCompoundStmt(del);
    }

    protected JLDel postDelSynchronized(JLDel del) {
        return postDelCompoundStmt(del);
    }

    protected JLDel postDelTerm(JLDel del) {
        return postDelNode(del);
    }

    protected JLDel postDelThrow(JLDel del) {
        return postDelStmt(del);
    }

    protected JLDel postDelTry(JLDel del) {
        return postDelCompoundStmt(del);
    }

    protected JLDel postDelTypeNode(JLDel del) {
        return postDelTerm(del);
    }

    protected JLDel postDelUnary(JLDel del) {
        return postDelExpr(del);
    }

    protected JLDel postDelWhile(JLDel del) {
        return postDelLoop(del);
    }

}
