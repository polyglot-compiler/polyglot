package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * Utility class which is used to disambiguate ambiguous
 * AST nodes (Expr, Type, Receiver, Qualifier, Prefix).
 */
public class Disamb_c implements Disamb
{
    protected SemanticVisitor v;
    protected Position pos;
    protected Prefix prefix;
    protected String name;

    protected NodeFactory nf;
    protected TypeSystem ts;
    protected Context c;

    /**
     * Disambiguate the prefix and name into a unambiguous node type.
     * @return An unambiguous AST node, or null if disambiguation
     *         fails.
     */
    public Node disambiguate(SemanticVisitor v, Position pos,
            Prefix prefix, String name) throws SemanticException {

        this.v = v;
        this.pos = pos;
        this.prefix = prefix;
        this.name = name;

        nf = v.nodeFactory();
        ts = v.typeSystem();
        c = v.context();

        if (prefix instanceof Ambiguous) {
            throw new SemanticException(
                "Cannot disambiguate node with ambiguous prefix.");
        }

        if (prefix instanceof PackageNode) {
            PackageNode pn = (PackageNode) prefix;
            return disambiguatePackagePrefix(pn);
        } else if (prefix instanceof TypeNode) {
            TypeNode tn = (TypeNode) prefix;
            return disambiguateTypeNodePrefix(tn);
        } else if (prefix instanceof Expr) {
            Expr e = (Expr) prefix;
            return disambiguateExprPrefix(e);
        } else if (prefix == null) {
            return disambiguateNoPrefix();
        }

        return null;
    }

    protected Node disambiguatePackagePrefix(PackageNode pn) throws SemanticException {

        Resolver pc = ts.packageContextResolver(c.importTable(), pn.package_());
        Qualifier q = pc.findQualifier(name);

        if (q.isPackage()) {
            return nf.PackageNode(pos, q.toPackage());
        } else if (q.isType()) {
            return nf.CanonicalTypeNode(pos, q.toType());
        } else {
            return null;
        }
    }


    protected Node disambiguateTypeNodePrefix(TypeNode tn) throws SemanticException {

        // Try static fields.
        Type t = tn.type();

        if (t.isReference()) {
            try {
                FieldInstance fi = ts.findField(t.toReference(), name, c);
                return nf.Field(pos, tn, name);
            } catch (SemanticException e) {
                // ignore so we can check if we're a member class.
            }
        }

        // Try member classes.
        if (t.isClass()) {
            Resolver tc = ts.classContextResolver(t.toClass());
            Type type = tc.findType(name);
            return nf.CanonicalTypeNode(pos, type);
        }

        return null;
    }

    protected Node disambiguateExprPrefix(Expr e) throws SemanticException {
        // Must be a non-static field.
        return nf.Field(pos, e, name);
    }

    protected Node disambiguateNoPrefix() throws SemanticException {

        try {
            // First try local variables and fields.
            VarInstance vi = c.findVariable(name);

            if (vi instanceof FieldInstance) {
                FieldInstance fi = (FieldInstance) vi;
                Receiver r = makeMissingFieldTarget(fi);
                return nf.Field(pos, r, name);
            } else if (vi instanceof LocalInstance) {
                LocalInstance li = (LocalInstance) vi;
                return nf.Local(pos, name).localInstance(li);
            }
        } catch (SemanticException e) {
            // Then try types.
            try {
                Type type = c.findType(name);
                return nf.CanonicalTypeNode(pos, type);
            } catch (SemanticException e2) {
                // must be a package--ignore the error
            }
        }

        return nf.PackageNode(pos, ts.packageForName(name));
    }

    protected Receiver makeMissingFieldTarget(FieldInstance fi) throws SemanticException {
        Receiver r;

        if (fi.flags().isStatic()) {
            r = nf.CanonicalTypeNode(pos, ts.staticTarget(fi.container()));
        } else {
            // The field is non-static, so we must prepend with
            // "this", but we need to determine if the "this"
            // should be qualified.  Get the enclosing class which
            // brought the field into scope.  This is different
            // from fi.container().  fi.container() returns a super
            // type of the class we want.
            ParsedClassType scope = c.findFieldScope(name);

            if (! scope.isSame(c.currentClass())) {
                r = nf.This(pos, nf.CanonicalTypeNode(pos, ts.staticTarget(scope)));
            } else {
                r = nf.This(pos);
            }
        }

        return r;
    }

}


