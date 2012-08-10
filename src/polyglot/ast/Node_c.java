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

import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.StringUtil;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.ConstantChecker;
import polyglot.visit.DumpAst;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.Translator;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A <code>Node</code> represents an AST node.  All AST nodes must implement
 * this interface.  Nodes should be immutable: methods which set fields
 * of the node should copy the node, set the field in the copy, and then
 * return the copy.
 */
public abstract class Node_c implements Node {
    protected Position position;
    protected JL del;
    protected Ext ext;
    protected boolean error;

    public Node_c(Position pos) {
        assert (pos != null);
        this.position = pos;
        this.error = false;
    }

    @Override
    public void init(Node node) {
        if (node != this) {
            throw new InternalCompilerError("Cannot use a Node as a delegate or extension.");
        }
    }

    @Override
    public Node node() {
        return this;
    }

    @Override
    public JL del() {
        return del != null ? del : this;
    }

    @Override
    public Node del(JL del) {
        if (this.del == del) {
            return this;
        }

        JL old = this.del;
        this.del = null;

        Node_c n = (Node_c) copy();

        n.del = del != this ? del : null;

        if (n.del != null) {
            n.del.init(n);
        }

        this.del = old;

        return n;
    }

    @Override
    public Ext ext(int n) {
        if (n < 1) throw new InternalCompilerError("n must be >= 1");
        if (n == 1) return ext();
        return ext(n - 1).ext();
    }

    @Override
    public Node ext(int n, Ext ext) {
        if (n < 1) throw new InternalCompilerError("n must be >= 1");
        if (n == 1) return ext(ext);

        Ext prev = this.ext(n - 1);
        if (prev == null)
            throw new InternalCompilerError("cannot set the nth extension if there is no (n-1)st extension");
        return this.ext(n - 1, prev.ext(ext));
    }

    @Override
    public Ext ext() {
        return ext;
    }

    @Override
    public Node ext(Ext ext) {
        if (this.ext == ext) {
            return this;
        }

        Ext old = this.ext;
        this.ext = null;

        Node_c n = (Node_c) copy();

        n.ext = ext;

        if (n.ext != null) {
            n.ext.init(n);
        }

        this.ext = old;

        return n;
    }

    @Override
    public Object copy() {
        try {
            Node_c n = (Node_c) super.clone();

            if (this.del != null) {
                n.del = (JL) this.del.copy();
                n.del.init(n);
            }

            if (this.ext != null) {
                n.ext = (Ext) this.ext.copy();
                n.ext.init(n);
            }

            return n;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone() weirdness.");
        }
    }

    @Override
    public Position position() {
        return this.position;
    }

    @Override
    public Node position(Position position) {
        Node_c n = (Node_c) copy();
        n.position = position;
        return n;
    }

    @Override
    public boolean isDisambiguated() {
        return !(this instanceof Ambiguous);
    }

    @Override
    public boolean isTypeChecked() {
        return isDisambiguated();
    }

    @Override
    public boolean error() {
        return error;
    }

    @Override
    public Node error(boolean flag) {
        Node_c n = (Node_c) copy();
        n.error = flag;
        return n;
    }

    @Override
    public Node visitChild(Node n, NodeVisitor v) {
        if (n == null) {
            return null;
        }

        return v.visitEdge(this, n);
    }

    @Override
    public Node visit(NodeVisitor v) {
        return v.visitEdge(null, this);
    }

    /** 
     * @deprecated Call {@link Node#visitChild(Node, NodeVisitor)} instead.
     */
    @Deprecated
    @Override
    public Node visitEdge(Node parent, NodeVisitor v) {
        Node n = v.override(parent, this);

        if (n == null) {
            NodeVisitor v_ = v.enter(parent, this);

            if (v_ == null) {
                throw new InternalCompilerError("NodeVisitor.enter() returned null.");
            }

            n = this.del().visitChildren(v_);

            if (n == null) {
                throw new InternalCompilerError("Node_c.visitChildren() returned null.");
            }

            n = v.leave(parent, this, n, v_);

            if (n == null) {
                throw new InternalCompilerError("NodeVisitor.leave() returned null.");
            }
        }

        return n;
    }

    /**
     * Visit all the elements of a list.
     * @param l The list to visit.
     * @param v The visitor to use.
     * @return A new list with each element from the old list
     *         replaced by the result of visiting that element.
     *         If <code>l</code> is <code>null</code>,
     *         <code>null</code> is returned.
     */
    @Override
    public <T extends Node> List<T> visitList(List<T> l, NodeVisitor v) {
        if (l == null) {
            return null;
        }

        List<T> result = l;
        List<T> vl = new ArrayList<T>(l.size());

        for (T n : l) {
            Node m = visitChild(n, v);
            if (n != m) {
                result = vl;
            }
            if (m != null) {
                @SuppressWarnings("unchecked")
                T t = (T) m;
                vl.add(t);
            }
        }

        return result;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        return this;
    }

    /**
     * Push a new scope upon entering this node, and add any declarations to the
     * context that should be in scope when visiting children of this node.
     * @param c the current <code>Context</code>
     * @return the <code>Context</code> to be used for visiting this node. 
     */
    @Override
    public Context enterScope(Context c) {
        return c;
    }

