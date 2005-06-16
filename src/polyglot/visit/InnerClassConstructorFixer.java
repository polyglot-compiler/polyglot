/*
 * Created on May 18, 2005
 */
package polyglot.visit;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.*;

/**
 * @author nystrom
 *
 * This class translates inner classes to static nested classes with a field
 * pointing to the enclosing instance.
 */
public class InnerClassConstructorFixer extends InnerClassAbstractRemover
{
    public InnerClassConstructorFixer(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    protected Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        if (n instanceof New) {
            New newExp = (New) n;
            ClassType ct = (ClassType) newExp.objectType().type();
            
            // If instantiating an inner class, pass in the environment at
            // the class declaration.  env(ct) will be empty of the class
            // was not inner.
            List newArgs = new ArrayList(newExp.arguments());
            newArgs.addAll(envAsActuals(env(ct, true), ct.outer(), newExp.qualifier()));
            newExp = (New) newExp.arguments(newArgs);
            
            // Remove the qualifier.
            // FIXME: should pass in with arguments.
            // FIXME: need a barrier after this pass.
            // FIXME: should rewrite "new" after the barrier.
            // or should pass in all enclosing classes
            newExp = (New) newExp.qualifier(null);
            
            n = newExp;
        }
        
        if (n instanceof ConstructorCall) {
            ConstructorCall cc = (ConstructorCall) n;
            
            ClassType ct = context.currentClass();
            
            if (cc.kind() == ConstructorCall.THIS) {
                List newArgs = new ArrayList();
                newArgs.addAll(cc.arguments());
                newArgs.addAll(envAsActuals(env(ct, true), ct.outer(), cc.qualifier()));
                
                ConstructorCall newCC = (ConstructorCall) cc.arguments(newArgs);
                newCC = newCC.qualifier(null);
                n = newCC;
            }
            else {
                // adjust the super call arguments
                List newArgs = new ArrayList();
                newArgs.addAll(cc.arguments());
                ClassType sup = (ClassType) ct.superType();
                if (sup.isInnerClass()) {
                    newArgs.addAll(envAsActuals(env(sup, true), sup.outer(), cc.qualifier()));
                }
                else {
                    newArgs.addAll(envAsActuals(env(sup, true), null, null));
                }
                
                ConstructorCall newCC = (ConstructorCall) cc.arguments(newArgs);
                newCC = newCC.qualifier(null);
                n = newCC;
            }
        }
              
          n = super.leaveCall(old, n, v);
          return n;
    }
}
