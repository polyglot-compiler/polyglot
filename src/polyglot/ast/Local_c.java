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
import polyglot.types.TypeSystem;
import polyglot.types.VarInstance;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/** 
 * A local variable expression.
 */
public class Local_c extends Expr_c implements Local {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Id name;
    protected LocalInstance li;

//    @Deprecated
    public Local_c(Position pos, Id name) {
        this(pos, name, null);
    }

    public Local_c(Position pos, Id name, Ext ext) {
        super(pos, ext);
        assert (name != null);
        this.name = name;
    }

    @Override
    public Precedence precedence() {
        return Precedence.LITERAL;
    }

    @Override
    public Id id() {
        return this.name;
    }

    @Override
    public Local id(Id name) {
        return id(this, name);
    }

    protected <N extends Local_c> N id(N n, Id name) {
        if (n.name == name) return n;
        n = copyIfNeeded(n);
        n.name = name;
        return n;
    }

    @Override
    public String name() {
        return this.name.id();
    }

    @Override
    public Local name(String name) {
        return id(this.name.id(name));
    }

    @Override
    public Flags flags() {
        return li.flags();
    }

    @Override
    public VarInstance varInstance() {
        return localInstance();
    }

    @Override
    public LocalInstance localInstance() {
        return li;
    }

    @Override
    public Local localInstance(LocalInstance li) {
        return localInstance(this, li);
    }

    protected <N extends Local_c> N localInstance(N n, LocalInstance li) {
        if (n.li == li) return n;
        n = copyIfNeeded(n);
        n.li = li;
        return n;
    }

    /** Reconstruct the expression. */
    protected <N extends Local_c> N reconstruct(N n, Id name) {
        n = id(n, name);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Id name = visitChild(this.name, v);
        return reconstruct(this, name);
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
        n = localInstance(n, li);
        return n;
    }

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

        Local_c n = this;
        n = localInstance(n, li);
        n = type(n, li.type());
        return n;
    }

    @Override
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        Local_c n = (Local_c) super.extRewrite(rw);
        n = localInstance(n, null);
        return n;
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

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        tr.print(this, name, w);
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
    }

    @Override
    public boolean constantValueSet(Lang lang) {
        return li != null && li.constantValueSet();
    }

    @Override
    public boolean isConstant(Lang lang) {
        return li != null && li.isConstant();
    }

    @Override
    public Object constantValue(Lang lang) {
        if (!lang.isConstant(this, lang)) return null;
        return li.constantValue();
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Local(this.position, this.name);
    }

}
