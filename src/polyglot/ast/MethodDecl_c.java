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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.main.Report;
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
 * A method declaration.
 */
public class MethodDecl_c extends Term_c implements MethodDecl {
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

    /** Get the flags of the method. */
    @Override
    public Flags flags() {
        return this.flags;
    }

    /** Set the flags of the method. */
    @Override
    public MethodDecl flags(Flags flags) {
        if (flags.equals(this.flags)) return this;
        MethodDecl_c n = (MethodDecl_c) copy();
        n.flags = flags;
        return n;
    }

    /** Get the return type of the method. */
    @Override
    public TypeNode returnType() {
        return this.returnType;
    }

    /** Set the return type of the method. */
    @Override
    public MethodDecl returnType(TypeNode returnType) {
        MethodDecl_c n = (MethodDecl_c) copy();
        n.returnType = returnType;
        return n;
    }

    /** Get the name of the method. */
    @Override
    public Id id() {
        return this.name;
    }

    /** Set the name of the method. */
    @Override
    public MethodDecl id(Id name) {
        MethodDecl_c n = (MethodDecl_c) copy();
        n.name = name;
        return n;
    }

    /** Get the name of the method. */
    @Override
    public String name() {
        return this.name.id();
    }

    /** Set the name of the method. */
    @Override
    public MethodDecl name(String name) {
        return id(this.name.id(name));
    }

    /** Get the formals of the method. */
    @Override
    public List<Formal> formals() {
        return Collections.unmodifiableList(this.formals);
    }

    /** Set the formals of the method. */
    @Override
    public MethodDecl formals(List<Formal> formals) {
        MethodDecl_c n = (MethodDecl_c) copy();
        n.formals = ListUtil.copy(formals, true);
        return n;
    }

    /** Get the exception types of the method. */
    @Override
    public List<TypeNode> throwTypes() {
        return Collections.unmodifiableList(this.throwTypes);
    }

    /** Set the exception types of the method. */
    @Override
    public MethodDecl throwTypes(List<TypeNode> throwTypes) {
        MethodDecl_c n = (MethodDecl_c) copy();
        n.throwTypes = ListUtil.copy(throwTypes, true);
        return n;
    }

    @Override
    public Term codeBody() {
        return this.body;
    }

    /** Get the body of the method. */
    @Override
    public Block body() {
        return this.body;
    }

    /** Set the body of the method. */
    @Override
    public CodeBlock body(Block body) {
        MethodDecl_c n = (MethodDecl_c) copy();
        n.body = body;
        return n;
    }

    /** Get the method instance of the method. */
    @Override
    public MethodInstance methodInstance() {
        return mi;
    }

    /** Set the method instance of the method. */
    @Override
    public MethodDecl methodInstance(MethodInstance mi) {
        if (mi == this.mi) return this;
        MethodDecl_c n = (MethodDecl_c) copy();
        n.mi = mi;
        return n;
    }

    @Override
    public CodeInstance codeInstance() {
        return procedureInstance();
    }

    /** Get the procedure instance of the method. */
    @Override
    public ProcedureInstance procedureInstance() {
        return mi;
    }

    /** Reconstruct the method. */
    protected MethodDecl_c reconstruct(TypeNode returnType, Id name,
            List<Formal> formals, List<TypeNode> throwTypes, Block body) {
        if (returnType != this.returnType || name != this.name
                || !CollectionUtil.equals(formals, this.formals)
                || !CollectionUtil.equals(throwTypes, this.throwTypes)
                || body != this.body) {
            MethodDecl_c n = (MethodDecl_c) copy();
            n.returnType = returnType;
            n.name = name;
            n.formals = ListUtil.copy(formals, true);
            n.throwTypes = ListUtil.copy(throwTypes, true);
            n.body = body;
            return n;
        }

        return this;
    }

    /** Visit the children of the method. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Id name = (Id) visitChild(this.name, v);
        List<Formal> formals = visitList(this.formals, v);
        TypeNode returnType = (TypeNode) visitChild(this.returnType, v);
        List<TypeNode> throwTypes = visitList(this.throwTypes, v);
        Block body = (Block) visitChild(this.body, v);
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

    /** Type check the method. */
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

    protected void throwsCheck(TypeChecker tc) throws SemanticException {
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

    protected void overrideMethodCheck(TypeChecker tc) throws SemanticException {
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
    public String toString() {
        return flags.translate() + returnType + " " + name + "(...)";
    }

    /** Write the method to an output file. */
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
        prettyPrintHeader(flags(), w, tr);

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
