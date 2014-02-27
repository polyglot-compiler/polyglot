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
import java.util.LinkedList;
import java.util.List;

import polyglot.main.Report;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.CodeInstance;
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
import polyglot.util.Copy;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A method declaration.
 */
public class MethodDecl_c extends Term_c implements MethodDecl,
        ProcedureDeclOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Flags flags;
    protected TypeNode returnType;
    protected Id name;
    protected List<Formal> formals;
    protected List<TypeNode> throwTypes;
    protected Block body;
    protected MethodInstance mi;

    public MethodDecl_c(Position pos, Flags flags, TypeNode returnType,
            Id name, List<Formal> formals, List<TypeNode> throwTypes, Block body) {
        super(pos);
        assert (flags != null && returnType != null && name != null
                && formals != null && throwTypes != null); // body may be null
        this.flags = flags;
        this.returnType = returnType;
        this.name = name;
        this.formals = ListUtil.copy(formals, true);
        this.throwTypes = ListUtil.copy(throwTypes, true);
        this.body = body;
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
    public Flags flags() {
        return this.flags;
    }

    @Override
    public MethodDecl flags(Flags flags) {
        return flags(this, flags);
    }

    protected <N extends MethodDecl_c> N flags(N n, Flags flags) {
        if (n.flags.equals(flags)) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.flags = flags;
        return n;
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
        if (n == this) n = Copy.Util.copy(n);
        n.returnType = returnType;
        return n;
    }

    @Override
    public Id id() {
        return this.name;
    }

    @Override
    public MethodDecl id(Id name) {
        return id(this, name);
    }

    protected <N extends MethodDecl_c> N id(N n, Id name) {
        if (n.name == name) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.name = name;
        return n;
    }

    @Override
    public String name() {
        return this.name.id();
    }

    @Override
    public MethodDecl name(String name) {
        return id(this.name.id(name));
    }

    @Override
    public List<Formal> formals() {
        return this.formals;
    }

    @Override
    public MethodDecl formals(List<Formal> formals) {
        return formals(this, formals);
    }

    protected <N extends MethodDecl_c> N formals(N n, List<Formal> formals) {
        if (CollectionUtil.equals(n.formals, formals)) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.formals = ListUtil.copy(formals, true);
        return n;
    }

    @Override
    public List<TypeNode> throwTypes() {
        return this.throwTypes;
    }

    @Override
    public MethodDecl throwTypes(List<TypeNode> throwTypes) {
        return throwTypes(this, throwTypes);
    }

    protected <N extends MethodDecl_c> N throwTypes(N n,
            List<TypeNode> throwTypes) {
        if (CollectionUtil.equals(n.throwTypes, throwTypes)) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.throwTypes = ListUtil.copy(throwTypes, true);
        return n;
    }

    @Override
    public Term codeBody() {
        return body();
    }

    @Override
    public Block body() {
        return this.body;
    }

    @Override
    public MethodDecl body(Block body) {
        return body(this, body);
    }

    protected <N extends MethodDecl_c> N body(N n, Block body) {
        if (n.body == body) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.body = body;
        return n;
    }

    @Override
    public CodeInstance codeInstance() {
        return procedureInstance();
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
        if (n == this) n = Copy.Util.copy(n);
        n.mi = mi;
        return n;
    }

    /** Reconstruct the method. */
    protected MethodDecl_c reconstruct(TypeNode returnType, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body) {
        MethodDecl_c n = this;
        n = returnType(n, returnType);
        n = id(n, name);
        n = formals(n, formals);
        n = throwTypes(n, throwTypes);
        n = body(n, body);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Id name = visitChild(this.name, v);
        List<Formal> formals = visitList(this.formals, v);
        TypeNode returnType = visitChild(this.returnType, v);
        List<TypeNode> throwTypes = visitList(this.throwTypes, v);
        Block body = visitChild(this.body, v);
        return reconstruct(returnType, name, formals, throwTypes, body);
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
        if (this.mi.isCanonical()) {
            // already done
            return this;
        }

        if (!returnType.isDisambiguated()) {
            return this;
        }

        mi.setReturnType(returnType.type());

        List<Type> formalTypes = new LinkedList<Type>();
        List<Type> throwTypes = new LinkedList<Type>();

        for (Formal f : formals) {
            if (!f.isDisambiguated()) {
                return this;
            }
            formalTypes.add(f.declType());
        }

        mi.setFormalTypes(formalTypes);

        for (TypeNode tn : throwTypes()) {
            if (!tn.isDisambiguated()) {
                return this;
            }
            throwTypes.add(tn.type());
        }

        mi.setThrowTypes(throwTypes);

        return this;
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

        for (MethodInstance mj : mi.implemented()) {

            if (!ts.isAccessible(mj, tc.context())) {
                continue;
            }

            ts.checkOverride(mi, mj);
        }
    }

    @Override
    public NodeVisitor exceptionCheckEnter(ExceptionChecker ec)
            throws SemanticException {
        return ec.push(methodInstance().throwTypes());
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
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        ((JLang) tr.lang()).prettyPrintHeader(this, flags(), w, tr);

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

        if (mi != null) {
            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(instance " + mi + ")");
            w.end();
        }

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
                             this.body);
    }

}
