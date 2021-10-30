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
package polyglot.ext.jl5.visit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import polyglot.ast.ArrayInit;
import polyglot.ast.Block;
import polyglot.ast.Call;
import polyglot.ast.Case;
import polyglot.ast.Cast;
import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassLit;
import polyglot.ast.ClassMember;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.FieldDecl;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.IntLit;
import polyglot.ast.Local;
import polyglot.ast.MethodDecl;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Return;
import polyglot.ast.Stmt;
import polyglot.ast.Switch;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.JL5Options;
import polyglot.ext.jl5.ast.EnumConstantDecl;
import polyglot.ext.jl5.ast.JL5ClassDeclExt;
import polyglot.ext.jl5.ast.JL5EnumDeclExt;
import polyglot.ext.jl5.ast.JL5Ext;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5LocalInstance;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.frontend.Job;
import polyglot.qq.QQ;
import polyglot.types.ClassType;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.LocalInstance;
import polyglot.types.MemberInstance;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * Translate enums to Java 1.4 language features.
 */
public class RemoveEnums extends ContextVisitor {
    /**
     * Convenience field, used for quasi quoting
     */
    private QQ qq;

    /**
     * Is the visitor in an enum declaration?
     */
    private boolean inEnumDecl = false;

    /**
     * If the visitor is in an enum declaration, then this is the type of the declaration.
     */
    private ClassType enumDeclType = null;

    private final String enumImplClass;
    private final String enumSetImplClass;

    private final boolean translateEnumSet;

    /**
     * ClassMembers to add at the closest surrounding class body.
     * An element is pushed when entering a ClassBody, and popped when exiting a ClassBody.
     */
    private Stack<List<ClassMember>> classMembersToAdd = new Stack<>();

    public RemoveEnums(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
        qq = new QQ(job.extensionInfo());

        enumImplClass = ((JL5Options) job.extensionInfo().getOptions()).enumImplClass;
        enumSetImplClass = ((JL5Options) job.extensionInfo().getOptions()).enumSetImplClass;
        translateEnumSet = !enumSetImplClass.equals("java.util.EnumSet");
    }

    @Override
    protected NodeVisitor enterCall(Node n) throws SemanticException {
        if (n instanceof ClassBody) {
            classMembersToAdd.push(new ArrayList<ClassMember>());
        }
        if (n instanceof ClassDecl) {
            JL5ClassDeclExt ext = (JL5ClassDeclExt) JL5Ext.ext(n);
            if (ext instanceof JL5EnumDeclExt) {
                return inEnumDecl(((ClassDecl) n).type());
            }
        }
        return super.enterCall(n);
    }

    private NodeVisitor inEnumDecl(ClassType enumDeclType) {
        RemoveEnums re = (RemoveEnums) copy();
        re.inEnumDecl = true;
        re.enumDeclType = enumDeclType;
        return re;
    }

    @Override
    protected Node leaveCall(Node n) throws SemanticException {
        if (n instanceof ClassBody) {
            n = addWaitingClassMembers((ClassBody) n);
        }
        if (n instanceof ClassDecl && JL5Ext.ext(n) instanceof JL5EnumDeclExt) {
            return translateEnumDecl((ClassDecl) n);
        }
        if (n instanceof EnumConstantDecl) {
            return translateEnumConstantDecl((EnumConstantDecl) n);
        }
        if (inEnumDecl && n instanceof ConstructorDecl) {
            return translateEnumConstructor((ConstructorDecl) n);
        }
        if (n instanceof Switch) {
            return translateSwitch((Switch) n);
        }
        if (n instanceof Case) {
            return translateCase((Case) n);
        }
        if (n instanceof TypeNode) {
            return translateTypeNode((TypeNode) n);
        }
        return n;
    }

