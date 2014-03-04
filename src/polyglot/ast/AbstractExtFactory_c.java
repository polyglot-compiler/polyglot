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
 * This abstract implementation of {@code ExtFactory} provides
 * a way of chaining together ExtFactories, and default implementations
 * of factory methods for each node.
 * 
 * <p>
 * For a given type of AST node {@code N}, there are three methods:
 * {@code extN()},  {@code extNImpl()} and {@code postExtN(Ext)}. 
 * The method {@code extN()} calls {@code extNImpl()} to create
 * an appropriate extension object, and if other {@code ExtFactory}s are 
 * chained onto this one, it will also call {@code extN()} on the next 
 * {@code ExtFactory}. The method {@code extN()} will then 
 * call {@code postExtN}, passing in the newly created extension object.
 * 
 * <p>
 * The default implementation of {@code extNImpl()} is to simply call
 * {@code extMImpl()}, where {@code M} is the immediate 
 * superclass of {@code N}. Similarly, the default implementation of
 * {@code postExtN(Ext)} is to call {@code postExtM(Ext)}.
 * 
 * @see polyglot.ast.AbstractDelFactory_c has a very similar structure. 
 */
public abstract class AbstractExtFactory_c implements ExtFactory {
    protected AbstractExtFactory_c() {
        this(null);
    }

    protected AbstractExtFactory_c(ExtFactory nextExtFactory) {
        this.nextExtFactory = nextExtFactory;
    }

    /**
     * The next extFactory in the chain. Whenever an extension is instantiated,
     * the next extFactory should be called to see if it also has an extension,
     * and if so, the extensions should be joined together using the method
     * {@code composeExts}
     */
    private ExtFactory nextExtFactory;

