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

import java.util.Iterator;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Catch_c;
import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class MultiCatch_c extends Catch_c implements MultiCatch {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<TypeNode> alternatives;

    public MultiCatch_c(Position pos, Formal formal,
            List<TypeNode> alternatives, Block body) {
        super(pos, formal, body);
        this.alternatives = alternatives;
    }

    @Override
    public List<TypeNode> alternatives() {
        return this.alternatives;
    }

    @Override
    public MultiCatch alternatives(List<TypeNode> alternatives) {
        MultiCatch_c n = (MultiCatch_c) copy();
        n.alternatives = alternatives;
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        MultiCatch_c n = (MultiCatch_c) super.visitChildren(v);
        List<TypeNode> as = visitList(this.alternatives, v);
        if (CollectionUtil.equals(as, n.alternatives())) {
            return n;
        }
        return n.alternatives(as);
    }

    @Override
    public String toString() {
        StringBuilder types = new StringBuilder();
        Iterator<TypeNode> i = this.alternatives.iterator();
        while (i.hasNext()) {
            types.append(i.next().toString());
            if (i.hasNext()) {
                types.append('|');
            }
        }
        return "catch (" + this.formal().flags().translate() + types + " "
                + formal.name() + ") " + body;
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        MultiCatch n = (MultiCatch) super.buildTypes(tb);
        Formal f = n.formal();
        f.localInstance().setFlags(f.localInstance().flags().Final()); // formal is implicitly final
        return n;

    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        MultiCatch n = (MultiCatch) super.typeCheck(tc);

        for (int i = 0; i < this.alternatives.size(); i++) {
            Type ti = this.alternatives().get(i).type();
            for (int j = i + 1; j < this.alternatives.size(); j++) {
                Type tj = this.alternatives().get(j).type();
                if (ti.isSubtype(tj) || tj.isSubtype(ti)) {
                    throw new SemanticException("Alternatives in a multi-catch statement must not be subclasses of each other.",
                                                this.alternatives()
                                                    .get(j)
                                                    .position());
                }

            }
        }

        return n;
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("catch (");

        w.begin(0);
        w.write(formal().flags().translate());
        Iterator<TypeNode> i = this.alternatives.iterator();
        while (i.hasNext()) {
            print(i.next(), w, tr);
            if (i.hasNext()) {
                w.write("|");
            }
        }

        w.write(" ");
        w.write(formal().name());
        w.end();

        w.write(")");
        printSubStmt(body, w, tr);
    }

}