    /**
     * Add any waiting class members to the ClassBody
     */
    private Node addWaitingClassMembers(ClassBody n) {
        List<ClassMember> membersToAdd = classMembersToAdd.pop();

        for (ClassMember m : membersToAdd) {
            if (m instanceof FieldDecl) {
                FieldDecl fd = (FieldDecl) m;
                if (fd.fieldInstance().container() == null) {
                    fd.fieldInstance().setContainer(this.context().currentClass());
                }
            }
            if (m instanceof MethodDecl) {
                MethodDecl md = (MethodDecl) m;
                if (md.methodInstance().container() == null) {
                    md.methodInstance().setContainer(this.context().currentClass());
                }
            }
            n = n.addMember(m);
        }
        return n;
    }

    /**
     * Translate an enum declaration to java 1.4 features.
     * Creates a new ClassDecl.
     */
    private Node translateEnumDecl(ClassDecl enumDecl) throws SemanticException {
        ClassBody body = enumDecl.body();

        body = addEnumUtilityMembers(body, enumDecl.type());

        ClassDecl classDecl =
                nf.ClassDecl(
                        enumDecl.position(),
                        JL5Flags.clearEnum(enumDecl.type().flags()),
                        enumDecl.id(),
                        nf.CanonicalTypeNode(enumDecl.position(), ts.typeForName(enumImplClass)),
                        enumDecl.interfaces(),
                        body,
                        enumDecl.javadoc());

        // XXX type information. This is a little dodgy
        classDecl = classDecl.type(enumDecl.type());

        // Debugging: print out the translated class decl as an enum
        //        System.err.println("###");
        //        CodeWriter cw = new OptimalCodeWriter(System.err, 80);
        //        prettyPrintClassDeclAsEnum(classDecl, cw, new PrettyPrinter());
        //        try {
        //            cw.flush();
        //        } catch (IOException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        return classDecl;
    }

    /**
     * Translate a constructor declared in an enum constructor into a suitable constructor.
     * Addes two new arguments for the name and ordinal of the enum constant, and hands those to
     * the super class.
     */
    private Node translateEnumConstructor(ConstructorDecl n) {
        Position pos = Position.compilerGenerated();
        Id enumName = nodeFactory().Id(pos, "enum$name");
        Id enumOrdinal = nodeFactory().Id(pos, "enum$ordinal");

        List<Stmt> oldStmts = new LinkedList<>(n.body().statements());
        ConstructorCall existingCC = null;
        if (!oldStmts.isEmpty() && oldStmts.get(0) instanceof ConstructorCall) {
            existingCC = (ConstructorCall) oldStmts.remove(0);
        } else {
            existingCC =
                    nodeFactory()
                            .ConstructorCall(
                                    pos, ConstructorCall.SUPER, Collections.<Expr>emptyList());
        }

        // add two new arguments to the constructor
        List<Formal> newFormals = new ArrayList<>();
        JL5LocalInstance enumNameLI =
                (JL5LocalInstance) ts.localInstance(pos, Flags.NONE, ts.String(), enumName.id());
        enumNameLI.setProcedureFormal(true);
        newFormals.add(
                nodeFactory()
                        .Formal(
                                pos,
                                Flags.NONE,
                                nodeFactory().CanonicalTypeNode(pos, ts.String()),
                                enumName)
                        .localInstance(enumNameLI));

        JL5LocalInstance enumOrdLI =
                (JL5LocalInstance) ts.localInstance(pos, Flags.NONE, ts.Int(), enumOrdinal.id());
        enumOrdLI.setProcedureFormal(true);
        newFormals.add(
                nodeFactory()
                        .Formal(
                                pos,
                                Flags.NONE,
                                nodeFactory().CanonicalTypeNode(pos, ts.Int()),
                                enumOrdinal)
                        .localInstance(enumOrdLI));
        newFormals.addAll(n.formals());
        n = (ConstructorDecl) n.formals(newFormals);

        // use those arguments in the constructor call
        List<Expr> constructorCallArgs = new ArrayList<>();
        constructorCallArgs.add(nodeFactory().Local(pos, enumName).localInstance(enumNameLI));
        constructorCallArgs.add(nodeFactory().Local(pos, enumOrdinal).localInstance(enumOrdLI));
        if (existingCC.kind() == ConstructorCall.THIS) {
            constructorCallArgs.addAll(existingCC.arguments());
        }

        List<Stmt> newStmts = new ArrayList<>();
        newStmts.add((ConstructorCall) existingCC.arguments(constructorCallArgs));
        newStmts.addAll(oldStmts);
        n = (ConstructorDecl) n.body(nodeFactory().Block(pos, newStmts));
        return n;
    }

