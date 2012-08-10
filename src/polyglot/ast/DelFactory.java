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
 * A <code>DelFactory</code> constructs delegates. It is only used by
 * a <code>NodeFactory</code>, during the creation of AST nodes.
 */
public interface DelFactory {

    //////////////////////////////////////////////////////////////////
    // Factory Methods
    //////////////////////////////////////////////////////////////////

    JL delId();

    JL delAmbAssign();

    JL delAmbExpr();

    JL delAmbPrefix();

    JL delAmbQualifierNode();

    JL delAmbReceiver();

    JL delAmbTypeNode();

    JL delArrayAccess();

    JL delArrayInit();

    JL delArrayTypeNode();

    JL delAssert();

    JL delAssign();

    JL delLocalAssign();

    JL delFieldAssign();

    JL delArrayAccessAssign();

    JL delBinary();

    JL delBlock();

    JL delBooleanLit();

    JL delBranch();

    JL delCall();

    JL delCanonicalTypeNode();

    JL delCase();

    JL delCast();

    JL delCatch();

    JL delCharLit();

    JL delClassBody();

    JL delClassDecl();

    JL delClassLit();

    JL delClassMember();

    JL delCodeDecl();

    JL delCompoundStmt();

    JL delConditional();

    JL delConstructorCall();

    JL delConstructorDecl();

    JL delDo();

    JL delEmpty();

    JL delEval();

    JL delExpr();

    JL delField();

    JL delFieldDecl();

    JL delFloatLit();

    JL delFor();

    JL delFormal();

    JL delIf();

    JL delImport();

    JL delInitializer();

    JL delInstanceof();

    JL delIntLit();

    JL delLabeled();

    JL delLit();

    JL delLocal();

    JL delLocalClassDecl();

    JL delLocalDecl();

    JL delLoop();

    JL delMethodDecl();

    JL delNewArray();

    JL delNode();

    JL delNodeList();

    JL delNew();

    JL delNullLit();

    JL delNumLit();

    JL delPackageNode();

    JL delProcedureDecl();

    JL delReturn();

    JL delSourceCollection();

    JL delSourceFile();

    JL delSpecial();

    JL delStmt();

    JL delStringLit();

    JL delSwitchBlock();

    JL delSwitchElement();

    JL delSwitch();

    JL delSynchronized();

    JL delTerm();

    JL delThrow();

    JL delTry();

    JL delTypeNode();

    JL delUnary();

    JL delWhile();
}
