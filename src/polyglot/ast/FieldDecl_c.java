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

import polyglot.frontend.MissingDependencyException;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.Goal;
import polyglot.translate.ExtensionRewriter;
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
import polyglot.util.SerialVersionUID;
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
 * A {@code FieldDecl} is an immutable representation of the declaration
 * of a field of a class.
 */
public class FieldDecl_c extends Term_c implements FieldDecl {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Flags flags;
    protected TypeNode type;
    protected Id name;
    protected Expr init;
    protected FieldInstance fi;
    protected InitializerInstance ii;
    protected Javadoc javadoc;

    /**
     * @deprecated Use constructor with Javadoc
     */
    @Deprecated
    public FieldDecl_c(Position pos, Flags flags, TypeNode type, Id name,
            Expr init) {
        this(pos, flags, type, name, init, null, null);
    }

//  @Deprecated
    public FieldDecl_c(Position pos, Flags flags, TypeNode type, Id name,
            Expr init, Javadoc javadoc) {
        this(pos, flags, type, name, init, javadoc, null);
    }

    /**
     * @deprecated Use constructor with Javadoc
     */
    @Deprecated
    public FieldDecl_c(Position pos, Flags flags, TypeNode type, Id name,
            Expr init, Ext ext) {
        this(pos, flags, type, name, init, null, ext);
    }

    public FieldDecl_c(Position pos, Flags flags, TypeNode type, Id name,
            Expr init, Javadoc javadoc, Ext ext) {
        super(pos, ext);
        assert flags != null && type != null && name != null; // init may be null
        this.flags = flags;
        this.type = type;
        this.name = name;
        this.init = init;
        this.javadoc = javadoc;
    }

    @Override
    public boolean isDisambiguated() {
        return fi != null && fi.isCanonical()
                && (init == null || ii != null && ii.isCanonical())
                && super.isDisambiguated();
    }

    @Override
    public MemberInstance memberInstance() {
        return fieldInstance();
    }

    @Override
    public VarInstance varInstance() {
        return fieldInstance();
    }

    @Override
    public CodeInstance codeInstance() {
        return initializerInstance();
    }

    @Override
    public InitializerInstance initializerInstance() {
        return ii;
    }

    @Override
    public FieldDecl initializerInstance(InitializerInstance ii) {
        return initializerInstance(this, ii);
    }

