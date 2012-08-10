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
import polyglot.types.CodeInstance;
import polyglot.types.Context;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.InitializerInstance;
import polyglot.types.MemberInstance;
import polyglot.types.ParsedClassType;
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
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.PruningVisitor;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A <code>FieldDecl</code> is an immutable representation of the declaration
 * of a field of a class.
 */
public class FieldDecl_c extends Term_c implements FieldDecl {
    protected Flags flags;
    protected TypeNode type;
    protected Id name;
    protected Expr init;
    protected FieldInstance fi;
    protected InitializerInstance ii;

    public FieldDecl_c(Position pos, Flags flags, TypeNode type, Id name,
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
        return fi != null && fi.isCanonical()
                && (init == null || (ii != null && ii.isCanonical()))
                && super.isDisambiguated();
    }

    @Override
    public MemberInstance memberInstance() {
        return fi;
    }

    @Override
    public VarInstance varInstance() {
        return fi;
    }

    @Override
    public CodeInstance codeInstance() {
        return ii;
    }

    /** Get the initializer instance of the initializer. */
    @Override
    public InitializerInstance initializerInstance() {
        return ii;
    }

    /** Set the initializer instance of the initializer. */
    @Override
    public FieldDecl initializerInstance(InitializerInstance ii) {
        if (ii == this.ii) return this;
        FieldDecl_c n = (FieldDecl_c) copy();
        n.ii = ii;
        return n;
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
    public FieldDecl flags(Flags flags) {
        if (flags.equals(this.flags)) return this;
        FieldDecl_c n = (FieldDecl_c) copy();
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
    public FieldDecl type(TypeNode type) {
        FieldDecl_c n = (FieldDecl_c) copy();
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
    public FieldDecl id(Id name) {
        FieldDecl_c n = (FieldDecl_c) copy();
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
    public FieldDecl name(String name) {
        return id(this.name.id(name));
    }

    @Override
    public Term codeBody() {
        return init;
    }

    /** Get the initializer of the declaration. */
    @Override
    public Expr init() {
        return init;
    }

    /** Set the initializer of the declaration. */
    @Override
    public FieldDecl init(Expr init) {
        FieldDecl_c n = (FieldDecl_c) copy();
        n.init = init;
        return n;
    }

    /** Set the field instance of the declaration. */
    @Override
    public FieldDecl fieldInstance(FieldInstance fi) {
        if (fi == this.fi) return this;
        FieldDecl_c n = (FieldDecl_c) copy();
        n.fi = fi;
        return n;
    }

    /** Get the field instance of the declaration. */
    @Override
    public FieldInstance fieldInstance() {
        return fi;
    }

    /** Reconstruct the declaration. */
    protected FieldDecl_c reconstruct(TypeNode type, Id name, Expr init) {
        if (this.type != type || this.name != name || this.init != init) {
            FieldDecl_c n = (FieldDecl_c) copy();
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

    @Override
    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
        return tb.pushCode();
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        TypeSystem ts = tb.typeSystem();

        ParsedClassType ct = tb.currentClass();

        if (ct == null) {
            return this;
        }

        Flags f = flags;

        if (ct.flags().isInterface()) {
            f = f.Public().Static().Final();
        }

        FieldDecl n;

        if (init != null) {
            Flags iflags = f.isStatic() ? Flags.STATIC : Flags.NONE;
            InitializerInstance ii =
                    ts.initializerInstance(init.position(), ct, iflags);
            n = initializerInstance(ii);
        }
        else {
            n = this;
        }

        // XXX: MutableFieldInstance
        FieldInstance fi =
                ts.fieldInstance(position(),
                                 ct,
                                 f,
                                 ts.unknownType(position()),
                                 name.id());
        ct.addField(fi);

        return n.flags(f).fieldInstance(fi);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (this.fi.isCanonical()) {
            // Nothing to do.
            return this;
        }

        if (declType().isCanonical()) {
            this.fi.setType(declType());
        }

        return this;
    }

    @Override
    public Context enterScope(Context c) {
        if (ii != null) {
            return c.pushCode(ii);
        }
        return c;
    }

    public static class AddDependenciesVisitor extends NodeVisitor {
        protected Scheduler scheduler;
        protected FieldInstance fi;

        AddDependenciesVisitor(Scheduler scheduler, FieldInstance fi) {
            this.scheduler = scheduler;
            this.fi = fi;
        }

        @Override
        public Node leave(Node old, Node n, NodeVisitor v) {
            if (n instanceof Field) {
                Field f = (Field) n;
                if (!f.fieldInstance().orig().constantValueSet()) {
                    Goal newGoal =
                            scheduler.FieldConstantsChecked(f.fieldInstance()
                                                             .orig());
                    Goal myGoal = scheduler.FieldConstantsChecked(this.fi);

                    for (Goal g : newGoal.prerequisiteGoals(scheduler)) {
                        if (scheduler.prerequisiteDependsOn(g, myGoal)) {
                            this.fi.setNotConstant();
                            return n;
                        }
                    }
                    throw new MissingDependencyException(newGoal, true);
                }
            }
            return n;
        }
    }

    @Override
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
        if (init == null || !fi.flags().isFinal()) {
            fi.setNotConstant();
            return this;
        }
        if (!init.isConstant()) {
            fi.setNotConstant();
        }
        else {
            fi.setConstantValue(init.constantValue());
        }

        return this;
    }

    @Override
    public boolean constantValueSet() {
        return fi != null && fi.constantValueSet();
    }

    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc)
            throws SemanticException {
        FieldDecl nn = this;
        FieldDecl old = this;

        NodeVisitor childv = tc.enter(parent, this);

        if (childv instanceof PruningVisitor) {
            return nn;
        }

        boolean constantValueSet = false;
        FieldInstance fi = nn.fieldInstance();
        if (fi.constantValueSet()) {
            constantValueSet = true;
        }

        TypeChecker childtc = (TypeChecker) childv;
        nn = (FieldDecl) nn.visitChildren(childtc);
        nn = (FieldDecl) tc.leave(parent, old, nn, childtc);

        if (!constantValueSet) {
            ConstantChecker cc =
                    new ConstantChecker(tc.job(),
                                        tc.typeSystem(),
                                        tc.nodeFactory());
            cc = (ConstantChecker) cc.context(childtc.context());
            nn = (FieldDecl) nn.del().checkConstants(cc);
        }

        return nn;
    }

    /** Type check the declaration. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        // Get the fi flags, not the node flags since the fi flags
        // account for being nested within an interface.
        Flags flags = fi.flags();

        try {
            ts.checkFieldFlags(flags);
        }
        catch (SemanticException e) {
            throw new SemanticException(e.getMessage(), position());
        }

        if (tc.context().currentClass().flags().isInterface()) {
            if (flags.isProtected() || flags.isPrivate()) {
                throw new SemanticException("Interface members must be public.",
                                            position());
            }
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

        // check that inner classes do not declare static fields, unless they
        // are compile-time constants
        if (flags().isStatic()
                && fieldInstance().container().toClass().isInnerClass()) {
            // it's a static field in an inner class.
            if (!flags().isFinal() || init == null || !init.isConstant()) {
                throw new SemanticException("Inner classes cannot declare "
                        + "static fields, unless they are compile-time "
                        + "constant fields.", this.position());
            }

        }

        return this;
    }

    @Override
    public NodeVisitor exceptionCheckEnter(ExceptionChecker ec)
            throws SemanticException {
        return ec.push(new ExceptionChecker.CodeTypeReporter("field initializer"));
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
    public Term firstChild() {
        return type;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (init != null) {
            v.visitCFG(type, init, ENTRY);
            v.visitCFG(init, this, EXIT);
        }
        else {
            v.visitCFG(type, this, EXIT);
        }

        return succs;
    }

    @Override
    public String toString() {
        return flags.translate() + type + " " + name
                + (init != null ? " = " + init : "");
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        boolean isInterface =
                fi != null && fi.container() != null
                        && fi.container().toClass().flags().isInterface();

        Flags f = flags;

        if (isInterface) {
            f = f.clearPublic();
            f = f.clearStatic();
            f = f.clearFinal();
        }

        w.write(f.translate());
        print(type, w, tr);
        w.allowBreak(2, 2, " ", 1);
        tr.print(this, name, w);

        if (init != null) {
            w.write(" =");
            w.allowBreak(2, " ");
            print(init, w, tr);
        }

        w.write(";");
    }

    @Override
    public void dump(CodeWriter w) {
        super.dump(w);

        if (fi != null) {
            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(instance " + fi + ")");
            w.end();
        }

        w.allowBreak(4, " ");
        w.begin(0);
        w.write("(name " + name + ")");
        w.end();
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.FieldDecl(this.position,
                            this.flags,
                            this.type,
                            this.name,
                            this.init);
    }

}
