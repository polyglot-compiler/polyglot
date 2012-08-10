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
 * A <code>New</code> is an immutable representation of the use of the
 * <code>new</code> operator to create a new instance of a class.  In
 * addition to the type of the class being created, a <code>New</code> has a
 * list of arguments to be passed to the constructor of the object and an
 * optional <code>ClassBody</code> used to support anonymous classes.
 */
public class New_c extends Expr_c implements New {
    protected Expr qualifier;
    protected TypeNode tn;
    protected List<Expr> arguments;
    protected ClassBody body;
    protected ConstructorInstance ci;
    protected ParsedClassType anonType;

    public New_c(Position pos, Expr qualifier, TypeNode tn,
            List<Expr> arguments, ClassBody body) {
        super(pos);
        assert (tn != null && arguments != null); // qualifier and body may be null
        this.qualifier = qualifier;
        this.tn = tn;
        this.arguments = ListUtil.copy(arguments, true);
        this.body = body;
    }

    /** Get the qualifier expression of the allocation. */
    @Override
    public Expr qualifier() {
        return this.qualifier;
    }

    /** Set the qualifier expression of the allocation. */
    @Override
    public New qualifier(Expr qualifier) {
        New_c n = (New_c) copy();
        n.qualifier = qualifier;
        return n;
    }

    /** Get the type we are instantiating. */
    @Override
    public TypeNode objectType() {
        return this.tn;
    }

    /** Set the type we are instantiating. */
    @Override
    public New objectType(TypeNode tn) {
        New_c n = (New_c) copy();
        n.tn = tn;
        return n;
    }

    @Override
    public ParsedClassType anonType() {
        return this.anonType;
    }

    @Override
    public New anonType(ParsedClassType anonType) {
        if (anonType == this.anonType) return this;
        New_c n = (New_c) copy();
        n.anonType = anonType;
        return n;
    }

    @Override
    public ProcedureInstance procedureInstance() {
        return constructorInstance();
    }

    @Override
    public ConstructorInstance constructorInstance() {
        return this.ci;
    }

    @Override
    public New constructorInstance(ConstructorInstance ci) {
        if (ci == this.ci) return this;
        New_c n = (New_c) copy();
        n.ci = ci;
        return n;
    }

    @Override
    public List<Expr> arguments() {
        return this.arguments;
    }

    @Override
    public ProcedureCall arguments(List<Expr> arguments) {
        New_c n = (New_c) copy();
        n.arguments = ListUtil.copy(arguments, true);
        return n;
    }

    @Override
    public ClassBody body() {
        return this.body;
    }

    @Override
    public New body(ClassBody body) {
        New_c n = (New_c) copy();
        n.body = body;
        return n;
    }

    /** Reconstruct the expression. */
    protected New_c reconstruct(Expr qualifier, TypeNode tn,
            List<Expr> arguments, ClassBody body) {
        if (qualifier != this.qualifier || tn != this.tn
                || !CollectionUtil.equals(arguments, this.arguments)
                || body != this.body) {
            New_c n = (New_c) copy();
            n.tn = tn;
            n.qualifier = qualifier;
            n.arguments = ListUtil.copy(arguments, true);
            n.body = body;
            return n;
        }

        return this;
    }

