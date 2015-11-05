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
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.LocalInstance;
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
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A {@code LocalDecl} is an immutable representation of a local variable
 * declaration statement: a type, a name and an optional initializer.
 */
public class LocalDecl_c extends Stmt_c implements LocalDecl {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Flags flags;
    protected TypeNode type;
    protected Id name;
    protected Expr init;
    protected LocalInstance li;

//    @Deprecated
    public LocalDecl_c(Position pos, Flags flags, TypeNode type, Id name,
            Expr init) {
        this(pos, flags, type, name, init, null);
    }

    public LocalDecl_c(Position pos, Flags flags, TypeNode type, Id name,
            Expr init, Ext ext) {
        super(pos, ext);
        assert flags != null && type != null && name != null; // init may be null
        this.flags = flags;
        this.type = type;
        this.name = name;
        this.init = init;
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
    public LocalDecl flags(Flags flags) {
        return flags(this, flags);
    }

    protected <N extends LocalDecl_c> N flags(N n, Flags flags) {
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
    public LocalDecl type(TypeNode type) {
        return type(this, type);
    }

    protected <N extends LocalDecl_c> N type(N n, TypeNode type) {
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
    public LocalDecl id(Id name) {
        return id(this, name);
    }

    protected <N extends LocalDecl_c> N id(N n, Id name) {
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
    public LocalDecl name(String name) {
        return id(this.name.id(name));
    }

    @Override
    public Expr init() {
        return init;
    }

    @Override
    public LocalDecl init(Expr init) {
        return init(this, init);
    }

    protected <N extends LocalDecl_c> N init(N n, Expr init) {
        if (n.init == init) return n;
        n = copyIfNeeded(n);
        n.init = init;
        return n;
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
    public LocalDecl localInstance(LocalInstance li) {
        return localInstance(this, li);
    }

    protected <N extends LocalDecl_c> N localInstance(N n, LocalInstance li) {
        if (n.li == li) return n;
        n = copyIfNeeded(n);
        n.li = li;
        return n;
    }

    /** Reconstruct the declaration. */
    protected <N extends LocalDecl_c> N reconstruct(N n, TypeNode type, Id name,
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

    /**
     * Add the declaration of the variable as we enter the scope of the
     * initializer
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
        return this;
    }

    /**
     * Override superclass behavior to check if the variable is multiply
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

        return localInstance(li);
    }

    protected static class AddDependenciesVisitor extends NodeVisitor {
        protected ConstantChecker cc;
        protected LocalInstance li;

        AddDependenciesVisitor(JLang lang, ConstantChecker cc,
                LocalInstance li) {
            super(lang);
            this.cc = cc;
            this.li = li;
        }

        @Override
        public Node leave(Node old, Node n, NodeVisitor v) {
            if (n instanceof Field) {
                Field f = (Field) n;
                if (!f.fieldInstance().orig().constantValueSet()) {
                    Scheduler scheduler = cc.job().extensionInfo().scheduler();
                    Goal g = scheduler.FieldConstantsChecked(f.fieldInstance()
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

        if (init == null || !cc.lang().isConstant(init, cc.lang())
                || !li.flags().isFinal()) {
            li.setNotConstant();
        }
        else {
            li.setConstantValue(cc.lang().constantValue(init, cc.lang()));
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
            // the expected type of the initializer is the type
            // of the local.
            return type.type();
        }

        return child.type();
    }

    @Override
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        LocalDecl_c n = (LocalDecl_c) super.extRewrite(rw);
        n = localInstance(n, null);
        return n;
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
            w.allowBreak(2, 3, " ", 1);
        }
        tr.print(this, name, w);

        if (init != null) {
            w.write(" =");
            w.allowBreak(2, 2, " ", 1);
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
        return nf.LocalDecl(position, flags, type, name, init);
    }

}
