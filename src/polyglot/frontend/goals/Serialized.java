/*
 * Serialized.java
 * 
 * Author: nystrom
 * Creation date: Dec 19, 2004
 */
package polyglot.frontend.goals;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.Compiler;
import polyglot.types.TypeSystem;
import polyglot.visit.ClassSerializer;
import polyglot.visit.NodeVisitor;


public class Serialized extends SourceFileGoal {
    public Serialized(Job job) { super(job); }
    
    public Pass createPass(ExtensionInfo extInfo) {
        Compiler compiler = extInfo.compiler();
        if (compiler.serializeClassInfo()) {
            TypeSystem ts = extInfo.typeSystem();
            NodeFactory nf = extInfo.nodeFactory();
            return new VisitorPass(this,
                                   new ClassSerializer(ts,
                                                       nf,
                                                       job().source().lastModified(),
                                                       compiler.errorQueue(),
                                                       extInfo.version()));
        }
        else {
            return new EmptyPass(this) {
                public boolean run() {
                    Serialized.this.markRun();
                    return true;
                }
            };
        }
    }
}