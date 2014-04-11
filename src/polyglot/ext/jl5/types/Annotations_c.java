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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import polyglot.types.Type;
import polyglot.types.TypeObject_c;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public class Annotations_c extends TypeObject_c implements Annotations {
    private static final long serialVersionUID = SerialVersionUID.generate();

    private Map<Type, Map<String, AnnotationElementValue>> annotations;

    public Annotations_c(JL5TypeSystem ts, Position pos) {
        super(ts, pos);
        this.annotations = Collections.emptyMap();
    }

    public Annotations_c(
            Map<Type, Map<String, AnnotationElementValue>> annotations,
            JL5TypeSystem ts, Position pos) {
        super(ts, pos);
        this.annotations = annotations;
    }

    @Override
    public boolean isCanonical() {
        return true;
    }

    @Override
    public Set<Type> annotationTypes() {
        return Collections.unmodifiableSet(annotations.keySet());
    }

    @Override
    public Set<Type> retainedAnnotationTypes() {
        Set<Type> retAnnType = new LinkedHashSet<>(annotations.keySet());
        JL5TypeSystem ts = (JL5TypeSystem) this.typeSystem();
        for (Iterator<Type> iter = retAnnType.iterator(); iter.hasNext();) {
            Type t = iter.next();
            if (!ts.isRetainedAnnotation(t)) {
                iter.remove();
            }
        }
        return Collections.unmodifiableSet(retAnnType);
    }

    @Override
    public Map<String, AnnotationElementValue> elementValuePairs(
            Type annotationType) {
        if (annotations.containsKey(annotationType)) {
            return Collections.unmodifiableMap(annotations.get(annotationType));
        }
        return null;
    }

    @Override
    public boolean hasAnnotationType(Type annotationType) {
        return annotations.containsKey(annotationType);
    }

    @Override
    public AnnotationElementValue singleElement(Type annotationType) {
        if (annotations.containsKey(annotationType)) {
            return annotations.get(annotationType).get("value");
        }
        return null;
    }
}
