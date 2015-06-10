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
package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.ClassBody;
import polyglot.ast.Expr;
import polyglot.ast.Id;
import polyglot.ast.Javadoc;
import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ast.Term_c;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.MemberInstance;
import polyglot.types.ParsedClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.UnknownType;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Copy;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

public class EnumConstantDecl_c extends Term_c implements EnumConstantDecl {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<Expr> args;
    protected Id name;
    protected Flags flags;
    protected ClassBody body;
    protected EnumInstance enumInstance;
    protected ConstructorInstance constructorInstance;
    protected ParsedClassType type;
    protected long ordinal;
    protected Javadoc javadoc;

    public EnumConstantDecl_c(Position pos, Flags flags, Id name,
            List<Expr> args, ClassBody body, Javadoc javadoc) {
        super(pos);
        this.name = name;
        this.args = args;
        this.body = body;
        this.flags = flags;
        this.javadoc = javadoc;
    }

    /**
     * @deprecated Use constructor with Javadoc
     */
    @Deprecated
    public EnumConstantDecl_c(Position pos, Flags flags, Id name,
            List<Expr> args, ClassBody body) {
        this(pos, flags, name, args, body, null);
    }

    @Override
    public long ordinal() {
        return ordinal;
    }

    @Override
    public EnumConstantDecl ordinal(long ordinal) {
        return ordinal(this, ordinal);
    }

    protected <N extends EnumConstantDecl_c> N ordinal(N n, long ordinal) {
        if (n.ordinal == ordinal) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.ordinal = ordinal;
        return n;
    }

    @Override
    public List<Expr> args() {
        return args;
    }

    @Override
    public EnumConstantDecl args(List<Expr> args) {
        return args(this, args);
    }

    protected <N extends EnumConstantDecl_c> N args(N n, List<Expr> args) {
        if (CollectionUtil.equals(n.args, args)) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.args = args;
        return n;
    }

    @Override
    public Id name() {
        return id();
    }

    @Override
    public Id id() {
        return name;
    }

    @Override
    public EnumConstantDecl name(Id name) {
        return name(this, name);
    }

    protected <N extends EnumConstantDecl_c> N name(N n, Id name) {
        if (n.name == name) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.name = name;
        return n;
    }

    @Override
    public ClassBody body() {
        return body;
    }

    @Override
    public EnumConstantDecl body(ClassBody body) {
        return body(this, body);
    }

    protected <N extends EnumConstantDecl_c> N body(N n, ClassBody body) {
        if (n.body == body) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.body = body;
        return n;
    }

    @Override
    public ParsedClassType type() {
        return type;
    }

    @Override
    public EnumConstantDecl type(ParsedClassType pct) {
        return type(this, pct);
    }

    protected <N extends EnumConstantDecl_c> N type(N n, ParsedClassType type) {
        if (n.type == type) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.type = type;
        return n;
    }

    @Override
    public MemberInstance memberInstance() {
        return enumInstance();
    }

    @Override
    public EnumInstance enumInstance() {
        return enumInstance;
    }

    @Override
    public EnumConstantDecl enumInstance(EnumInstance ei) {
        return enumInstance(this, ei);
    }

    protected <N extends EnumConstantDecl_c> N enumInstance(N n, EnumInstance ei) {
        if (n.enumInstance == ei) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.enumInstance = ei;
        return n;
    }

    @Override
    public ConstructorInstance constructorInstance() {
        return constructorInstance;
    }

    @Override
    public EnumConstantDecl constructorInstance(ConstructorInstance ci) {
        return constructorInstance(this, ci);
    }

    protected <N extends EnumConstantDecl_c> N constructorInstance(N n,
            ConstructorInstance ci) {
        if (n.constructorInstance == ci) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.constructorInstance = ci;
        return n;
    }

    @Override
    public Flags flags() {
        return flags;
    }

