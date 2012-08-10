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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.types.ClassType;
import polyglot.types.CodeInstance;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.MemberInstance;
import polyglot.types.ParsedClassType;
import polyglot.types.ProcedureInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A <code>ConstructorDecl</code> is an immutable representation of a
 * constructor declaration as part of a class body.
 */
public class ConstructorDecl_c extends Term_c implements ConstructorDecl {
    protected Flags flags;
    protected Id name;
    protected List<Formal> formals;
    protected List<TypeNode> throwTypes;
    protected Block body;
    protected ConstructorInstance ci;

    public ConstructorDecl_c(Position pos, Flags flags, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body) {
        super(pos);
        assert (flags != null && name != null && formals != null && throwTypes != null); // body may be null
        this.flags = flags;
        this.name = name;
        this.formals = ListUtil.copy(formals, true);
        this.throwTypes = ListUtil.copy(throwTypes, true);
        this.body = body;
    }

    @Override
    public boolean isDisambiguated() {
        return ci != null && ci.isCanonical() && super.isDisambiguated();
    }

    @Override
    public MemberInstance memberInstance() {
        return ci;
    }

    /** Get the flags of the constructor. */
    @Override
    public Flags flags() {
        return this.flags;
    }

    /** Set the flags of the constructor. */
    @Override
    public ConstructorDecl flags(Flags flags) {
        if (flags.equals(this.flags)) return this;
        ConstructorDecl_c n = (ConstructorDecl_c) copy();
        n.flags = flags;
        return n;
    }

    /** Get the name of the constructor. */
    @Override
    public Id id() {
        return this.name;
    }

    /** Set the name of the constructor. */
    @Override
    public ConstructorDecl id(Id name) {
        ConstructorDecl_c n = (ConstructorDecl_c) copy();
        n.name = name;
        return n;
    }

    /** Get the name of the constructor. */
    @Override
    public String name() {
        return this.name.id();
    }

    /** Set the name of the constructor. */
    @Override
    public ConstructorDecl name(String name) {
        return id(this.name.id(name));
    }

    /** Get the formals of the constructor. */
    @Override
    public List<Formal> formals() {
        return Collections.unmodifiableList(this.formals);
    }

    /** Set the formals of the constructor. */
    @Override
    public ConstructorDecl formals(List<Formal> formals) {
        ConstructorDecl_c n = (ConstructorDecl_c) copy();
        n.formals = ListUtil.copy(formals, true);
        return n;
    }

    /** Get the throwTypes of the constructor. */
    @Override
    public List<TypeNode> throwTypes() {
        return Collections.unmodifiableList(this.throwTypes);
    }

    /** Set the throwTypes of the constructor. */
    @Override
    public ConstructorDecl throwTypes(List<TypeNode> throwTypes) {
        ConstructorDecl_c n = (ConstructorDecl_c) copy();
        n.throwTypes = ListUtil.copy(throwTypes, true);
        return n;
    }

    @Override
    public Term codeBody() {
        return this.body;
    }

    /** Get the body of the constructor. */
    @Override
    public Block body() {
        return this.body;
    }

    /** Set the body of the constructor. */
    @Override
    public CodeBlock body(Block body) {
        ConstructorDecl_c n = (ConstructorDecl_c) copy();
        n.body = body;
        return n;
    }

    /** Get the constructorInstance of the constructor. */
    @Override
    public ConstructorInstance constructorInstance() {
        return ci;
    }

    /** Get the procedureInstance of the constructor. */
    @Override
    public ProcedureInstance procedureInstance() {
        return ci;
    }

    @Override
    public CodeInstance codeInstance() {
        return procedureInstance();
    }

    /** Set the constructorInstance of the constructor. */
    @Override
    public ConstructorDecl constructorInstance(ConstructorInstance ci) {
        if (ci == this.ci) return this;
        ConstructorDecl_c n = (ConstructorDecl_c) copy();
        n.ci = ci;
        return n;
    }

    /** Reconstruct the constructor. */
    protected ConstructorDecl_c reconstruct(Id name, List<Formal> formals,
            List<TypeNode> throwTypes, Block body) {
        if (name != this.name || !CollectionUtil.equals(formals, this.formals)
                || !CollectionUtil.equals(throwTypes, this.throwTypes)
                || body != this.body) {
            ConstructorDecl_c n = (ConstructorDecl_c) copy();
            n.name = name;
            n.formals = ListUtil.copy(formals, true);
            n.throwTypes = ListUtil.copy(throwTypes, true);
            n.body = body;
            return n;
        }

        return this;
    }

