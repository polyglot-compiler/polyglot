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
package polyglot.ext.jl5.types;

import polyglot.types.Resolver;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.util.UniqueID;

public class CaptureConvertedWildCardType_c extends TypeVariable_c implements
        CaptureConvertedWildCardType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    /**
     * Do not recurse when translating capture-converted wildcard appearing
     * recursively in its bound.
     */
    private boolean inBound = false;

    public CaptureConvertedWildCardType_c(TypeSystem ts, Position pos) {
        super(ts, pos, UniqueID.newID("capture"), null); // we'll replace this unknown type soon.
    }

    @Override
    public boolean isExtendsConstraint() {
        return !isSuperConstraint();
    }

    @Override
    public boolean isSuperConstraint() {
        return hasLowerBound();
    }

    @Override
    public String translate(Resolver c) {
        return translate(c, false);
    }

    @Override
    public String toString() {
        return translate(null, true);
    }

    private String translate(Resolver c, boolean printCaptureName) {
        StringBuffer sb = new StringBuffer();
        if (printCaptureName) {
            sb.append(name);
            sb.append("-of ");
        }
        sb.append('?');
        if (!inBound) {
            inBound = true;
            if (!ts.Object().equals(upperBound)) {
                sb.append(" extends ");
                if (c == null)
                    sb.append(upperBound);
                else sb.append(upperBound.translate(c));
            }
            else if (lowerBound != null) {
                sb.append(" super ");
                if (c == null)
                    sb.append(lowerBound);
                else sb.append(lowerBound.translate(c));
            }
            inBound = false;
        }
        return sb.toString();
    }
}
