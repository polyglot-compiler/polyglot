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
package polyglot.ext.jl7.ast;

import polyglot.ast.Block;
import polyglot.ast.Catch;
import polyglot.ast.JLDel;
import polyglot.ast.Node;
import polyglot.ast.NodeOps;
import polyglot.ext.jl5.ast.JL5CaseOps;
import polyglot.ext.jl5.ast.JL5Del_c;
import polyglot.ext.jl5.ast.JL5SwitchOps;
import polyglot.types.TypeSystem;
import polyglot.util.SerialVersionUID;
import polyglot.util.SubtypeSet;

public class JL7Del_c extends JL5Del_c implements JL7Del {
    private static final long serialVersionUID = SerialVersionUID.generate();
    public static final JL7Del_c instance = new JL7Del_c();

    protected JL7Del_c() {
    }

    public JL7Ext jl7ext(Node n) {
        return JL7Ext.ext(n);
    }

    @Override
    protected NodeOps NodeOps(Node n) {
        return jl7ext(n);
    }

    @Override
    protected JL7TryOps TryOps(Node n) {
        return (JL7TryOps) jl7ext(n);
    }

    @Override
    protected JL5CaseOps JL5CaseOps(Node n) {
        return (JL5CaseOps) jl7ext(n);
    }

    @Override
    protected JL5SwitchOps JL5SwitchOps(Node n) {
        return (JL5SwitchOps) jl7ext(n);
    }

    // JL7TryOps

    @Override
    public void checkPreciseRethrows(Node n, JLDel lang, TypeSystem typeSystem,
            Block b) {
        TryOps(n).checkPreciseRethrows(lang, typeSystem, b);
    }

    @Override
    public void preciseRethrowsForCatchBlock(Node n, JLDel lang, Catch cb,
            SubtypeSet thrown) {
        TryOps(n).preciseRethrowsForCatchBlock(lang, cb, thrown);
    }
}