    /** Visit the children of the constructor. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Id name = (Id) visitChild(this.name, v);
        List<Formal> formals = visitList(this.formals, v);
        List<TypeNode> throwTypes = visitList(this.throwTypes, v);
        Block body = (Block) visitChild(this.body, v);
        return reconstruct(name, formals, throwTypes, body);
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

        List<Type> formalTypes = new ArrayList<Type>(formals.size());
        for (int i = 0; i < formals.size(); i++) {
            formalTypes.add(ts.unknownType(position()));
        }

        List<Type> throwTypes = new ArrayList<Type>(throwTypes().size());
        for (int i = 0; i < throwTypes().size(); i++) {
            throwTypes.add(ts.unknownType(position()));
        }

        ConstructorInstance ci =
                ts.constructorInstance(position(),
                                       ct,
                                       flags,
                                       formalTypes,
                                       throwTypes);
        ct.addConstructor(ci);

        return constructorInstance(ci);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (this.ci.isCanonical()) {
            // already done
            return this;
        }

        List<Type> formalTypes = new LinkedList<Type>();
        List<Type> throwTypes = new LinkedList<Type>();

        for (Formal f : formals) {
            if (!f.isDisambiguated()) {
                return this;
            }
            formalTypes.add(f.declType());
        }

        ci.setFormalTypes(formalTypes);

        for (TypeNode tn : throwTypes()) {
            if (!tn.isDisambiguated()) {
                return this;
            }
            throwTypes.add(tn.type());
        }

        ci.setThrowTypes(throwTypes);

        return this;
    }

    @Override
    public Context enterScope(Context c) {
        return c.pushCode(ci);
    }

    /** Type check the constructor. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Context c = tc.context();
        TypeSystem ts = tc.typeSystem();

        ClassType ct = c.currentClass();

        if (ct.flags().isInterface()) {
            throw new SemanticException("Cannot declare a constructor inside an interface.",
                                        position());
        }

        if (ct.isAnonymous()) {
            throw new SemanticException("Cannot declare a constructor inside an anonymous class.",
                                        position());
        }

        String ctName = ct.name();

        if (!ctName.equals(name.id())) {
            throw new SemanticException("Constructor name \"" + name
                    + "\" does not match name of containing class \"" + ctName
                    + "\".", position());
        }

        try {
            ts.checkConstructorFlags(flags());
        }
        catch (SemanticException e) {
            throw new SemanticException(e.getMessage(), position());
        }

        if (body == null && !flags().isNative()) {
            throw new SemanticException("Missing constructor body.", position());
        }

        if (body != null && flags().isNative()) {
            throw new SemanticException("A native constructor cannot have a body.",
                                        position());
        }

        for (TypeNode tn : throwTypes()) {
            Type t = tn.type();
            if (!t.isThrowable()) {
                throw new SemanticException("Type \""
                                                    + t
                                                    + "\" is not a subclass of \""
                                                    + ts.Throwable() + "\".",
                                            tn.position());
            }
        }

        return this;
    }

    @Override
    public NodeVisitor exceptionCheckEnter(ExceptionChecker ec)
            throws SemanticException {
        return ec.push(constructorInstance().throwTypes());
    }

    @Override
    public String toString() {
        return flags.translate() + name + "(...)";
    }

    /** Write the constructor to an output file. */
    public void prettyPrintHeader(CodeWriter w, PrettyPrinter tr) {
        w.begin(0);
        w.write(flags().translate());

        tr.print(this, name, w);
        w.write("(");

        w.begin(0);

        for (Iterator<Formal> i = formals.iterator(); i.hasNext();) {
            Formal f = i.next();
            print(f, w, tr);

            if (i.hasNext()) {
                w.write(",");
                w.allowBreak(0, " ");
            }
        }

        w.end();
        w.write(")");

        if (!throwTypes().isEmpty()) {
            w.allowBreak(6);
            w.write("throws ");

            for (Iterator<TypeNode> i = throwTypes().iterator(); i.hasNext();) {
                TypeNode tn = i.next();
                print(tn, w, tr);

                if (i.hasNext()) {
                    w.write(",");
                    w.allowBreak(4, " ");
                }
            }
        }

        w.end();
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        prettyPrintHeader(w, tr);

        if (body != null) {
            printSubStmt(body, w, tr);
        }
        else {
            w.write(";");
        }
    }

    @Override
    public void dump(CodeWriter w) {
        super.dump(w);

        if (ci != null) {
            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(instance " + ci + ")");
            w.end();
        }
    }

    @Override
    public Term firstChild() {
        return listChild(formals(), body() != null ? body() : null);
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (body() != null) {
            v.visitCFGList(formals(), body(), ENTRY);
            v.visitCFG(body(), this, EXIT);
        }
        else {
            v.visitCFGList(formals(), this, EXIT);
        }

        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.ConstructorDecl(this.position,
                                  this.flags,
                                  this.name,
                                  this.formals,
                                  this.throwTypes,
                                  this.body);
    }

}
