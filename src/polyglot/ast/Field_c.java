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

import java.util.Collections;
import java.util.List;

import polyglot.translate.ExtensionRewriter;
import polyglot.types.Context;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.VarInstance;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ConstantChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A {@code Field} is an immutable representation of a Java field
 * access.  It consists of field name and may also have either a 
 * {@code Type} or an {@code Expr} containing the field being 
 * accessed.
 */
public class Field_c extends Expr_c implements Field {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Receiver target;
    protected Id name;
    protected FieldInstance fi;
    protected boolean targetImplicit;

//    @Deprecated
    public Field_c(Position pos, Receiver target, Id name) {
        this(pos, target, name, null);
    }

    public Field_c(Position pos, Receiver target, Id name, Ext ext) {
        super(pos, ext);
        assert target != null && name != null;
        this.target = target;
        this.name = name;
        targetImplicit = false;

        if (target == null) {
            throw new InternalCompilerError("Cannot create a field with a null "
                    + "target.  Use AmbExpr or prefix "
                    + "with the appropriate type node or " + "this.");
        }
    }

    @Override
    public Precedence precedence() {
        return Precedence.LITERAL;
    }

    @Override
    public Receiver target() {
        return target;
    }

    @Override
    public Field target(Receiver target) {
        return target(this, target);
    }

    protected <N extends Field_c> N target(N n, Receiver target) {
        if (n.target == target) return n;
        n = copyIfNeeded(n);
        n.target = target;
        return n;
    }

    @Override
    public Id id() {
        return name;
    }

    @Override
    public Field id(Id name) {
        return id(this, name);
    }

    protected <N extends Field_c> N id(N n, Id name) {
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
    public Field name(String name) {
        return id(this.name.id(name));
    }

    @Override
    public Flags flags() {
        return fi.flags();
    }

    @Override
    public VarInstance varInstance() {
        return fieldInstance();
    }

    @Override
    public FieldInstance fieldInstance() {
        return fi;
    }

    @Override
    public Field fieldInstance(FieldInstance fi) {
        return fieldInstance(this, fi);
    }

    protected <N extends Field_c> N fieldInstance(N n, FieldInstance fi) {
        if (n.fi == fi) return n;
        n = copyIfNeeded(n);
        n.fi = fi;
        return n;
    }

    @Override
    public boolean isTargetImplicit() {
        return targetImplicit;
    }

    @Override
    public Field targetImplicit(boolean targetImplicit) {
        return targetImplicit(this, targetImplicit);
    }

    protected <N extends Field_c> N targetImplicit(N n, boolean targetImplicit) {
        if (n.targetImplicit == targetImplicit) return n;
        n = copyIfNeeded(n);
        n.targetImplicit = targetImplicit;
        return n;
    }

    /** Reconstruct the field. */
    protected <N extends Field_c> N reconstruct(N n, Receiver target, Id name) {
        n = target(n, target);
        n = id(n, name);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Receiver target = visitChild(this.target, v);
        Id name = visitChild(this.name, v);
        return reconstruct(this, target, name);
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        Field_c n = (Field_c) super.buildTypes(tb);

        TypeSystem ts = tb.typeSystem();

        FieldInstance fi =
                ts.fieldInstance(position(),
                                 tb.currentClass(),
                                 Flags.NONE,
                                 ts.unknownType(position()),
                                 name.id());
        n = fieldInstance(n, fi);
        return n;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Context c = tc.context();
        TypeSystem ts = tc.typeSystem();

        if (target.type().isReference()) {
            FieldInstance fi =
                    ts.findField(target.type().toReference(),
                                 name.id(),
                                 c.currentClass(),
                                 !(target instanceof Special));

            if (fi == null) {
                throw new InternalCompilerError("Cannot access field on node of type "
                        + target.getClass().getName() + ".");
            }

            Field_c f = this;
            f = fieldInstance(f, fi);
            f = type(f, fi.type());
            f.checkConsistency(c);

            if (!fi.flags().isStatic() && target instanceof TypeNode) {
                throw new SemanticException("Non-static field " + name.id()
                                                    + " cannot be referenced "
                                                    + "from a static context.",
                                            f.position());
            }

            return f;
        }

        throw new SemanticException("Cannot access field \""
                                            + name.id()
                                            + "\" "
                                            + (target instanceof Expr
                                                    ? "on an expression " : "")
                                            + "of non-reference type \""
                                            + target.type() + "\".",
                                    target.position());
    }

    @Override
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
        // Just check if the field is constant to force a dependency to be
        // created.
        cc.lang().isConstant(this, cc.lang());
        return this;
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        if (child == target) {
            return fi.container();
        }

        return child.type();
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.begin(0);
        if (!targetImplicit) {
            // explicit target.
            if (target instanceof Expr) {
                printSubExpr((Expr) target, w, tr);
            }
            else if (target instanceof TypeNode
                    || target instanceof AmbReceiver) {
                print(target, w, tr);
            }

            w.write(".");
            w.allowBreak(2, 3, "", 0);
        }
        tr.print(this, name, w);
        w.end();
    }

