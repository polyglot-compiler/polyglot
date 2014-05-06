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

package polyglot.ast;

import java.util.List;

import polyglot.translate.ExtensionRewriter;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A {@code Formal} represents a formal parameter for a procedure
 * or catch block.  It consists of a type and a variable identifier.
 */
public class Formal_c extends Term_c implements Formal {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected LocalInstance li;
    protected Flags flags;
    protected TypeNode type;
    protected Id name;

//    @Deprecated
    public Formal_c(Position pos, Flags flags, TypeNode type, Id name) {
        this(pos, flags, type, name, null);
    }

    public Formal_c(Position pos, Flags flags, TypeNode type, Id name, Ext ext) {
        super(pos, ext);
        assert (flags != null && type != null && name != null);
        this.flags = flags;
        this.type = type;
        this.name = name;
    }

    @Override
    public boolean isDisambiguated() {
        return li != null && li.isCanonical() && super.isDisambiguated();
    }

    @Override
    public Type declType() {
        return type.type();
    }

    @Override
    public Flags flags() {
        return flags;
    }

    @Override
    public Formal flags(Flags flags) {
        return flags(this, flags);
    }

    protected <N extends Formal_c> N flags(N n, Flags flags) {
        if (n.flags.equals(flags)) return n;
        n = copyIfNeeded(n);
        n.flags = flags;
        return n;
    }

    @Override
    public TypeNode type() {
        return type;
    }

    @Override
    public Formal type(TypeNode type) {
        return type(this, type);
    }

    protected <N extends Formal_c> N type(N n, TypeNode type) {
        if (n.type == type) return n;
        n = copyIfNeeded(n);
        n.type = type;
        return n;
    }

    @Override
    public Id id() {
        return name;
    }

    @Override
    public Formal id(Id name) {
        return id(this, name);
    }

    protected <N extends Formal_c> N id(N n, Id name) {
        if (n.name == name) return n;
        n = copyIfNeeded(n);
        n.name = name;
        return n;
    }

    @Override
    public String name() {
        return name.id();
    }

    @Override
    public Formal name(String name) {
        return id(this.name.id(name));
    }

    @Override
    public LocalInstance localInstance() {
        return li;
    }

    @Override
    public Formal localInstance(LocalInstance li) {
        return localInstance(this, li);
    }

    protected <N extends Formal_c> N localInstance(N n, LocalInstance li) {
        if (n.li == li) return n;
        n = copyIfNeeded(n);
        n.li = li;
        return n;
    }

    /** Reconstruct the formal. */
    protected <N extends Formal_c> N reconstruct(N n, TypeNode type, Id name) {
        n = type(n, type);
        n = id(n, name);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode type = visitChild(this.type, v);
        Id name = visitChild(this.name, v);
        return reconstruct(this, type, name);
    }

    @Override
    public void addDecls(Context c) {
        c.addVariable(li);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write(flags.translate());
        print(type, w, tr);
        w.write(" ");
        tr.print(this, name, w);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        Formal_c n = (Formal_c) super.buildTypes(tb);

        TypeSystem ts = tb.typeSystem();

        LocalInstance li =
                ts.localInstance(position(),
                                 flags(),
                                 ts.unknownType(position()),
                                 name());

        n = localInstance(n, li);
        return n;
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
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        Formal_c n = (Formal_c) super.extRewrite(rw);
        n = localInstance(n, null);
        return n;
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
