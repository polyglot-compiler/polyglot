package polyglot.visit;

import java.util.*;
import java.util.ArrayList;
import java.util.Iterator;

import polyglot.ast.*;
import polyglot.types.SemanticException;

/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself.
 */
public class SupertypeDisambiguator extends Disambiguator
{
    public SupertypeDisambiguator(DisambiguationDriver dd) {
        super(dd);
    }
    
    public Node override(Node parent, Node n) {
        if (n instanceof ClassDecl) {
            ClassDecl cd = (ClassDecl) n;
            Node old = cd;
            
            // Call enter to handle scoping. 
            SupertypeDisambiguator v = (SupertypeDisambiguator) enter(parent, cd);
            
            // Now visit the supertypes only.
            cd = cd.superClass((TypeNode) cd.visitChild(cd.superClass(), v));
            if (v.hasErrors()) return cd;
            
            List newInterfaces = new ArrayList();
            for (Iterator i = cd.interfaces().iterator(); i.hasNext(); ) {
                TypeNode tn = (TypeNode) i.next();
                newInterfaces.add((TypeNode) cd.visitChild(tn, v));
                if (v.hasErrors()) return cd;
            }
            cd = cd.interfaces(newInterfaces);
            
            // Force the supertypes of cd.type() to be updated.
            cd = (ClassDecl) leave(parent, old, cd, v);
            if (this.hasErrors()) return cd;
            
            // Now visit the class body.
            cd = cd.body((ClassBody) cd.visitChild(cd.body(), v));
            if (v.hasErrors()) return cd;
            
            // Finally, rebulid the node again.
            return leave(parent, old, cd, v);
        }
        
        // Skip ClassMembers that are not ClassDecls.  These will be
        // handled by the SignatureDisambiguator visitor.
        if (n instanceof ClassMember) {
            return n;
        }
        
        if (n instanceof Stmt || n instanceof Expr) {
            return n;
        }
        
        return super.override(parent, n);
    }
}