    @Override
    public ExtFactory nextExtFactory() {
        return nextExtFactory;
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

    // ******************************************
    // Final methods that call the Impl methods to construct 
    // extensions, and then check with nextExtFactory to see if it
    // also has an extension. Finally, call an appropriate post method,
    // to allow subclasses to perform operations on the construction Exts
    // ******************************************
    @Override
    public final Ext extId() {
        Ext e = extIdImpl();
        return postExtId(e);
    }

    @Override
    public final Ext extAmbAssign() {
        Ext e = extAmbAssignImpl();
        return postExtAmbAssign(e);
    }

    @Override
    public final Ext extAmbExpr() {
        Ext e = extAmbExprImpl();
        return postExtAmbExpr(e);
    }

    @Override
    public final Ext extAmbPrefix() {
        Ext e = extAmbPrefixImpl();
        return postExtAmbPrefix(e);
    }

    @Override
    public final Ext extAmbQualifierNode() {
        Ext e = extAmbQualifierNodeImpl();
        return postExtAmbQualifierNode(e);
    }

    @Override
    public final Ext extAmbReceiver() {
        Ext e = extAmbReceiverImpl();
        return postExtAmbReceiver(e);
    }

    @Override
    public final Ext extAmbTypeNode() {
        Ext e = extAmbTypeNodeImpl();
        return postExtAmbTypeNode(e);
    }

    @Override
    public final Ext extArrayAccess() {
        Ext e = extArrayAccessImpl();
        return postExtArrayAccess(e);
    }

    @Override
    public final Ext extArrayInit() {
        Ext e = extArrayInitImpl();
        return postExtArrayInit(e);
    }

    @Override
    public final Ext extArrayTypeNode() {
        Ext e = extArrayTypeNodeImpl();
        return postExtArrayTypeNode(e);
    }

    @Override
    public final Ext extAssert() {
        Ext e = extAssertImpl();
        return postExtAssert(e);
    }

    @Override
    public final Ext extAssign() {
        Ext e = extAssignImpl();
        return postExtAssign(e);
    }

    @Override
    public final Ext extLocalAssign() {
        Ext e = extLocalAssignImpl();
        return postExtLocalAssign(e);
    }

    @Override
    public final Ext extFieldAssign() {
        Ext e = extFieldAssignImpl();
        return postExtFieldAssign(e);
    }

    @Override
    public final Ext extArrayAccessAssign() {
        Ext e = extArrayAccessAssignImpl();
        return postExtArrayAccessAssign(e);
    }

    @Override
    public final Ext extBinary() {
        Ext e = extBinaryImpl();
        return postExtBinary(e);
    }

    @Override
    public final Ext extBlock() {
        Ext e = extBlockImpl();
        return postExtBlock(e);
    }

    @Override
    public final Ext extBooleanLit() {
        Ext e = extBooleanLitImpl();
        return postExtBooleanLit(e);
    }

    @Override
    public final Ext extBranch() {
        Ext e = extBranchImpl();
        return postExtBranch(e);
    }

    @Override
    public final Ext extCall() {
        Ext e = extCallImpl();
        return postExtCall(e);
    }

    @Override
    public final Ext extCanonicalTypeNode() {
        Ext e = extCanonicalTypeNodeImpl();
        return postExtCanonicalTypeNode(e);
    }

    @Override
    public final Ext extCase() {
        Ext e = extCaseImpl();
        return postExtCase(e);
    }

    @Override
    public final Ext extCast() {
        Ext e = extCastImpl();
        return postExtCast(e);
    }

    @Override
    public final Ext extCatch() {
        Ext e = extCatchImpl();
        return postExtCatch(e);
    }

    @Override
    public final Ext extCharLit() {
        Ext e = extCharLitImpl();
        return postExtCharLit(e);
    }

    @Override
    public final Ext extClassBody() {
        Ext e = extClassBodyImpl();
        return postExtClassBody(e);
    }

    @Override
    public final Ext extClassDecl() {
        Ext e = extClassDeclImpl();
        return postExtClassDecl(e);
    }

    @Override
    public final Ext extClassLit() {
        Ext e = extClassLitImpl();
        return postExtClassLit(e);
    }

    @Override
    public final Ext extClassMember() {
        Ext e = extClassMemberImpl();
        return postExtClassMember(e);
    }

    @Override
    public final Ext extCodeDecl() {
        Ext e = extCodeDeclImpl();
        return postExtCodeDecl(e);
    }

    @Override
    public final Ext extCompoundStmt() {
        Ext e = extCompoundStmtImpl();
        return postExtCompoundStmt(e);
    }

    @Override
    public final Ext extConditional() {
        Ext e = extConditionalImpl();
        return postExtConditional(e);
    }

    @Override
    public final Ext extConstructorCall() {
        Ext e = extConstructorCallImpl();
        return postExtConstructorCall(e);
    }

    @Override
    public final Ext extConstructorDecl() {
        Ext e = extConstructorDeclImpl();
        return postExtConstructorDecl(e);
    }

    @Override
    public final Ext extDo() {
        Ext e = extDoImpl();
        return postExtDo(e);
    }

    @Override
    public final Ext extEmpty() {
        Ext e = extEmptyImpl();
        return postExtEmpty(e);
    }

    @Override
    public final Ext extEval() {
        Ext e = extEvalImpl();
        return postExtEval(e);
    }

    @Override
    public final Ext extExpr() {
        Ext e = extExprImpl();
        return postExtExpr(e);
    }

    @Override
    public final Ext extField() {
        Ext e = extFieldImpl();
        return postExtField(e);
    }

    @Override
    public final Ext extFieldDecl() {
        Ext e = extFieldDeclImpl();
        return postExtFieldDecl(e);
    }

    @Override
    public final Ext extFloatLit() {
        Ext e = extFloatLitImpl();
        return postExtFloatLit(e);
    }

    @Override
    public final Ext extFor() {
        Ext e = extForImpl();
        return postExtFor(e);
    }

    @Override
    public final Ext extFormal() {
        Ext e = extFormalImpl();
        return postExtFormal(e);
    }

    @Override
    public final Ext extIf() {
        Ext e = extIfImpl();
        return postExtIf(e);
    }

    @Override
    public final Ext extImport() {
        Ext e = extImportImpl();
        return postExtImport(e);
    }

    @Override
    public final Ext extInitializer() {
        Ext e = extInitializerImpl();
        return postExtInitializer(e);
    }

    @Override
    public final Ext extInstanceof() {
        Ext e = extInstanceofImpl();
        return postExtInstanceof(e);
    }

    @Override
    public final Ext extIntLit() {
        Ext e = extIntLitImpl();
        return postExtIntLit(e);
    }

    @Override
    public final Ext extLabeled() {
        Ext e = extLabeledImpl();
        return postExtLabeled(e);
    }

    @Override
    public final Ext extLit() {
        Ext e = extLitImpl();
        return postExtLit(e);
    }

    @Override
    public final Ext extLocal() {
        Ext e = extLocalImpl();
        return postExtLocal(e);
    }

    @Override
    public final Ext extLocalClassDecl() {
        Ext e = extLocalClassDeclImpl();
        return postExtLocalClassDecl(e);
    }

    @Override
    public final Ext extLocalDecl() {
        Ext e = extLocalDeclImpl();
        return postExtLocalDecl(e);
    }

    @Override
    public final Ext extLoop() {
        Ext e = extLoopImpl();
        return postExtLoop(e);
    }

    @Override
    public final Ext extMethodDecl() {
        Ext e = extMethodDeclImpl();
        return postExtMethodDecl(e);
    }

    @Override
    public final Ext extNewArray() {
        Ext e = extNewArrayImpl();
        return postExtNewArray(e);
    }

    @Override
    public final Ext extNode() {
        Ext e = extNodeImpl();
        return postExtNode(e);
    }

    @Override
    public final Ext extNodeList() {
        Ext e = extNodeListImpl();
        return postExtNodeList(e);
    }

    @Override
    public final Ext extNew() {
        Ext e = extNewImpl();
        return postExtNew(e);
    }

    @Override
    public final Ext extNullLit() {
        Ext e = extNullLitImpl();
        return postExtNullLit(e);
    }

    @Override
    public final Ext extNumLit() {
        Ext e = extNumLitImpl();
        return postExtNumLit(e);
    }

    @Override
    public final Ext extPackageNode() {
        Ext e = extPackageNodeImpl();
        return postExtPackageNode(e);
    }

    @Override
    public final Ext extProcedureDecl() {
        Ext e = extProcedureDeclImpl();
        return postExtProcedureDecl(e);
    }

    @Override
    public final Ext extReturn() {
        Ext e = extReturnImpl();
        return postExtReturn(e);
    }

    @Override
    public final Ext extSourceCollection() {
        Ext e = extSourceCollectionImpl();
        return postExtSourceCollection(e);
    }

    @Override
    public final Ext extSourceFile() {
        Ext e = extSourceFileImpl();
        return postExtSourceFile(e);
    }

    @Override
    public final Ext extSpecial() {
        Ext e = extSpecialImpl();
        return postExtSpecial(e);
    }

    @Override
    public final Ext extStmt() {
        Ext e = extStmtImpl();
        return postExtStmt(e);
    }

    @Override
    public final Ext extStringLit() {
        Ext e = extStringLitImpl();
        return postExtStringLit(e);
    }

    @Override
    public final Ext extSwitchBlock() {
        Ext e = extSwitchBlockImpl();
        return postExtSwitchBlock(e);
    }

    @Override
    public final Ext extSwitchElement() {
        Ext e = extSwitchElementImpl();
        return postExtSwitchElement(e);
    }

    @Override
    public final Ext extSwitch() {
        Ext e = extSwitchImpl();
        return postExtSwitch(e);
    }

    @Override
    public final Ext extSynchronized() {
        Ext e = extSynchronizedImpl();
        return postExtSynchronized(e);
    }

    @Override
    public final Ext extTerm() {
        Ext e = extTermImpl();
        return postExtTerm(e);
    }

    @Override
    public final Ext extThrow() {
        Ext e = extThrowImpl();
        return postExtThrow(e);
    }

    @Override
    public final Ext extTry() {
        Ext e = extTryImpl();
        return postExtTry(e);
    }

    @Override
    public final Ext extTypeNode() {
        Ext e = extTypeNodeImpl();
        return postExtTypeNode(e);
    }

    @Override
    public final Ext extUnary() {
        Ext e = extUnaryImpl();
        return postExtUnary(e);
    }

    @Override
    public final Ext extWhile() {
        Ext e = extWhileImpl();
        return postExtWhile(e);
    }

    // ********************************************
    // Impl methods
    // ********************************************

    /**
     * Create the {@code Ext} object for a {@code Name} AST node.
     * @return the {@code Ext} object for a {@code Name} AST node.
     */
    protected Ext extIdImpl() {
        return extNodeImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code AmbAssign} AST node.
     * @return the {@code Ext} object for a {@code AmbAssign} AST node.
     */
    protected Ext extAmbAssignImpl() {
        return extAssignImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code AmbExpr} AST node.
     * @return the {@code Ext} object for a {@code AmbExpr} AST node.
     */
    protected Ext extAmbExprImpl() {
        return extExprImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code AmbPrefix} AST node.
     * @return the {@code Ext} object for a {@code AmbPrefix} AST node.
     */
    protected Ext extAmbPrefixImpl() {
        return extNodeImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code AmbQualifierNode} AST node.
     * @return the {@code Ext} object for a {@code AmbQualifierNode} AST node.
     */
    protected Ext extAmbQualifierNodeImpl() {
        return extNodeImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code AmbReceiver} AST node.
     * @return the {@code Ext} object for a {@code AmbReceiver} AST node.
     */
    protected Ext extAmbReceiverImpl() {
        return extNodeImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code AmbTypeNode} AST node.
     * @return the {@code Ext} object for a {@code AmbTypeNode} AST node.
     */
    protected Ext extAmbTypeNodeImpl() {
        return extTypeNodeImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code ArrayAccess} AST node.
     * @return the {@code Ext} object for a {@code ArrayAccess} AST node.
     */
    protected Ext extArrayAccessImpl() {
        return extExprImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code ArrayInit} AST node.
     * @return the {@code Ext} object for a {@code ArrayInit} AST node.
     */
    protected Ext extArrayInitImpl() {
        return extExprImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code ArrayTypeNode} AST node.
     * @return the {@code Ext} object for a {@code ArrayTypeNode} AST node.
     */
    protected Ext extArrayTypeNodeImpl() {
        return extTypeNodeImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Assert} AST node.
     * @return the {@code Ext} object for a {@code Assert} AST node.
     */
    protected Ext extAssertImpl() {
        return extStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Assign} AST node.
     * @return the {@code Ext} object for a {@code Assign} AST node.
     */
    protected Ext extAssignImpl() {
        return extExprImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code LocalAssign} AST node.
     * @return the {@code Ext} object for a {@code LocalAssign} AST node.
     */
    protected Ext extLocalAssignImpl() {
        return extAssignImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code FieldAssign} AST node.
     * @return the {@code Ext} object for a {@code FieldAssign} AST node.
     */
    protected Ext extFieldAssignImpl() {
        return extAssignImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code ArrayAccessAssign} AST node.
     * @return the {@code Ext} object for a {@code ArrayAccessAssign} AST node.
     */
    protected Ext extArrayAccessAssignImpl() {
        return extAssignImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Binary} AST node.
     * @return the {@code Ext} object for a {@code Binary} AST node.
     */
    protected Ext extBinaryImpl() {
        return extExprImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Block} AST node.
     * @return the {@code Ext} object for a {@code Block} AST node.
     */
    protected Ext extBlockImpl() {
        return extCompoundStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code BooleanLit} AST node.
     * @return the {@code Ext} object for a {@code BooleanLit} AST node.
     */
    protected Ext extBooleanLitImpl() {
        return extLitImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Branch} AST node.
     * @return the {@code Ext} object for a {@code Branch} AST node.
     */
    protected Ext extBranchImpl() {
        return extStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Call} AST node.
     * @return the {@code Ext} object for a {@code Call} AST node.
     */
    protected Ext extCallImpl() {
        return extExprImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code CanonicalTypeNode} AST node.
     * @return the {@code Ext} object for a {@code CanonicalTypeNode} AST node.
     */
    protected Ext extCanonicalTypeNodeImpl() {
        return extTypeNodeImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Case} AST node.
     * @return the {@code Ext} object for a {@code Case} AST node.
     */
    protected Ext extCaseImpl() {
        return extSwitchElementImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Cast} AST node.
     * @return the {@code Ext} object for a {@code Cast} AST node.
     */
    protected Ext extCastImpl() {
        return extExprImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Catch} AST node.
     * @return the {@code Ext} object for a {@code Catch} AST node.
     */
    protected Ext extCatchImpl() {
        return extCompoundStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code CharLit} AST node.
     * @return the {@code Ext} object for a {@code CharLit} AST node.
     */
    protected Ext extCharLitImpl() {
        return extNumLitImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code ClassBody} AST node.
     * @return the {@code Ext} object for a {@code ClassBody} AST node.
     */
    protected Ext extClassBodyImpl() {
        return extTermImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code ClassDecl} AST node.
     * @return the {@code Ext} object for a {@code ClassDecl} AST node.
     */
    protected Ext extClassDeclImpl() {
        return extTermImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code ClassLit} AST node.
     * @return the {@code Ext} object for a {@code ClassLit} AST node.
     */
    protected Ext extClassLitImpl() {
        return extLitImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code ClassMember} AST node.
     * @return the {@code Ext} object for a {@code ClassMember} AST node.
     */
    protected Ext extClassMemberImpl() {
        return extTermImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code CodeDecl} AST node.
     * @return the {@code Ext} object for a {@code CodeDecl} AST node.
     */
    protected Ext extCodeDeclImpl() {
        return extClassMemberImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code CompoundStmt} AST node.
     * @return the {@code Ext} object for a {@code CompoundStmt} AST node.
     */
    protected Ext extCompoundStmtImpl() {
        return extStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Conditional} AST node.
     * @return the {@code Ext} object for a {@code Conditional} AST node.
     */
    protected Ext extConditionalImpl() {
        return extExprImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code ConstructorCall} AST node.
     * @return the {@code Ext} object for a {@code ConstructorCall} AST node.
     */
    protected Ext extConstructorCallImpl() {
        return extStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code ConstructorDecl} AST node.
     * @return the {@code Ext} object for a {@code ConstructorDecl} AST node.
     */
    protected Ext extConstructorDeclImpl() {
        return extProcedureDeclImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Do} AST node.
     * @return the {@code Ext} object for a {@code Do} AST node.
     */
    protected Ext extDoImpl() {
        return extLoopImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Empty} AST node.
     * @return the {@code Ext} object for a {@code Empty} AST node.
     */
    protected Ext extEmptyImpl() {
        return extStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Eval} AST node.
     * @return the {@code Ext} object for a {@code Eval} AST node.
     */
    protected Ext extEvalImpl() {
        return extStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Expr} AST node.
     * @return the {@code Ext} object for a {@code Expr} AST node.
     */
    protected Ext extExprImpl() {
        return extTermImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Field} AST node.
     * @return the {@code Ext} object for a {@code Field} AST node.
     */
    protected Ext extFieldImpl() {
        return extExprImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code FieldDecl} AST node.
     * @return the {@code Ext} object for a {@code FieldDecl} AST node.
     */
    protected Ext extFieldDeclImpl() {
        return extClassMemberImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code FloatLit} AST node.
     * @return the {@code Ext} object for a {@code FloatLit} AST node.
     */
    protected Ext extFloatLitImpl() {
        return extLitImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code For} AST node.
     * @return the {@code Ext} object for a {@code For} AST node.
     */
    protected Ext extForImpl() {
        return extLoopImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Formal} AST node.
     * @return the {@code Ext} object for a {@code Formal} AST node.
     */
    protected Ext extFormalImpl() {
        return extTermImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code If} AST node.
     * @return the {@code Ext} object for a {@code If} AST node.
     */
    protected Ext extIfImpl() {
        return extCompoundStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Import} AST node.
     * @return the {@code Ext} object for a {@code Import} AST node.
     */
    protected Ext extImportImpl() {
        return extNodeImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Initializer} AST node.
     * @return the {@code Ext} object for a {@code Initializer} AST node.
     */
    protected Ext extInitializerImpl() {
        return extCodeDeclImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Instanceof} AST node.
     * @return the {@code Ext} object for a {@code Instanceof} AST node.
     */
    protected Ext extInstanceofImpl() {
        return extExprImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code IntLit} AST node.
     * @return the {@code Ext} object for a {@code IntLit} AST node.
     */
    protected Ext extIntLitImpl() {
        return extNumLitImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Labeled} AST node.
     * @return the {@code Ext} object for a {@code Labeled} AST node.
     */
    protected Ext extLabeledImpl() {
        return extCompoundStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Lit} AST node.
     * @return the {@code Ext} object for a {@code Lit} AST node.
     */
    protected Ext extLitImpl() {
        return extExprImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Local} AST node.
     * @return the {@code Ext} object for a {@code Local} AST node.
     */
    protected Ext extLocalImpl() {
        return extExprImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code LocalClassDecl} AST node.
     * @return the {@code Ext} object for a {@code LocalClassDecl} AST node.
     */
    protected Ext extLocalClassDeclImpl() {
        return extCompoundStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code LocalDecl} AST node.
     * @return the {@code Ext} object for a {@code LocalDecl} AST node.
     */
    protected Ext extLocalDeclImpl() {
        return extStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Loop} AST node.
     * @return the {@code Ext} object for a {@code Loop} AST node.
     */
    protected Ext extLoopImpl() {
        return extCompoundStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code MethodDecl} AST node.
     * @return the {@code Ext} object for a {@code MethodDecl} AST node.
     */
    protected Ext extMethodDeclImpl() {
        return extProcedureDeclImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code NewArray} AST node.
     * @return the {@code Ext} object for a {@code NewArray} AST node.
     */
    protected Ext extNewArrayImpl() {
        return extExprImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Node} AST node.
     * @return the {@code Ext} object for a {@code Node} AST node.
     */
    protected Ext extNodeImpl() {
        return null;
    }

    /**
     * Create the {@code Ext} object for a {@code NodeList} AST node.
     * @return the {@code Ext} object for a {@code NodeList} AST node.
     */
    protected Ext extNodeListImpl() {
        return extNodeImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code New} AST node.
     * @return the {@code Ext} object for a {@code New} AST node.
     */
    protected Ext extNewImpl() {
        return extExprImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code NullLit} AST node.
     * @return the {@code Ext} object for a {@code NullLit} AST node.
     */
    protected Ext extNullLitImpl() {
        return extLitImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code NumLit} AST node.
     * @return the {@code Ext} object for a {@code NumLit} AST node.
     */
    protected Ext extNumLitImpl() {
        return extLitImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code PackageNode} AST node.
     * @return the {@code Ext} object for a {@code PackageNode} AST node.
     */
    protected Ext extPackageNodeImpl() {
        return extNodeImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code ProcedureDecl} AST node.
     * @return the {@code Ext} object for a {@code ProcedureDecl} AST node.
     */
    protected Ext extProcedureDeclImpl() {
        return extCodeDeclImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Return} AST node.
     * @return the {@code Ext} object for a {@code Return} AST node.
     */
    protected Ext extReturnImpl() {
        return extStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code SourceCollection} AST node.
     * @return the {@code Ext} object for a {@code SourceCollection} AST node.
     */
    protected Ext extSourceCollectionImpl() {
        return extNodeImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code SourceFile} AST node.
     * @return the {@code Ext} object for a {@code SourceFile} AST node.
     */
    protected Ext extSourceFileImpl() {
        return extNodeImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Special} AST node.
     * @return the {@code Ext} object for a {@code Special} AST node.
     */
    protected Ext extSpecialImpl() {
        return extExprImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Stmt} AST node.
     * @return the {@code Ext} object for a {@code Stmt} AST node.
     */
    protected Ext extStmtImpl() {
        return extTermImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code StringLit} AST node.
     * @return the {@code Ext} object for a {@code StringLit} AST node.
     */
    protected Ext extStringLitImpl() {
        return extLitImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code SwitchBlock} AST node.
     * @return the {@code Ext} object for a {@code SwitchBlock} AST node.
     */
    protected Ext extSwitchBlockImpl() {
        return extSwitchElementImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code SwitchElement} AST node.
     * @return the {@code Ext} object for a {@code SwitchElement} AST node.
     */
    protected Ext extSwitchElementImpl() {
        return extStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Switch} AST node.
     * @return the {@code Ext} object for a {@code Switch} AST node.
     */
    protected Ext extSwitchImpl() {
        return extCompoundStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Synchronized} AST node.
     * @return the {@code Ext} object for a {@code Synchronized} AST node.
     */
    protected Ext extSynchronizedImpl() {
        return extCompoundStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Term} AST node.
     * @return the {@code Ext} object for a {@code Term} AST node.
     */
    protected Ext extTermImpl() {
        return extNodeImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Throw} AST node.
     * @return the {@code Ext} object for a {@code Throw} AST node.
     */
    protected Ext extThrowImpl() {
        return extStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Try} AST node.
     * @return the {@code Ext} object for a {@code Try} AST node.
     */
    protected Ext extTryImpl() {
        return extCompoundStmtImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code TypeNode} AST node.
     * @return the {@code Ext} object for a {@code TypeNode} AST node.
     */
    protected Ext extTypeNodeImpl() {
        return extTermImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code Unary} AST node.
     * @return the {@code Ext} object for a {@code Unary} AST node.
     */
    protected Ext extUnaryImpl() {
        return extExprImpl();
    }

    /**
     * Create the {@code Ext} object for a {@code While} AST node.
     * @return the {@code Ext} object for a {@code While} AST node.
     */
    protected Ext extWhileImpl() {
        return extLoopImpl();
    }

    // ********************************************
    // Post methods
    // ********************************************

    protected Ext postExtId(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtAmbAssign(Ext ext) {
        return postExtAssign(ext);
    }

    protected Ext postExtAmbExpr(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtAmbPrefix(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtAmbQualifierNode(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtAmbReceiver(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtAmbTypeNode(Ext ext) {
        return postExtTypeNode(ext);
    }

    protected Ext postExtArrayAccess(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtArrayInit(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtArrayTypeNode(Ext ext) {
        return postExtTypeNode(ext);
    }

    protected Ext postExtAssert(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtAssign(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtLocalAssign(Ext ext) {
        return postExtAssign(ext);
    }

    protected Ext postExtFieldAssign(Ext ext) {
        return postExtAssign(ext);
    }

    protected Ext postExtArrayAccessAssign(Ext ext) {
        return postExtAssign(ext);
    }

    protected Ext postExtBinary(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtBlock(Ext ext) {
        return postExtCompoundStmt(ext);
    }

    protected Ext postExtBooleanLit(Ext ext) {
        return postExtLit(ext);
    }

    protected Ext postExtBranch(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtCall(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtCanonicalTypeNode(Ext ext) {
        return postExtTypeNode(ext);
    }

    protected Ext postExtCase(Ext ext) {
        return postExtSwitchElement(ext);
    }

    protected Ext postExtCast(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtCatch(Ext ext) {
        return postExtCompoundStmt(ext);
    }

    protected Ext postExtCharLit(Ext ext) {
        return postExtNumLit(ext);
    }

    protected Ext postExtClassBody(Ext ext) {
        return postExtTerm(ext);
    }

    protected Ext postExtClassDecl(Ext ext) {
        return postExtTerm(ext);
    }

    protected Ext postExtClassLit(Ext ext) {
        return postExtLit(ext);
    }

    protected Ext postExtClassMember(Ext ext) {
        return postExtTerm(ext);
    }

    protected Ext postExtCodeDecl(Ext ext) {
        return postExtClassMember(ext);
    }

    protected Ext postExtCompoundStmt(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtConditional(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtConstructorCall(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtConstructorDecl(Ext ext) {
        return postExtProcedureDecl(ext);
    }

    protected Ext postExtDo(Ext ext) {
        return postExtLoop(ext);
    }

    protected Ext postExtEmpty(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtEval(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtExpr(Ext ext) {
        return postExtTerm(ext);
    }

    protected Ext postExtField(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtFieldDecl(Ext ext) {
        return postExtClassMember(ext);
    }

    protected Ext postExtFloatLit(Ext ext) {
        return postExtLit(ext);
    }

    protected Ext postExtFor(Ext ext) {
        return postExtLoop(ext);
    }

    protected Ext postExtFormal(Ext ext) {
        return postExtTerm(ext);
    }

    protected Ext postExtIf(Ext ext) {
        return postExtCompoundStmt(ext);
    }

    protected Ext postExtImport(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtInitializer(Ext ext) {
        return postExtCodeDecl(ext);
    }

    protected Ext postExtInstanceof(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtIntLit(Ext ext) {
        return postExtNumLit(ext);
    }

    protected Ext postExtLabeled(Ext ext) {
        return postExtCompoundStmt(ext);
    }

    protected Ext postExtLit(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtLocal(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtLocalClassDecl(Ext ext) {
        return postExtCompoundStmt(ext);
    }

    protected Ext postExtLocalDecl(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtLoop(Ext ext) {
        return postExtCompoundStmt(ext);
    }

    protected Ext postExtMethodDecl(Ext ext) {
        return postExtProcedureDecl(ext);
    }

    protected Ext postExtNewArray(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtNode(Ext ext) {
        return ext;
    }

    protected Ext postExtNodeList(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtNew(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtNullLit(Ext ext) {
        return postExtLit(ext);
    }

    protected Ext postExtNumLit(Ext ext) {
        return postExtLit(ext);
    }

    protected Ext postExtPackageNode(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtProcedureDecl(Ext ext) {
        return postExtCodeDecl(ext);
    }

    protected Ext postExtReturn(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtSourceCollection(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtSourceFile(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtSpecial(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtStmt(Ext ext) {
        return postExtTerm(ext);
    }

    protected Ext postExtStringLit(Ext ext) {
        return postExtLit(ext);
    }

    protected Ext postExtSwitchBlock(Ext ext) {
        return postExtSwitchElement(ext);
    }

    protected Ext postExtSwitchElement(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtSwitch(Ext ext) {
        return postExtCompoundStmt(ext);
    }

    protected Ext postExtSynchronized(Ext ext) {
        return postExtCompoundStmt(ext);
    }

    protected Ext postExtTerm(Ext ext) {
        return postExtNode(ext);
    }

    protected Ext postExtThrow(Ext ext) {
        return postExtStmt(ext);
    }

    protected Ext postExtTry(Ext ext) {
        return postExtCompoundStmt(ext);
    }

    protected Ext postExtTypeNode(Ext ext) {
        return postExtTerm(ext);
    }

    protected Ext postExtUnary(Ext ext) {
        return postExtExpr(ext);
    }

    protected Ext postExtWhile(Ext ext) {
        return postExtLoop(ext);
    }

}