    protected EnumConstantDecl_c reconstruct(List<Expr> args, ClassBody body) {
        EnumConstantDecl_c n = this;
        n = args(n, args);
        n = body(n, body);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        List<Expr> args = visitList(this.args, v);
        ClassBody body = visitChild(this.body, v);
        return reconstruct(args, body);
    }

    @Override
    public Context enterChildScope(Node child, Context c) {
        if (child == body && type != null && body != null) {
            c = c.pushClass(type, type);
        }
        return super.enterChildScope(child, c);
    }

    @Override
    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
        if (body() != null)
            return tb.pushCode().pushAnonClass(position()).enterAnonClass();
        else return tb.pushCode();
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tb.typeSystem();

        List<UnknownType> l = new ArrayList<>(args().size());
        for (int i = 0; i < args().size(); i++) {
            l.add(ts.unknownType(position()));
        }

        ConstructorInstance ci =
                ts.constructorInstance(position(),
                                       ts.Object(),
                                       Flags.NONE,
                                       l,
                                       Collections.<Type> emptyList());

        EnumConstantDecl_c n = this;
        n = constructorInstance(n, ci);
        JL5ParsedClassType enumType = null;
        if (n.body() != null) {
            ParsedClassType type = tb.currentClass();
            n = type(n, type);
            type.setMembersAdded(true);
            enumType = (JL5ParsedClassType) tb.pop().currentClass();

            if (!type.supertypesResolved()) {
                type.superType(enumType);
                type.setSupertypesResolved(true);
            }

        }
        else {
            // this is not an anonymous class extending the enum
            enumType = (JL5ParsedClassType) tb.currentClass();
            n = type(n, enumType);
        }

        // now add the appropriate enum declaration to the containing class
        if (enumType == null) {
            return n;
        }
        EnumInstance ei =
                ts.enumInstance(position(),
                                enumType,
                                Flags.NONE,
                                name.id(),
                                ordinal);
        enumType.addEnumConstant(ei);
        n = enumInstance(n, ei);

        return n;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        Context c = tc.context();
        JL5ParsedClassType ct = (JL5ParsedClassType) c.currentClass();

        List<Type> argTypes = new LinkedList<>();
        for (Expr e : this.args) {
            argTypes.add(e.type());
        }

        ConstructorInstance ci =
                ts.findConstructor(ct, argTypes, c.currentClass(), false);
        EnumConstantDecl_c n = constructorInstance(this, ci);

        if (!n.flags().isEmpty()) {
            throw new SemanticException("Cannot have modifier(s): " + flags
                    + " on enum constant declaration", this.position());
        }

        if (this.body != null) {
            ts.checkClassConformance(type);
        }

        return n;
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        Iterator<Expr> i = this.args().iterator();
        Iterator<? extends Type> j =
                constructorInstance().formalTypes().iterator();

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
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        EnumConstantDecl_c n = (EnumConstantDecl_c) super.extRewrite(rw);
        n = enumInstance(n, null);
        n = constructorInstance(n, null);
        n = type(n, null);
        return n;
    }

    @Override
    public String toString() {
        return name + "(" + args + ")" + (body != null ? "..." : "");
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write(name.id());
        if (args != null && !args.isEmpty()) {
            w.write(" ( ");
            Iterator<Expr> it = args.iterator();
            while (it.hasNext()) {
                Expr e = it.next();
                print(e, w, tr);
                if (it.hasNext()) {
                    w.write(", ");
                    w.allowBreak(0);
                }
            }
            w.write(" )");
        }
        if (body != null) {
            w.write(" {");
            print(body, w, tr);
            w.write("}");
        }
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        return succs;
    }

    @Override
    public Term firstChild() {
        return this;
    }

    @Override
    public EnumConstantDecl javadoc(Javadoc javadoc) {
        return javadoc(this, javadoc);
    }

    protected <N extends EnumConstantDecl_c> N javadoc(N n, Javadoc javadoc) {
        if (n.javadoc == javadoc) return n;
        n = copyIfNeeded(n);
        n.javadoc = javadoc;
        return n;
    }

    @Override
    public Javadoc javadoc() {
        return javadoc;
    }
}
