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

import java.util.LinkedList;
import java.util.List;

import polyglot.types.CodeInstance;
import polyglot.types.Flags;
import polyglot.types.ProcedureInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;

/**
 * A {@code ProcedureDecl} is an immutable representation of a
 * constructor or method declaration as part of a class body.
 */
public abstract class ProcedureDecl_c extends Term_c
        implements ProcedureDecl, ProcedureDeclOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Flags flags;
    protected Id name;
    protected List<Formal> formals;
    protected List<TypeNode> throwTypes;
    protected Block body;
    protected Javadoc javadoc;

    /**
     * @deprecated Use constructor with Javadoc
     */
    @Deprecated
    public ProcedureDecl_c(Position pos, Flags flags, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body) {
        this(pos, flags, name, formals, throwTypes, body, null, null);
    }

//    @Deprecated
    public ProcedureDecl_c(Position pos, Flags flags, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body,
            Javadoc javadoc) {
        this(pos, flags, name, formals, throwTypes, body, javadoc, null);
    }

    /**
     * @deprecated Use constructor with Javadoc
     */
    @Deprecated
    public ProcedureDecl_c(Position pos, Flags flags, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body,
            Ext ext) {
        this(pos, flags, name, formals, throwTypes, body, null, ext);
    }

    public ProcedureDecl_c(Position pos, Flags flags, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body,
            Javadoc javadoc, Ext ext) {
        super(pos, ext);
        assert flags != null && name != null && formals != null
                && throwTypes != null; // body may be null
        this.flags = flags;
        this.name = name;
        this.formals = ListUtil.copy(formals, true);
        this.throwTypes = ListUtil.copy(throwTypes, true);
        this.body = body;
        this.javadoc = javadoc;
    }

    @Override
    public Flags flags() {
        return flags;
    }

    @Override
    public ProcedureDecl flags(Flags flags) {
        return flags(this, flags);
    }

    protected <N extends ProcedureDecl_c> N flags(N n, Flags flags) {
        if (n.flags.equals(flags)) return n;
        n = copyIfNeeded(n);
        n.flags = flags;
        return n;
    }

    @Override
    public Id id() {
        return name;
    }

    @Override
    public ProcedureDecl id(Id name) {
        return id(this, name);
    }

    protected <N extends ProcedureDecl_c> N id(N n, Id name) {
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
    public ProcedureDecl name(String name) {
        return id(this.name.id(name));
    }

    @Override
    public List<Formal> formals() {
        return formals;
    }

    @Override
    public ProcedureDecl formals(List<Formal> formals) {
        return formals(this, formals);
    }

    protected <N extends ProcedureDecl_c> N formals(N n, List<Formal> formals) {
        if (CollectionUtil.equals(n.formals, formals)) return n;
        n = copyIfNeeded(n);
        n.formals = ListUtil.copy(formals, true);
        return n;
    }

    @Override
    public List<TypeNode> throwTypes() {
        return throwTypes;
    }

    @Override
    public ProcedureDecl throwTypes(List<TypeNode> throwTypes) {
        return throwTypes(this, throwTypes);
    }

    protected <N extends ProcedureDecl_c> N throwTypes(N n,
            List<TypeNode> throwTypes) {
        if (CollectionUtil.equals(n.throwTypes, throwTypes)) return n;
        n = copyIfNeeded(n);
        n.throwTypes = ListUtil.copy(throwTypes, true);
        return n;
    }

    @Override
    public Term codeBody() {
        return body();
    }

    @Override
    public Block body() {
        return body;
    }

    @Override
    public CodeBlock body(Block body) {
        return body(this, body);
    }

    protected <N extends ProcedureDecl_c> N body(N n, Block body) {
        if (n.body == body) return n;
        n = copyIfNeeded(n);
        n.body = body;
        return n;
    }

    @Override
    public CodeInstance codeInstance() {
        return procedureInstance();
    }

    @Override
    public abstract ProcedureInstance procedureInstance();

    @Override
    public NodeVisitor buildTypesEnter(TypeBuilder tb)
            throws SemanticException {
        return tb.pushCode();
    }

    /** Reconstruct the procedure. */
    protected <N extends ProcedureDecl_c> N reconstruct(N n, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body) {
        n = id(n, name);
        n = formals(n, formals);
        n = throwTypes(n, throwTypes);
        n = body(n, body);
        return n;
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        ProcedureInstance pi = procedureInstance();
        if (pi.isCanonical()) {
            // already done
            return this;
        }

        List<Type> formalTypes = new LinkedList<>();
        List<Type> throwTypes = new LinkedList<>();

        for (Formal f : formals) {
            if (!f.isDisambiguated()) {
                return this;
            }
            formalTypes.add(f.declType());
        }

        pi.setFormalTypes(formalTypes);

        for (TypeNode tn : throwTypes()) {
            if (!tn.isDisambiguated()) {
                return this;
            }
            throwTypes.add(tn.type());
        }

        pi.setThrowTypes(throwTypes);

        return this;
    }

    @Override
    public NodeVisitor exceptionCheckEnter(ExceptionChecker ec)
            throws SemanticException {
        return ec.push(procedureInstance().throwTypes());
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (javadoc != null) javadoc.prettyPrint(w, tr);

        w.begin(0);
        ((JLang) tr.lang()).prettyPrintHeader(this, flags(), w, tr);

        if (body != null) {
            printSubStmt(body, w, tr);
        }
        else {
            w.write(";");
        }
        w.end();
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

        ProcedureInstance pi = procedureInstance();
        if (pi != null) {
            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(instance " + pi + ")");
            w.end();
        }
    }

    @Override
    public ProcedureDecl javadoc(Javadoc javadoc) {
        return javadoc(this, javadoc);
    }

    protected <N extends ProcedureDecl_c> N javadoc(N n, Javadoc javadoc) {
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
