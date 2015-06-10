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
package polyglot.ext.jl5.parse;

import java.util.LinkedList;
import java.util.List;

import polyglot.ext.jl5.ast.AnnotationElem;
import polyglot.types.Flags;
import polyglot.util.Position;

public class FlagAnnotations {

    protected Flags classicFlags;
    protected List<AnnotationElem> annotations;

    public FlagAnnotations() {
        classicFlags = Flags.NONE;
        annotations = new LinkedList<>();
    }

    public FlagAnnotations(Position pos) {
        this();
        classicFlags = classicFlags.position(pos);
    }

    public Flags flags() {
        return classicFlags;
    }

    public FlagAnnotations flags(Flags flags) {
        this.classicFlags = flags;
        return this;
    }

    public FlagAnnotations setFlag(Flags flags) {
        Flags f = this.flags().set(flags);
        return this.flags(f);
    }

    public FlagAnnotations annotations(List<AnnotationElem> annotations) {
        this.annotations = annotations;
        return this;
    }

    public List<AnnotationElem> annotations() {
        return annotations;
    }

    public FlagAnnotations addAnnotation(AnnotationElem o) {
        annotations.add(o);
        return this;
    }

    public Position position() {
        // Figure out the flag position.
        Position flagsPos = null;
        if (classicFlags != null && !classicFlags.flags().isEmpty())
            flagsPos = classicFlags.position();

        // Check whether there are any annotations. If none, return the flag position.
        if (annotations == null || annotations.isEmpty()) {
            return flagsPos;
        }

        AnnotationElem firstAnnotation = annotations.get(0);
        AnnotationElem lastAnnotation = annotations.get(annotations.size() - 1);

        return new Position(firstAnnotation.position(),
                            new Position(lastAnnotation.position(), flagsPos));
    }
}
