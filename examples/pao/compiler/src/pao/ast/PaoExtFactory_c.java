/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.ast;

import polyglot.ast.Ext;
import polyglot.ast.AbstractExtFactory_c;
import pao.extension.PaoBinaryExt_c;
import pao.extension.PaoCastExt_c;
import pao.extension.PaoExt_c;
import pao.extension.PaoInstanceofExt_c;

/**
 * Extension factory for the pao extension. The extension factory 
 * is responsible for creating <code>Ext</code> objects, and is
 * used only by the <code>NodeFactory</code>. 
 */
public class PaoExtFactory_c extends AbstractExtFactory_c  {
    PaoExtFactory_c() {
        super();
    }

    /**
     * @return the default Ext object for all AST 
     * nodes other than <code>InstanceOf</code>, 
     * <code>Cast</code> and <code>Binary</code>.
     */
    public Ext extNodeImpl() {
        return new PaoExt_c();
    }

    /**
     * @see AbstractExtFactory_c#extInstanceofImpl()
     */
    public Ext extInstanceofImpl() {
        return new PaoInstanceofExt_c();
    }

    /**
     * @see AbstractExtFactory_c#extCastImpl()
     */
    public Ext extCastImpl() {
        return new PaoCastExt_c();
    }

    /**
     * @see AbstractExtFactory_c#extBinaryImpl()
     */
    public Ext extBinaryImpl() {
        return new PaoBinaryExt_c();
    }
}
