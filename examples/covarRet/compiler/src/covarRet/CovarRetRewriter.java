package jltools.ext.covarRet;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.frontend.*;
import jltools.visit.*;
import java.util.*;

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
              NodeFactory nf = nodeFactory();
              return nf.Cast(p, nf.CanonicalTypeNode(p, c.methodInstance().returnType()), c);
            }
        }
        else if (n instanceof MethodDecl) {
            /* Change the return type to be the same as the superclass's */
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

    private Type getOverridenReturnType(MethodInstance mi) {
      Type t = mi.container().superType();

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
                return mj.returnType();
              }

          }
      }
      return null;
    }
}
