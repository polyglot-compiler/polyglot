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

import polyglot.types.ClassType;
import polyglot.types.CodeInstance;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.InitializerInstance;
import polyglot.types.MemberInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SubtypeSet;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * An <code>Initializer</code> is an immutable representation of an
 * initializer block in a Java class (which appears outside of any
 * method).  Such a block is executed before the code for any of the
 * constructors.  Such a block can optionally be static, in which case
 * it is executed when the class is loaded.
 */
public class Initializer_c extends Term_c implements Initializer {
    protected Flags flags;
    protected Block body;
    protected InitializerInstance ii;

    public Initializer_c(Position pos, Flags flags, Block body) {
        super(pos);
        assert (flags != null && body != null);
        this.flags = flags;
        this.body = body;
    }

    @Override
    public boolean isDisambiguated() {
        return ii != null && ii.isCanonical() && super.isDisambiguated();
    }

    @Override
    public MemberInstance memberInstance() {
        return ii;
    }

    /** Get the flags of the initializer. */
    @Override
    public Flags flags() {
        return this.flags;
    }

    /** Set the flags of the initializer. */
    @Override
    public Initializer flags(Flags flags) {
        if (flags.equals(this.flags)) return this;
        Initializer_c n = (Initializer_c) copy();
        n.flags = flags;
        return n;
    }

    /** Get the initializer instance of the initializer. */
    @Override
    public InitializerInstance initializerInstance() {
        return ii;
    }

    @Override
    public CodeInstance codeInstance() {
        return initializerInstance();
    }

    /** Set the initializer instance of the initializer. */
    @Override
    public Initializer initializerInstance(InitializerInstance ii) {
        if (ii == this.ii) return this;
        Initializer_c n = (Initializer_c) copy();
        n.ii = ii;
        return n;
    }

    @Override
    public Term codeBody() {
        return this.body;
    }

    /** Get the body of the initializer. */
    @Override
    public Block body() {
        return this.body;
    }

    /** Set the body of the initializer. */
    @Override
    public CodeBlock body(Block body) {
        Initializer_c n = (Initializer_c) copy();
        n.body = body;
        return n;
    }

    /** Reconstruct the initializer. */
    protected Initializer_c reconstruct(Block body) {
        if (body != this.body) {
            Initializer_c n = (Initializer_c) copy();
            n.body = body;
            return n;
        }

        return this;
    }

    /** Visit the children of the initializer. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Block body = (Block) visitChild(this.body, v);
        return reconstruct(body);
    }

    @Override
    public Context enterScope(Context c) {
        return c.pushCode(ii);
    }

    @Override
    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
        return tb.pushCode();
    }

    /**
     * Return the first (sub)term performed when evaluating this
     * term.
     */
    @Override
    public Term firstChild() {
        return body();
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(body(), this, EXIT);
        return succs;
    }

    /** Build type objects for the method. */
    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        TypeSystem ts = tb.typeSystem();
        ClassType ct = tb.currentClass();
        InitializerInstance ii = ts.initializerInstance(position(), ct, flags);
        return initializerInstance(ii);
    }

    /** Type check the initializer. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        try {
            ts.checkInitializerFlags(flags());
        }
        catch (SemanticException e) {
            throw new SemanticException(e.getMessage(), position());
        }

        // check that inner classes do not declare static initializers
        if (flags().isStatic()
                && initializerInstance().container().toClass().isInnerClass()) {
            // it's a static initializer in an inner class.
            throw new SemanticException("Inner classes cannot declare "
                    + "static initializers.", this.position());
        }

        return this;
    }

    @Override
    public NodeVisitor exceptionCheckEnter(ExceptionChecker ec)
            throws SemanticException {
        if (initializerInstance().flags().isStatic()) {
            return ec.push(new ExceptionChecker.CodeTypeReporter("static initializer block"));
        }

        if (!initializerInstance().container().toClass().isAnonymous()) {
            ec =
                    ec.push(new ExceptionChecker.CodeTypeReporter("instance initializer block"));

            // An instance initializer of a named class may not throw
            // a checked exception unless that exception or one of its 
            // superclasses is explicitly declared in the throws clause
            // of each contructor or its class, and the class has at least
            // one explicitly declared constructor.
            SubtypeSet allowed = null;
            Type throwable = ec.typeSystem().Throwable();
            ClassType container = initializerInstance().container().toClass();
            for (ConstructorInstance ci : container.constructors()) {
                if (allowed == null) {
                    allowed = new SubtypeSet(throwable);
                    allowed.addAll(ci.throwTypes());
                }
                else {
                    // intersect allowed with ci.throwTypes()
                    SubtypeSet other = new SubtypeSet(throwable);
                    other.addAll(ci.throwTypes());
                    SubtypeSet inter = new SubtypeSet(throwable);
                    for (Type t : allowed) {
                        if (other.contains(t)) {
                            // t or a supertype is thrown by other.
                            inter.add(t);
                        }
                    }
                    for (Type t : other) {
                        if (allowed.contains(t)) {
                            // t or a supertype is thrown by the allowed.
                            inter.add(t);
                        }
                    }
                    allowed = inter;
                }
            }
            // allowed is now an intersection of the throw types of all
            // constructors

            ec = ec.push(allowed);

            return ec;
        }

        return ec.push();
    }

    /** Write the initializer to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.begin(0);
        w.write(flags.translate());
        print(body, w, tr);
        w.end();
    }

    @Override
    public void dump(CodeWriter w) {
        super.dump(w);

        if (ii != null) {
            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(instance " + ii + ")");
            w.end();
        }
    }

    @Override
    public String toString() {
        return flags.translate() + "{ ... }";
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Initializer(this.position, this.flags, this.body);
    }

}
