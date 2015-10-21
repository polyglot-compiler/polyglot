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

import polyglot.types.ClassType;
import polyglot.types.Context;
import polyglot.types.FieldInstance;
import polyglot.types.LocalInstance;
import polyglot.types.Named;
import polyglot.types.NoClassException;
import polyglot.types.NoMemberException;
import polyglot.types.Qualifier;
import polyglot.types.Resolver;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.VarInstance;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.ContextVisitor;

/**
 * Utility class which is used to disambiguate ambiguous
 * AST nodes (Expr, Type, Receiver, Qualifier, Prefix).
 */
public class Disamb_c implements Disamb {
    protected ContextVisitor v;
    protected Position pos;
    protected Prefix prefix;
    protected Id name;

    protected NodeFactory nf;
    protected TypeSystem ts;
    protected Context c;
    protected Ambiguous amb;

    @Deprecated
    @Override
    public Node disambiguate(Ambiguous amb, ContextVisitor v, Position pos,
            Prefix prefix, String name) throws SemanticException {
        return disambiguate(amb, v, pos, prefix, v.nodeFactory().Id(pos, name));
    }

    @Override
    public Node disambiguate(Ambiguous amb, ContextVisitor v, Position pos,
            Prefix prefix, Id name) throws SemanticException {

        this.v = v;
        this.pos = pos;
        this.prefix = prefix;
        this.name = name;
        this.amb = amb;

        nf = v.nodeFactory();
        ts = v.typeSystem();
        c = v.context();

        if (prefix instanceof Ambiguous) {
            throw new SemanticException("Cannot disambiguate node with ambiguous prefix.");
        }

        Node result = null;

        if (prefix instanceof PackageNode) {
            PackageNode pn = (PackageNode) prefix;
            result = disambiguatePackagePrefix(pn);
        }
        else if (prefix instanceof TypeNode) {
            TypeNode tn = (TypeNode) prefix;
            result = disambiguateTypeNodePrefix(tn);
        }
        else if (prefix instanceof Expr) {
            Expr e = (Expr) prefix;
            result = disambiguateExprPrefix(e);
        }
        else if (prefix == null) {
            result = disambiguateNoPrefix();
        }

        return result;
    }

    /**
     * @throws SemanticException
     */
    protected Node disambiguatePackagePrefix(PackageNode pn)
            throws SemanticException {
        Resolver pc =
                ts.packageContextResolver(pn.package_(), c.currentClass());

        Named n = pc.find(name.id());

        Qualifier q = null;

        if (n instanceof Qualifier) {
            q = (Qualifier) n;
        }
        else {
            return null;
        }

        if (q.isPackage() && packageOK()) {
            return nf.PackageNode(pos, q.toPackage());
        }
        else if (q.isType() && typeOK()) {
            if (!ts.classAccessibleFromPackage(q.toType().toClass(),
                                               c.package_())) {
                throw new SemanticException("Cannot access type " + q
                        + " from package " + c.package_() + ".");
            }
            return nf.CanonicalTypeNode(pos, q.toType());
        }

        return null;
    }

    protected Node disambiguateTypeNodePrefix(TypeNode tn)
            throws SemanticException {
        // Try static fields.
        Type t = tn.type();

        if (t.isReference() && exprOK()) {
            try {
                FieldInstance fi =
                        ts.findField(t.toReference(),
                                     name.id(),
                                     c.currentClass(),
                                     true);
                return nf.Field(pos, tn, name)
                         .fieldInstance(fi)
                         .type(ts.unknownType(pos));
            }
            catch (NoMemberException e) {
                if (e.getKind() != NoMemberException.FIELD) {
                    // something went wrong...
                    throw e;
                }

                // ignore so we can check if we're a member class.
            }
        }

        // Try member classes.
        if (t.isClass() && typeOK()) {
            Named n;
            try {
                n = ts.findMemberClass(t.toClass(),
                                       name.id(),
                                       c.currentClass());
            }
            catch (NoClassException e) {
                return null;
            }
            if (n instanceof ClassType) {
                ClassType ct = (ClassType) n;
                if (!ts.classAccessible(ct, c)) {
                    throw new SemanticException("Cannot access type " + ct
                            + ".");
                }
                return nf.CanonicalTypeNode(pos, ct);
            }
        }

        return null;
    }

