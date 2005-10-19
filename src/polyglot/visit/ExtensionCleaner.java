package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.frontend.ExtensionInfo;
import polyglot.util.*;
import polyglot.types.Package;

import java.util.*;

/**
 * This visitor overwrites all extension object refs with null,
 * sets delegate object refs to point back to the node,
 * and strips type information out.
 **/
public class ExtensionCleaner extends NodeVisitor {
    protected NodeFactory nf;
    protected TypeSystem ts;
    protected ExtensionInfo javaExt;

    public ExtensionCleaner(ExtensionInfo javaExt) {
        this.javaExt = javaExt;
        this.nf = javaExt.nodeFactory();
        this.ts = javaExt.typeSystem();
    }

    public Node leave(Node old, Node n, NodeVisitor v) {
        n = n.ext(null);
        n = n.del(null);
        n = strip(n);
        return n;
    }

    protected Node strip(Node n) {
        // FIXME: should use method dispatch for this.
        if (n instanceof Call) {
            n = ((Call) n).methodInstance(null);
        }

        if (n instanceof ClassDecl) {
            n = ((ClassDecl) n).type(null);
        }

        if (n instanceof ConstructorCall) {
            n = ((ConstructorCall) n).constructorInstance(null);
        }

        if (n instanceof Field) {
            n = ((Field) n).fieldInstance(null);
        }

        if (n instanceof FieldDecl) {
            n = ((FieldDecl) n).fieldInstance(null);
            n = ((FieldDecl) n).initializerInstance(null);
        }

        if (n instanceof Formal) {
            n = ((Formal) n).localInstance(null);
        }

        if (n instanceof Initializer) {
            n = ((Initializer) n).initializerInstance(null);
        }

        if (n instanceof Local) {
            n = ((Local) n).localInstance(null);
        }

        if (n instanceof LocalDecl) {
            n = ((LocalDecl) n).localInstance(null);
        }

        if (n instanceof MethodDecl) {
            n = ((MethodDecl) n).methodInstance(null);
        }

        if (n instanceof New) {
            n = ((New) n).anonType(null);
            n = ((New) n).constructorInstance(null);
        }

        if (n instanceof TypeNode) {
            n = convert((TypeNode) n);
        }

        if (n instanceof PackageNode) {
            n = convert((PackageNode) n);
        }

        if (n instanceof Expr) {
            n = ((Expr) n).type(null);
        }

        return n;
    }

    protected TypeNode convert(TypeNode n) {
        Type t = n.type();

        if (n instanceof CanonicalTypeNode) {
            if (t.typeSystem() == ts) {
                return n;
            }
            else {
                throw new InternalCompilerError("Unexpected Jx type: " + t + " found in rewritten AST.");
            }
        }

        // Must be an AmbTypeNode

        if (t != null && t.isCanonical()) {
            if (t.typeSystem() == ts) {
                return nf.CanonicalTypeNode(n.position(), t);
            }
        }

        n = n.type(null);

        return n;
    }

    protected PackageNode convert(PackageNode n) {
        Package p = n.package_();

        if (p != null && p.isCanonical()) {
            if (p.typeSystem() == ts) {
                return nf.PackageNode(n.position(), p);
            }
        }
                  
        return nf.PackageNode(n.position(), ts.createPackage(n.toString()));
    }
}
