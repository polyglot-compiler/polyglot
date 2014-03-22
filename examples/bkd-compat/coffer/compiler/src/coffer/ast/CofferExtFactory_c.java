/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.ast;

import polyglot.ast.AbstractExtFactory_c;
import polyglot.ast.Ext;
import coffer.extension.AssignExt_c;
import coffer.extension.CofferExt_c;
import coffer.extension.FreeExt_c;
import coffer.extension.LocalDeclExt_c;
import coffer.extension.LocalExt_c;
import coffer.extension.NewExt_c;
import coffer.extension.ProcedureCallExt_c;
import coffer.extension.ProcedureDeclExt_c;
import coffer.extension.SpecialExt_c;

/** An implementation of the <code>CofferNodeFactory</code> interface. 
 */
public class CofferExtFactory_c extends AbstractExtFactory_c {
    public CofferExtFactory_c() {
        super();
    }

    public final Ext extCanonicalKeySetNode() {
        Ext e = extCanonicalKeySetNodeImpl();
        return e;
    }

    public final Ext extFree() {
        Ext e = extFreeImpl();
        return e;
    }

    public final Ext extKeyNode() {
        Ext e = extKeyNodeImpl();
        return e;
    }

    public final Ext extThrowConstraintNode() {
        Ext e = extThrowConstraintNodeImpl();
        return e;
    }

    public final Ext extTrackedTypeNode() {
        Ext e = extTrackedTypeNodeImpl();
        return e;
    }

    public final Ext extAmbKeySetNode() {
        Ext e = extAmbKeySetNodeImpl();
        return e;
    }

    @Override
    public Ext extNodeImpl() {
        return new CofferExt_c();
    }

    @Override
    public Ext extAssignImpl() {
        return new AssignExt_c();
    }

    @Override
    public Ext extLocalImpl() {
        return new LocalExt_c();
    }

    @Override
    public Ext extSpecialImpl() {
        return new SpecialExt_c();
    }

    @Override
    public Ext extLocalDeclImpl() {
        return new LocalDeclExt_c();
    }

    @Override
    public Ext extConstructorCallImpl() {
        return extProcedureCallImpl();
    }

    @Override
    public Ext extCallImpl() {
        return extProcedureCallImpl();
    }

    public Ext extProcedureCallImpl() {
        return new ProcedureCallExt_c();
    }

    @Override
    public Ext extNewImpl() {
        return new NewExt_c();
    }

    public Ext extFreeImpl() {
        return new FreeExt_c();
    }

    public Ext extCanonicalKeySetNodeImpl() {
        return extNodeImpl();
    }

    public Ext extAmbKeySetNodeImpl() {
        return extNodeImpl();
    }

    public Ext extKeyNodeImpl() {
        return extNodeImpl();
    }

    public Ext extTrackedTypeNodeImpl() {
        return extTypeNodeImpl();
    }

    public Ext extThrowConstraintNodeImpl() {
        return extNodeImpl();
    }

    @Override
    public Ext extProcedureDeclImpl() {
        return new ProcedureDeclExt_c();
    }

}