    /**
     * @throws SemanticException
     */
    protected Node disambiguateExprPrefix(Expr e) throws SemanticException {
        // Must be a non-static field.
        if (exprOK()) {
            return nf.Field(pos, e, name).type(ts.unknownType(pos));
        }
        return null;
    }

    protected Node disambiguateNoPrefix() throws SemanticException {
        if (exprOK()) {
            // First try local variables and fields.
            VarInstance vi = c.findVariableSilent(name.id());

            if (vi != null) {
                Node n = disambiguateVarInstance(vi);
                if (n != null) return n;
            }
        }

        // no variable found. try types.
        if (typeOK()) {
            try {
                Named n = c.find(name.id());
                if (n instanceof Type) {
                    Type type = (Type) n;
                    if (!type.isCanonical()) {
                        throw new InternalCompilerError("Found an ambiguous type in the context: "
                                + type, pos);
                    }
                    return nf.CanonicalTypeNode(pos, type);
                }
            }
            catch (NoClassException e) {
                if (!name.id().equals(e.getClassName())) {
                    // hmm, something else must have gone wrong
                    // rethrow the exception
                    throw e;
                }

                // couldn't find a type named name.
                // It must be a package--ignore the exception.
            }
        }

        // Must be a package then...
        if (packageOK()) {
            return nf.PackageNode(pos, ts.packageForName(name.id()));
        }

        return null;
    }

    protected Node disambiguateVarInstance(VarInstance vi)
            throws SemanticException {
        if (vi instanceof FieldInstance) {
            FieldInstance fi = (FieldInstance) vi;
            Receiver r = makeMissingFieldTarget(fi);
            return nf.Field(pos, r, name)
                     .fieldInstance(fi)
                     .targetImplicit(true)
                     .type(ts.unknownType(pos));
        }
        else if (vi instanceof LocalInstance) {
            LocalInstance li = (LocalInstance) vi;
            return nf.Local(pos, name)
                     .localInstance(li)
                     .type(ts.unknownType(pos));
        }
        return null;
    }

    protected Receiver makeMissingFieldTarget(FieldInstance fi)
            throws SemanticException {
        Receiver r;
        // Get the enclosing class which
        // brought the field into scope.  This is different
        // from fi.container().  fi.container() returns a super
        // type of the class we want.
        ClassType scope = c.findFieldScope(name.id());

        if (fi.flags().isStatic()) {
            // Even if the field is static, the receiver type is not necessarily
            // fi.container() because fi.container() could be inaccessible
            // from the context class.  The appropriate receiver type is the
            // scope class, which inherits the field.
            r = nf.CanonicalTypeNode(pos, scope);
        }
        else {
            // The field is non-static, so we must prepend with
            // "this", but we need to determine if the "this"
            // should be qualified.
            if (!ts.equals(scope, c.currentClass()))
                r = nf.This(pos.startOf(), nf.CanonicalTypeNode(pos, scope));
            else r = nf.This(pos.startOf());
        }

        return r;
    }

    protected boolean typeOK() {
        return !(amb instanceof Expr)
                && (amb instanceof TypeNode || amb instanceof QualifierNode
                        || amb instanceof Receiver || amb instanceof Prefix);

    }

    protected boolean packageOK() {
        return !(amb instanceof Receiver)
                && (amb instanceof QualifierNode || amb instanceof Prefix);
    }

    protected boolean exprOK() {
        return !(amb instanceof QualifierNode) && !(amb instanceof TypeNode)
                && (amb instanceof Expr || amb instanceof Receiver
                        || amb instanceof Prefix);
    }

    @Override
    public String toString() {
        return "Disamb(" + amb.getClass().getName() + ": " + amb + ")";
    }
}
