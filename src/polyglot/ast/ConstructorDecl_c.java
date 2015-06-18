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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.translate.ExtensionRewriter;
import polyglot.types.ClassType;
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
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A {@code ConstructorDecl} is an immutable representation of a
 * constructor declaration as part of a class body.
 */
public class ConstructorDecl_c extends ProcedureDecl_c implements
        ConstructorDecl {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected ConstructorInstance ci;

    /**
     * @deprecated Use constructor with Javadoc
     */
    @Deprecated
    public ConstructorDecl_c(Position pos, Flags flags, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body) {
        this(pos, flags, name, formals, throwTypes, body, null, null);
    }

//  @Deprecated
    public ConstructorDecl_c(Position pos, Flags flags, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body,
            Javadoc javadoc) {
        this(pos, flags, name, formals, throwTypes, body, javadoc, null);
    }

    /**
     * @deprecated Use constructor with Javadoc
     */
    @Deprecated
    public ConstructorDecl_c(Position pos, Flags flags, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body, Ext ext) {
        this(pos, flags, name, formals, throwTypes, body, null, ext);
    }

    public ConstructorDecl_c(Position pos, Flags flags, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body,
            Javadoc javadoc, Ext ext) {
        super(pos, flags, name, formals, throwTypes, body, javadoc, ext);
    }

    @Override
    public boolean isDisambiguated() {
        return ci != null && ci.isCanonical() && super.isDisambiguated();
    }

    @Override
    public MemberInstance memberInstance() {
        return ci;
    }

    @Override
    public ProcedureInstance procedureInstance() {
        return constructorInstance();
    }

    @Override
    public ConstructorInstance constructorInstance() {
        return ci;
    }

    @Override
    public ConstructorDecl constructorInstance(ConstructorInstance ci) {
        return constructorInstance(this, ci);
    }

    protected <N extends ConstructorDecl_c> N constructorInstance(N n,
            ConstructorInstance ci) {
        if (n.ci == ci) return n;
        n = copyIfNeeded(n);
        n.ci = ci;
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Id name = visitChild(this.name, v);
        List<Formal> formals = visitList(this.formals, v);
        List<TypeNode> throwTypes = visitList(this.throwTypes, v);
        Block body = visitChild(this.body, v);
        return reconstruct(this, name, formals, throwTypes, body);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        TypeSystem ts = tb.typeSystem();

        ParsedClassType ct = tb.currentClass();

        if (ct == null) {
            return this;
        }

        List<Type> formalTypes = new ArrayList<>(formals.size());
        for (int i = 0; i < formals.size(); i++) {
            formalTypes.add(ts.unknownType(position()));
        }

        List<Type> throwTypes = new ArrayList<>(throwTypes().size());
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
    public Context enterScope(Context c) {
        return c.pushCode(ci);
    }

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
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        ConstructorDecl_c n = (ConstructorDecl_c) super.extRewrite(rw);
        n = constructorInstance(n, null);
        return n;
    }

    @Override
    public String toString() {
        return flags.translate() + name + "(...)";
    }

    @Override
    public void prettyPrintHeader(Flags flags, CodeWriter w, PrettyPrinter tr) {
        w.begin(0);
        w.write(flags.translate());

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
                                  this.body,
                                  javadoc);
    }

}