    /**
     * Adds utility members: a valueOf method, a values method, and a values field that contains all the enum constants.
     */
    private ClassBody addEnumUtilityMembers(ClassBody body, ClassType enumDeclType)
            throws SemanticException {
        ClassType old = this.enumDeclType;
        this.enumDeclType = enumDeclType;
        body = addValuesField(body);
        body = addValuesMethod(body);
        body = addValueOfMethod(body);
        this.enumDeclType = old;
        return body;
    }

    private ClassBody addValuesField(ClassBody body) {
        // private static final T[] values = {decl1, decl2, ...};
        Position pos = Position.compilerGenerated();
        List<Expr> decls = new ArrayList<>();
        for (MemberInstance mi : enumDeclType.toClass().members()) {
            if (mi instanceof EnumInstance) {
                EnumInstance ei = (EnumInstance) mi;
                Field f =
                        nf.Field(
                                pos,
                                nf.CanonicalTypeNode(pos, enumDeclType),
                                nf.Id(pos, ei.name()));
                decls.add(f);
            }
        }

        ArrayInit ai = nf.ArrayInit(pos, decls);
        FieldDecl fd =
                nf.FieldDecl(
                        pos,
                        Flags.STATIC.Final(),
                        nf.CanonicalTypeNode(pos, ts.arrayOf(enumDeclType)),
                        nf.Id(pos, "values"),
                        ai);
        FieldInstance fi =
                ts.fieldInstance(pos, enumDeclType, Flags.NONE, ts.arrayOf(enumDeclType), "values");

        fd = fd.fieldInstance(fi);

        body = body.addMember(fd);
        return body;
    }

    private ClassBody addValuesMethod(ClassBody body) {
        // public static T[] values() { return (T[])T.values.clone(); }
        Position pos = Position.compilerGenerated();
        Field f = nf.Field(pos, nf.CanonicalTypeNode(pos, enumDeclType), nf.Id(pos, "values"));
        Id clid = nodeFactory().Id(pos, "clone");
        Call cl = nf.Call(pos, f, clid);
        Cast cst = nf.Cast(pos, nf.CanonicalTypeNode(pos, ts.arrayOf(enumDeclType)), cl);
        Return ret = nf.Return(pos, cst);
        Id mdid = nodeFactory().Id(pos, "values");
        MethodDecl md =
                nf.MethodDecl(
                        pos,
                        Flags.PUBLIC.Static(),
                        nf.CanonicalTypeNode(pos, ts.arrayOf(enumDeclType)),
                        mdid,
                        Collections.<Formal>emptyList(),
                        Collections.<TypeNode>emptyList(),
                        nf.Block(pos, ret));

        MethodInstance mi =
                ts.methodInstance(
                        pos,
                        enumDeclType,
                        Flags.NONE,
                        ts.arrayOf(enumDeclType),
                        "values",
                        Collections.<Type>emptyList(),
                        Collections.<Type>emptyList());
        md = md.methodInstance(mi);

        return body.addMember(md);
    }

