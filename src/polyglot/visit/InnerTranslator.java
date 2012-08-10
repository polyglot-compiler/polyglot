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

package polyglot.visit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import polyglot.ast.Assign;
import polyglot.ast.Block;
import polyglot.ast.Call;
import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassMember;
import polyglot.ast.CodeDecl;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.FieldAssign;
import polyglot.ast.FieldDecl;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.Local;
import polyglot.ast.LocalClassDecl;
import polyglot.ast.LocalDecl;
import polyglot.ast.MethodDecl;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.ProcedureDecl;
import polyglot.ast.Special;
import polyglot.ast.Stmt;
import polyglot.ast.TypeNode;
import polyglot.types.ClassType;
import polyglot.types.CodeInstance;
import polyglot.types.ConstructorInstance;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.LocalInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ParsedClassType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

/**
 * @author xinqi
 *
 * This class translates inner classes to static nested classes with a field referring to the enclosing 
 * instance. It will also add "new" methods to the enclosing class corresponding to constructors of inner
 * classes.
 * 
 */

public class InnerTranslator extends NodeVisitor {
    protected TypeSystem ts;
    protected NodeFactory nf;

    protected class ClassInfo {
        ParsedClassType ct;
        Map<String, Integer> localNameCount; // Count how many local/anonymous classes with a particular name have appeared.
        List<ClassDecl> newMemberClasses; // New member class declarations converted from local/anonymous classes.
        List<MethodDecl> newMemberMethods; // New member methods added.
        List<Formal> newConsFormals; // The list of added formals to constructors. 
        // The first one should be the reference to the outer class instance, 
        // and the remaining ones are the final locals.
        List<ClassInfo> innerClassInfo; // List of inner class info. 
        boolean hasOuterField;
        CodeInfo insideCode; // For local/anonymous classes, this is the code where the declaration is.

        public ClassInfo(ParsedClassType ct) {
            this.ct = ct;
            localNameCount = new HashMap<String, Integer>();
            newMemberClasses = new LinkedList<ClassDecl>();
            newMemberMethods = new LinkedList<MethodDecl>();
            newConsFormals = new LinkedList<Formal>();
            innerClassInfo = new LinkedList<ClassInfo>();
            hasOuterField = false;
            insideCode = null;
        }

        @Override
        public String toString() {
            return ct.toString();
        }

        // For anonymous classes, name would be "".
        public int addLocalClassName(String name) {
            if (localNameCount.containsKey(name)) {
                int i = localNameCount.get(name);
                localNameCount.put(name, i + 1);
                return i;
            }
            else {
                localNameCount.put(name, 1);
                return 0;
            }
        }

        // Generate new names for local/anonymous classes.
        public String localClassName(String name, int nameCount) {
            String thisName = ct.fullName();
            return namePrefix() + thisName + "$" + nameCount + name;
        }

        public ParsedClassType classType() {
            return ct;
        }

        public void addConsFormal(Formal f) {
            newConsFormals.add(f);
            FieldInstance fi =
                    ts.fieldInstance(Position.compilerGenerated(),
                                     ct,
                                     Flags.PROTECTED,
                                     f.type().type(),
                                     newFieldName(f.name()));
            ct.addField(fi);
        }

        public List<Formal> newConsFormals() {
            return newConsFormals;
        }

        public List<ClassDecl> newMemberClasses() {
            return newMemberClasses;
        }

        public void addMemberClass(ClassDecl cd) {
            newMemberClasses.add(cd);
            ct.addMemberClass(cd.type());
        }

        public List<MethodDecl> newMemberMethods() {
            return newMemberMethods;
        }

        public void addMemberMethods(MethodDecl md) {
            newMemberMethods.add(md);
            ct.addMethod(md.methodInstance());
        }

//		public ClassType memberClassNamed(String name) {
//			return cd.type().memberClassNamed(name);
//		}

        public void addInnerClassInfo(ClassInfo cinfo) {
            innerClassInfo.add(cinfo);
        }

        public ClassInfo findInnerClassInfo(ClassType ct) {
            for (ClassInfo cinfo : innerClassInfo) {
                if (cinfo.classType().equals(ct)) {
                    return cinfo;
                }
            }
            return null;
        }

        public void hasOuterField(boolean b) {
            hasOuterField = b;
        }

        public boolean hasOuterField() {
            return hasOuterField;
        }

        public CodeInfo insideCode() {
            return insideCode;
        }

        public void insideCode(CodeInfo ci) {
            insideCode = ci;
        }
    }

    // Information about methods, constructors, and initializers.
    protected class CodeInfo {
        CodeInstance ci;
        List<LocalInstance> finalArgs; // the list of final arguments if any. 
        List<ClassInfo> localClassInfo; // List of local/anonymous class info.
        Stack<LinkedList<LocalInstance>> blockFinals; // stack of lists of final variables defined in a block.

        public CodeInfo(CodeInstance ci) {
            this.ci = ci;
            finalArgs = new LinkedList<LocalInstance>();
            localClassInfo = new LinkedList<ClassInfo>();
            blockFinals = new Stack<LinkedList<LocalInstance>>();
        }

        @Override
        public String toString() {
            return ci.toString();
        }

        public void addFinalArg(LocalInstance li) {
            finalArgs.add(li);
        }

        public void pushBlock() {
            blockFinals.push(new LinkedList<LocalInstance>());
        }

        public void popBlock() {
            blockFinals.pop();
        }

        public void addFinalLocal(LocalInstance li) {
            List<LocalInstance> current = blockFinals.peek();
            current.add(li);
        }

