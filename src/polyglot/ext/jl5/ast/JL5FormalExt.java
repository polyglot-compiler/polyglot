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

import java.util.List;

import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ext.jl5.types.Annotations;
import polyglot.ext.jl5.types.JL5ArrayType;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5LocalInstance;
import polyglot.types.ArrayType;
import polyglot.types.Declaration;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.util.CodeWriter;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5FormalExt extends JL5AnnotatedElementExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected boolean isVarArg = false;

    protected boolean isCatchFormal = false;

    public JL5FormalExt() {
        this(false, null);
    }

    public JL5FormalExt(boolean isVarArg, List<AnnotationElem> annotations) {
        super(annotations);
        this.isVarArg = isVarArg;
    }

    public boolean isVarArg() {
        return isVarArg;
    }

    public boolean isCatchFormal() {
        return isCatchFormal;
    }

    public void setIsCatchFormal(boolean isCatchFormal) {
        this.isCatchFormal = isCatchFormal;
    }

    @Override
    public void setAnnotations(Annotations annotations) {
        Formal f = (Formal) this.node();
        JL5LocalInstance li = (JL5LocalInstance) f.localInstance();
        li.setAnnotations(annotations);
    }

    @Override
    protected Declaration declaration() {
        Formal f = (Formal) this.node();
        return f.localInstance();
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Formal f = (Formal) this.node();

        if (!f.flags().clear(Flags.FINAL).equals(Flags.NONE)) {
            throw new SemanticException("Modifier: " + f.flags().clearFinal()
                    + " not allowed here.", f.position());
        }
        return super.typeCheck(tc);

    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        Formal f = (Formal) this.node();

        if (isVarArg) {
            ((JL5ArrayType) f.type().type()).setVarArg();
        }
        return superLang().disambiguate(this.node(), ar);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        super.prettyPrint(w, tr);

        Formal f = (Formal) this.node();
        w.write(JL5Flags.clearVarArgs(f.flags()).translate());
        if (isVarArg) {
            w.write(((ArrayType) f.type().type()).base().toString());
            //print(type, w, tr);
            w.write(" ...");
        }
        else {
            print(f.type(), w, tr);
        }
        w.write(" ");
        w.write(f.name());

    }

}
