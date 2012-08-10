/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import java.util.List;

import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.types.VarInstance;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/** 
 * A local variable expression.
 */
public class Local_c extends Expr_c implements Local {
    protected Id name;
    protected LocalInstance li;

    public Local_c(Position pos, Id name) {
        super(pos);
        assert (name != null);
        this.name = name;
    }

    /** Get the precedence of the local. */
    @Override
    public Precedence precedence() {
        return Precedence.LITERAL;
    }

    /** Get the name of the local. */
    @Override
    public Id id() {
        return this.name;
    }

    /** Set the name of the local. */
    @Override
    public Local id(Id name) {
        Local_c n = (Local_c) copy();
        n.name = name;
        return n;
    }

    /** Get the name of the local. */
    @Override
    public String name() {
        return this.name.id();
    }

    /** Set the name of the local. */
    @Override
    public Local name(String name) {
        return id(this.name.id(name));
    }

    /** Return the access flags of the variable. */
    @Override
    public Flags flags() {
        return li.flags();
    }

    /** Get the local instance of the local. */
    @Override
    public VarInstance varInstance() {
        return li;
    }

    /** Get the local instance of the local. */
    @Override
    public LocalInstance localInstance() {
        return li;
    }

    /** Set the local instance of the local. */
    @Override
    public Local localInstance(LocalInstance li) {
        if (li == this.li) return this;
        Local_c n = (Local_c) copy();
        n.li = li;
        return n;
    }

    /** Reconstruct the expression. */
    protected Local_c reconstruct(Id name) {
        if (name != this.name) {
            Local_c n = (Local_c) copy();
            n.name = name;
            return n;
        }

        return this;
    }

    /** Visit the children of the constructor. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Id name = (Id) visitChild(this.name, v);
        return reconstruct(name);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        Local_c n = (Local_c) super.buildTypes(tb);

        TypeSystem ts = tb.typeSystem();

        LocalInstance li =
                ts.localInstance(position(),
                                 Flags.NONE,
                                 ts.unknownType(position()),
                                 name.id());
        return n.localInstance(li);
    }

    /** Type check the local. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Context c = tc.context();
        LocalInstance li = c.findLocal(name.id());

        // if the local is defined in an outer class, then it must be final
        if (!c.isLocal(li.name())) {
            // this local is defined in an outer class
            if (!li.flags().isFinal()) {
                throw new SemanticException("Local variable \""
                                                    + li.name()
                                                    + "\" is accessed from an inner class, and must be declared "
                                                    + "final.",
                                            this.position());
            }
        }

        return localInstance(li).type(li.type());
    }

    @Override
    public Term firstChild() {
        return null;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        return succs;
    }

    @Override
    public String toString() {
        return name.toString();
    }

    /** Write the local to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        tr.print(this, name, w);
    }

    /** Dumps the AST. */
    @Override
    public void dump(CodeWriter w) {
        super.dump(w);

        if (li != null) {
            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(instance " + li + ")");
            w.end();
        }
    }

    @Override
    public boolean constantValueSet() {
        return li != null && li.constantValueSet();
    }

    @Override
    public boolean isConstant() {
        return li != null && li.isConstant();
    }

    @Override
    public Object constantValue() {
        if (!isConstant()) return null;
        return li.constantValue();
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Local(this.position, this.name);
    }

}
