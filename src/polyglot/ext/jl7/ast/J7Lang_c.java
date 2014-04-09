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
import polyglot.ast.Case;
import polyglot.ast.Catch;
import polyglot.ast.Ext;
import polyglot.ast.Lang;
import polyglot.ast.New;
import polyglot.ast.NewOps;
import polyglot.ast.Node;
import polyglot.ast.NodeOps;
import polyglot.ast.Switch;
import polyglot.ast.Try;
import polyglot.ext.jl5.ast.J5Lang_c;
import polyglot.ext.jl5.ast.JL5CaseOps;
import polyglot.ext.jl5.ast.JL5SwitchOps;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.SubtypeSet;

public class J7Lang_c extends J5Lang_c implements J7Lang {
    public static final J7Lang_c instance = new J7Lang_c();

    public static J7Lang lang(NodeOps n) {
        while (n != null) {
            Lang lang = n.lang();
            if (lang instanceof J7Lang) return (J7Lang) lang;
            if (n instanceof Ext)
                n = ((Ext) n).pred();
            else return null;
        }
        throw new InternalCompilerError("Impossible to reach");
    }

    protected J7Lang_c() {
    }

    protected static JL7Ext jl7ext(Node n) {
        return JL7Ext.ext(n);
    }

    @Override
    protected NodeOps NodeOps(Node n) {
        return jl7ext(n);
    }

    @Override
    protected NewOps NewOps(New n) {
        return (NewOps) jl7ext(n);
    }

    @Override
    protected JL5CaseOps JL5CaseOps(Case n) {
        return (JL5CaseOps) jl7ext(n);
    }

    @Override
    protected JL5SwitchOps JL5SwitchOps(Switch n) {
        return (JL5SwitchOps) jl7ext(n);
    }

    @Override
    protected JL7TryOps TryOps(Try n) {
        return (JL7TryOps) jl7ext(n);
    }

    // JL7TryOps

    @Override
    public final void checkPreciseRethrows(Try n, J7Lang lang,
            TypeSystem typeSystem, Block b) {
        TryOps(n).checkPreciseRethrows(lang, typeSystem, b);
    }

    @Override
    public final void preciseRethrowsForCatchBlock(Try n, J7Lang lang,
            Catch cb, SubtypeSet thrown) {
        TryOps(n).preciseRethrowsForCatchBlock(lang, cb, thrown);
    }
}
