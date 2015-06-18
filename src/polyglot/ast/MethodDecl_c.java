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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import polyglot.main.Report;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.MemberInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ParsedClassType;
import polyglot.types.ProcedureInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A method declaration.
 */
public class MethodDecl_c extends ProcedureDecl_c implements MethodDecl {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected TypeNode returnType;
    protected MethodInstance mi;

    /**
     * @deprecated Use constructor with Javadoc
     */
    @Deprecated
    public MethodDecl_c(Position pos, Flags flags, TypeNode returnType,
            Id name, List<Formal> formals, List<TypeNode> throwTypes, Block body) {
        this(pos, flags, returnType, name, formals, throwTypes, body, null, null);
    }

//    @Deprecated
    public MethodDecl_c(Position pos, Flags flags, TypeNode returnType,
            Id name, List<Formal> formals, List<TypeNode> throwTypes,
            Block body, Javadoc javadoc) {
        this(pos,
             flags,
             returnType,
             name,
             formals,
             throwTypes,
             body,
             javadoc,
             null);
    }

    /**
     * @deprecated Use constructor with Javadoc
     */
    @Deprecated
    public MethodDecl_c(Position pos, Flags flags, TypeNode returnType,
            Id name, List<Formal> formals, List<TypeNode> throwTypes,
            Block body, Ext ext) {
        this(pos,
             flags,
             returnType,
             name,
             formals,
             throwTypes,
             body,
             null,
             ext);
    }

    public MethodDecl_c(Position pos, Flags flags, TypeNode returnType,
            Id name, List<Formal> formals, List<TypeNode> throwTypes,
            Block body, Javadoc javadoc, Ext ext) {
        super(pos, flags, name, formals, throwTypes, body, javadoc, ext);
        assert (returnType != null);
        this.returnType = returnType;
    }

    @Override
    public boolean isDisambiguated() {
        return mi != null && mi.isCanonical() && super.isDisambiguated();
    }

    @Override
    public MemberInstance memberInstance() {
        return mi;
    }

    @Override
    public TypeNode returnType() {
        return this.returnType;
    }

    @Override
    public MethodDecl returnType(TypeNode returnType) {
        return returnType(this, returnType);
    }

    protected <N extends MethodDecl_c> N returnType(N n, TypeNode returnType) {
        if (n.returnType == returnType) return n;
        n = copyIfNeeded(n);
        n.returnType = returnType;
        return n;
    }

    @Override
    public ProcedureInstance procedureInstance() {
        return methodInstance();
    }

    @Override
    public MethodInstance methodInstance() {
        return mi;
    }

    @Override
    public MethodDecl methodInstance(MethodInstance mi) {
        return methodInstance(this, mi);
    }

    protected <N extends MethodDecl_c> N methodInstance(N n, MethodInstance mi) {
        if (n.mi == mi) return n;
        n = copyIfNeeded(n);
        n.mi = mi;
        return n;
    }

    /** Reconstruct the method. */
    protected <N extends MethodDecl_c> N reconstruct(N n, TypeNode returnType,
            Id name, List<Formal> formals, List<TypeNode> throwTypes, Block body) {
        n = super.reconstruct(n, name, formals, throwTypes, body);
        n = returnType(n, returnType);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Id name = visitChild(this.name, v);
        List<Formal> formals = visitList(this.formals, v);
        TypeNode returnType = visitChild(this.returnType, v);
        List<TypeNode> throwTypes = visitList(this.throwTypes, v);
        Block body = visitChild(this.body, v);
        return reconstruct(this, returnType, name, formals, throwTypes, body);
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

        Flags f = this.flags;

        if (ct.flags().isInterface()) {
            f = f.Public().Abstract();
        }

        MethodInstance mi =
                ts.methodInstance(position(),
                                  ct,
                                  f,
                                  ts.unknownType(position()),
                                  name.id(),
                                  formalTypes,
                                  throwTypes);
        ct.addMethod(mi);
        return methodInstance(mi);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (!returnType.isDisambiguated()) {
            return this;
        }
        mi.setReturnType(returnType.type());
        return super.disambiguate(ar);
    }

    @Override
    public Context enterScope(Context c) {
        if (Report.should_report(TOPICS, 5))
            Report.report(5, "enter scope of method " + name);
        c = c.pushCode(mi);
        return c;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        // Get the mi flags, not the node flags since the mi flags
        // account for being nested within an interface.
        Flags flags = mi.flags();

        if (tc.context().currentClass().flags().isInterface()) {
            if (flags.isProtected() || flags.isPrivate()) {
                throw new SemanticException("Interface methods must be public.",
                                            position());
            }
        }

        try {
            ts.checkMethodFlags(flags);
        }
        catch (SemanticException e) {
            throw new SemanticException(e.getMessage(), position());
        }

        if (body == null && !(flags.isAbstract() || flags.isNative())) {
            throw new SemanticException("Missing method body.", position());
        }

        if (body != null && (flags.isAbstract() || flags.isNative())) {
            throw new SemanticException("An abstract method cannot have a body.",
                                        position());
        }

        if (body != null && flags.isNative()) {
            throw new SemanticException("A native method cannot have a body.",
                                        position());
        }

        // check that all the thrown types are subtypes of Throwable
        throwsCheck(tc);

        // check that inner classes do not declare static methods
        if (flags.isStatic()
                && methodInstance().container().toClass().isInnerClass()) {
            // it's a static method in an inner class.
            throw new SemanticException("Inner classes cannot declare "
                    + "static methods.", this.position());
        }

        overrideMethodCheck(tc);

        return this;
    }

    public void throwsCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();
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
    }

    public void overrideMethodCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        for (MethodInstance mj : mi.implemented())
            ts.checkOverride(mi, mj);
    }

    @Override
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        MethodDecl_c n = (MethodDecl_c) super.extRewrite(rw);
        n = methodInstance(n, null);
        return n;
    }

    @Override
    public String toString() {
        return flags.translate() + returnType + " " + name + "(...)";
    }

    @Override
    public void prettyPrintHeader(Flags flags, CodeWriter w, PrettyPrinter tr) {
        w.begin(0);
        w.write(flags.translate());
        print(returnType, w, tr);
        w.allowBreak(2, 2, " ", 1);
        w.write(name + "(");

        w.allowBreak(2, 2, "", 0);
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
    public void dump(CodeWriter w) {
        super.dump(w);

        w.allowBreak(4, " ");
        w.begin(0);
        w.write("(name " + name + ")");
        w.end();
    }

    @Override
    public Term firstChild() {
        return listChild(formals(), returnType());
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFGList(formals(), returnType(), ENTRY);

        if (body() == null) {
            v.visitCFG(returnType(), this, EXIT);
        }
        else {
            v.visitCFG(returnType(), body(), ENTRY);
            v.visitCFG(body(), this, EXIT);
        }

        return succs;
    }

    private static final Collection<String> TOPICS =
            CollectionUtil.list(Report.types, Report.context);

    @Override
    public Node copy(NodeFactory nf) {
        return nf.MethodDecl(this.position,
                             this.flags,
                             this.returnType,
                             this.name,
                             this.formals,
                             this.throwTypes,
                             this.body,
                             javadoc);
    }

}