    /** Visit the children of the expression. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr qualifier = (Expr) visitChild(this.qualifier, v);
        TypeNode tn = (TypeNode) visitChild(this.tn, v);
        List<Expr> arguments = visitList(this.arguments, v);
        ClassBody body = (ClassBody) visitChild(this.body, v);
        return reconstruct(qualifier, tn, arguments, body);
    }

    @Override
    public Context enterChildScope(Node child, Context c) {
        if (child == body && anonType != null && body != null) {
            c = c.pushClass(anonType, anonType);
        }
        return super.enterChildScope(child, c);
    }

    @Override
    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
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

        List<Type> l = new ArrayList<Type>(n.arguments.size());
        for (int i = 0; i < n.arguments.size(); i++) {
            l.add(ts.unknownType(position()));
        }

        ConstructorInstance ci =
                ts.constructorInstance(position(),
                                       tb.currentClass(),
                                       Flags.NONE,
                                       l,
                                       Collections.<Type> emptyList());
        n = (New_c) n.constructorInstance(ci);

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
            ParsedClassType type = tb.currentClass();
            n = (New_c) n.anonType(type);

            type.setMembersAdded(true);

            //            n = n.addTypeBelow(type);
        }

        return n.type(ts.unknownType(position()));
    }

    @Override
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
            throws SemanticException {
        New nn = this;
        New old = nn;

        BodyDisambiguator bd = new BodyDisambiguator(ar);
        NodeVisitor childv = bd.enter(parent, this);

        if (childv instanceof PruningVisitor) {
            return nn;
        }

        BodyDisambiguator childbd = (BodyDisambiguator) childv;

        // Disambiguate the qualifier and object type, if possible.
        if (nn.qualifier() == null) {
            nn =
                    nn.objectType((TypeNode) nn.visitChild(nn.objectType(),
                                                           childbd));
            if (childbd.hasErrors()) throw new SemanticException();

            if (!nn.objectType().isDisambiguated()) {
                return nn;
            }

            if (nn.objectType().type().isClass()) {
                ClassType ct = nn.objectType().type().toClass();

                if (ct.isMember() && !ct.flags().isStatic()) {
                    nn = ((New_c) nn).findQualifier(ar, ct);

                    nn =
                            nn.qualifier((Expr) nn.visitChild(nn.qualifier(),
                                                              childbd));
                    if (childbd.hasErrors()) throw new SemanticException();
                }
            }
        }
        else {
            nn = nn.qualifier((Expr) nn.visitChild(nn.qualifier(), childbd));
            if (childbd.hasErrors()) throw new SemanticException();

            if (nn.objectType() instanceof Ambiguous) {

                // We have to disambiguate the type node as if it were a member of the
                // static type, outer, of the qualifier.  For Java this is simple: type
                // nested type is just a name and we
                // use that name to lookup a member of the outer class.  For some
                // extensions (e.g., PolyJ), the type node may be more complex than
                // just a name.  We'll just punt here and let the extensions handle
                // this complexity.

                String name = nn.objectType().name();

                if (nn.qualifier().isDisambiguated()
                        && nn.qualifier().type() != null
                        && nn.qualifier().type().isCanonical()) {
                    TypeSystem ts = ar.typeSystem();
                    NodeFactory nf = ar.nodeFactory();
                    Context c = ar.context();

                    if (!nn.qualifier().type().isClass()) {
                        throw new SemanticException("Cannot instantiate member class of non-class type.",
                                                    nn.position());
                    }

                    ClassType outer = nn.qualifier().type().toClass();
                    ClassType ct =
                            ts.findMemberClass(outer, name, c.currentClass());
                    TypeNode tn =
                            nf.CanonicalTypeNode(nn.objectType().position(), ct);
                    nn = nn.objectType(tn);
                }
            }
            else if (!nn.objectType().isDisambiguated()) {
                // not yet disambiguated.
                return nn;
            }
            else {
                // already disambiguated
            }
        }

        // Now disambiguate the actuals.
        nn = (New) nn.arguments(nn.visitList(nn.arguments(), childbd));
        if (childbd.hasErrors()) throw new SemanticException();

        if (nn.body() != null) {
            if (!nn.objectType().isDisambiguated()) {
                return nn;
            }

            ClassType ct = nn.objectType().type().toClass();

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
            nn = nn.body((ClassBody) nn.visitChild(nn.body(), supDisamb));
            if (supDisamb.hasErrors()) throw new SemanticException();

            SignatureDisambiguator sigDisamb =
                    new SignatureDisambiguator(childbd);
            nn = nn.body((ClassBody) nn.visitChild(nn.body(), sigDisamb));
            if (sigDisamb.hasErrors()) throw new SemanticException();

            // Now visit the body.
            nn = nn.body((ClassBody) nn.visitChild(nn.body(), childbd));
            if (childbd.hasErrors()) throw new SemanticException();
        }

        nn = (New) bd.leave(parent, old, nn, childbd);

        return nn;
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        // Everything is done in disambiguateOverride.
        return this;
    }

    /**
     * @param ar
     * @param ct
     * @throws SemanticException
     */
    protected New findQualifier(AmbiguityRemover ar, ClassType ct)
            throws SemanticException {
        // If we're instantiating a non-static member class, add a "this"
        // qualifier.
        NodeFactory nf = ar.nodeFactory();
        Context c = ar.context();

        // Search for the outer class of the member.  The outer class is
        // not just ct.outer(); it may be a subclass of ct.outer().
        Type outer = findEnclosingClass(c, ct);

        if (outer == null) {
            throw new SemanticException("Could not find non-static member class \""
                                                + ct.name() + "\".",
                                        position());
        }

        // Create the qualifier.
        Expr q;

        if (outer.equals(c.currentClass())) {
            q = nf.This(position().startOf());
        }
        else {
            q =
                    nf.This(position().startOf(),
                            nf.CanonicalTypeNode(position(), outer));
        }

        q = q.type(outer);
        return qualifier(q);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        List<Type> argTypes = new ArrayList<Type>(arguments.size());

        for (Expr e : arguments) {
            argTypes.add(e.type());
        }

        if (!tn.type().isClass()) {
            throw new SemanticException("Must have a class for a new expression.",
                                        this.position());
        }

        typeCheckFlags(tc);
        typeCheckNested(tc);

        if (this.body != null) {
            ts.checkClassConformance(anonType);
        }

        ClassType ct = tn.type().toClass();

        if (!ct.flags().isInterface()) {
            Context c = tc.context();
            if (anonType != null) {
                c = c.pushClass(anonType, anonType);
            }
            ci = ts.findConstructor(ct, argTypes, c.currentClass());
        }
        else {
            ci = ts.defaultConstructor(this.position(), ct);
        }

        New n = this.constructorInstance(ci);

        if (anonType != null) {
            // The type of the new expression is the anonymous type, not the base type.
            ct = anonType;
        }

        return n.type(ct);
    }

