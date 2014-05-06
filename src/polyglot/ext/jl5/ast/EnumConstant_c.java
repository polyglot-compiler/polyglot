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

import polyglot.ast.Field_c;
import polyglot.ast.Id;
import polyglot.ast.Lang;
import polyglot.ast.Node;
import polyglot.ast.Receiver;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public class EnumConstant_c extends Field_c implements EnumConstant {
    public EnumConstant_c(Position pos, Receiver target, Id name) {
        super(pos, target, name);
    }

    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public boolean constantValueSet(Lang lang) {
        return true;
    }

    @Override
    public boolean isConstant(Lang lang) {
        // An enum constant is not a compile-time constant. See JLS 3rd edition 15.28.
        return false;
    }

    @Override
    public Object constantValue(Lang lang) {
        return enumInstance();
    }

    @Override
    public EnumInstance enumInstance() {
        return (EnumInstance) fieldInstance();
    }

    @Override
    public Node enumInstance(EnumInstance enumInstance) {
        return fieldInstance(enumInstance);
    }

}