    private ClassBody addValueOfMethod(ClassBody body) throws SemanticException {
        // public static T valueOf(String s) { return (T)Enum.valueOf(T.class, s); }
        Position pos = Position.compilerGenerated();
        Local arg = nf.Local(pos, nf.Id(pos, "s"));
        ClassLit clazz = nf.ClassLit(pos, nf.CanonicalTypeNode(pos, enumDeclType));

        LocalInstance argLI = ts.localInstance(pos, Flags.NONE, ts.String(), arg.name());
        arg = arg.localInstance(argLI);

        Call cl =
                nf.Call(
                        pos,
                        nf.CanonicalTypeNode(pos, ts.typeForName(enumImplClass)),
                        nf.Id(pos, "valueOf"),
                        clazz,
                        arg);
        Cast cst = nf.Cast(pos, nf.CanonicalTypeNode(pos, enumDeclType), cl);
        Return ret = nf.Return(pos, cst);

        Formal formal =
                nf.Formal(pos, Flags.NONE, nf.CanonicalTypeNode(pos, ts.String()), nf.Id(pos, "s"))
                        .localInstance(argLI);
        MethodDecl md =
                nf.MethodDecl(
                        pos,
                        Flags.PUBLIC.Static(),
                        nf.CanonicalTypeNode(pos, enumDeclType),
                        nf.Id(pos, "valueOf"),
                        Collections.singletonList(formal),
                        Collections.<TypeNode>emptyList(),
                        nf.Block(pos, ret));
        MethodInstance mi =
                ts.methodInstance(
                        pos,
                        enumDeclType,
                        Flags.NONE,
                        enumDeclType,
                        "valueOf",
                        Collections.singletonList((Type) ts.String()),
                        Collections.<Type>emptyList());
        md = md.methodInstance(mi);

        return body.addMember(md);
    }

    /**
     * Translate an EnumConstantDecl to a FieldDecl.
     * @param ecd
     * @return
     */
    private Node translateEnumConstantDecl(EnumConstantDecl ecd) {
        List<Expr> args = new ArrayList<>();
        // add the name and ordinal
        args.add(nf.StringLit(Position.compilerGenerated(), ecd.name().id()));
        args.add(nf.IntLit(Position.compilerGenerated(), IntLit.INT, ecd.ordinal()));
        args.addAll(ecd.args());
        Expr init =
                nf.New(
                        Position.compilerGenerated(),
                        nf.CanonicalTypeNode(Position.compilerGenerated(), enumDeclType),
                        args,
                        ecd.body());
        init = init.type(enumDeclType);

        FieldDecl fd =
                nf.FieldDecl(
                        ecd.position(),
                        Flags.FINAL.Public().Static(),
                        nf.CanonicalTypeNode(Position.compilerGenerated(), enumDeclType),
                        ecd.name(),
                        init);

        // type information
        return fd;
    }

    /**
     * Take a ClassDecl that is the translation of an enum declaration (as produced by this class)
     * and pretty prints it like a Java 1.5 enum. This method lets us get around a "feature" of
     * Java 1.5 compilers that refuse to let classes directly extend java.lang.Enum. (OK, this is
     * actually part of the Java Language Spec.) So in order to produce code that is compilable
     * by a Java 1.5 compiler, we need to output the translation of an enum declaration as an
     * enum declaration.
     */
    public static void prettyPrintClassDeclAsEnum(ClassDecl decl, CodeWriter w, PrettyPrinter tr) {
        w.begin(0);
        w.write(decl.flags().clearStatic().translate());
        w.write("enum ");
        w.write(decl.name());

        w.unifiedBreak(0);
        w.end();
        w.write("{");

        // figure out which members to add and which to ignore
        List<FieldDecl> enumConstDecls = new ArrayList<>();
        List<ClassMember> otherMembers = new ArrayList<>();
        for (ClassMember cm : decl.body().members()) {
            boolean isEnumConstDecl = false;
            boolean addMember = true;
            if (cm instanceof FieldDecl) {
                FieldDecl fd = (FieldDecl) cm;
                if (fd.type().type() == decl.type()) {
                    // the type of this field is the same as the decl type
                    isEnumConstDecl = true;
                }
                if (fd.name().equals("values")) {
                    // it's one of the members we added
                    addMember = false;
                }
            }
            if (cm instanceof MethodDecl) {
                MethodDecl md = (MethodDecl) cm;
                if (md.name().equals("valueOf") || md.name().equals("values")) {
                    // it's one of the members we added
                    addMember = false;
                }
            }

            if (isEnumConstDecl) {
                enumConstDecls.add((FieldDecl) cm);
            } else if (addMember) {
                otherMembers.add(cm);
            }
        }

        // print the enum const decls
        Iterator<FieldDecl> iter = enumConstDecls.iterator();
        w.allowBreak(1, " ");
        while (iter.hasNext()) {
            FieldDecl fd = iter.next();
            prettyPrintEnumConstFieldDecl(fd, w, tr);
            if (iter.hasNext()) {
                w.write(",");
                w.allowBreak(1, " ");
            }
        }
        w.write(";");
        w.newline();

        // print the rest
        w.begin(0);
        ClassMember prev = null;

        for (Iterator<ClassMember> i = otherMembers.iterator(); i.hasNext(); ) {
            ClassMember member = i.next();
            if (member instanceof polyglot.ast.CodeDecl || prev instanceof polyglot.ast.CodeDecl) {
                w.newline(0);
            }
            prev = member;
            w.begin(0);
            if (member instanceof ConstructorDecl) {
                prettyPrintConstructorDeclAsEnumConstructorDecl((ConstructorDecl) member, w, tr);
            } else {
                tr.print(decl, member, w);
            }
            w.end();
            if (i.hasNext()) {
                w.newline(0);
            }
        }

        w.end();

        w.write("}");
        w.newline(0);
    }