    protected <N extends FieldDecl_c> N initializerInstance(N n,
            InitializerInstance ii) {
        if (n.ii == ii) return n;
        n = copyIfNeeded(n);
        n.ii = ii;
        return n;
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
    public FieldDecl flags(Flags flags) {
        return flags(this, flags);
    }

    protected <N extends FieldDecl_c> N flags(N n, Flags flags) {
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
    public FieldDecl type(TypeNode type) {
        return type(this, type);
    }

    protected <N extends FieldDecl_c> N type(N n, TypeNode type) {
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
    public FieldDecl id(Id name) {
        return id(this, name);
    }

    protected <N extends FieldDecl_c> N id(N n, Id name) {
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
    public FieldDecl name(String name) {
        return id(this.name.id(name));
    }

    @Override
    public Term codeBody() {
        return init();
    }

    @Override
    public Expr init() {
        return init;
    }

    @Override
    public FieldDecl init(Expr init) {
        return init(this, init);
    }

    protected <N extends FieldDecl_c> N init(N n, Expr init) {
        if (n.init == init) return n;
        n = copyIfNeeded(n);
        n.init = init;
        return n;
    }

    @Override
    public FieldInstance fieldInstance() {
        return fi;
    }

    @Override
    public FieldDecl fieldInstance(FieldInstance fi) {
        return fieldInstance(this, fi);
    }

    protected <N extends FieldDecl_c> N fieldInstance(N n, FieldInstance fi) {
        if (n.fi == fi) return n;
        n = copyIfNeeded(n);
        n.fi = fi;
        return n;
    }

    /** Reconstruct the declaration. */
    protected <N extends FieldDecl_c> N reconstruct(N n, TypeNode type, Id name,
            Expr init) {
        n = type(n, type);
        n = id(n, name);
        n = init(n, init);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode type = visitChild(this.type, v);
        Id name = visitChild(this.name, v);
        Expr init = visitChild(this.init, v);
        return reconstruct(this, type, name, init);
    }

    @Override
    public NodeVisitor buildTypesEnter(TypeBuilder tb)
            throws SemanticException {
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

        FieldDecl_c n = this;

        if (init != null) {
            Flags iflags = f.isStatic() ? Flags.STATIC : Flags.NONE;
            InitializerInstance ii =
                    ts.initializerInstance(init.position(), ct, iflags);
            n = initializerInstance(n, ii);
        }

        // XXX: MutableFieldInstance
        FieldInstance fi =
                ts.fieldInstance(position(),
                                 ct,
                                 f,
                                 ts.unknownType(position()),
                                 name.id());
        ct.addField(fi);

        n = flags(n, f);
        n = fieldInstance(n, fi);
        return n;
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (fi.isCanonical()) {
            // Nothing to do.
            return this;
        }

        if (declType().isCanonical()) {
            fi.setType(declType());
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

        AddDependenciesVisitor(JLang lang, Scheduler scheduler,
                FieldInstance fi) {
            super(lang);
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
                    Goal myGoal = scheduler.FieldConstantsChecked(fi);

                    for (Goal g : newGoal.prerequisiteGoals(scheduler)) {
                        if (scheduler.prerequisiteDependsOn(g, myGoal)) {
                            fi.setNotConstant();
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
        if (!cc.lang().isConstant(init, cc.lang())) {
            fi.setNotConstant();
        }
        else {
            fi.setConstantValue(cc.lang().constantValue(init, cc.lang()));
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
        nn = (FieldDecl) tc.lang().visitChildren(nn, childtc);
        nn = (FieldDecl) tc.leave(parent, old, nn, childtc);

        if (!constantValueSet) {
            ConstantChecker cc =
                    new ConstantChecker(tc.job(),
                                        tc.typeSystem(),
                                        tc.nodeFactory());
            cc = (ConstantChecker) cc.context(childtc.context());
            nn = (FieldDecl) tc.lang().checkConstants(nn, cc);
        }

        return nn;
    }

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
                ((ArrayInit) init).typeCheckElements(tc, type.type());
            }
            else {
                if (!ts.isImplicitCastValid(init.type(), type.type())
                        && !ts.typeEquals(init.type(), type.type())
                        && !ts.numericConversionValid(type.type(),
                                                      tc.lang()
                                                        .constantValue(init,
                                                                       tc.lang()))) {

                    throw new SemanticException("The type of the variable "
                            + "initializer \"" + init.type()
                            + "\" does not match that of "
                            + "the declaration \"" + type.type() + "\".",
                                                init.position());
                }
            }
        }

        // check that inner classes do not declare static fields, unless they
        // are compile-time constants
        if (flags().isStatic()
                && fieldInstance().container().toClass().isInnerClass()) {
            // it's a static field in an inner class.
            if (!flags().isFinal() || init == null
                    || !tc.lang().isConstant(init, tc.lang())) {
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
            // the expected type of the initializer is the type
            // of the field.
            return type.type();
        }

        return child.type();
    }

    @Override
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        FieldDecl_c n = (FieldDecl_c) super.extRewrite(rw);
        n = fieldInstance(n, null);
        n = initializerInstance(n, null);
        return n;
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
        boolean isInterface = fi != null && fi.container() != null
                && fi.container().toClass().flags().isInterface();

        Flags f = flags;

        if (isInterface) {
            f = f.clearPublic();
            f = f.clearStatic();
            f = f.clearFinal();
        }

        if (javadoc != null) javadoc.prettyPrint(w, tr);

        w.write(f.translate());
        print(type, w, tr);
        w.allowBreak(2, 3, " ", 1);
        tr.print(this, name, w);

        if (init != null) {
            w.write(" =");
            w.allowBreak(2, 2, " ", 1);
            print(init, w, tr);
        }

        w.write(";");
    }

    @Override
    public void dump(CodeWriter w) {
        super.dump(w);

        if (javadoc != null) {
            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(javadoc ...)");
            w.end();
        }

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
        return nf.FieldDecl(position, flags, type, name, init);
    }

    @Override
    public FieldDecl javadoc(Javadoc javadoc) {
        return javadoc(this, javadoc);
    }

    protected <N extends FieldDecl_c> N javadoc(N n, Javadoc javadoc) {
        if (n.javadoc == javadoc) return n;
        n = copyIfNeeded(n);
        n.javadoc = javadoc;
        return n;
    }

    @Override
    public Javadoc javadoc() {
        return javadoc;
    }
}