    /**
     * Push a new scope for visiting the child node <code>child</code>. 
     * The default behavior is to delegate the call to the child node, and let
     * it add appropriate declarations that should be in scope. However,
     * this method gives parent nodes have the ability to modify this behavior.
     * @param child the child node about to be entered.
     * @param c the current <code>Context</code>
     * @return the <code>Context</code> to be used for visiting node 
     *           <code>child</code>
     */
    @Override
    public Context enterChildScope(Node child, Context c) {
        return child.del().enterScope(c);
    }

    /**
     * Add any declarations to the context that should be in scope when
     * visiting later sibling nodes.
     */
    @Override
    public void addDecls(Context c) {
    }

    // These methods override the methods in Ext_c.
    // These are the default implementation of these passes.

    @Override
    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
        return tb;
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        return this;
    }

    @Override
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
            throws SemanticException {
        return null;
    }

    @Override
    public NodeVisitor disambiguateEnter(AmbiguityRemover ar)
            throws SemanticException {
        return ar;
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        return this;
    }

    /** Type check the AST. */
    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc)
            throws SemanticException {
        return null;
    }

    @Override
    public NodeVisitor typeCheckEnter(TypeChecker tc) throws SemanticException {
        return tc;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        return this;
    }

    @Override
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
        return this;
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        return child.type();
    }

    @Override
    public NodeVisitor exceptionCheckEnter(ExceptionChecker ec)
            throws SemanticException {
        return ec.push();
    }

    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        List<? extends Type> l = this.del().throwTypes(ec.typeSystem());
        for (Type exc : l) {
            ec.throwsException(exc, position());
        }
        return this;
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        return Collections.emptyList();
    }

    /** Dump the AST for debugging. */
    @Override
    public void dump(OutputStream os) {
        CodeWriter cw = Compiler.createCodeWriter(os);
        NodeVisitor dumper = new DumpAst(cw);
        dumper = dumper.begin();
        this.visit(dumper);
        cw.newline();
        dumper.finish();
    }

    /** Dump the AST for debugging. */
    @Override
    public void dump(Writer w) {
        CodeWriter cw = Compiler.createCodeWriter(w);
        NodeVisitor dumper = new DumpAst(cw);
        dumper = dumper.begin();
        this.visit(dumper);
        cw.newline();
        dumper.finish();
    }

    /** Pretty-print the AST for debugging. */
    @Override
    public void prettyPrint(OutputStream os) {
        try {
            CodeWriter cw = Compiler.createCodeWriter(os);
            this.del().prettyPrint(cw, new PrettyPrinter());
            cw.flush();
        }
        catch (java.io.IOException e) {
        }
    }

    /** Pretty-print the AST for debugging. */
    @Override
    public void prettyPrint(Writer w) {
        try {
            CodeWriter cw = Compiler.createCodeWriter(w);
            this.del().prettyPrint(cw, new PrettyPrinter());
            cw.flush();
        }
        catch (java.io.IOException e) {
        }
    }

    /** Pretty-print the AST using the given <code>CodeWriter</code>. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
    }

    public void printBlock(Node n, CodeWriter w, PrettyPrinter pp) {
        w.begin(0);
        print(n, w, pp);
        w.end();
    }

    public void printSubStmt(Stmt stmt, CodeWriter w, PrettyPrinter pp) {
        if (stmt instanceof Block) {
            w.write(" ");
            print(stmt, w, pp);
        }
        else {
            w.allowBreak(4, " ");
            printBlock(stmt, w, pp);
        }
    }

    public void print(Node child, CodeWriter w, PrettyPrinter pp) {
        pp.print(this, child, w);
    }

    /** Translate the AST using the given <code>CodeWriter</code>. */
    @Override
    public void translate(CodeWriter w, Translator tr) {
        // By default, just rely on the pretty printer.
        this.del().prettyPrint(w, tr);
    }

    @Override
    public void dump(CodeWriter w) {
        w.write(StringUtil.getShortNameComponent(getClass().getName()));

        w.allowBreak(4, " ");
        w.begin(0);
        w.write("(del ");
        if (del() == this)
            w.write("*");
        else w.write(del().toString());
        w.write(")");
        w.end();

        Ext ext = ext();
        while (true) {
            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(ext ");
            if (ext == null) {
                w.write("null");
            }
            else {
                ext.dump(w);
            }
            w.write(")");
            w.end();
            if (ext != null) ext = ext.ext();
            if (ext == null) {
                break;
            }
        }

        w.allowBreak(4, " ");
        w.begin(0);
        w.write("(position "
                + (position != null ? position.toString() : "UNKNOWN") + ")");
        w.end();
    }

    @Override
    public String toString() {
        // This is really slow and so you are encouraged to override.
        // return new StringPrettyPrinter(5).toString(this);

        // Not slow anymore.
        return getClass().getName();
    }

    @Override
    public Node copy(NodeFactory nf) {
        throw new InternalCompilerError("Unimplemented operation. This class "
                + "("
                + this.getClass().getName()
                + ") does "
                + "not implement copy(NodeFactory). This compiler extension should"
                + " either implement the method, or not invoke this method.");
    }

    @Override
    public Node copy(ExtensionInfo extInfo) throws SemanticException {
        return this.del().copy(extInfo.nodeFactory());
    }

}
