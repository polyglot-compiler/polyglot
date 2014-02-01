/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.extension;

import pao.ast.PaoLang;
import pao.types.PaoTypeSystem;
import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;

/**
 * Default implementation of <code>PaoExt</code>.
 */
public class PaoExt_c extends Ext_c implements PaoExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public static PaoExt ext(Node n) {
        Ext e = n.ext();
        while (e != null && !(e instanceof PaoExt)) {
            e = e.ext();
        }
        if (e == null) {
            throw new InternalCompilerError("No Pao extension object for node "
                    + n + " (" + n.getClass() + ")", n.position());
        }
        return (PaoExt) e;
    }

    @Override
    public final PaoLang lang() {
        return PaoLang.instance;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return superLang().typeCheck(this.node(), tc);
    }

    /**
     * Default implementation of <code>rewrite</code>, returns the node
     * unchanged.
     * 
     * @see PaoExt#rewrite(PaoTypeSystem, NodeFactory)
     */
    @Override
    public Node rewrite(PaoTypeSystem ts, NodeFactory nf) {
        return node();
    }
}
