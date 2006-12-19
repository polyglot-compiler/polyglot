/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * Copyright (c) 2006 IBM Corporation
 * 
 */

package polyglot.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.Position;
import polyglot.util.InternalCompilerError;
import polyglot.visit.ContextVisitor;

/**
 * Utility class which is used to disambiguate ambiguous
 * AST nodes (Expr, Type, Receiver, Qualifier, Prefix).
 */
public class Disamb_c implements Disamb
{
    protected ContextVisitor v;
    protected Position pos;
    protected Prefix prefix;
    protected String name;

    protected NodeFactory nf;
    protected TypeSystem ts;
    protected Context c;
    protected Ambiguous amb;

    /**
     * Disambiguate the prefix and name into a unambiguous node.
     * @return An unambiguous AST node, or null if disambiguation
     *         fails.
     */
    public Node disambiguate(Ambiguous amb, ContextVisitor v, Position pos,
            Prefix prefix, String name) throws SemanticException {

        this.v = v;
        this.pos = pos;
        this.prefix = prefix;
        this.name = name;
        this.amb = amb;

        nf = v.nodeFactory();
        ts = v.typeSystem();
        c = v.context();

        if (prefix instanceof Ambiguous) {
            throw new SemanticException(
                "Cannot disambiguate node with ambiguous prefix.");
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

    protected Node disambiguatePackagePrefix(PackageNode pn) throws SemanticException {
        Resolver pc = ts.packageContextResolver(pn.package_());

        Named n;
        
        try {
            n = pc.find(name);
        }
        catch (SemanticException e) {
            return null;
        }

        Qualifier q = null;

        if (n instanceof Qualifier) {
            q = (Qualifier) n;
        } else {
            return null;
        }
        
        if (q.isPackage() && packageOK()) {
            return nf.PackageNode(pos, q.toPackage());
        }
        else if (q.isType() && typeOK()) {
            return nf.CanonicalTypeNode(pos, q.toType());
        }

        return null;
    }


    protected Node disambiguateTypeNodePrefix(TypeNode tn) 
        throws SemanticException 
    {
        // Try static fields.
        Type t = tn.type();

        if (t.isReference() && exprOK()) {
            try {
                FieldInstance fi = ts.findField(t.toReference(), name, c.currentClass());
                return nf.Field(pos, tn, name).fieldInstance(fi);
            } catch (NoMemberException e) {
                if (e.getKind() != e.FIELD) {
                    // something went wrong...
                    throw e;
                }
                
                // ignore so we can check if we're a member class.
            }
        }

        // Try member classes.
        if (t.isClass() && typeOK()) {
            Resolver tc = t.toClass().resolver();
            Named n;
            try {
                n = tc.find(name);
            }
            catch (NoClassException e) {
                return null;
            }
            if (n instanceof Type) {
                Type type = (Type) n;
                return nf.CanonicalTypeNode(pos, type);
            }
        }

        return null;
    }

    protected Node disambiguateExprPrefix(Expr e) throws SemanticException {
        // Must be a non-static field.
        if (exprOK()) {
            return nf.Field(pos, e, name);
        }
        return null;
    }

    protected Node disambiguateNoPrefix() throws SemanticException {
        if (exprOK()) {
            // First try local variables and fields.
            VarInstance vi = c.findVariableSilent(name);
            
            if (vi != null) {
                Node n = disambiguateVarInstance(vi);
                if (n != null) return n;
            }
        }
        
        // no variable found. try types.
        if (typeOK()) {
            try {
                Named n = c.find(name);
                if (n instanceof Type) {
                    Type type = (Type) n;
                    if (! type.isCanonical()) {
                        throw new InternalCompilerError("Found an ambiguous type in the context: " + type, pos);
                    }
                    return nf.CanonicalTypeNode(pos, type);
                }
            } catch (NoClassException e) {
                if (!name.equals(e.getClassName())) {
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
            return nf.PackageNode(pos, ts.packageForName(name));
        }

        return null;
    }

    protected Node disambiguateVarInstance(VarInstance vi) throws SemanticException {
        if (vi instanceof FieldInstance) {
            FieldInstance fi = (FieldInstance) vi;
            Receiver r = makeMissingFieldTarget(fi);
            return nf.Field(pos, r, name).fieldInstance(fi).targetImplicit(true);
        } else if (vi instanceof LocalInstance) {
            LocalInstance li = (LocalInstance) vi;
            return nf.Local(pos, name).localInstance(li);
        }
        return null;
    }

    protected Receiver makeMissingFieldTarget(FieldInstance fi) throws SemanticException {
        Receiver r;

        if (fi.flags().isStatic()) {
            r = nf.CanonicalTypeNode(pos, fi.container());
        } else {
            // The field is non-static, so we must prepend with
            // "this", but we need to determine if the "this"
            // should be qualified.  Get the enclosing class which
            // brought the field into scope.  This is different
            // from fi.container().  fi.container() returns a super
            // type of the class we want.
            ClassType scope = c.findFieldScope(name);

            if (! ts.equals(scope, c.currentClass())) {
                r = nf.This(pos.startOf(), nf.CanonicalTypeNode(pos, scope));
            } else {
                r = nf.This(pos.startOf());
            }
        }

        return r;
    }

    protected boolean typeOK() {
        return ! (amb instanceof Expr) &&
              (amb instanceof TypeNode || amb instanceof QualifierNode ||
               amb instanceof Receiver || amb instanceof Prefix);

    }

    protected boolean packageOK() {
        return ! (amb instanceof Receiver) &&
              (amb instanceof QualifierNode || amb instanceof Prefix);
    }

    protected boolean exprOK() {
        return ! (amb instanceof QualifierNode) &&
               ! (amb instanceof TypeNode) &&
              (amb instanceof Expr || amb instanceof Receiver ||
               amb instanceof Prefix);
    }
    
    public String toString() {
        return "Disamb(" + amb.getClass().getName() + ": " + amb + ")";
    }
}
