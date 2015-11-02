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

import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.FieldInstance;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
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
 * A {@code ClassBody} represents the body of a class or interface
 * declaration or the body of an anonymous class.
 */
public class ClassBody_c extends Term_c implements ClassBody {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<ClassMember> members;

//    @Deprecated
    public ClassBody_c(Position pos, List<ClassMember> members) {
        this(pos, members, null);
    }

    public ClassBody_c(Position pos, List<ClassMember> members, Ext ext) {
        super(pos, ext);
        assert members != null;
        this.members = ListUtil.copy(members, true);
    }

    @Override
    public List<ClassMember> members() {
        return members;
    }

    @Override
    public ClassBody members(List<ClassMember> members) {
        return members(this, members);
    }

    protected <N extends ClassBody_c> N members(N n,
            List<ClassMember> members) {
        if (CollectionUtil.equals(n.members, members)) return n;
        n = copyIfNeeded(n);
        n.members = ListUtil.copy(members, true);
        return n;
    }

    @Override
    public ClassBody addMember(ClassMember member) {
        List<ClassMember> l = new ArrayList<>(members.size() + 1);
        l.addAll(members);
        l.add(member);
        return members(l);
    }

    protected <N extends ClassBody_c> N reconstruct(N n,
            List<ClassMember> members) {
        n = members(n, members);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<ClassMember> members = visitList(this.members, v);
        return reconstruct(this, members);
    }

    @Override
    public NodeVisitor buildTypesEnter(TypeBuilder tb)
            throws SemanticException {
        return tb.enterAnonClass();
    }

    @Override
    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        return this;
    }

    @Override
    public String toString() {
        return "{ ... }";
    }

    protected void duplicateFieldCheck(TypeChecker tc)
            throws SemanticException {
        ClassType type = tc.context().currentClass();

        List<FieldInstance> l = new ArrayList<>(type.fields());

        for (int i = 0; i < l.size(); i++) {
            FieldInstance fi = l.get(i);

            for (int j = i + 1; j < l.size(); j++) {
                FieldInstance fj = l.get(j);

                if (fi.name().equals(fj.name())) {
                    throw new SemanticException("Duplicate field \"" + fj
                            + "\".", fj.position());
                }
            }
        }
    }

    protected void duplicateConstructorCheck(TypeChecker tc)
            throws SemanticException {
        ClassType type = tc.context().currentClass();
        TypeSystem ts = tc.typeSystem();

        List<ConstructorInstance> l = new ArrayList<>(type.constructors());

        for (int i = 0; i < l.size(); i++) {
            ConstructorInstance ci = l.get(i);

            for (int j = i + 1; j < l.size(); j++) {
                ConstructorInstance cj = l.get(j);

                if (isSameConstructor(ts, ci, cj)) {
                    throw new SemanticException("Duplicate constructor \"" + cj
                            + "\".", cj.position());
                }
            }
        }
    }

    protected void duplicateMethodCheck(TypeChecker tc)
            throws SemanticException {
        ClassType type = tc.context().currentClass();
        TypeSystem ts = tc.typeSystem();

        List<MethodInstance> l = new ArrayList<>(type.methods());

        for (int i = 0; i < l.size(); i++) {
            MethodInstance mi = l.get(i);

            for (int j = i + 1; j < l.size(); j++) {
                MethodInstance mj = l.get(j);

                if (isSameMethod(ts, mi, mj)) {
                    throw new SemanticException("Duplicate method \"" + mj
                            + "\".", mj.position());
                }
            }
        }
    }

    protected void duplicateMemberClassCheck(TypeChecker tc)
            throws SemanticException {
        ClassType type = tc.context().currentClass();

        List<ClassType> l = new ArrayList<>(type.memberClasses());

        for (int i = 0; i < l.size(); i++) {
            ClassType mi = l.get(i);

            for (int j = i + 1; j < l.size(); j++) {
                ClassType mj = l.get(j);

                if (mi.name().equals(mj.name())) {
                    throw new SemanticException("Duplicate member type \"" + mj
                            + "\".", mj.position());
                }
            }
        }
    }

    protected boolean isSameMethod(TypeSystem ts, MethodInstance mi,
            MethodInstance mj) {
        return mi.isSameMethod(mj);
    }

    protected boolean isSameConstructor(TypeSystem ts, ConstructorInstance ci,
            ConstructorInstance cj) {
        return ci.isSameConstructor(cj);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        duplicateFieldCheck(tc);
        duplicateConstructorCheck(tc);
        duplicateMethodCheck(tc);
        duplicateMemberClassCheck(tc);

        return this;
    }

    @Override
    public NodeVisitor exceptionCheckEnter(ExceptionChecker ec)
            throws SemanticException {
        return ec.push();
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (!members.isEmpty()) {
            w.newline(4);
            w.begin(0);
            ClassMember prev = null;

            for (Iterator<ClassMember> i = members.iterator(); i.hasNext();) {
                ClassMember member = i.next();
                if (prev != null && (member instanceof polyglot.ast.CodeDecl
                        && !(prev instanceof polyglot.ast.ClassDecl)
                        || prev instanceof polyglot.ast.CodeDecl)) {
                    w.newline(0);
                }
                prev = member;
                printBlock(member, w, tr);
                if (i.hasNext()) {
                    w.newline(0);
                }
            }

            w.end();
            w.newline(0);
        }
    }

    @Override
    public Term firstChild() {
        // Do _not_ visit class members.
        return null;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.ClassBody(position, members);
    }

}