    @Override
    public void dump(CodeWriter w) {
        super.dump(w);

        if (fi != null) {
            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(instance " + fi + ")");
            w.end();
        }

        w.allowBreak(4, " ");
        w.begin(0);
        w.write("(name \"" + name + "\")");
        w.end();
    }

    @Override
    public Term firstChild() {
        if (target instanceof Term) {
            return (Term) target;
        }

        return null;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (target instanceof Term) {
            v.visitCFG((Term) target, this, EXIT);
        }

        return succs;
    }

    @Override
    public String toString() {
        return (target != null ? target + "." : "") + name;
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        if (target instanceof Expr && !(target instanceof Special)) {
            return Collections.singletonList((Type) ts.NullPointerException());
        }

        return Collections.<Type> emptyList();
    }

    @Override
    public NodeVisitor extRewriteEnter(ExtensionRewriter rw)
            throws SemanticException {
        if (isTargetImplicit()) {
            // don't translate the target
            return rw.bypass(target());
        }
        return super.extRewriteEnter(rw);
    }

    @Override
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        if (isTargetImplicit()) {
            // don't translate the target.
            // Need to have an ambiguous expression that will be disambiguated later
            return rw.nodeFactory().AmbExpr(position, name);
        }
        Field_c n = (Field_c) super.extRewrite(rw);
        n = fieldInstance(n, null);
        return n;
    }

    @Override
    public boolean constantValueSet(Lang lang) {
        if (fi != null
                && (target instanceof TypeNode || target instanceof Special
                        && targetImplicit)) {
            return fi.constantValueSet();
        }
        return fi != null;
    }

    @Override
    public boolean isConstant(Lang lang) {
        if (fi != null
                && (target instanceof TypeNode || target instanceof Special
                        && targetImplicit)) {
            return fi.isConstant();
        }

        return false;
    }

    @Override
    public Object constantValue(Lang lang) {
        if (lang.isConstant(this, lang)) {
            return fi.constantValue();
        }

        return null;
    }

    /**
     * Check the consistency of the implicit target inserted by the compiler by
     * asserting that the FieldInstance in the Context for this field's name is
     * the same as the FieldInstance we assigned to this field.
     */
    protected void checkConsistency(Context c) {
        if (targetImplicit) {
            VarInstance vi = c.findVariableSilent(name.id());
            if (vi instanceof FieldInstance) {
                FieldInstance rfi = (FieldInstance) vi;
                // Compare the original (declaration) fis, not the actuals.
                // We do this because some extensions that do type substitutions
                // perform the substitution
                // on the fi after lookup and some extensions modify lookup
                // itself to do the substitution.
                if (c.typeSystem().equals(rfi.orig(), fi.orig())) {
                    // all is OK.
                    return;
                }
                System.out.println("(found) rfi is " + rfi.orig());
                System.out.println("(actual) fi is " + fi.orig());
            }
            throw new InternalCompilerError("Field "
                                                    + this
                                                    + " has an "
                                                    + "implicit target, but the name "
                                                    + name.id()
                                                    + " resolves to " + vi
                                                    + " instead of " + target,
                                            position());
        }
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Field(position, target, name);
    }

}
