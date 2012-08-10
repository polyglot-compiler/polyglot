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

import polyglot.frontend.MissingDependencyException;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.Goal;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.VarInstance;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ConstantChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A <code>LocalDecl</code> is an immutable representation of the declaration
 * of a local variable.
 */
public class LocalDecl_c extends Stmt_c implements LocalDecl {
    protected Flags flags;
    protected TypeNode type;
    protected Id name;
    protected Expr init;
    protected LocalInstance li;

    public LocalDecl_c(Position pos, Flags flags, TypeNode type, Id name,
            Expr init) {
        super(pos);
        assert (flags != null && type != null && name != null); // init may be null
        this.flags = flags;
        this.type = type;
        this.name = name;
        this.init = init;
    }

    @Override
    public boolean isDisambiguated() {
        return li != null && li.isCanonical() && super.isDisambiguated();
    }

    /** Get the type of the declaration. */
    @Override
    public Type declType() {
        return type.type();
    }

    /** Get the flags of the declaration. */
    @Override
    public Flags flags() {
        return flags;
    }

    /** Set the flags of the declaration. */
    @Override
    public LocalDecl flags(Flags flags) {
        if (flags.equals(this.flags)) return this;
        LocalDecl_c n = (LocalDecl_c) copy();
        n.flags = flags;
        return n;
    }

    /** Get the type node of the declaration. */
    @Override
    public TypeNode type() {
        return type;
    }

    /** Set the type of the declaration. */
    @Override
    public LocalDecl type(TypeNode type) {
        if (type == this.type) return this;
        LocalDecl_c n = (LocalDecl_c) copy();
        n.type = type;
        return n;
    }

    /** Get the name of the declaration. */
    @Override
    public Id id() {
        return name;
    }

    /** Set the name of the declaration. */
    @Override
    public LocalDecl id(Id name) {
        LocalDecl_c n = (LocalDecl_c) copy();
        n.name = name;
        return n;
    }

    /** Get the name of the declaration. */
    @Override
    public String name() {
        return name.id();
    }

    /** Set the name of the declaration. */
    @Override
    public LocalDecl name(String name) {
        return id(this.name.id(name));
    }

    /** Get the initializer of the declaration. */
    @Override
    public Expr init() {
        return init;
    }

    /** Set the initializer of the declaration. */
    @Override
    public LocalDecl init(Expr init) {
        if (init == this.init) return this;
        LocalDecl_c n = (LocalDecl_c) copy();
        n.init = init;
        return n;
    }

    /** Set the local instance of the declaration. */
    @Override
    public LocalDecl localInstance(LocalInstance li) {
        if (li == this.li) return this;
        LocalDecl_c n = (LocalDecl_c) copy();
        n.li = li;
        return n;
    }

    /** Get the local instance of the declaration. */
    @Override
    public LocalInstance localInstance() {
        return li;
    }

    @Override
    public VarInstance varInstance() {
        return li;
    }

    /** Reconstruct the declaration. */
    protected LocalDecl_c reconstruct(TypeNode type, Id name, Expr init) {
        if (this.type != type || this.name != name || this.init != init) {
            LocalDecl_c n = (LocalDecl_c) copy();
            n.type = type;
            n.name = name;
            n.init = init;
            return n;
        }

        return this;
    }

    /** Visit the children of the declaration. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode type = (TypeNode) visitChild(this.type, v);
        Id name = (Id) visitChild(this.name, v);
        Expr init = (Expr) visitChild(this.init, v);
        return reconstruct(type, name, init);
    }

    /**
     * Add the declaration of the variable as we enter the scope of the
     * intializer
     */
    @Override
    public Context enterChildScope(Node child, Context c) {
        if (child == init) {
            c.addVariable(li);
        }
        return super.enterChildScope(child, c);
    }

    @Override
    public void addDecls(Context c) {
        // Add the declaration of the variable in case we haven't already done
        // so in enterScope, when visiting the initializer.
        c.addVariable(li);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        LocalDecl_c n = (LocalDecl_c) super.buildTypes(tb);

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
        return this;
    }