    protected void typeCheckNested(TypeChecker tc) throws SemanticException {
        ClassType ct = tn.type().toClass();
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
                qualifierClassType = findEnclosingClass(tc.context(), ct);
            }
            if (qualifierClassType == null) {
                throw new SemanticException("Could not find non-static member class \""
                                                    + ct.name() + "\".",
                                            position());
            }
        }
        else {
            // ct is a nested class, not an inner class
            // mustn't have a qualifier
            if (qualifier != null) {
                throw new SemanticException("Cannot provide a containing instance for non-inner class "
                                                    + ct.fullName() + ".",
                                            qualifier.position());
            }
            // make sure that all enclosing classes are static.
            for (ClassType t = ct; t.isMember(); t = t.outer()) {
                if (!t.flags().isStatic()) {
                    throw new SemanticException("Cannot allocate non-static member class \""
                                                        + t + "\".",
                                                position());
                }
            }

        }
    }

    protected ClassType findEnclosingClass(Context c, ClassType ct) {
        if (ct == anonType) {
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

                if (ts.equals(mt, ct)) {
                    return t;
                }
            }
            catch (SemanticException e) {
            }

            t = t.outer();
        }

        return null;
    }

    protected void typeCheckFlags(TypeChecker tc) throws SemanticException {
        if (tn.type().isClass()) {
            typeCheckFlags(tc, tn.type().toClass().flags());
        }
    }

    protected void typeCheckFlags(TypeChecker tc, Flags classFlags)
            throws SemanticException {

        if (this.body == null) {
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

        Iterator<Expr> i = this.arguments.iterator();
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

    /** Get the precedence of the expression. */
    @Override
    public Precedence precedence() {
        return Precedence.LITERAL;
    }

    @Override
    public String toString() {
        return (qualifier != null ? (qualifier.toString() + ".") : "") + "new "
                + tn + "(...)" + (body != null ? " " + body : "");
    }

    protected void printQualifier(CodeWriter w, PrettyPrinter tr) {
        if (qualifier != null) {
            print(qualifier, w, tr);
            w.write(".");
        }
    }

    protected void printArgs(CodeWriter w, PrettyPrinter tr) {
        w.write("(");
        w.allowBreak(2, 2, "", 0);
        w.begin(0);

        for (Iterator<Expr> i = arguments.iterator(); i.hasNext();) {
            Expr e = i.next();

            print(e, w, tr);

            if (i.hasNext()) {
                w.write(",");
                w.allowBreak(0);
            }
        }

        w.end();
        w.write(")");
    }

    protected void printBody(CodeWriter w, PrettyPrinter tr) {
        if (body != null) {
            w.write(" {");
            print(body, w, tr);
            w.write("}");
        }
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        printQualifier(w, tr);
        w.write("new ");

        // We need to be careful when pretty printing "new" expressions for
        // member classes.  For the expression "e.new C()" where "e" has
        // static type "T", the TypeNode for "C" is actually the type "T.C".
        // But, if we print "T.C", the post compiler will try to lookup "T"
        // in "T".  Instead, we print just "C".
        if (qualifier != null) {
            w.write(tn.name());
        }
        else {
            print(tn, w, tr);
        }

        printArgs(w, tr);
        printBody(w, tr);
    }

    @Override
    public Term firstChild() {
        return qualifier != null ? (Term) qualifier : tn;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (qualifier != null) {
            v.visitCFG(qualifier, tn, ENTRY);
        }

        if (body() != null) {
            v.visitCFG(tn, listChild(arguments, body()), ENTRY);
            v.visitCFGList(arguments, body(), ENTRY);
            v.visitCFG(body(), this, EXIT);
        }
        else {
            if (!arguments.isEmpty()) {
                v.visitCFG(tn, listChild(arguments, (Expr) null), ENTRY);
                v.visitCFGList(arguments, this, EXIT);
            }
            else {
                v.visitCFG(tn, this, EXIT);
            }
        }

        return succs;
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        List<Type> l = new LinkedList<Type>();
        l.addAll(ci.throwTypes());
        l.addAll(ts.uncheckedExceptions());
        return l;
    }

    /**
     * @param parent
     * @param tc
     */
    @Override
    public Node typeCheckOverride(Node parent, TypeChecker tc)
            throws SemanticException {
        New nn = this;
        New old = nn;

        BodyDisambiguator bd = new BodyDisambiguator(tc);
        NodeVisitor childv = tc.enter(parent, this);

        if (childv instanceof PruningVisitor) {
            return nn;
        }

        TypeChecker childtc = (TypeChecker) childv;

        if (nn.qualifier() != null) {
            nn = nn.qualifier((Expr) nn.visitChild(nn.qualifier(), childtc));
            if (childtc.hasErrors()) throw new SemanticException();

            if (!nn.qualifier().type().isCanonical()) {
                return nn;
            }

            // Force the object type and class body, if any, to be disambiguated.
            nn = bd.visitEdge(parent, nn);
            if (bd.hasErrors()) throw new SemanticException();

            if (!nn.objectType().isDisambiguated()) {
                return nn;
            }
        }

        // Now type check the rest of the children.
        nn = nn.objectType((TypeNode) nn.visitChild(nn.objectType(), childtc));
        if (childtc.hasErrors()) throw new SemanticException();

        if (!nn.objectType().type().isCanonical()) {
            return nn;
        }

        nn = (New) nn.arguments(nn.visitList(nn.arguments(), childtc));
        if (childtc.hasErrors()) throw new SemanticException();

        nn = nn.body((ClassBody) nn.visitChild(nn.body(), childtc));
        if (childtc.hasErrors()) throw new SemanticException();

        nn = (New) tc.leave(parent, old, nn, childtc);

        ConstantChecker cc =
                new ConstantChecker(tc.job(), tc.typeSystem(), tc.nodeFactory());
        cc = (ConstantChecker) cc.context(childtc.context());
        nn = (New) nn.del().checkConstants(cc);

        return nn;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.New(this.position,
                      this.qualifier,
                      this.tn,
                      this.arguments,
                      this.body);
    }

}
