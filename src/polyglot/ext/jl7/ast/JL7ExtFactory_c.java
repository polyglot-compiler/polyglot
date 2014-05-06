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

import polyglot.ast.Ext;
import polyglot.ast.ExtFactory;

public final class JL7ExtFactory_c extends JL7AbstractExtFactory_c {

    public JL7ExtFactory_c() {
        super();
    }

    public JL7ExtFactory_c(ExtFactory nextExtFactory) {
        super(nextExtFactory);
    }

    @Override
    protected Ext extNodeImpl() {
        JL7Ext ext = new JL7Ext();
        return ext;
    }

    @Override
    protected Ext extCaseImpl() {
        JL7CaseExt ext = new JL7CaseExt();
        return ext;
    }

    @Override
    protected Ext extNewImpl() {
        JL7NewExt ext = new JL7NewExt();
        return ext;
    }

    @Override
    protected Ext extResourceImpl() {
        JL7ResourceExt ext = new JL7ResourceExt();
        return ext;
    }

    @Override
    protected Ext extSwitchImpl() {
        JL7SwitchExt ext = new JL7SwitchExt();
        return ext;
    }

    @Override
    protected Ext extThrowImpl() {
        JL7ThrowExt ext = new JL7ThrowExt();
        return ext;
    }

    @Override
    protected Ext extTryImpl() {
        JL7TryExt ext = new JL7TryExt();
        return ext;
    }
}