    /**
     * Override superclass behaviour to check if the variable is multiply
     * defined.
     */
    @Override
    public NodeVisitor typeCheckEnter(TypeChecker tc) throws SemanticException {
        // Check if the variable is multiply defined.
        // we do it in type check enter, instead of type check since
        // we add the declaration before we enter the scope of the
        // initializer.
        Context c = tc.context();

        LocalInstance outerLocal = c.findLocalSilent(li.name());

        if (outerLocal != null && c.isLocal(li.name())) {
            throw new SemanticException("Local variable \"" + name
                    + "\" multiply defined.  " + "Previous definition at "
                    + outerLocal.position() + ".", position());
        }

        return super.typeCheckEnter(tc);
    }

    /** Type check the declaration. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        LocalInstance li = this.li;

        try {
            ts.checkLocalFlags(flags);
        }
        catch (SemanticException e) {
            throw new SemanticException(e.getMessage(), position());
        }

        if (init != null) {
            if (init instanceof ArrayInit) {
                ((ArrayInit) init).typeCheckElements(type.type());
            }
            else {
                if (!ts.isImplicitCastValid(init.type(), type.type())
                        && !ts.typeEquals(init.type(), type.type())
                        && !ts.numericConversionValid(type.type(),
                                                      init.constantValue())) {
                    throw new SemanticException("The type of the variable "
                                                        + "initializer \""
                                                        + init.type()
                                                        + "\" does not match that of "
                                                        + "the declaration \""
                                                        + type.type() + "\".",
                                                init.position());
                }
            }
        }

        return localInstance(li);
    }

    protected static class AddDependenciesVisitor extends NodeVisitor {
        protected ConstantChecker cc;
        protected LocalInstance li;

        AddDependenciesVisitor(ConstantChecker cc, LocalInstance li) {
            this.cc = cc;
            this.li = li;
        }

        @Override
        public Node leave(Node old, Node n, NodeVisitor v) {
            if (n instanceof Field) {
                Field f = (Field) n;
                if (!f.fieldInstance().orig().constantValueSet()) {
                    Scheduler scheduler = cc.job().extensionInfo().scheduler();
                    Goal g =
                            scheduler.FieldConstantsChecked(f.fieldInstance()
                                                             .orig());
                    throw new MissingDependencyException(g);
                }
            }
            if (n instanceof Local) {
                Local l = (Local) n;
                if (!l.localInstance().orig().constantValueSet()) {
                    // Undefined variable or forward reference.
                    li.setNotConstant();
                }
            }
            return n;
        }
    }

    @Override
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
//        if (init != null && ! init.constantValueSet()) {
//            // HACK to add dependencies for computing the constant value.
//            init.visit(new AddDependenciesVisitor(cc, li));
//            return this;
//        }

        if (init == null || !init.isConstant() || !li.flags().isFinal()) {
            li.setNotConstant();
        }
        else {
            li.setConstantValue(init.constantValue());
        }

        return this;
    }

    @Override
    public boolean constantValueSet() {
        return li != null && li.constantValueSet();
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        if (child == init) {
            TypeSystem ts = av.typeSystem();

            // If the RHS is an integral constant, we can relax the expected
            // type to the type of the constant.
            if (ts.numericConversionValid(type.type(), child.constantValue())) {
                return child.type();
            }
            else {
                return type.type();
            }
        }

        return child.type();
    }

    @Override
    public String toString() {
        return flags.translate() + type + " " + name
                + (init != null ? " = " + init : "") + ";";
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        boolean printSemi = tr.appendSemicolon(true);
        boolean printType = tr.printType(true);

        w.write(flags.translate());
        if (printType) {
            print(type, w, tr);
            w.write(" ");
        }
        tr.print(this, name, w);

        if (init != null) {
            w.write(" =");
            w.allowBreak(2, " ");
            print(init, w, tr);
        }

        if (printSemi) {
            w.write(";");
        }

        tr.printType(printType);
        tr.appendSemicolon(printSemi);
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
    public Term firstChild() {
        return type();
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (init() != null) {
            v.visitCFG(type(), init(), ENTRY);
            v.visitCFG(init(), this, EXIT);
        }
        else {
            v.visitCFG(type(), this, EXIT);
        }

        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.LocalDecl(this.position,
                            this.flags,
                            this.type,
                            this.name,
                            this.init);
    }

}
