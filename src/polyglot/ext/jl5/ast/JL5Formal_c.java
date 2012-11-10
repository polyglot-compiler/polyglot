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

import java.util.Collections;
import java.util.List;

import polyglot.ast.Formal;
import polyglot.ast.Formal_c;
import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5ArrayType;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.visit.AnnotationChecker;
import polyglot.types.ArrayType;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5Formal_c extends Formal_c implements JL5Formal {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected boolean isVarArg;
    protected List<AnnotationElem> annotations;

    public JL5Formal_c(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name) {
        this(pos, flags, annotations, type, name, false);
    }

    public JL5Formal_c(Position pos, Flags flags,
            List<AnnotationElem> annotations, TypeNode type, Id name,
            boolean variable) {
        super(pos, flags, type, name);
        this.isVarArg = variable;
        if (annotations == null) {
            annotations = Collections.emptyList();
        }
        this.annotations = annotations;
    }

    @Override
    public boolean isVarArg() {
        return isVarArg;
    }

    protected Formal reconstruct(TypeNode type,
            List<AnnotationElem> annotations, Id name) {
        if (this.type() != type
                || !CollectionUtil.equals(annotations, this.annotations)
                || this.id() != name) {
            JL5Formal_c n = (JL5Formal_c) copy();
            n.type = type;
            n.name = name;
            n.annotations = annotations;
            n.name = name;
            return n;
        }
        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode type = (TypeNode) visitChild(this.type(), v);
        Id name = (Id) visitChild(this.id(), v);
        List<AnnotationElem> annots = visitList(this.annotations, v);

        return reconstruct(type, annots, name);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        if (!flags().clear(Flags.FINAL).equals(Flags.NONE)) {
            throw new SemanticException("Modifier: " + flags().clearFinal()
                    + " not allowed here.", position());
        }
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        ts.checkDuplicateAnnotations(annotations);
        return super.typeCheck(tc);

    }

    @Override
    public JL5Formal annotationElems(List<AnnotationElem> annotations) {
        JL5Formal_c n = (JL5Formal_c) copy();
        n.annotations = annotations;
        return n;
    }

    @Override
    public Node annotationCheck(AnnotationChecker annoCheck)
            throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) annoCheck.typeSystem();
        for (AnnotationElem elem : annotations) {
            ts.checkAnnotationApplicability(elem, this.localInstance());
        }
        return this;
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (isVarArg()) {
            ((JL5ArrayType) type().type()).setVarArg();
        }
        JL5Formal_c form = (JL5Formal_c) super.disambiguate(ar);

        return form;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write(JL5Flags.clearVarArgs(flags).translate());
        if (isVarArg()) {
            w.write(((ArrayType) type.type()).base().toString());
            //print(type, w, tr);
            w.write(" ...");
        }
        else {
            print(type, w, tr);
        }
        w.write(" ");
        w.write(name.id());

    }

    @Override
    public List<AnnotationElem> annotationElems() {
        return this.annotations;
    }
}
