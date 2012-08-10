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

/**
 * An <code>ExtFactory</code> constructs extensions. It is only used by
 * a <code>NodeFactory</code>, during the creation of AST nodes. ExtFactories
 * may be chained together (see AbstractExtFactory_c) to allow extensions to be
 * composed.
 */
public interface ExtFactory {

    /**
     * The next extFactory in the chain. 
     */
    ExtFactory nextExtFactory();

    //////////////////////////////////////////////////////////////////
    // Factory Methods
    //////////////////////////////////////////////////////////////////

    Ext extId();

    Ext extAmbAssign();

    Ext extAmbExpr();

    Ext extAmbPrefix();

    Ext extAmbQualifierNode();

    Ext extAmbReceiver();

    Ext extAmbTypeNode();

    Ext extArrayAccess();

    Ext extArrayInit();

    Ext extArrayTypeNode();

    Ext extAssert();

    Ext extAssign();

    Ext extLocalAssign();

    Ext extFieldAssign();

    Ext extArrayAccessAssign();

    Ext extBinary();

    Ext extBlock();

    Ext extBooleanLit();

    Ext extBranch();

    Ext extCall();

    Ext extCanonicalTypeNode();

    Ext extCase();

    Ext extCast();

    Ext extCatch();

    Ext extCharLit();

    Ext extClassBody();

    Ext extClassDecl();

    Ext extClassLit();

    Ext extClassMember();

    Ext extCodeDecl();

    Ext extCompoundStmt();

    Ext extConditional();

    Ext extConstructorCall();

    Ext extConstructorDecl();

    Ext extDo();

    Ext extEmpty();

    Ext extEval();

    Ext extExpr();

    Ext extField();

    Ext extFieldDecl();

    Ext extFloatLit();

    Ext extFor();

    Ext extFormal();

    Ext extIf();

    Ext extImport();

    Ext extInitializer();

    Ext extInstanceof();

    Ext extIntLit();

    Ext extLabeled();

    Ext extLit();

    Ext extLocal();

    Ext extLocalClassDecl();

    Ext extLocalDecl();

    Ext extLoop();

    Ext extMethodDecl();

    Ext extNewArray();

    Ext extNode();

    Ext extNodeList();

    Ext extNew();

    Ext extNullLit();

    Ext extNumLit();

    Ext extPackageNode();

    Ext extProcedureDecl();

    Ext extReturn();

    Ext extSourceCollection();

    Ext extSourceFile();

    Ext extSpecial();

    Ext extStmt();

    Ext extStringLit();

    Ext extSwitchBlock();

    Ext extSwitchElement();

    Ext extSwitch();

    Ext extSynchronized();

    Ext extTerm();

    Ext extThrow();

    Ext extTry();

    Ext extTypeNode();

    Ext extUnary();

    Ext extWhile();
}
