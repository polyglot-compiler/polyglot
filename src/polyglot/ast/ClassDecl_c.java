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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import polyglot.frontend.MissingDependencyException;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.Goal;
import polyglot.main.Report;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.MemberInstance;
import polyglot.types.MethodInstance;
import polyglot.types.Named;
import polyglot.types.NoClassException;
import polyglot.types.ParsedClassType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Copy;
import polyglot.util.InternalCompilerError;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A {@code ClassDecl} is the definition of a class, abstract class,
 * or interface. It may be a public or other top-level class, or an inner
 * named class, or an anonymous class.
 */
public class ClassDecl_c extends Term_c implements ClassDecl, ClassDeclOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Flags flags;
    protected Id name;
    protected TypeNode superClass;
    protected List<TypeNode> interfaces;
    protected ClassBody body;
    protected ConstructorInstance defaultCI;
    protected boolean implicitMembersAdded;

    protected ParsedClassType type;

    protected Javadoc javadoc;

    /**
     * @deprecated Use constructor with Javadoc
     */
    @Deprecated
    public ClassDecl_c(Position pos, Flags flags, Id name, TypeNode superClass,
            List<TypeNode> interfaces, ClassBody body) {
        this(pos, flags, name, superClass, interfaces, body, null, null);
    }

//    @Deprecated
    public ClassDecl_c(Position pos, Flags flags, Id name, TypeNode superClass,
            List<TypeNode> interfaces, ClassBody body, Javadoc javadoc) {
        this(pos, flags, name, superClass, interfaces, body, javadoc, null);
    }

    /**
     * @deprecated Use constructor with Javadoc
     */
    @Deprecated
    public ClassDecl_c(Position pos, Flags flags, Id name, TypeNode superClass,
            List<TypeNode> interfaces, ClassBody body, Ext ext) {
        this(pos, flags, name, superClass, interfaces, body, null, ext);
    }

    public ClassDecl_c(Position pos, Flags flags, Id name, TypeNode superClass,
            List<TypeNode> interfaces, ClassBody body, Javadoc javadoc,
            Ext ext) {
        super(pos, ext);
        assert flags != null && name != null && interfaces != null
                && body != null; // superClass may be null, interfaces may be empty
        this.flags = flags;
        this.name = name;
        this.superClass = superClass;
        this.interfaces = ListUtil.copy(interfaces, true);
        this.body = body;
        this.javadoc = javadoc;
    }

    @Override
    public boolean isDisambiguated() {
        return super.isDisambiguated() && type != null && type.isCanonical()
                && type.supertypesResolved() && type.signaturesResolved();
    }

    @Override
    public MemberInstance memberInstance() {
        return type;
    }

    @Override
    public ParsedClassType type() {
        return type;
    }

    @Override
    public ClassDecl type(ParsedClassType type) {
        return type(this, type);
    }

    protected <N extends ClassDecl_c> N type(N n, ParsedClassType type) {
        if (n.type == type) return n;
        n = copyIfNeeded(n);
        n.type = type;
        return n;
    }

    @Override
    public Flags flags() {
        return flags;
    }

    @Override
    public ClassDecl flags(Flags flags) {
        return flags(this, flags);
    }

    protected <N extends ClassDecl_c> N flags(N n, Flags flags) {
        if (n.flags.equals(flags)) return n;
        n = copyIfNeeded(n);
        n.flags = flags;
        return n;
    }

    @Override
    public ClassDecl javadoc(Javadoc javadoc) {
        return javadoc(this, javadoc);
    }

    protected <N extends ClassDecl_c> N javadoc(N n, Javadoc javadoc) {
        if (n.javadoc == javadoc) return n;
        n = copyIfNeeded(n);
        n.javadoc = javadoc;
        return n;
    }

    @Override
    public Id id() {
        return name;
    }

    @Override
    public ClassDecl id(Id name) {
        return id(this, name);
    }

    protected <N extends ClassDecl_c> N id(N n, Id name) {
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
    public ClassDecl name(String name) {
        return id(this.name.id(name));
    }

    @Override
    public TypeNode superClass() {
        return superClass;
    }

    @Override
    public ClassDecl superClass(TypeNode superClass) {
        return superClass(this, superClass);
    }

    protected <N extends ClassDecl_c> N superClass(N n, TypeNode superClass) {
        if (n.superClass == superClass) return n;
        n = copyIfNeeded(n);
        n.superClass = superClass;
        return n;
    }

    @Override
    public List<TypeNode> interfaces() {
        return interfaces;
    }

    @Override
    public ClassDecl interfaces(List<TypeNode> interfaces) {
        return interfaces(this, interfaces);
    }

    protected <N extends ClassDecl_c> N interfaces(N n,
            List<TypeNode> interfaces) {
        if (CollectionUtil.equals(n.interfaces, interfaces)) return n;
        n = copyIfNeeded(n);
        n.interfaces = ListUtil.copy(interfaces, true);
        return n;
    }

    @Override
    public ClassBody body() {
        return body;
    }

    @Override
    public ClassDecl body(ClassBody body) {
        return body(this, body);
    }

    protected <N extends ClassDecl_c> N body(N n, ClassBody body) {
        if (n.body == body) return n;
        n = copyIfNeeded(n);
        n.body = body;
        return n;
    }

    protected <N extends ClassDecl_c> N reconstruct(N n, Id name,
            TypeNode superClass, List<TypeNode> interfaces, ClassBody body) {
        n = id(n, name);
        n = superClass(n, superClass);
        n = interfaces(n, interfaces);
        n = body(n, body);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Id name = visitChild(this.name, v);
        TypeNode superClass = visitChild(this.superClass, v);
        List<TypeNode> interfaces = visitList(this.interfaces, v);
        ClassBody body = visitChild(this.body, v);
        return reconstruct(this, name, superClass, interfaces, body);
    }

    @Override
    public NodeVisitor buildTypesEnter(TypeBuilder tb)
            throws SemanticException {
        tb = tb.pushClass(position(), flags, name.id());

        ParsedClassType type = tb.currentClass();

        // Member classes of interfaces are implicitly public and static.
        if (type.isMember() && type.outer().flags().isInterface()) {
            type.flags(type.flags().Public().Static());
        }

        // Member interfaces are implicitly static.
        if (type.isMember() && type.flags().isInterface()) {
            type.flags(type.flags().Static());
        }

        // Interfaces are implicitly abstract.
        if (type.flags().isInterface()) {
            type.flags(type.flags().Abstract());
        }

        return tb;
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        ParsedClassType type = tb.currentClass();

        if (type == null) {
            return this;
        }

        // Add a default constructor to the ClassType, but not to the
        // ClassDecl; that will be added later.
        ConstructorInstance ci = null;

        // Mark members added before adding the constructor
        // to prevent a MissingDependencyException.
        type.setMembersAdded(true);

        if (type.defaultConstructorNeeded()) {
            ci = tb.typeSystem().defaultConstructor(position(), type);
            type.addConstructor(ci);
        }

        ClassDecl_c n = this;

        // TODO remove if this is not needed
        if (n.defaultCI != ci) {
            n = Copy.Util.copy(n);
            n.defaultCI = ci;
        }

        n = type(n, type);
        n = flags(n, type.flags());
        return n;
    }

    @Override
    public Context enterChildScope(Node child, Context c) {
        if (child == body) {
            TypeSystem ts = c.typeSystem();
            c = c.pushClass(type, ts.staticTarget(type).toClass());
        }
        else {
            // Add this class to the context, but don't push a class scope.
            // This allows us to detect loops in the inheritance
            // hierarchy, but avoids an infinite loop.
            c = c.pushBlock();
            c.addNamed(type);
        }
        return super.enterChildScope(child, c);
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (type == null) {
            throw new InternalCompilerError("Missing type.", position());
        }
        TypeSystem ts = ar.typeSystem();

        ClassDecl_c n = disambiguateSupertypes(ar);
        checkSupertypeCycles(ts);

        ParsedClassType type = n.type();

        if (!type.signaturesResolved()) {
            Scheduler scheduler = ar.job().extensionInfo().scheduler();
            Goal g = scheduler.SignaturesResolved(type);
            throw new MissingDependencyException(g);
        }

        // Make sure that the inStaticContext flag of the class is correct.
        Context ctxt = ar.context();
        type.inStaticContext(ctxt.inStaticContext());

        if (!implicitMembersAdded && flags.isInterface()
                && interfaces.isEmpty()) {
            // See JLS 2nd Ed. | 9.2.
            // If an interface has no direct superinterfaces, then the interface
            // implicitly declares a public abstract member method corresponding
            // to each public instance method declared in Object, unless a
            // method with the same signature, same return type, and a
            // compatible throws clause is explicitly declared by the interface.
            List<? extends MethodInstance> objectMethods =
                    ts.Object().methods();
            List<MethodInstance> implicitlyDeclaredMethods =
                    new ArrayList<>(objectMethods.size());
            for (MethodInstance mi : objectMethods) {
                Flags flags = mi.flags();
                if (!flags.isPublic()) continue;
                boolean methodNeeded = true;
                List<? extends MethodInstance> mjs =
                        type.methods(mi.name(), mi.formalTypes());
                for (MethodInstance mj : mjs) {
                    // It follows that it is a compile-time error if the
                    // interface declares a method with the same signature
                    // and different return types or incompatible throws
                    // clause.
                    ts.checkOverride(mj, mi);
                    methodNeeded = false;
                }
                if (methodNeeded)
                    implicitlyDeclaredMethods.add(mi.container(type)
                                                    .flags(flags.Abstract()
                                                                .clearFinal()));
            }
            for (MethodInstance mi : implicitlyDeclaredMethods)
                type.addMethod(mi);
            implicitMembersAdded = true;
        }

        // FIXME: shouldn't reach MembersAdded(type) until here!
        return addDefaultConstructorIfNeeded(ts, ar.nodeFactory());
    }

    protected ClassDecl_c disambiguateSupertypes(AmbiguityRemover ar)
            throws SemanticException {
        boolean supertypesResolved = true;

//        System.out.println("  " + ar + ".disamsuper: " + this);

        if (!type.supertypesResolved()) {
            if (superClass != null && !superClass.isDisambiguated()) {
                supertypesResolved = false;
            }

            for (TypeNode tn : interfaces) {
                if (!tn.isDisambiguated()) {
                    supertypesResolved = false;
                }
            }

            if (!supertypesResolved) {
                Scheduler scheduler = ar.job().extensionInfo().scheduler();
                Goal g = scheduler.SupertypesResolved(type);
                throw new MissingDependencyException(g);
            }
            else {
                setSuperClass(ar, superClass);
                setInterfaces(ar, interfaces);
                type.setSupertypesResolved(true);
            }
        }

        return this;
    }

    protected void checkSupertypeCycles(TypeSystem ts)
            throws SemanticException {
        if (type.superType() != null) {
            if (!type.superType().isReference()) {
                throw new SemanticException("Cannot extend type "
                        + type.superType() + ".",
                                            superClass != null
                                                    ? superClass.position()
                                                    : position());
            }
            ReferenceType t = (ReferenceType) type.superType();
            ts.checkCycles(t);
        }

        for (Type it : type.interfaces()) {
            if (!it.isReference()) {
                String s = type.flags().isInterface() ? "extend" : "implement";
                throw new SemanticException("Cannot " + s + " type " + it + ".",
                                            position());
            }
            ReferenceType t = (ReferenceType) it;
            ts.checkCycles(t);
        }
    }

    /**
     * @throws SemanticException
     */
    protected void setSuperClass(AmbiguityRemover ar, TypeNode superClass)
            throws SemanticException {
        TypeSystem ts = ar.typeSystem();

        if (superClass != null) {
            Type t = superClass.type();
            if (Report.should_report(Report.types, 3))
                Report.report(3, "setting superclass of " + type + " to " + t);
            type.superType(t);
        }
        else if (type.equals(ts.Object())
                || type.fullName().equals(ts.Object().fullName())
                || flags.isInterface()) {
            // the type is an interface or ts.Object(), so it has no superclass.
            if (Report.should_report(Report.types, 3))
                Report.report(3,
                              "setting superclass of " + type + " to " + null);
            type.superType(null);
        }
        else {
            // the superclass was not specified, and the type is not the same
            // as ts.Object() (which is typically java.lang.Object)
            // As such, the default superclass is ts.Object().
            if (Report.should_report(Report.types,
                                     3))
                Report.report(3,
                              "setting superclass of " + type + " to "
                                      + ts.Object());
            type.superType(ts.Object());
        }
    }

    /**
     * @throws SemanticException
     */
    protected void setInterfaces(AmbiguityRemover ar,
            List<TypeNode> newInterfaces) throws SemanticException {
        for (TypeNode tn : newInterfaces) {
            ClassType t = (ClassType) tn.type();

            if (Report.should_report(Report.types, 3))
                Report.report(3, "adding interface of " + type + " to " + t);

            type.addInterface(t);
        }
    }

    protected Node addDefaultConstructorIfNeeded(TypeSystem ts, NodeFactory nf)
            throws SemanticException {
        if (defaultConstructorNeeded()) {
            return nf.lang().addDefaultConstructor(this, ts, nf, defaultCI);
        }
        return this;
    }

    protected boolean defaultConstructorNeeded() {
        if (defaultCI == null) {
            // It wasn't needed when we checked during buildTypes.
            return false;
        }

        // We added it to the type, check if it's in the class body.
        for (ClassMember cm : body().members()) {
            if (cm instanceof ConstructorDecl) {
                ConstructorDecl cd = (ConstructorDecl) cm;
                if (cd.constructorInstance() == defaultCI) {
                    // Already added
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public Node addDefaultConstructor(TypeSystem ts, NodeFactory nf,
            ConstructorInstance defaultCI) throws SemanticException {
        ConstructorInstance ci = defaultCI;
        if (ci == null) {
            throw new InternalCompilerError("addDefaultConstructor called without defaultCI set");
        }

        Block block = null;
        if (type.superType() instanceof ClassType) {
            ConstructorInstance sci =
                    ts.findConstructor((ClassType) type.superType(),
                                       Collections.<Type> emptyList(),
                                       type,
                                       false);

            ConstructorCall cc = nf.SuperCall(position().startOf(),
                                              Collections.<Expr> emptyList());
            cc = cc.constructorInstance(sci);
            block = nf.Block(position().startOf(), cc);
        }
        else {
            block = nf.Block(position().startOf());
        }
        ConstructorDecl cd =
                nf.ConstructorDecl(body().position().startOf(),
                                   Flags.PUBLIC,
                                   nf.Id(body().position().startOf(),
                                         name.id()),
                                   Collections.<Formal> emptyList(),
                                   Collections.<TypeNode> emptyList(),
                                   block);
        cd = cd.constructorInstance(ci);
        return body(body.addMember(cd));
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        if (this.type().isNested() && this.type() != null) {
            // The class cannot have the same simple name as any enclosing class.
            ClassType container = type.outer();

            while (container != null) {
                if (!container.isAnonymous()) {
                    String name = ((Named) container).name();

                    if (name.equals(this.name.id())) {
                        throw new SemanticException("Cannot declare member "
                                + "class \"" + type
                                + "\" inside class with the " + "same name.",
                                                    position());
                    }
                }
                if (container.isNested()) {
                    container = container.outer();
                }
                else {
                    break;
                }
            }

            if (this.type().isLocal()) {
                // a local class name cannot be redeclared within the same
                // method, constructor or initializer, and within its scope
                Context ctxt = tc.context();

                if (ctxt.isLocal(name.id())) {
                    // something with the same name was declared locally.
                    // (but not in an enclosing class)
                    try {
                        Named nm = ctxt.find(name.id());
                        if (nm instanceof Type) {
                            Type another = (Type) nm;
                            if (another.isClass()
                                    && another.toClass().isLocal()) {
                                throw new SemanticException("Cannot declare local "
                                        + "class \"" + type
                                        + "\" within the same "
                                        + "method, constructor or initializer as another "
                                        + "local class of the same name.",
                                                            position());
                            }
                        }
                    }
                    catch (NoClassException e) {
                    }
                }
            }
        }

        // check that inner classes do not declare member interfaces
        if (type().isMember() && flags().isInterface()
                && type().outer().isInnerClass()) {
            // it's a member interface in an inner class.
            throw new SemanticException("Inner classes cannot declare "
                    + "member interfaces.", this.position());
        }

        // Make sure that static members are not declared inside inner classes
        if (type().isMember() && type().flags().isStatic()
                && type().outer().isInnerClass()) {
            throw new SemanticException("Inner classes cannot declare static "
                    + "member classes.", position());
        }

        if (type.superType() != null) {
            if (!type.superType().isClass()
                    || type.superType().toClass().flags().isInterface()) {
                throw new SemanticException("Cannot extend non-class \""
                        + type.superType() + "\".", position());
            }

            if (type.superType().toClass().flags().isFinal()) {
                throw new SemanticException("Cannot extend final class \""
                        + type.superType() + "\".", position());
            }

            if (type.equals(tc.typeSystem().Object())
                    || type.fullName()
                           .equals(tc.typeSystem().Object().fullName())) {
                throw new SemanticException("Class \"" + type
                        + "\" cannot have a superclass.",
                                            superClass.position());
            }
        }

        Set<Type> superInterfaces = new HashSet<>();
        for (TypeNode tn : interfaces) {
            Type t = tn.type();

            if (!t.isClass() || !t.toClass().flags().isInterface()) {
                throw new SemanticException("Superinterface " + t + " of "
                        + type + " is not an interface.", tn.position());
            }

            if (type.equals(tc.typeSystem().Object())
                    || type.fullName()
                           .equals(tc.typeSystem().Object().fullName())) {
                throw new SemanticException("Class " + type
                        + " cannot have a superinterface.", tn.position());
            }

            if (superInterfaces.contains(t)) {
                // A compile-time error occurs if the same interface is
                // mentioned two or more times in a single implements clause.
                // See JLS 2nd Ed. | 8.1.4
                throw new SemanticException("Duplicate interface " + t,
                                            tn.position());
            }
            superInterfaces.add(t);
        }

        TypeSystem ts = tc.typeSystem();

        try {
            if (type.isTopLevel()) {
                ts.checkTopLevelClassFlags(type.flags());
            }
            if (type.isMember()) {
                ts.checkMemberClassFlags(type.flags());
            }
            if (type.isLocal()) {
                ts.checkLocalClassFlags(type.flags());
            }
        }
        catch (SemanticException e) {
            throw new SemanticException(e.getMessage(), position());
        }

        // check the class implements all abstract methods that it needs to.
        ts.checkClassConformance(type);

        ts.checkInterfaceFieldFlags(type);

        return this;
    }

    @Override
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        ClassDecl_c n = (ClassDecl_c) super.extRewrite(rw);
        n = copyIfNeeded(n);
        n = type(n, null);
        n.defaultCI = null;
        return n;
    }

    @Override
    public Term firstChild() {
        return body();
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(this.body(), this, EXIT);
        return succs;
    }

    @Override
    public String toString() {
        return flags.clearInterface().translate()
                + (flags.isInterface() ? "interface " : "class ") + name + " "
                + body;
    }

    @Override
    public void prettyPrintHeader(CodeWriter w, PrettyPrinter tr) {
        if (javadoc != null) javadoc.prettyPrint(w, tr);

        w.begin(0);
        if (flags.isInterface()) {
            w.write(flags.clearInterface().clearAbstract().translate());
        }
        else {
            w.write(flags.translate());
        }

        if (flags.isInterface()) {
            w.write("interface ");
        }
        else {
            w.write("class ");
        }

        tr.print(this, name, w);

        if (superClass() != null) {
            w.allowBreak(0);
            w.write("extends ");
            print(superClass(), w, tr);
        }

        if (!interfaces.isEmpty()) {
            w.allowBreak(2);
            if (flags.isInterface()) {
                w.write("extends ");
            }
            else {
                w.write("implements ");
            }

            w.begin(0);
            for (Iterator<TypeNode> i = interfaces().iterator(); i.hasNext();) {
                TypeNode tn = i.next();
                print(tn, w, tr);

                if (i.hasNext()) {
                    w.write(",");
                    w.allowBreak(0);
                }
            }
            w.end();
        }
        w.unifiedBreak(0);
        w.end();
        w.write("{");
    }

    @Override
    public void prettyPrintFooter(CodeWriter w, PrettyPrinter tr) {
        w.write("}");
        w.newline(0);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        ((JLang) tr.lang()).prettyPrintHeader(this, w, tr);
        print(body(), w, tr);
        ((JLang) tr.lang()).prettyPrintFooter(this, w, tr);
    }

    @Override
    public void dump(CodeWriter w) {
        super.dump(w);

        w.allowBreak(4, " ");
        w.begin(0);
        w.write("(name " + name + ")");
        w.end();

        if (javadoc != null) {
            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(javadoc ...)");
            w.end();
        }

        if (type != null) {
            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(type " + type + ")");
            w.end();
        }
    }

    @Override
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
            throws SemanticException {
        /*
        // Don't do anything special for member classes; the disambiguation passes
        // for the container have already been run and visited this class.
        if (type.isMember()) {
            return null;
        }
        
        ClassDecl n = this;
        Node old = n;

        // Ensure supertypes and signatures are disambiguated for all
        // classes visible from this class's scope.

        // Disambiguate supertypes and signatures.
        SupertypeDisambiguator supDisamb = new SupertypeDisambiguator(ar);
        n = (ClassDecl) supDisamb.visitEdgeNoOverride(parent, n);
        if (supDisamb.hasErrors()) throw new SemanticException();

        // Hack to force n.disambiguate() to be called and supertypes set.
        // n = (ClassDecl) new SupertypeDisambiguator(tc).leave(parent, old, n, new SupertypeDisambiguator(tc));
        SignatureDisambiguator sigDisamb = new SignatureDisambiguator(ar);
        n = (ClassDecl) sigDisamb.visitEdgeNoOverride(parent, n);
        if (sigDisamb.hasErrors()) throw new SemanticException();

        // Call enter and leave to manage the context.
        AmbiguityRemover childVisitor = (AmbiguityRemover) ar.enter(parent, n);
        if (ar.hasErrors()) throw new SemanticException();

        n = (ClassDecl) n.visitChildren(childVisitor);
        if (childVisitor.hasErrors()) throw new SemanticException();

        return ar.leave(parent, old, n, childVisitor);
        */

        return null;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.ClassDecl(position,
                            flags,
                            name,
                            superClass,
                            interfaces,
                            body,
                            javadoc);
    }

    @Override
    public Javadoc javadoc() {
        return javadoc;
    }
}