        public List<LocalInstance> finalList() {
            List<LocalInstance> result = new LinkedList<LocalInstance>();
            result.addAll(finalArgs);
            for (List<LocalInstance> l : blockFinals) {
                result.addAll(l);
            }
            return result;
        }

        public ClassInfo findLocalClassInfo(ClassType ct) {
            for (ClassInfo cinfo : localClassInfo) {
                if (cinfo.classType().equals(ct)) {
                    return cinfo;
                }
            }
            return null;
        }

        /*
         * Add the name of a local/anonymous class, where anonymous classes have name "".
         */
        public void addLocalClassInfo(ClassInfo cinfo) {
            localClassInfo.add(cinfo);
        }

        /*
         * Return whether the code is static.
         * Local/anonymous classes inside static code should not have outer field.
         */
        public boolean isStatic() {
            return ci.flags().isStatic();
        }

        /*
         * Check whether a final local variable with the specified name exists.
         */
        public boolean existFinal(String name) {
            for (int i = blockFinals.size() - 1; i >= 0; i--) {
                List<LocalInstance> l = blockFinals.get(i);
                for (LocalInstance li : l) {
                    if (li.name().equals(name)) return true;
                }
            }
            for (LocalInstance li : finalArgs) {
                if (li.name().equals(name)) return true;
            }
            return false;
        }
    }

    protected Stack<ClassInfo> classContext; // The context stack of all the enclosing classes.
    // It is a stack of ClassInfo.
    protected Stack<CodeInfo> codeContext; // The context stack of all the enclosing code.
    // It is a stack of CodeInfo.
    protected HashMap<String, ClassInfo> innerClassInfoMap; // The map from full names to class infos of inner classes.
    protected Stack<Boolean> insideCode; // Boolean stack that indicates whether it is inside a piece of code now. 
    protected Stack<Boolean> staticFieldDecl; // Boolean stack that indicates whether it is inside 

    // the initialization of a static field.

    protected String namePrefix() {
        return "jl$";
    }

    /*
     * Generate the new name for a field that comes from a final local variable.
     */
    protected String newFieldName(String name) {
        return namePrefix() + name;
    }

    public InnerTranslator(TypeSystem ts, NodeFactory nf) {
        this.ts = ts;
        this.nf = nf;
        classContext = new Stack<ClassInfo>();
        codeContext = new Stack<CodeInfo>();
        innerClassInfoMap = new HashMap<String, ClassInfo>();
        insideCode = new Stack<Boolean>();
        staticFieldDecl = new Stack<Boolean>();
    }

    @Override
    public NodeVisitor enter(Node n) {
        if (n instanceof ClassDecl) {
            ClassDecl cd = (ClassDecl) n;
            enterClassDecl(cd);
        }
        else if (n instanceof New) {
            New newExpr = (New) n;
            enterNew(newExpr);
        }
        else if (n instanceof CodeDecl) {
            CodeDecl cd = (CodeDecl) n;
            enterCodeDecl(cd);
        }
        else if (n instanceof Block) {
            CodeInfo cinfo = codeContext.peek();
            cinfo.pushBlock();
        }
        else if (n instanceof LocalDecl) {
            LocalDecl ld = (LocalDecl) n;
            enterLocalDecl(ld);
        }
        else if (n instanceof ClassBody) {
            insideCode.push(false);
        }
        else if (n instanceof FieldDecl) {
            FieldDecl fd = (FieldDecl) n;
            enterFieldDecl(fd);
        }
        return this;
    }

    protected void enterFieldDecl(FieldDecl fd) {
        if (fd.flags().isStatic()) {
            staticFieldDecl.push(true);
        }
        else {
            staticFieldDecl.push(false);
        }
    }

    protected void enterClassDecl(ClassDecl cd) {
        ParsedClassType ct = cd.type();
        ClassInfo cinfo = new ClassInfo(ct);
        if (ct.isInnerClass() && ct.isMember()) {
            ClassInfo classInfo = classContext.peek();
            cinfo.addConsFormal(produceOuterFormal(ct, classInfo.classType()));
            cinfo.hasOuterField(true);
            classInfo.addInnerClassInfo(cinfo);
            innerClassInfoMap.put(ct.fullName(), cinfo);
        }

        if (ct.isLocal()) {
            CodeInfo codeInfo = codeContext.peek();
            ClassInfo classInfo = classContext.peek();
            if (!codeInfo.isStatic()) {
                // If this local/anonymous class is inside a static method, 
                // then it shouldn't have an outer field.
                cinfo.addConsFormal(produceOuterFormal(cd.type(),
                                                       classInfo.classType()));
                cinfo.hasOuterField(true);
            }
            codeInfo.addLocalClassInfo(cinfo);
            cinfo.insideCode(codeInfo);
            ct.kind(ClassType.MEMBER);
            ct.outer(classInfo.classType());
            ct.needSerialization(false); // local classes don't need serialization.
            String className =
                    classInfo.localClassName(cd.name(),
                                             classInfo.addLocalClassName(cd.name()));
            ct.name(className);

            for (LocalInstance li : codeInfo.finalList()) {
                Id name = nf.Id(Position.compilerGenerated(), li.name());
                Formal f =
                        nf.Formal(Position.compilerGenerated(),
                                  Flags.NONE,
                                  nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                       li.type()),
                                  name);
                f =
                        f.localInstance(ts.localInstance(Position.compilerGenerated(),
                                                         f.flags(),
                                                         f.type().type(),
                                                         f.name()));
                cinfo.addConsFormal(f);
            }
            innerClassInfoMap.put(ct.fullName(), cinfo);
        }

