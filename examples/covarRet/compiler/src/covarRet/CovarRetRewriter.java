package jltools.ext.covarRet;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.frontend.*;
import jltools.visit.*;
import java.util.*;

/**
 * This visitor rewrites the AST to translate from Java with covariant returns
 * to standard Java.
 */
public class CovarRetRewriter extends SemanticVisitor
{
    public CovarRetRewriter(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    public Node leaveCall(Node n) {
        if (n instanceof Call) {
            /* Add a cast to the appropriate subclass, if neccssary */
            Call c = (Call) n;

            Position p = c.position();

            // Insert a cast, (always at the moment)
            Type overridenRetType = getOverridenReturnType(c.methodInstance());
            if (overridenRetType != null && !overridenRetType.isImplicitCastValid(c.expectedType())) {
              // The overriden return type cannot be implicitly cast to the
              // expected type, so explicitly cast it.
              NodeFactory nf = nodeFactory();
              return nf.Cast(p, nf.CanonicalTypeNode(p, c.methodInstance().returnType()), c);
            }
        }
        else if (n instanceof MethodDecl) {
            // Change the return type of the overridden method
            // to be the same as the superclass's
            MethodDecl md = (MethodDecl)n;
            Position p = md.position();

            MethodInstance mi = md.methodInstance();
            Type overridenRetType = getOverridenReturnType(mi);
            if (overridenRetType != null) {
              return md.returnType(nodeFactory().CanonicalTypeNode(p, overridenRetType));
            }
        }

        return n;
    }

    /**
     * Get the return type of the method that mi overrides if
     * that return type varies from the return type of the method mi.
     * Return null otherwise.
     */
    private Type getOverridenReturnType(MethodInstance mi) {
      Type t = mi.container().superType();
      Type retType = null;

      while (t instanceof ReferenceType) {
          ReferenceType rt = (ReferenceType) t;
          t = rt.superType();

          for (Iterator j = rt.methods().iterator(); j.hasNext(); ) {
              MethodInstance mj = (MethodInstance) j.next();

              if (! mi.name().equals(mj.name()) ||
                  ! ts.hasSameArguments(mi, mj) ||
                  ! ts.isAccessible(mj, this.context)) {

                  continue;
              }

              if (ts.isSubtype(mi.returnType(), mj.returnType()) && !ts.isSame(mi.returnType(), mj.returnType())) {
                // mj.returnType() is the type to use!
                retType = mj.returnType();
              }

          }
      }
      return retType;
    }
}
