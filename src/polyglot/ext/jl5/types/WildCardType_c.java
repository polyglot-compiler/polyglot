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

import java.util.Collections;
import java.util.List;

import polyglot.types.FieldInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.ReferenceType_c;
import polyglot.types.Resolver;
import polyglot.types.Type;
import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public class WildCardType_c extends ReferenceType_c implements WildCardType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    private ReferenceType upperBound;
    private ReferenceType lowerBound;

    public WildCardType_c(TypeSystem ts, Position position,
            ReferenceType upperBound, ReferenceType lowerBound) {
        super(ts, position);
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
    }

    @Override
    public FieldInstance fieldNamed(String name) {
        for (FieldInstance fi : fields()) {
            if (fi.name().equals(name)) {
                return fi;
            }
        }
        return null;
    }

    @Override
    public boolean isCanonical() {
        return true;
    }

    @Override
    public ReferenceType upperBound() {
        return upperBound;
    }

    @Override
    public WildCardType upperBound(ReferenceType newUpperBound) {
        if (upperBound == newUpperBound) {
            return this;
        }
        WildCardType_c n = (WildCardType_c) copy();
        n.upperBound = newUpperBound;
        return n;
    }

    @Override
    public WildCardType lowerBound(ReferenceType newLowerBound) {
        if (lowerBound == newLowerBound) {
            return this;
        }
        WildCardType_c n = (WildCardType_c) copy();
        n.lowerBound = newLowerBound;
        return n;
    }

    @Override
    public ReferenceType lowerBound() {
        return lowerBound;
    }

    @Override
    public List<? extends MethodInstance> methods() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends FieldInstance> fields() {
        return Collections.emptyList();
    }

    @Override
    public Type superType() {
        if (this.upperBound().isClass()
                && !this.upperBound().toClass().flags().isInterface()) {
            return this.upperBound();
        }
        return ts.Object();
    }

    @Override
    public List<? extends ReferenceType> interfaces() {
        if (this.upperBound().isClass()
                && this.upperBound().toClass().flags().isInterface()) {
            return Collections.singletonList(this.upperBound());
        }
        return Collections.emptyList();
    }

    @Override
    public String translate(Resolver c) {
        StringBuffer sb = new StringBuffer();
        sb.append('?');
        if (!ts.Object().equals(upperBound)) {
            sb.append(" extends ");
            sb.append(upperBound.translate(c));
        }
        else if (lowerBound != null) {
            sb.append(" super ");
            sb.append(lowerBound.translate(c));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('?');
        if (!ts.Object().equals(upperBound)) {
            sb.append(" extends ");
            sb.append(upperBound);
        }
        else if (lowerBound != null) {
            sb.append(" super ");
            sb.append(lowerBound);
        }
        return sb.toString();
    }

    @Override
    public boolean equalsImpl(TypeObject t) {
        if (t instanceof WildCardType_c) {
            WildCardType_c that = (WildCardType_c) t;
            if (!(upperBound == that.upperBound || upperBound != null
                    && typeSystem().equals(upperBound, that.upperBound))) {
                return false;
            }
            if (!(lowerBound == that.lowerBound || lowerBound != null
                    && typeSystem().equals(lowerBound, that.lowerBound))) {
                return false;
            }
            return true;
        }
        return super.equalsImpl(t);
    }

    @Override
    public boolean typeEqualsImpl(Type t) {
        if (t instanceof WildCardType_c) {
            WildCardType_c that = (WildCardType_c) t;
            if (!(upperBound == that.upperBound || upperBound != null
                    && typeSystem().typeEquals(upperBound, that.upperBound))) {
                return false;
            }
            if (!(lowerBound == that.lowerBound || lowerBound != null
                    && typeSystem().typeEquals(lowerBound, that.lowerBound))) {
                return false;
            }
            return true;
        }
        return super.typeEqualsImpl(t);
    }

    @Override
    public int hashCode() {
        return 723492 ^ (lowerBound == null ? 0 : lowerBound.hashCode())
                ^ (upperBound == null ? 0 : upperBound.hashCode());
    }

    @Override
    public boolean isExtendsConstraint() {
        return !isSuperConstraint();
    }

    @Override
    public boolean isSuperConstraint() {
        return lowerBound != null;
    }

    @Override
    public boolean hasLowerBound() {
        return lowerBound != null;
    }

    @Override
    public boolean descendsFromImpl(Type ancestor) {
        if (super.descendsFromImpl(ancestor)) {
            return true;
        }
        if (ts.isSubtype(upperBound(), ancestor)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isCastValidImpl(Type toType) {
        if (super.isCastValidImpl(toType)) {
            return true;
        }
        // try the upper bound. See JLS 3rd Ed 5.5
        return this.upperBound().isCastValid(toType);
    }

}
