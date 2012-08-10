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
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A <code>Formal</code> represents a formal parameter for a procedure
 * or catch block.  It consists of a type and a variable identifier.
 */
public class Formal_c extends Term_c implements Formal {
    protected LocalInstance li;
    protected Flags flags;
    protected TypeNode type;
    protected Id name;

//    protected boolean reachable;

    public Formal_c(Position pos, Flags flags, TypeNode type, Id name) {
        super(pos);
        assert (flags != null && type != null && name != null);
        this.flags = flags;
        this.type = type;
        this.name = name;
    }

    @Override
    public boolean isDisambiguated() {
        return li != null && li.isCanonical() && super.isDisambiguated();
    }

    /** Get the type of the formal. */
    @Override
    public Type declType() {
        return type.type();
    }

    /** Get the flags of the formal. */
    @Override
    public Flags flags() {
        return flags;
    }

    /** Set the flags of the formal. */
    @Override
    public Formal flags(Flags flags) {
        if (flags.equals(this.flags)) return this;
        Formal_c n = (Formal_c) copy();
        n.flags = flags;
        return n;
    }

    /** Get the type node of the formal. */
    @Override
    public TypeNode type() {
        return type;
    }

    /** Set the type node of the formal. */
    @Override
    public Formal type(TypeNode type) {
        Formal_c n = (Formal_c) copy();
        n.type = type;
        return n;
    }

    /** Get the name of the formal. */
    @Override
    public Id id() {
        return name;
    }

    /** Set the name of the formal. */
    @Override
    public Formal id(Id name) {
        Formal_c n = (Formal_c) copy();
        n.name = name;
        return n;
    }

    /** Get the name of the formal. */
    @Override
    public String name() {
        return name.id();
    }

    /** Set the name of the formal. */
    @Override
    public Formal name(String name) {
        return id(this.name.id(name));
    }

    /** Get the local instance of the formal. */
    @Override
    public LocalInstance localInstance() {
        return li;
    }

    /** Set the local instance of the formal. */
    @Override
    public Formal localInstance(LocalInstance li) {
        if (li == this.li) return this;
        Formal_c n = (Formal_c) copy();
        n.li = li;
        return n;
    }

    /** Reconstruct the formal. */
    protected Formal_c reconstruct(TypeNode type, Id name) {
        if (this.type != type) {
            Formal_c n = (Formal_c) copy();
            n.type = type;
            n.name = name;
            return n;
        }

        return this;
    }

    /** Visit the children of the formal. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode type = (TypeNode) visitChild(this.type, v);
        Id name = (Id) visitChild(this.name, v);
        return reconstruct(type, name);
    }

    @Override
    public void addDecls(Context c) {
        c.addVariable(li);
    }

    /** Write the formal to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write(flags.translate());
        print(type, w, tr);
        w.write(" ");
        tr.print(this, name, w);
    }

    /** Build type objects for the formal. */
    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        Formal_c n = (Formal_c) super.buildTypes(tb);

        TypeSystem ts = tb.typeSystem();

        LocalInstance li =
                ts.localInstance(position(),
                                 flags(),
                                 ts.unknownType(position()),
                                 name());

        return n.localInstance(li);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (li.isCanonical()) {
            return this;
        }
        if (declType().isCanonical()) {
            li.setType(declType());
        }
        li.setNotConstant();

        return this;
    }

    /** Type check the formal. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        // Check if the variable is multiply defined.
        Context c = tc.context();

        LocalInstance outerLocal = c.findLocalSilent(li.name());

        if (outerLocal != null && outerLocal != li && c.isLocal(li.name())) {
            throw new SemanticException("Local variable \"" + name
                    + "\" multiply defined.  " + "Previous definition at "
                    + outerLocal.position() + ".", position());
        }

        TypeSystem ts = tc.typeSystem();

        try {
            ts.checkLocalFlags(flags());
        }
        catch (SemanticException e) {
            throw new SemanticException(e.getMessage(), position());
        }

        return this;
    }

    @Override
    public Term firstChild() {
        return type;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(type, this, EXIT);
        return succs;
    }

    @Override
    public void dump(CodeWriter w) {
        super.dump(w);

        if (li != null) {
            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(instance " + li + ")");
            w.end();
        }

        w.allowBreak(4, " ");
        w.begin(0);
        w.write("(name " + name + ")");
        w.end();
    }

    @Override
    public String toString() {
        return flags.translate() + type + " " + name;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Formal(this.position, this.flags, this.type, this.name);
    }

}