        classContext.push(cinfo);
    }

    protected void enterNew(New newExpr) {
        if (newExpr.body() != null) {
            // If this is an anonymous class declaration.
            ParsedClassType ct = newExpr.anonType();
            ct.flags(Flags.NONE);
            ClassInfo cinfo = new ClassInfo(ct);

            // Check whether the anonymous class is defined outside a code (as the initialization of a field)
            boolean inCode = insideCode.peek().booleanValue();
            CodeInfo codeInfo = null;
            if (inCode) {
                codeInfo = codeContext.peek();
            }
            ClassInfo classInfo = classContext.peek();

            if (inCode && !codeInfo.isStatic() || !inCode
                    && !staticFieldDecl.peek().booleanValue()) {
                // If this local/anonymous class is inside a static method, 
                // then it shouldn't have an outer field.
                cinfo.addConsFormal(produceOuterFormal(ct,
                                                       classInfo.classType()));
                cinfo.hasOuterField(true);
            }

            if (inCode) {
                codeInfo.addLocalClassInfo(cinfo);
            }
            else {
                classInfo.addInnerClassInfo(cinfo);
            }
            cinfo.insideCode(codeInfo);

            ct.kind(ClassType.MEMBER);
            ct.outer(classInfo.classType());
            ct.needSerialization(false); // anonymous classes don't need serialization.
            String className =
                    classInfo.localClassName("",
                                             classInfo.addLocalClassName(""));
            ct.name(className);

            if (inCode) {
                for (LocalInstance li : codeInfo.finalList()) {
                    Id name = nf.Id(Position.compilerGenerated(), li.name());
                    Formal f =
                            nf.Formal(Position.compilerGenerated(),
                                      Flags.NONE,
                                      nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                           li.type()),
                                      name);
                    f =
                            f.localInstance(ts.localInstance(Position.compilerGenerated(),
                                                             f.flags(),
                                                             f.type().type(),
                                                             f.name()));
                    cinfo.addConsFormal(f);
                }
            }

            innerClassInfoMap.put(ct.fullName(), cinfo);

            classContext.push(cinfo);
        }
    }

    protected void enterCodeDecl(CodeDecl cd) {
        CodeInfo cinfo = new CodeInfo(cd.codeInstance());

        // If it is a constructor or method, find all the final arguments and add them to the finalVar map. 
        if (cd instanceof ProcedureDecl) {
            ProcedureDecl pd = (ProcedureDecl) cd;
            for (Formal f : pd.formals()) {
                if (f.flags().isFinal()) {
                    cinfo.addFinalArg(f.localInstance());
                }
            }
        }

        codeContext.push(cinfo);
        insideCode.push(true);
    }

    protected void enterLocalDecl(LocalDecl ld) {
        if (ld.flags().isFinal()) {
            codeContext.peek().addFinalLocal(ld.localInstance());
        }
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        if (n instanceof ClassDecl) {
            ClassDecl cd = (ClassDecl) n;
            return leaveClassDecl(old, cd, v);
        }
        else if (n instanceof New) {
            New newExpr = (New) n;
            return leaveNew(old, newExpr, v);
        }
        else if (n instanceof ConstructorCall) {
            ConstructorCall cc = (ConstructorCall) n;
            return leaveConstructorCall(old, cc, v);
        }
        else if (n instanceof Special) {
            Special s = (Special) n;
            return leaveSpecial(old, s, v);
        }
        else if (n instanceof Field) {
            Field field = (Field) n;
            return leaveField(old, field, v);
        }
        else if (n instanceof Call) {
            // Like Field accesses, we might also have method calls that have no explicit "A.this." 
            // qualifiers, while it means to.
            Call c = (Call) n;
            return leaveCall(old, c, v);
        }
        else if (n instanceof LocalClassDecl) {
            // Need to remove local class declarations.
            return nf.Empty(Position.compilerGenerated());
        }
        else if (n instanceof CodeDecl) {
            codeContext.pop();
            insideCode.pop();
        }
        else if (n instanceof Block) {
            CodeInfo cinfo = codeContext.peek();
            cinfo.popBlock();
        }
        else if (n instanceof Local) {
            Local local = (Local) n;
            return leaveLocal(old, local, v);
        }
        else if (n instanceof ClassBody) {
            insideCode.pop();
        }
        else if (n instanceof FieldDecl) {
            staticFieldDecl.pop();
        }

        return n;
    }

    protected Node leaveClassDecl(Node old, ClassDecl cd, NodeVisitor v) {
        ParsedClassType ct = cd.type();

        ClassInfo selfInfo = classContext.pop();

        // Do nothing if it is already a static class, or it is a toplevel class,  
        // but need to add those classes converted from local/anonymous classes.
        if (ct.flags().isStatic() || ct.isTopLevel()) {
            if (selfInfo.newMemberClasses().size() > 0
                    || selfInfo.newMemberMethods().size() > 0) {
                cd = addNewMembers(cd, selfInfo);
            }
            return cd;
        }

        // Deal with ordinary inner classes.
        if (selfInfo.insideCode() == null) {
            cd = updateClassDecl(cd, ct, selfInfo);
        }
        else {
            ClassInfo cinfo = classContext.peek();
            cd = cd.name(ct.name());
            cd = updateClassDecl(cd, ct, selfInfo);
            cinfo.addMemberClass(cd);
        }

        return cd;
    }

    protected Node leaveNew(Node old, New newExpr, NodeVisitor v) {
        if (newExpr.body() != null) {
            // Anonymous class declaration.
            // Need to create a class declaration, and add it to the enclosing class.
            ParsedClassType ct = newExpr.anonType();
            Id name = nf.Id(Position.compilerGenerated(), ct.name());
            ClassDecl cd =
                    nf.ClassDecl(Position.compilerGenerated(),
                                 ct.flags(),
                                 name,
                                 nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                      ct.superType()),
                                 Collections.<TypeNode> emptyList(),
                                 newExpr.body());
            cd = cd.type(ct);

            ClassInfo selfInfo = classContext.pop();
            ClassInfo cinfo = classContext.peek();
            cd = cd.name(ct.name());
            cd = addAnonymousConstructor(cd, ct, selfInfo, newExpr);
            cd = updateClassDecl(cd, ct, selfInfo);
            cinfo.addMemberClass(cd);
            newExpr = (New) newExpr.type(ct);
        }

        return updateNewExpr(newExpr);
    }

    protected Node leaveConstructorCall(Node old, ConstructorCall cc,
            NodeVisitor v) {
        ClassInfo cinfo = classContext.peek();
        return updateConstructorCall(cc, cinfo);
    }

    protected Node leaveSpecial(Node old, Special s, NodeVisitor v) {
        if (s.kind() == Special.THIS && s.qualifier() != null) {
            ClassType tOuter = (ClassType) s.qualifier().type();
            ClassType tThis = classContext.peek().classType();
            Expr t = s.qualifier(null);
            while (!ts.equals(tOuter, tThis)) {
//				t = nf.Field(Position.compilerGenerated(), t, newFieldName(outerThisName(tThis)));
                t = produceOuterField(tThis, t);
                tThis = tThis.outer();
            }

            return t;
        }

        return s;
    }

    protected Node leaveField(Node old, Field field, NodeVisitor v) {
        // Check whether the field access is a disguised form of "A.this.f".
        // Note: we only need to check non-static fields!
        if (!field.flags().isStatic() && field.isTargetImplicit()) {
            ClassType tThis = classContext.peek().classType();
            ClassType tOuter = findField(field.name(), tThis);
            Expr t = produceThis(tThis);
            while (!ts.equals(tOuter, tThis)) {
//				t = nf.Field(Position.compilerGenerated(), t, newFieldName(outerThisName(tThis)));
                t = produceOuterField(tThis, t);
                tThis = tThis.outer();
            }
            Id name = nf.Id(Position.compilerGenerated(), field.name());
            Field f = nf.Field(field.position(), t, name);
            f = f.fieldInstance(field.fieldInstance());
            return f;
        }

        return field;
    }

    protected Node leaveCall(Node old, Call c, NodeVisitor v) {
        MethodInstance mi = c.methodInstance();
        if (!mi.flags().isStatic() && c.isTargetImplicit()) {
            ClassType tThis = classContext.peek().classType();
            ClassType tOuter = findMethod(mi, tThis);
            Expr t = produceThis(tThis);
            while (!ts.equals(tOuter, tThis)) {
//				t = nf.Field(Position.compilerGenerated(), t, newFieldName(outerThisName(tThis)));
                t = produceOuterField(tThis, t);
                tThis = tThis.outer();
            }
            Call nc = c.target(t).targetImplicit(false);
            return nc;
        }

        return c;
    }

    /**
     * Translate final local variables that should become field accesses of local/anonymous classes. 
     * @param old
     * @param local
     * @param v
     */
    protected Node leaveLocal(Node old, Local local, NodeVisitor v) {
        if (local.flags().isFinal()) {
            CodeInfo codeInfo = codeContext.peek();
            if (!codeInfo.existFinal(local.name())) {
                String newName = newFieldName(local.name());
                ClassType tThis = classContext.peek().classType();
                ClassType tOuter = findField(newName, tThis);
                Expr t = produceThis(tThis);
                while (!ts.equals(tOuter, tThis)) {
//					t = nf.Field(Position.compilerGenerated(), t, newFieldName(outerThisName(tThis)));
                    t = produceOuterField(tThis, t);
                    tThis = tThis.outer();
                }
                Id id = nf.Id(Position.compilerGenerated(), newName);
                Field f = nf.Field(Position.compilerGenerated(), t, id);
                f =
                        f.fieldInstance(ts.fieldInstance(Position.compilerGenerated(),
                                                         (ReferenceType) t.type(),
                                                         Flags.PROTECTED,
                                                         local.type(),
                                                         f.name()));
                return f;
            }
        }

        return local;
    }

    /**
     * Generate a special node "this" with the correct type.
     * @param ct
     */
    protected Special produceThis(ClassType ct) {
        Special s = nf.Special(Position.compilerGenerated(), Special.THIS);
        s = (Special) s.type(ct);
        return s;
    }

    /*
     * Add the constructor for an anonymous class.
     */
    protected ClassDecl addAnonymousConstructor(ClassDecl cd,
            ParsedClassType ct, ClassInfo cinfo, New newExpr) {
        List<Formal> formals =
                new ArrayList<Formal>(newExpr.arguments().size() + 1);
        List<Expr> args = new ArrayList<Expr>(newExpr.arguments().size() + 1);
        List<Type> ftypes = new ArrayList<Type>(newExpr.arguments().size() + 1);

        int i = 0;
        for (Expr arg : newExpr.arguments()) {
            Id id = nf.Id(Position.compilerGenerated(), "arg" + i);
            Formal f =
                    nf.Formal(Position.compilerGenerated(),
                              Flags.NONE,
                              nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                   arg.type()),
                              id);
            LocalInstance li =
                    ts.localInstance(Position.compilerGenerated(),
                                     Flags.NONE,
                                     arg.type(),
                                     "arg" + i);
            f = f.localInstance(li);
            formals.add(f);

            Local l = nf.Local(Position.compilerGenerated(), id);
            l = l.localInstance(li);
            args.add(l);

            ftypes.add(arg.type());
        }
        ConstructorCall cc = nf.SuperCall(Position.compilerGenerated(), args);
        cc = cc.constructorInstance(newExpr.constructorInstance());
        cc = updateConstructorCall(cc, cinfo);
        Id cid = nf.Id(Position.compilerGenerated(), ct.name());
        ConstructorDecl cons =
                nf.ConstructorDecl(Position.compilerGenerated(),
                                   Flags.NONE,
                                   cid,
                                   formals,
                                   Collections.<TypeNode> emptyList(),
                                   nf.Block(Position.compilerGenerated(), cc));
        ConstructorInstance consInst =
                ts.constructorInstance(Position.compilerGenerated(),
                                       ct,
                                       Flags.NONE,
                                       ftypes,
                                       Collections.<Type> emptyList());
        cons = cons.constructorInstance(consInst);
        List<ClassMember> members = cd.body().members();
        List<ClassMember> newMembers =
                new ArrayList<ClassMember>(members.size() + 1);
        newMembers.add(cons);
        newMembers.addAll(members);
        cd = cd.body(nf.ClassBody(cd.body().position(), newMembers));
        return cd;
    }

    /**
     * Find the class type inside which a field with specified name is declared.
     */
    protected ParsedClassType findField(String name, ClassType current) {
        for (int i = classContext.size() - 1; i >= 0; i--) {
            ClassInfo cinfo = classContext.get(i);
            ParsedClassType ct = cinfo.classType();
            try {
                ts.findField(ct, name, current);
            }
            catch (SemanticException se) {
                continue;
            }
            return ct;
        }
        throw new RuntimeException("Unable to find field " + name + ".");
    }

    /**
     * Find the class type inside which a field with specified name is declared.
     */
    protected ParsedClassType findMethod(MethodInstance mi, ClassType current) {
        for (int i = classContext.size() - 1; i >= 0; i--) {
            ClassInfo cinfo = classContext.get(i);
            ParsedClassType ct = cinfo.classType();
            try {
                ts.findMethod(ct, mi.name(), mi.formalTypes(), current);
            }
            catch (SemanticException se) {
                continue;
            }
            return ct;
        }
        throw new RuntimeException("Unable to find " + mi + ".");
    }

    protected ConstructorCall updateConstructorCall(ConstructorCall cc,
            ClassInfo selfInfo) {
        ConstructorInstance ci = cc.constructorInstance();
        ClassType ct = (ClassType) ci.container();
        if (cc.kind().equals(ConstructorCall.THIS)) {
            // If calling a constructor of the same class, just need to pass all the new formals. 
            ClassInfo cinfo = classContext.peek();
            ci = updateConstructorInst(ct, ci, cinfo);
            List<Formal> formals = cinfo.newConsFormals();
            List<Expr> args =
                    new ArrayList<Expr>(cc.arguments().size() + formals.size());
            args.addAll(cc.arguments());
            for (Formal f : formals) {
                Id id = nf.Id(Position.compilerGenerated(), f.name());
                Local l = nf.Local(Position.compilerGenerated(), id);
                l = l.localInstance(f.localInstance());
                l = (Local) l.type(f.type().type());
                args.add(l);
            }
            cc = (ConstructorCall) cc.arguments(args);
            cc = cc.constructorInstance(ci);
        }
        else {
            ClassInfo cinfo = innerClassInfoMap.get(ct.fullName());
            if (cinfo != null) {
                // it is an inner class.
                if (cinfo.insideCode() == null) {
                    // For member inner classes, only need to add one formal that refers to the outer instance.
                    ci = updateConstructorInst(ct, ci, cinfo);
                    List<Expr> args =
                            new ArrayList<Expr>(cc.arguments().size() + 1);
                    Formal f = selfInfo.newConsFormals().get(0);
                    Id id =
                            nf.Id(Position.compilerGenerated(),
                                  outerThisName(ct));
                    Local l = nf.Local(Position.compilerGenerated(), id);
                    l = l.localInstance(f.localInstance());
                    l = (Local) l.type(f.type().type());
                    args.addAll(cc.arguments());
                    args.add(l);
                    cc = (ConstructorCall) cc.arguments(args);
                    cc = cc.constructorInstance(ci);
                }
                else if (selfInfo.insideCode() == cinfo.insideCode()) {
                    // The super class is a local class, and they are within the same code.
                    ci = updateConstructorInst(ct, ci, cinfo);
                    List<Formal> formals = cinfo.newConsFormals();
                    List<Expr> args =
                            new ArrayList<Expr>(cc.arguments().size()
                                    + formals.size());
                    args.addAll(cc.arguments());
                    for (Formal f : formals) {
                        Id id = nf.Id(Position.compilerGenerated(), f.name());
                        args.add(nf.Local(Position.compilerGenerated(), id));
                    }
                    cc = (ConstructorCall) cc.arguments(args);
                    cc = cc.constructorInstance(ci);
                }
                else {
                    // The super class is a local class, and they are not within the same code.
                    // Need to first find an enclosing class that is inside the same code as the super class,
                    // and use its fields as arguments.
                    Id id =
                            nf.Id(Position.compilerGenerated(),
                                  outerThisName(ct));
                    Local outerLocal =
                            nf.Local(Position.compilerGenerated(), id);
                    outerLocal =
                            outerLocal.localInstance(selfInfo.newConsFormals()
                                                             .get(0)
                                                             .localInstance());
                    Expr outer = outerLocal;
                    ClassType outerCt = selfInfo.classType().outer();
                    ClassType tThis = ct;
                    ClassInfo outerCInfo =
                            innerClassInfoMap.get(outerCt.fullName());
                    while (outerCInfo.insideCode() != cinfo.insideCode()) {
//						outer = nf.Field(Position.compilerGenerated(), outer, newFieldName(outerThisName(tThis)));
                        outer = produceOuterField(tThis, outer);
                        tThis = outerCt;
                        outerCt = outerCt.outer();
                        outerCInfo = innerClassInfoMap.get(outerCt.fullName());
                    }
                    ci = updateConstructorInst(ct, ci, cinfo);
                    List<Formal> formals = cinfo.newConsFormals();
                    List<Expr> args =
                            new ArrayList<Expr>(cc.arguments().size()
                                    + formals.size());
                    args.addAll(cc.arguments());
                    for (Formal f : formals) {
                        Id fid =
                                nf.Id(Position.compilerGenerated(),
                                      newFieldName(f.name()));
                        args.add(nf.Field(Position.compilerGenerated(),
                                          outer,
                                          fid));
                    }
                    cc = (ConstructorCall) cc.arguments(args);
                    cc = cc.constructorInstance(ci);
                }
            }
        }
        return cc;
    }

    /*
     * Add new member classes/methods.
     */
    protected ClassDecl addNewMembers(ClassDecl cd, ClassInfo cinfo) {
        List<ClassMember> members =
                new ArrayList<ClassMember>(cd.body().members().size()
                        + cinfo.newMemberClasses().size()
                        + cinfo.newMemberMethods().size());
        members.addAll(cd.body().members());
        members.addAll(cinfo.newMemberClasses());
        members.addAll(cinfo.newMemberMethods());
        ClassBody b = nf.ClassBody(cd.body().position(), members);
        b = (ClassBody) b.exceptions(cd.body().exceptions());
        cd = cd.body(b);
        return cd;
    }

    /**
     * Find ClassInfo for ClassType ct, from innerClassInfoMap. 
     * @param ct
     */
    protected ClassInfo findClassInfo(ClassType ct) {
        ClassInfo cinfo = innerClassInfoMap.get(ct.fullName());
        return cinfo;
    }

    /**
     * Check whether ct is a type in source language.
     * @param ct
     */
    protected boolean isSourceType(ClassType ct) {
        return true;
    }

    /**
         * Update new expressions to include necessary arguments (for example,
         * enclosing instances) and eliminate qualifers. 
     * @param newExpr
     */
    protected Expr updateNewExpr(New newExpr) {
        ClassType ct = (ClassType) newExpr.type();
        ClassInfo classInfo = classContext.peek();
        ClassInfo cinfo = findClassInfo(ct);
//		boolean inCode = ((Boolean)insideCode.peek()).booleanValue();
//		if (inCode) {
//			CodeInfo codeInfo = (CodeInfo)codeContext.peek();
//			cinfo = codeInfo.findLocalClassInfo(ct);
//		}
//		if (cinfo == null) {
//			cinfo = classInfo.findInnerClassInfo(ct);
//		}
        if (cinfo != null) {
            ConstructorInstance ci = newExpr.constructorInstance();
            List<Formal> formals = cinfo.newConsFormals();
            List<Expr> args =
                    new ArrayList<Expr>(newExpr.arguments().size()
                            + formals.size());
            List<Type> ftypes =
                    new ArrayList<Type>(newExpr.arguments().size()
                            + formals.size());
            args.addAll(newExpr.arguments());
            ftypes.addAll(ci.formalTypes());
            Iterator<Formal> it = formals.iterator();
            if (cinfo.hasOuterField()) {
                if (newExpr.qualifier() != null) {
                    args.add(newExpr.qualifier());
                    ftypes.add(newExpr.qualifier().type());
                }
                else {
                    args.add(nf.This(Position.compilerGenerated()));
                    ftypes.add(classInfo.classType());
                }
                it.next(); // Only if there is an outer field, the first argument needs to be skipped.
            }
            for (; it.hasNext();) {
                Formal f = it.next();
                Id id = nf.Id(Position.compilerGenerated(), f.name());
                args.add(nf.Local(Position.compilerGenerated(), id));
                ftypes.add(f.type().type());
            }

            New nExpr = (New) newExpr.arguments(args);
            ci.setFormalTypes(ftypes);
            if (newExpr.anonType() != null) {
                ci.setContainer(newExpr.anonType());
                nExpr =
                        nExpr.objectType(nf.CanonicalTypeNode(Position.compilerGenerated(),
                                                              newExpr.anonType()));
            }

            nExpr = nExpr.qualifier(null);
            nExpr = nExpr.anonType(null);
            nExpr = nExpr.body(null);
            nExpr = nExpr.constructorInstance(ci);

            return nExpr;
        }
        else if (ct.isInnerClass() && isSourceType(ct)) {
            // Maybe we have encountered the new expression of an inner class before it is translated.
            // But we have to make sure that ct is a type in the source language.
            ConstructorInstance ci = newExpr.constructorInstance();
            List<Expr> args =
                    new ArrayList<Expr>(newExpr.arguments().size() + 1);
            List<Type> ftypes =
                    new ArrayList<Type>(newExpr.arguments().size() + 1);
            args.addAll(newExpr.arguments());
            ftypes.addAll(ci.formalTypes());
            if (newExpr.qualifier() != null) {
                args.add(newExpr.qualifier());
                ftypes.add(newExpr.qualifier().type());
            }
            else {
                args.add(nf.This(Position.compilerGenerated()));
                ftypes.add(classContext.peek().classType());
            }
            ci.setFormalTypes(ftypes);
            New nExpr = (New) newExpr.arguments(args);
            nExpr = nExpr.qualifier(null);
            nExpr = nExpr.constructorInstance(ci);
            return nExpr;
        }
        return newExpr;
    }

    protected ConstructorDecl produceDefaultConstructor(ParsedClassType ct,
            ClassInfo cinfo) {
        ConstructorCall cc =
                nf.ConstructorCall(Position.compilerGenerated(),
                                   ConstructorCall.SUPER,
                                   Collections.<Expr> emptyList());
        ConstructorInstance cci =
                ts.constructorInstance(Position.compilerGenerated(),
                                       (ClassType) ct.superType(),
                                       Flags.PUBLIC, // XXX: how to find the real flags? 
                                       Collections.<Type> emptyList(),
                                       Collections.<Type> emptyList());
        cc = cc.constructorInstance(cci);
        cc = updateConstructorCall(cc, cinfo);
        Id id = nf.Id(Position.compilerGenerated(), ct.name());
        ConstructorDecl cd =
                nf.ConstructorDecl(Position.compilerGenerated(),
                                   Flags.PUBLIC,
                                   id,
                                   Collections.<Formal> emptyList(),
                                   Collections.<TypeNode> emptyList(),
                                   nf.Block(Position.compilerGenerated(), cc));
        ConstructorInstance cdi =
                ts.constructorInstance(Position.compilerGenerated(),
                                       ct,
                                       Flags.PUBLIC,
                                       Collections.<Type> emptyList(),
                                       Collections.<Type> emptyList());
        cd = cd.constructorInstance(cdi);
        return cd;
    }

    protected ClassDecl updateClassDecl(ClassDecl cd, ParsedClassType ct,
            ClassInfo cinfo) {
        Flags f = ct.flags().Static();
        ct.flags(f);

        List<ClassMember> members = new LinkedList<ClassMember>();

        List<FieldDecl> fields = produceFieldDecls(ct, cinfo);
        members.addAll(fields);

//		for (Iterator it = fields.iterator(); it.hasNext(); ) {
//			FieldDecl fd = (FieldDecl)it.next();
//			ct.addField(fd.fieldInstance());
//		}

        ct.setConstructors(Collections.<ConstructorInstance> emptyList());
        for (ClassMember m : cd.body().members()) {
            if (m instanceof ConstructorDecl) {
                ConstructorDecl cons = (ConstructorDecl) m;
                ConstructorDecl newCons =
                        updateConstructor(cd, ct, cons, cinfo);
                members.add(newCons);
                ct.addConstructor(newCons.constructorInstance());
            }
            else {
                members.add(m);
            }
        }

        if (ct.constructors().size() == 0) {
            // Add a default constructor for inner classes, if there is none,
            // in case that it is inherited from another inner class, and 
            // the default constructor is not having "default" behavior.
            ConstructorDecl cons =
                    updateConstructor(cd,
                                      ct,
                                      produceDefaultConstructor(ct, cinfo),
                                      cinfo);
            members.add(cons);
            ct.addConstructor(cons.constructorInstance());
        }

        List<ClassDecl> newMemClasses = cinfo.newMemberClasses();
        members.addAll(newMemClasses);
        List<MethodDecl> newMethods = cinfo.newMemberMethods();
        members.addAll(newMethods);
//		for (Iterator it = newMemClasses.iterator(); it.hasNext(); ) {
//			ClassDecl memCd = (ClassDecl)it.next();
//			ct.addMemberClass(memCd.type());
//		}

        ClassBody cb = cd.body();
        cb = cb.members(members);
        cd = cd.body(cb);
        cd = cd.type(ct);
        cd = cd.flags(f);

        return cd;
    }

    protected List<FieldDecl> produceFieldDecls(ClassType ct, ClassInfo cinfo) {
        List<Formal> newFormals = cinfo.newConsFormals();
        List<FieldDecl> fields = new ArrayList<FieldDecl>(newFormals.size());
        for (Formal formal : newFormals) {
            Id id =
                    nf.Id(Position.compilerGenerated(),
                          newFieldName(formal.name()));
            FieldDecl fd =
                    nf.FieldDecl(Position.compilerGenerated(),
                                 Flags.PROTECTED,
                                 formal.type(),
                                 id);
            FieldInstance fi =
                    ts.fieldInstance(Position.compilerGenerated(),
                                     ct,
                                     Flags.PROTECTED,
                                     formal.type().type(),
                                     newFieldName(formal.name()));
            fd = fd.fieldInstance(fi);
            fields.add(fd);
        }
        return fields;
    }

    // Return the name of the "outer this" field and formal in ct. 
    protected String outerThisName(ClassType ct) {
        return "outer$this";
    }

    /*
     * ct  - ParsedClassType of the inner class that we are dealing with.
     * oct - ParsedClassType of the outer class.
     */
    protected Formal produceOuterFormal(ParsedClassType ct, ParsedClassType oct) {
        Id fn = nf.Id(Position.compilerGenerated(), outerThisName(ct));
        Formal formal =
                nf.Formal(Position.compilerGenerated(),
                          Flags.NONE,
                          nf.CanonicalTypeNode(Position.compilerGenerated(),
                                               oct),
                          fn);
        formal =
                formal.localInstance(ts.localInstance(Position.compilerGenerated(),
                                                      formal.flags(),
                                                      formal.type().type(),
                                                      formal.name()));
        return formal;
    }

    protected Field produceOuterField(ClassType ct, Expr rec) {
        Id id =
                nf.Id(Position.compilerGenerated(),
                      newFieldName(outerThisName(ct)));
        Field f = nf.Field(Position.compilerGenerated(), rec, id);
        f =
                f.fieldInstance(ts.fieldInstance(Position.compilerGenerated(),
                                                 ct,
                                                 Flags.PROTECTED,
                                                 ct.container(), // FIXME: use the type of outer formal stored in cinfo?
                                                 f.name()));
        return f;
    }

    protected ConstructorInstance updateConstructorInst(ClassType ct,
            ConstructorInstance ci, ClassInfo cinfo) {
        List<Formal> newFormals = cinfo.newConsFormals();
        List<Type> ftypes =
                new ArrayList<Type>(ci.formalTypes().size() + newFormals.size());
        ftypes.addAll(ci.formalTypes());
        for (Formal f : newFormals) {
            ftypes.add(f.type().type());
        }
        ci.setFormalTypes(ftypes);
        ci.setContainer(ct);
        return ci;
    }

    protected ConstructorCall produceDefaultSuperConstructorCall(ClassType ct) {
        ConstructorCall superCc =
                nf.ConstructorCall(Position.compilerGenerated(),
                                   ConstructorCall.SUPER,
                                   Collections.<Expr> emptyList());
        ConstructorInstance superCi =
                ts.constructorInstance(Position.compilerGenerated(),
                                       (ClassType) ct.superType(),
                                       Flags.PUBLIC,
                                       Collections.<Type> emptyList(),
                                       Collections.<Type> emptyList());
        superCc = superCc.constructorInstance(superCi);
        superCc = updateConstructorCall(superCc, classContext.peek());
        return superCc;
    }

    // Add new argument(s) to a constructor
    protected ConstructorDecl updateConstructor(ClassDecl cd, ClassType ct,
            ConstructorDecl cons, ClassInfo cinfo) {
        List<Formal> newFormals = cinfo.newConsFormals();
        List<Formal> formals =
                new ArrayList<Formal>(cons.formals().size() + newFormals.size());
        formals.addAll(cons.formals());
        formals.addAll(newFormals);

        List<Stmt> oldStmts = cons.body().statements();
        List<Stmt> stmts =
                new ArrayList<Stmt>(oldStmts.size() + newFormals.size());
        Iterator<Stmt> it = oldStmts.iterator();

        // Check whether the first statement is a constructor call.
        if (it.hasNext()) {
            Stmt s = it.next();
            if (s instanceof ConstructorCall) {
                stmts.add(s);
                // If it calls another constructor in the same class, we don't need to initialize the field.
                if (((ConstructorCall) s).kind() != ConstructorCall.THIS) {
                    stmts.addAll(produceFieldInits(cinfo));
                }
            }
            else {
                // If there is no explicit constructor call, we need to add the default 
                // constructor call, and update it, in case it is from another inner class,
                // and therefore needs adding new formals.
                stmts.add(produceDefaultSuperConstructorCall(ct));
                stmts.addAll(produceFieldInits(cinfo));
                stmts.add(s);
            }
        }
        else {
            stmts.add(produceDefaultSuperConstructorCall(ct));
            stmts.addAll(produceFieldInits(cinfo));
        }
        while (it.hasNext()) {
            stmts.add(it.next());
        }

        Block b = nf.Block(Position.compilerGenerated(), stmts);
        Id id = nf.Id(Position.compilerGenerated(), ct.name());
        ConstructorDecl newCons =
                nf.ConstructorDecl(Position.compilerGenerated(),
                                   cons.flags(),
                                   id,
                                   formals,
                                   cons.throwTypes(),
                                   b);
        newCons =
                newCons.constructorInstance(updateConstructorInst(ct,
                                                                  cons.constructorInstance(),
                                                                  cinfo));
        return newCons;
    }

    // Generate a list that contains all the field assignments for initializing newly added fields.
    protected List<Stmt> produceFieldInits(ClassInfo cinfo) {
        List<Formal> newFormals = cinfo.newConsFormals();
        List<Stmt> fInits = new ArrayList<Stmt>(newFormals.size());
        for (Formal formal : newFormals) {
            Id formalId = nf.Id(Position.compilerGenerated(), formal.name());
            Local local = nf.Local(Position.compilerGenerated(), formalId);
            local = local.localInstance(formal.localInstance());
            Special thisExpr = nf.This(Position.compilerGenerated());
            thisExpr = (Special) thisExpr.type(cinfo.classType());
            Id fieldId =
                    nf.Id(Position.compilerGenerated(),
                          newFieldName(formal.name()));
            Field field =
                    nf.Field(Position.compilerGenerated(), thisExpr, fieldId);
            field =
                    field.fieldInstance(ts.fieldInstance(Position.compilerGenerated(),
                                                         cinfo.classType(),
                                                         Flags.PROTECTED,
                                                         formal.type().type(),
                                                         field.name()));
            FieldAssign fAssign =
                    nf.FieldAssign(Position.compilerGenerated(),
                                   field,
                                   Assign.ASSIGN,
                                   local);
            Stmt stmt = nf.Eval(Position.compilerGenerated(), fAssign);
            fInits.add(stmt);
        }
        return fInits;
    }

}
