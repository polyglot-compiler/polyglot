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
}
