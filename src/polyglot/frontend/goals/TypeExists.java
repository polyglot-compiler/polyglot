/*
 * TypeExists
 * 
 * Author: nystrom
 * Creation date: Dec 15, 2004
 */
package polyglot.frontend.goals;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.passes.TypeExistsPass;
import polyglot.types.TypeSystem;

/**
 * Comment for <code>TypeExists</code>
 *
 * @author nystrom
 */
public class TypeExists extends AbstractGoal {
    private String typeName;
    private boolean satisfied;
    
    public TypeExists(String name) {
        super(null);
        this.typeName = name;
    }
    
    public Pass createPass(ExtensionInfo extInfo) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return new TypeExistsPass(extInfo.scheduler(), ts, this);
    }
    
    public String typeName() {
        return typeName;
    }

    public void markReached() {
        this.satisfied = true;
    }
    
    public boolean reached() {
        return this.satisfied;
    }
    
    public int hashCode() {
        return typeName.hashCode() + super.hashCode();
    }
    
    public boolean equals(Object o) {
        return o instanceof TypeExists && ((TypeExists) o).typeName.equals(typeName) && super.equals(o);
    }
    
    public String toString() {
        return "TypeExists(" + typeName + ")";
    }
}
