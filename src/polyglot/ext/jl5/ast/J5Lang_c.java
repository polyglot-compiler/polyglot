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
package polyglot.ext.jl5.ast;

import polyglot.ast.Call;
import polyglot.ast.CallOps;
import polyglot.ast.Case;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassDeclOps;
import polyglot.ast.Expr;
import polyglot.ast.ExprOps;
import polyglot.ast.Ext;
import polyglot.ast.JLang_c;
import polyglot.ast.Lang;
import polyglot.ast.Loop;
import polyglot.ast.LoopOps;
import polyglot.ast.New;
import polyglot.ast.NewOps;
import polyglot.ast.Node;
import polyglot.ast.NodeOps;
import polyglot.ast.ProcedureDecl;
import polyglot.ast.ProcedureDeclOps;
import polyglot.ast.Switch;
import polyglot.ast.Term;
import polyglot.ast.TermOps;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.visit.TypeChecker;

public class J5Lang_c extends JLang_c implements J5Lang {
    public static final J5Lang_c instance = new J5Lang_c();

    public static J5Lang lang(NodeOps n) {
        while (n != null) {
            Lang lang = n.lang();
            if (lang instanceof J5Lang) return (J5Lang) lang;
            if (n instanceof Ext)
                n = ((Ext) n).pred();
            else return null;
        }
        throw new InternalCompilerError("Impossible to reach");
    }

    protected J5Lang_c() {
    }

    protected static JL5Ext jl5ext(Node n) {
        return JL5Ext.ext(n);
    }

    @Override
    protected NodeOps NodeOps(Node n) {
        return jl5ext(n);
    }

    @Override
    protected CallOps CallOps(Call n) {
        return (CallOps) jl5ext(n);
    }

    @Override
    protected ClassDeclOps ClassDeclOps(ClassDecl n) {
        return (ClassDeclOps) jl5ext(n);
    }

    @Override
    protected ExprOps ExprOps(Expr n) {
        return (ExprOps) jl5ext(n);
    }

    @Override
    protected LoopOps LoopOps(Loop n) {
        return (LoopOps) jl5ext(n);
    }

    @Override
    protected NewOps NewOps(New n) {
        return (NewOps) jl5ext(n);
    }

    @Override
    protected ProcedureDeclOps ProcedureDeclOps(ProcedureDecl n) {
        return (ProcedureDeclOps) jl5ext(n);
    }

    @Override
    protected TermOps TermOps(Term n) {
        return (TermOps) jl5ext(n);
    }

    protected JL5CaseOps JL5CaseOps(Case n) {
        return (JL5CaseOps) jl5ext(n);
    }

    protected JL5SwitchOps JL5SwitchOps(Switch n) {
        return (JL5SwitchOps) jl5ext(n);
    }

    // JL5CaseOps

    @Override
    public final Case resolveCaseLabel(Case n, TypeChecker tc, Type switchType)
            throws SemanticException {
        return JL5CaseOps(n).resolveCaseLabel(tc, switchType);
    }

    // JL5SwitchOps

    @Override
    public final boolean isAcceptableSwitchType(Switch n, Type type) {
        return JL5SwitchOps(n).isAcceptableSwitchType(type);
    }
}
