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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import polyglot.ast.Assign;
import polyglot.ast.Block;
import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassMember;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Field;
import polyglot.ast.FieldAssign;
import polyglot.ast.FieldDecl;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.Local;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Special;
import polyglot.ast.Stmt;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.LocalInstance;
import polyglot.types.ParsedClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

/**
 * @author nystrom
 *
 * This class translates inner classes to static nested classes with a field
 * pointing to the enclosing instance.
 */
public class InnerClassRewriter extends InnerClassAbstractRemover {
    public InnerClassRewriter(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    /**
     * Translates a local class type into a field instance.
     * 
     * @param ct
     *          the class type that will contain the field.
     * @param outer
     *          the class type for which a field is to be created.
     */
    FieldInstance localToField(ParsedClassType ct, ClassType outer) {
        FieldInstance fi =
                ts.fieldInstance(Position.compilerGenerated(),
                                 ct,
                                 Flags.FINAL.Protected(),
                                 outer,
                                 mangleClassName(outer));
        return fi;
    }

    FieldDecl createFieldDecl(FieldInstance fi) {
        Id id = nf.Id(Position.compilerGenerated(), fi.name());
        FieldDecl fd =
                nf.FieldDecl(fi.position(),
                             fi.flags(),
                             nf.CanonicalTypeNode(fi.position(), fi.type()),
                             id);
        fd = fd.fieldInstance(fi);
        return fd;
    }

    class ClassBodyTranslator extends NodeVisitor {
        ParsedClassType ct;
        Map<ClassType, FieldInstance> fieldMap;
        Context outerContext;

        ClassBodyTranslator(ParsedClassType ct,
                Map<ClassType, FieldInstance> fieldMap, Context context) {
            this.ct = ct;
            this.fieldMap = fieldMap;
            this.outerContext = context;
        }

        @Override
        public Node leave(Node old, Node n, NodeVisitor v) {
            if (n instanceof Special) {
                Special s = (Special) n;
                if (s.qualifier() != null) {
                    FieldInstance fi = fieldMap.get(s.qualifier().type());
                    if (fi != null) {
                        Special this_ = nf.Special(s.position(), Special.THIS);
                        this_ = (Special) this_.type(ct);
                        Id id = nf.Id(Position.compilerGenerated(), fi.name());
                        Field f = nf.Field(s.position(), this_, id);
                        f = f.fieldInstance(fi);
                        f = (Field) f.type(fi.type());
                        n = f;
                    }
                }
            }
            if (n instanceof ConstructorDecl) {
                ConstructorDecl ctd = (ConstructorDecl) n;
                ClassType ct2 =
                        (ClassType) ctd.constructorInstance().container();
                if (ct2.equals(ct)) {
                    ctd = translateConstructorDecl(ct, ctd, fieldMap);
                }
                n = ctd;
            }
            return super.leave(old, n, v);
        }
    }

    void addEnvToCI(ConstructorInstance ci, List<ClassType> env) {
        List<Type> formals = new ArrayList<Type>(ci.formalTypes());
        formals.addAll(envAsFormalTypes(env));
        ci.setFormalTypes(formals);
    }

    ConstructorDecl translateConstructorDecl(ParsedClassType ct,
            ConstructorDecl cd, Map<ClassType, FieldInstance> m) {
        List<ClassType> env = env(ct, true);

        addEnvToCI(cd.constructorInstance(), env);

        cd = cd.name(ct.name());

        // Add the new formals.
        List<Formal> newFormals = new ArrayList<Formal>();
        newFormals.addAll(cd.formals());
        newFormals.addAll(envAsFormals(env));
        cd = cd.formals(newFormals);

        if (cd.body() == null) {
            // Must be a native constructor; just let the programmer
            // deal with it.
            return cd;
        }

        List<Stmt> oldStmts = cd.body().statements();
        List<Stmt> newStmts = new ArrayList<Stmt>();

        // Check if this constructor invokes another with a this call.
        // If so, don't initialize the fields, but do pass the environment
        // to the other constructor.
        ConstructorCall cc = null;

        if (oldStmts.size() >= 1) {
            Stmt s = oldStmts.get(0);
            if (s instanceof ConstructorCall) {
                cc = (ConstructorCall) s;
            }
        }

        if (cc != null) {
            newStmts.add(cc);
        }

        // Initialize the new fields.
        if (cc == null || cc.kind() == ConstructorCall.SUPER) {
            for (Formal f : envAsFormals(env)) {
                LocalInstance li = f.localInstance();
                FieldInstance fi = m.get(li.type());

                if (fi == null) {
                    // Not a enclosing class of ct, so must be an enclosing class
                    // of a supertype.  The supertype will initialize.
                    continue;
                }

                Special this_ =
                        nf.Special(Position.compilerGenerated(), Special.THIS);
                this_ = (Special) this_.type(ct);

                Id targetId = nf.Id(Position.compilerGenerated(), fi.name());
                Field target =
                        nf.Field(Position.compilerGenerated(), this_, targetId);
                target = target.fieldInstance(fi);
                target = (Field) target.type(fi.type());

                Id sourceId = nf.Id(Position.compilerGenerated(), li.name());
                Local source = nf.Local(Position.compilerGenerated(), sourceId);
                source = source.localInstance(li);
                source = (Local) source.type(li.type());

                FieldAssign assign =
                        nf.FieldAssign(Position.compilerGenerated(),
                                       target,
                                       Assign.ASSIGN,
                                       source);
                assign = (FieldAssign) assign.type(target.type());

                newStmts.add(nf.Eval(Position.compilerGenerated(), assign));
            }
        }

        if (cc != null) {
            for (int i = 1; i < oldStmts.size(); i++) {
                newStmts.add(oldStmts.get(i));
            }
        }
        else {
            newStmts.addAll(oldStmts);
        }

        Block b = cd.body().statements(newStmts);
        cd = (ConstructorDecl) cd.body(b);
        return cd;
    }

    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {
        if (n instanceof ClassDecl) {
            ClassDecl cd = (ClassDecl) n;

            ParsedClassType ct = cd.type();

            List<ClassType> env = env(ct, true);

            if (!env.isEmpty()) {
                // Translate the class body if any supertype (including ct itself)
                // is an inner class.
                Context innerContext =
                        cd.del().enterChildScope(cd.body(), context);
                cd = cd.body(translateClassBody(ct, cd.body(), innerContext));
            }

            n = cd;
        }

        n = super.leaveCall(old, n, v);
        return n;
    }

    protected ClassBody translateClassBody(ParsedClassType ct, ClassBody body,
            Context context) {
        List<ClassMember> members = new ArrayList<ClassMember>();

        List<ClassType> env = env(ct, false);

        Map<ClassType, FieldInstance> fieldMap =
                new HashMap<ClassType, FieldInstance>();

        for (ClassType outer : env) {
            FieldInstance fi = localToField(ct, outer);
            fieldMap.put(outer, fi);
            ct.addField(fi);
            members.add(createFieldDecl(fi));
        }

        // Now add existing members, making sure constructors appear
        // first.  The constructors may have field
        // initializers which must be run before other initializers.
        List<ConstructorDecl> ctors = new ArrayList<ConstructorDecl>();
        List<ClassMember> others = new ArrayList<ClassMember>();
        for (ClassMember cm : body.members()) {
            if (cm instanceof ConstructorDecl) {
                ctors.add((ConstructorDecl) cm);
            }
            else {
                others.add(cm);
            }
        }

        members.addAll(ctors);
        members.addAll(others);

        body = body.members(members);

        // Rewrite the class body.
        ClassBodyTranslator v = new ClassBodyTranslator(ct, fieldMap, context);
        v = (ClassBodyTranslator) v.begin();
        body = (ClassBody) body.visit(v);

        return body;
    }
}
