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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.translate.ExtensionRewriter;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.ParsedClassType;
import polyglot.types.ProcedureInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.BodyDisambiguator;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ConstantChecker;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.PruningVisitor;
import polyglot.visit.SignatureDisambiguator;
import polyglot.visit.SupertypeDisambiguator;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A {@code New} is an immutable representation of the use of the
 * {@code new} operator to create a new instance of a class.  In
 * addition to the type of the class being created, a {@code New} has a
 * list of arguments to be passed to the constructor of the object and an
 * optional {@code ClassBody} used to support anonymous classes.
 */
public class New_c extends Expr_c implements New, NewOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Expr qualifier;
    protected TypeNode objectType;
    protected List<Expr> arguments;
    protected ClassBody body;
    protected ConstructorInstance ci;
    protected ParsedClassType anonType;
    protected boolean qualifierImplicit;

//    @Deprecated
    public New_c(Position pos, Expr qualifier, TypeNode tn,
            List<Expr> arguments, ClassBody body) {
        this(pos, qualifier, tn, arguments, body, null);
    }

    public New_c(Position pos, Expr qualifier, TypeNode tn,
            List<Expr> arguments, ClassBody body, Ext ext) {
        super(pos, ext);
        assert tn != null && arguments != null; // qualifier and body may be null
        this.qualifier = qualifier;
        qualifierImplicit = qualifier == null;
        objectType = tn;
        this.arguments = ListUtil.copy(arguments, true);
        this.body = body;
    }

    @Override
    public Expr qualifier() {
        return qualifier;
    }

    @Override
    public New qualifier(Expr qualifier) {
        return qualifier(this, qualifier);
    }

    protected <N extends New_c> N qualifier(N n, Expr qualifier) {
        if (n.qualifier == qualifier) return n;
        n = copyIfNeeded(n);
        n.qualifier = qualifier;
        return n;
    }

    @Override
    public boolean isQualifierImplicit() {
        return qualifierImplicit;
    }

    @Override
    public New qualifierImplicit(boolean implicit) {
        return qualifierImplicit(this, implicit);
    }

    protected <N extends New_c> N qualifierImplicit(N n, boolean implicit) {
        if (n.qualifierImplicit == implicit) return n;
        n = copyIfNeeded(n);
        n.qualifierImplicit = implicit;
        return n;
    }

    @Override
    public TypeNode objectType() {
        return objectType;
    }

    @Override
    public New objectType(TypeNode objectType) {
        return objectType(this, objectType);
    }

    protected <N extends New_c> N objectType(N n, TypeNode objectType) {
        if (n.objectType == objectType) return n;
        n = copyIfNeeded(n);
        n.objectType = objectType;
        return n;
    }

    @Override
    public ParsedClassType anonType() {
        return anonType;
    }

    @Override
    public New anonType(ParsedClassType anonType) {
        return anonType(this, anonType);
    }

    protected <N extends New_c> N anonType(N n, ParsedClassType anonType) {
        if (n.anonType == anonType) return n;
        n = copyIfNeeded(n);
        n.anonType = anonType;
        return n;
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
    public New constructorInstance(ConstructorInstance ci) {
        return constructorInstance(this, ci);
    }

    protected <N extends New_c> N constructorInstance(N n,
            ConstructorInstance ci) {
        if (n.ci == ci) return n;
        n = copyIfNeeded(n);
        n.ci = ci;
        return n;
    }

    @Override
    public List<Expr> arguments() {
        return arguments;
    }

    @Override
    public New arguments(List<Expr> arguments) {
        return arguments(this, arguments);
    }

    protected <N extends New_c> N arguments(N n, List<Expr> arguments) {
        if (CollectionUtil.equals(n.arguments, arguments)) return n;
        n = copyIfNeeded(n);
        n.arguments = ListUtil.copy(arguments, true);
        return n;
    }

    @Override
    public ClassBody body() {
        return body;
    }

    @Override
    public New body(ClassBody body) {
        return body(this, body);
    }

    protected <N extends New_c> N body(N n, ClassBody body) {
        if (n.body == body) return n;
        n = copyIfNeeded(n);
        n.body = body;
        return n;
    }

    /** Reconstruct the expression. */
    protected <N extends New_c> N reconstruct(N n, Expr qualifier, TypeNode tn,
            List<Expr> arguments, ClassBody body) {
        n = objectType(n, tn);
        n = qualifier(n, qualifier);
        n = arguments(n, arguments);
        n = body(n, body);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr qualifier = visitChild(this.qualifier, v);
        TypeNode tn = visitChild(objectType, v);
        List<Expr> arguments = visitList(this.arguments, v);
        ClassBody body = visitChild(this.body, v);
        return reconstruct(this, qualifier, tn, arguments, body);
    }

    @Override
    public Context enterChildScope(Node child, Context c) {
        if (child == body && anonType != null && body != null) {
            c = c.pushClass(anonType, anonType);
        }
        return super.enterChildScope(child, c);
    }

    @Override
    public NodeVisitor buildTypesEnter(TypeBuilder tb)
            throws SemanticException {
        if (body != null) {
            /*
            // bypass the visiting of the body of the anonymous class. We'll
            // get around to visiting it in the buildTypes method.
            // We do this because we need to visit the body of the anonymous
            // class after we've pushed an anon class onto the type builder,
            // but we need to check the arguments, and qualifier, etc. outside
            // of the scope of the anon class.
            return tb.bypass(body);
             */
            return tb.pushAnonClass(position());
        }

        return tb;
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        New_c n = this;
        TypeSystem ts = tb.typeSystem();

        List<Type> l = new ArrayList<>(n.arguments.size());
        for (int i = 0; i < n.arguments.size(); i++) {
            l.add(ts.unknownType(position()));
        }

        ConstructorInstance ci =
                ts.constructorInstance(position(),
                                       tb.currentClass(),
                                       Flags.NONE,
                                       l,
                                       Collections.<Type> emptyList());
        n = constructorInstance(n, ci);

        if (n.body() != null) {
            /*
            // let's get a type builder that is prepared to visit the
            // body; tb wants to bypass it, due to the buildTypesEnter method.
            TypeBuilder bodyTB = (TypeBuilder)tb.visitChildren();
            
            // push an anonymous class on the stack.
            bodyTB = bodyTB.pushAnonClass(position());
            
            n = (New_c) n.body((ClassBody)n.body().visit(bodyTB));
            ParsedClassType type = (ParsedClassType) bodyTB.currentClass();
             */
            ParsedClassType type = tb.anonClass();
            n = anonType(n, type);

            type.setMembersAdded(true);

            //            n = n.addTypeBelow(type);
        }

        n = type(n, ts.unknownType(position()));
        return n;
    }

    @Override
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
            throws SemanticException {
        New_c nn = this;

        BodyDisambiguator bd = new BodyDisambiguator(ar);
        NodeVisitor childv = bd.enter(parent, this);

        if (childv instanceof PruningVisitor) return nn;

        BodyDisambiguator childbd = (BodyDisambiguator) childv;

        // Disambiguate the qualifier and object type, if possible.
        if (qualifier == null) {
            TypeNode objectType = visitChild(this.objectType, childbd);
            nn = objectType(nn, objectType);

            if (!objectType.isDisambiguated()) return nn;

            if (objectType.type().isClass()) {
                ClassType ct = objectType.type().toClass();

                if (ct.isInnerClass() && !ct.isLocal()) {
                    Expr qualifier =
                            visitChild(ar.lang().findQualifier(this, ar, ct),
                                       childbd);
                    nn = qualifier(nn, qualifier);
                    nn = qualifierImplicit(nn, true);
                }
            }
        }
        else {
            Expr qualifier = visitChild(this.qualifier, childbd);
            nn = qualifier(nn, qualifier);

            TypeNode objectType = this.objectType;
            if (objectType instanceof Ambiguous) {
                if (!qualifier.isTypeChecked()) return nn;
                if (!qualifier.type().isClass()) {
                    throw new SemanticException("Cannot instantiate member class of non-class type.",
                                                nn.position());
                }

                ClassType outer = qualifier.type().toClass();

                // We have to disambiguate the type node as if it were a member of the
                // static type, outer, of the qualifier.  For Java this is simple: type
                // nested type is just a name and we
                // use that name to lookup a member of the outer class.  For some
                // extensions (e.g., PolyJ), the type node may be more complex than
                // just a name.  We'll just punt here and let the extensions handle
                // this complexity.
                TypeNode tn =
                        ar.lang().findQualifiedTypeNode(this,
                                                        ar,
                                                        outer,
                                                        objectType);
                nn = objectType(nn, tn);
            }
            else if (!objectType.isDisambiguated()) {
                // not yet disambiguated.
                return nn;
            }
            else {
                // already disambiguated
            }
        }

        // Now disambiguate the actuals.
        nn = arguments(nn, visitList(arguments, childbd));

        if (body != null) {
            TypeNode objectType = nn.objectType;
            if (!objectType.isDisambiguated()) return nn;

            ClassType ct = objectType.type().toClass();
            ParsedClassType anonType = nn.anonType();

            if (anonType != null && !anonType.supertypesResolved()) {
                if (!ct.flags().isInterface()) {
                    anonType.superType(ct);
                }
                else {
                    anonType.superType(ar.typeSystem().Object());
                    anonType.addInterface(ct);
                }

                anonType.setSupertypesResolved(true);
            }

            SupertypeDisambiguator supDisamb =
                    new SupertypeDisambiguator(childbd);
            nn = body(nn, nn.visitChild(nn.body(), supDisamb));

            SignatureDisambiguator sigDisamb =
                    new SignatureDisambiguator(childbd);
            nn = body(nn, nn.visitChild(nn.body(), sigDisamb));

            // Now visit the body.
            nn = body(nn, nn.visitChild(nn.body(), childbd));
        }

        nn = (New_c) bd.leave(parent, this, nn, childbd);

        return nn;
    }

    @Override
    public TypeNode findQualifiedTypeNode(AmbiguityRemover ar, ClassType outer,
            TypeNode objectType) throws SemanticException {
        TypeSystem ts = ar.typeSystem();
        NodeFactory nf = ar.nodeFactory();
        Context c = ar.context();
        String name = objectType.name();
        ClassType ct = ts.findMemberClass(outer, name, c.currentClass());
        return nf.CanonicalTypeNode(objectType.position(), ct);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        // Everything is done in disambiguateOverride.
        return this;
    }

    @Override
    public Expr findQualifier(AmbiguityRemover ar, ClassType ct)
            throws SemanticException {
        NodeFactory nf = ar.nodeFactory();
        Context c = ar.context();

        // See JLS 2nd Ed. | 15.9.2.
        if (c.inStaticContext()) {
            if (body != null)
                return null;
            else throw new SemanticException("Inner class " + ct
                    + " cannot be instantiated in a static context.",
                                             position());
        }

        // If we're instantiating a non-static member class, add a "this"
        // qualifier.

        // Search for the outer class of the member.  The outer class is
        // not just ct.outer(); it may be a subclass of ct.outer().
        Type outer = ar.lang().findEnclosingClass(this, c, ct);

        if (outer == null) {
            throw new SemanticException("Could not find non-static member class \""
                    + ct.name() + "\".B" + this, position());
        }

        // Create the qualifier.
        Expr q;

        if (outer.equals(c.currentClass())) {
            q = nf.This(position().startOf());
        }
        else {
            q = nf.This(position().startOf(),
                        nf.CanonicalTypeNode(position(), outer));
        }
        q = q.type(outer);
        return q;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        List<Type> argTypes = new ArrayList<>(arguments.size());

        for (Expr e : arguments) {
            argTypes.add(e.type());
        }

        if (!objectType.type().isClass()) {
            throw new SemanticException("Must have a class for a new expression.",
                                        this.position());
        }

        tc.lang().typeCheckFlags(this, tc);
        tc.lang().typeCheckNested(this, tc);

        if (body != null) {
            ts.checkClassConformance(anonType);
        }

        ClassType ct = objectType.type().toClass();

        if (!ct.flags().isInterface()) {
            Context c = tc.context();
            if (anonType != null) {
                c = c.pushClass(anonType, anonType);
            }
            ci = ts.findConstructor(ct,
                                    argTypes,
                                    c.currentClass(),
                                    body == null);
        }
        else {
            ci = ts.defaultConstructor(this.position(), ct);
        }

        New_c n = constructorInstance(this, ci);

        if (anonType != null) {
            // The type of the new expression is the anonymous type, not the base type.
            ct = anonType;
        }
        else {
            if (ci.flags().isProtected()) {
                // A protected constructor C(...) can be accessed by new C(...)
                // only from within the package it which it is defined.
                // See JLS 2nd Ed. | 6.6.2.2.
                ClassType contextClass = tc.context().currentClass();
                if (!ts.equals(contextClass.package_(), ct.package_())) {
                    throw new SemanticException("Constructor " + ci.signature()
                            + " is inaccessible from class " + contextClass,
                                                position());
                }
            }
        }

        return type(n, ct);
    }

    @Override
    public void typeCheckNested(TypeChecker tc) throws SemanticException {
        ClassType ct = objectType.type().toClass();
        if (!ct.isMember()) {
            return;
        }
        // ct is a member class.
        if (ct.isInnerClass()) {
            // ct is an inner class
            ClassType qualifierClassType;
            if (qualifier != null) {
                // Get the qualifier type first.
                Type qt = qualifier.type();

                if (!qt.isClass()) {
                    throw new SemanticException("Cannot instantiate member class of a non-class type.",
                                                qualifier.position());
                }
                qualifierClassType = qt.toClass();
            }
            else {
                qualifierClassType =
                        tc.lang().findEnclosingClass(this, tc.context(), ct);
            }
            if (qualifierClassType == null) {
                throw new SemanticException("Could not find non-static member class \""
                        + ct.name() + "\".A" + this, position());
            }
        }
        else {
            // ct is a nested class, not an inner class
            // mustn't have a qualifier
            if (qualifier != null) {
                throw new SemanticException("Cannot provide a containing instance for non-inner class "
                        + ct.fullName() + ".", qualifier.position());
            }
            // make sure that all enclosing classes are static.
            for (ClassType t = ct; t.isMember(); t = t.outer()) {
                if (!t.flags().isStatic()) {
                    throw new SemanticException("Cannot allocate non-static member class \""
                            + t + "\".", position());
                }
            }

        }
    }

    @Override
    public ClassType findEnclosingClass(Context c, ClassType ct) {
        if (anonType != null) {
            // we need to find ct, is an anonymous class, and so
            // the enclosing class is the current class.
            return c.currentClass();
        }

        ClassType t = c.currentClass();
        TypeSystem ts = ct.typeSystem();
        String name = ct.name();
        while (t != null) {
            try {
                // HACK: PolyJ outer() doesn't work
                t = ts.staticTarget(t).toClass();
                ClassType mt = ts.findMemberClass(t, name, c.currentClass());

                if (ts.isSubtype(mt, ct)) {
                    return t;
                }
            }
            catch (SemanticException e) {
            }

            t = t.outer();
        }

        return null;
    }

    @Override
    public void typeCheckFlags(TypeChecker tc) throws SemanticException {
        if (objectType.type().isClass()) {
            typeCheckFlags(tc, objectType.type().toClass().flags());
        }
    }

    protected void typeCheckFlags(TypeChecker tc, Flags classFlags)
            throws SemanticException {

        if (body == null) {
            if (classFlags.isInterface()) {
                throw new SemanticException("Cannot instantiate an interface.",
                                            position());
            }

            if (classFlags.isAbstract()) {
                throw new SemanticException("Cannot instantiate an abstract class.",
                                            position());
            }
        }
        else {
            if (classFlags.isFinal()) {
                throw new SemanticException("Cannot create an anonymous subclass of a final class.",
                                            position());
            }

            if (classFlags.isInterface() && !arguments.isEmpty()) {
                throw new SemanticException("Cannot pass arguments to an anonymous class that "
                        + "implements an interface.",
                                            arguments.get(0).position());
            }
        }
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        if (child == qualifier) {
            ReferenceType t = ci.container();

            if (t.isClass() && t.toClass().isMember()) {
                t = t.toClass().container();
                return t;
            }

            return child.type();
        }

        Iterator<Expr> i = arguments.iterator();
        Iterator<? extends Type> j = ci.formalTypes().iterator();

        while (i.hasNext() && j.hasNext()) {
            Expr e = i.next();
            Type t = j.next();

            if (e == child) {
                return t;
            }
        }

        return child.type();
    }

    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        // something didn't work in the type check phase, so just ignore it.
        if (ci == null) {
            throw new InternalCompilerError(position(),
                                            "Null constructor instance after type check.");
        }

        for (Type t : ci.throwTypes()) {
            ec.throwsException(t, position());
        }

        return super.exceptionCheck(ec);
    }

    @Override
    public NodeVisitor extRewriteEnter(ExtensionRewriter rw)
            throws SemanticException {
        if (isQualifierImplicit()) {
            // don't translate the qualifier
            return rw.bypass(qualifier());
        }
        return super.extRewriteEnter(rw);
    }

    @Override
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        New_c n = (New_c) super.extRewrite(rw);
        n = constructorInstance(n, null);
        n = anonType(n, null);
        if (isQualifierImplicit()) {
            n = qualifier(n, null);
        }
        return n;
    }

    @Override
    public Precedence precedence() {
        return Precedence.LITERAL;
    }

    @Override
    public String toString() {
        return (qualifier != null ? qualifier.toString() + "." : "") + "new "
                + objectType + "(...)" + (body != null ? " " + body : "");
    }

    @Override
    public void printQualifier(CodeWriter w, PrettyPrinter tr) {
        if (qualifier != null && !qualifierImplicit) {
            printSubExpr(qualifier, w, tr);
            w.write(".");
        }
    }

    @Override
    public void printShortObjectType(CodeWriter w, PrettyPrinter tr) {
        w.write(objectType.name());
    }

    @Override
    public void printArgs(CodeWriter w, PrettyPrinter tr) {
        w.write("(");
        w.allowBreak(2, 2, "", 0);
        w.begin(0);

        for (Iterator<Expr> i = arguments.iterator(); i.hasNext();) {
            Expr e = i.next();

            w.begin(2);
            print(e, w, tr);
            w.end();

            if (i.hasNext()) {
                w.write(",");
                w.allowBreak(0);
            }
        }

        w.end();
        w.write(")");
    }

    @Override
    public void printBody(CodeWriter w, PrettyPrinter tr) {
        if (body != null) {
            w.write(" {");
            print(body, w, tr);
            w.write("}");
        }
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.begin(0);
        ((JLang) tr.lang()).printQualifier(this, w, tr);
        w.write("new ");

        // We need to be careful when pretty printing "new" expressions for
        // member classes.  For the expression "e.new C()" where "e" has
        // static type "T", the TypeNode for "C" is actually the type "T.C".
        // But, if we print "T.C", the post compiler will try to lookup "T"
        // in "T".  Instead, we print just "C".
        if (qualifier != null && !qualifierImplicit) {
            ((JLang) tr.lang()).printShortObjectType(this, w, tr);
        }
        else {
            print(objectType, w, tr);
        }

        ((JLang) tr.lang()).printArgs(this, w, tr);
        ((JLang) tr.lang()).printBody(this, w, tr);
        w.end();
    }

    @Override
    public Term firstChild() {
        return qualifier != null ? (Term) qualifier : objectType;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (qualifier != null) {
            v.visitCFG(qualifier, objectType, ENTRY);
        }

        if (body != null) {
            v.visitCFG(objectType, listChild(arguments, body), ENTRY);
            v.visitCFGList(arguments, body, ENTRY);
            v.visitCFG(body, this, EXIT);
        }
        else {
            if (!arguments.isEmpty()) {
                v.visitCFG(objectType, listChild(arguments, null), ENTRY);
                v.visitCFGList(arguments, this, EXIT);
            }
            else {
                v.visitCFG(objectType, this, EXIT);
            }
        }

        return succs;
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        List<Type> l = new LinkedList<>();
        l.addAll(ci.throwTypes());
        l.addAll(ts.uncheckedExceptions());
        return l;
    }

    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc)
            throws SemanticException {
        New_c nn = this;
        New old = nn;

        NodeVisitor childv = tc.enter(parent, this);

        if (childv instanceof PruningVisitor) {
            return nn;
        }

        TypeChecker childtc = (TypeChecker) childv;

        if (nn.qualifier() != null) {
            nn = qualifier(nn, nn.visitChild(nn.qualifier(), childtc));

            if (!nn.qualifier().type().isCanonical()) {
                return nn;
            }

            // Force the object type and class body, if any, to be disambiguated.
            BodyDisambiguator bd = new BodyDisambiguator(tc);
            nn = bd.visitEdge(parent, nn);

            if (!nn.objectType().isDisambiguated()) {
                return nn;
            }
        }

        // Now type check the rest of the children.
        nn = objectType(nn, nn.visitChild(nn.objectType(), childtc));
        if (!nn.objectType().type().isCanonical()) return nn;

        nn = arguments(nn, nn.visitList(nn.arguments(), childtc));
        nn = body(nn, nn.visitChild(nn.body(), childtc));
        nn = (New_c) tc.leave(parent, old, nn, childtc);

        ConstantChecker cc =
                new ConstantChecker(tc.job(),
                                    tc.typeSystem(),
                                    tc.nodeFactory());
        cc = (ConstantChecker) cc.context(childtc.context());
        nn = (New_c) tc.lang().checkConstants(nn, cc);

        return nn;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.New(position, qualifier, objectType, arguments, body);
    }

}