    private static void prettyPrintEnumConstFieldDecl(
            FieldDecl fd, CodeWriter w, PrettyPrinter tr) {
        w.write(fd.name());
        Expr init = fd.init();
        if (init instanceof New) {
            New ne = (New) init;
            // we have added two args to the new statement
            // remove them to print the args nicely
            List<Expr> newArgs = new LinkedList<>(ne.arguments());
            newArgs.remove(0);
            newArgs.remove(0);
            if (!newArgs.isEmpty()) {
                w.write("(");
                Iterator<Expr> iter = newArgs.iterator();
                while (iter.hasNext()) {
                    Expr e = iter.next();
                    tr.lang().prettyPrint(e, w, tr);
                    if (iter.hasNext()) {
                        w.write(",");
                        w.allowBreak(1, " ");
                    }
                }
                w.write(")");
            }

            if (ne.body() != null) {
                w.write(" {");
                tr.lang().prettyPrint(ne.body(), w, tr);
                w.write("}");
            }

        } else {
            throw new InternalCompilerError("Uh oh, don't know how to translate " + fd);
        }
    }

    private static void prettyPrintConstructorDeclAsEnumConstructorDecl(
            ConstructorDecl cd, CodeWriter w, PrettyPrinter tr) {
        // remove the two dummy arguments
        List<Formal> newFormals = new LinkedList<>(cd.formals());
        newFormals.remove(0);
        newFormals.remove(0);

        cd = (ConstructorDecl) cd.formals(newFormals);

        // remove the call to super

        List<Stmt> newStmts = new LinkedList<>(cd.body().statements());
        if (!newStmts.isEmpty() && newStmts.get(0) instanceof ConstructorCall) {
            newStmts.remove(0);
        }

        if (newStmts.isEmpty() && newFormals.isEmpty()) {
            // nothing to print
            return;
        }

        Block newBody = cd.body().statements(newStmts);
        cd = (ConstructorDecl) cd.body(newBody);
        tr.lang().prettyPrint(cd, w, tr);
    }

