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

import polyglot.ast.CallOps;
import polyglot.ast.ClassDeclOps;
import polyglot.ast.JLDel_c;
import polyglot.ast.NewOps;
import polyglot.ast.Node;
import polyglot.ast.NodeOps;
import polyglot.ast.ProcedureDeclOps;
import polyglot.util.SerialVersionUID;

public class JL5Del extends JLDel_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public static final JL5Del instance = new JL5Del();

    protected JL5Del() {
    }

    public JL5Ext jl5ext(Node n) {
        return JL5Ext.ext(n);
    }

    @Override
    public NodeOps NodeOps(Node n) {
        return jl5ext(n);
    }

    @Override
    public CallOps CallOps(Node n) {
        return (CallOps) jl5ext(n);
    }

    @Override
    public ClassDeclOps ClassDeclOps(Node n) {
        return (ClassDeclOps) jl5ext(n);
    }

    @Override
    public NewOps NewOps(Node n) {
        return (NewOps) jl5ext(n);
    }

    @Override
    public ProcedureDeclOps ProcedureDeclOps(Node n) {
        return (ProcedureDeclOps) jl5ext(n);
    }

    public JL5CaseOps CaseOps(Node n) {
        return (JL5CaseOps) jl5ext(n);
    }

    public JL5SwitchOps SwitchOps(Node n) {
        return (JL5SwitchOps) jl5ext(n);
    }
//
//    @Override
//    public Node visitChildren(NodeVisitor v) {
//        return jl5ext().visitChildren(v);
//    }
//
//    @Override
//    public Context enterScope(Context c) {
//        return jl5ext().enterScope(c);
//    }
//
//    @Override
//    public Context enterChildScope(Node child, Context c) {
//        return jl5ext().enterChildScope(child, c);
//    }
//
//    @Override
//    public Node buildTypes(TypeBuilder tb) throws SemanticException {
//        return jl5ext().buildTypes(tb);
//    }
//
//    @Override
//    public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
//            throws SemanticException {
//        return jl5ext().disambiguateOverride(parent, ar);
//    }
//
//    @Override
//    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
//        return jl5ext().disambiguate(ar);
//    }
//
//    @Override
//    public Node typeCheckOverride(Node parent, TypeChecker tc)
//            throws SemanticException {
//        return jl5ext().typeCheckOverride(parent, tc);
//    }
//
//    @Override
//    public NodeVisitor typeCheckEnter(TypeChecker tc) throws SemanticException {
//        return jl5ext().typeCheckEnter(tc);
//    }
//
//    @Override
//    public Node typeCheck(TypeChecker tc) throws SemanticException {
//        return jl5ext().typeCheck(tc);
//    }
//
//    @Override
//    public Type childExpectedType(Expr child, AscriptionVisitor av) {
//        return jl5ext().childExpectedType(child, av);
//    }
//
//    @Override
//    public Node checkConstants(ConstantChecker cc) throws SemanticException {
//        return jl5ext().checkConstants(cc);
//    }
//
//    @Override
//    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
//        jl5ext().prettyPrint(w, tr);
//    }
//
//    @Override
//    public void translate(CodeWriter w, Translator tr) {
//        jl5ext().translate(w, tr);
//    }
}