    private Node translateSwitch(Switch n) {
        if (n.expr().type().isPrimitive()) {
            // nothing to do with this switch
            return n;
        }
        // it's a switch on an enum
        // need to translate it.
        // The basic idea is given "switch (e) { case GREEN: ...; case RED: ...; }"
        // we translate it to "switch(enum$SwitchMap(e)) { case 0: ...; case 1: ...; }"
        // where enum$SwitchMap is a new method that maps from enum constant fields to some integers
        // (say, their ordinals)
        Position pos = Position.compilerGenerated();
        ClassType enumType = n.expr().type().toClass();
        Id methodName =
                nodeFactory().Id(pos, "enum$SwitchMap$" + enumType.fullName().replace('.', '_'));
        Call methodCall = nodeFactory().Call(pos, methodName, n.expr());
        // XXX missing type information on the methodcall
        n = n.expr(methodCall);

        // the body of the method is something like "private static int enum$SwitchMap(Object e) {
        // if (e == Coin.GREEN) return Coin.GREEN.ordinal(); ... return -1; }
        Id arg = nodeFactory().Id(pos, "e");
        List<Stmt> stmts = new ArrayList<>();

        LocalInstance argLI = ts.localInstance(pos, Flags.NONE, enumType, arg.id());
        // add the if statements
        for (FieldInstance field : enumConstantFieldInstances(enumType)) {
            int index = findEnumConstIndex(enumType, field);
            Stmt s =
                    qq.parseStmt(
                            "if (%E == %T." + field.name() + ") return " + index + ";",
                            nodeFactory().Local(pos, arg).localInstance(argLI).type(enumType),
                            enumType);
            stmts.add(s);
        }

        stmts.add(
                nodeFactory()
                        .Return(pos, nodeFactory().IntLit(pos, IntLit.INT, -1).type(ts.Int())));

        List<Formal> formals = new ArrayList<>();
        formals.add(
                nodeFactory()
                        .Formal(
                                pos,
                                Flags.NONE,
                                nodeFactory().CanonicalTypeNode(pos, ts.Object()),
                                arg)
                        .localInstance(argLI));
        Flags methodFlags = Flags.PRIVATE;
        if (this.context().currentClass().flags().isStatic()
                || this.context().currentClass().isTopLevel()) {
            methodFlags = methodFlags.Static();
        }
        MethodDecl switchMethod =
                nodeFactory()
                        .MethodDecl(
                                pos,
                                methodFlags,
                                nodeFactory().CanonicalTypeNode(pos, ts.Int()),
                                methodName,
                                formals,
                                Collections.<TypeNode>emptyList(),
                                nodeFactory().Block(pos, stmts));

        ClassType container = this.context().currentClass();
        MethodInstance mi =
                ts.methodInstance(
                        pos,
                        container,
                        methodFlags,
                        ts.Int(),
                        methodName.id(),
                        Collections.singletonList((Type) ts.Object()),
                        Collections.<Type>emptyList());
        switchMethod = switchMethod.methodInstance(mi);

        addClassMemberToAdd(switchMethod);

        return n;
    }

    private void addClassMemberToAdd(MethodDecl switchMethod) {
        String methodName = switchMethod.name();
        List<ClassMember> list = classMembersToAdd.peek();
        for (ClassMember cm : list) {
            if (cm instanceof MethodDecl && ((MethodDecl) cm).name().equals(methodName)) {
                // method by that name already exisits.
                return;
            }
        }
        list.add(switchMethod);
    }

    private static int findEnumConstIndex(ClassType enumType, FieldInstance field) {
        List<FieldInstance> l = enumConstantFieldInstances(enumType);
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i) == field) {
                return i;
            }
        }
        throw new InternalCompilerError(
                "Couldn't find field " + field + " in " + enumType + " as an enum constant");
    }

    private static List<FieldInstance> enumConstantFieldInstances(ClassType enumType) {
        List<FieldInstance> l = new ArrayList<>();
        for (FieldInstance f : enumType.fields()) {
            if (f.flags().isStatic() && f.type() == enumType) {
                // it's an enum
                l.add(f);
            }
        }
        return l;
    }

    private Node translateCase(Case n) {
        if (n.isDefault() || n.expr() == null || n.expr().type().isPrimitive()) {
            // nothing to do with this switch
            return n;
        }
        n = n.expr(nodeFactory().IntLit(Position.compilerGenerated(), IntLit.INT, n.value()));
        return n;
    }

    private Node translateTypeNode(TypeNode n) throws SemanticException {
        if (!translateEnumSet) return n;

        JL5TypeSystem jl5ts = (JL5TypeSystem) ts;

        if (ts.typeEquals(
                jl5ts.erasureType(n.type()),
                jl5ts.erasureType(ts.typeForName("java.util.EnumSet")))) {
            return nf.CanonicalTypeNode(n.position(), ts.typeForName(enumSetImplClass));
        }
        return n;
    }
}
