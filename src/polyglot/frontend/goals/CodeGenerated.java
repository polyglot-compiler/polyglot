/*
 * CompileGoal.java
 * 
 * Author: nystrom
 * Creation date: Dec 14, 2004
 */
package polyglot.frontend.goals;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.types.TypeSystem;
import polyglot.visit.Translator;

/**
 * Comment for <code>CompileGoal</code>
 *
 * @author nystrom
 */
public class CodeGenerated extends SourceFileGoal {

    /**
     * @param job The job to compile.
     */
    public CodeGenerated(Job job) {
        super(job);
    }
    
    public Pass createPass(ExtensionInfo extInfo) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return new OutputPass(this, new Translator(this, ts, nf,
                                                   extInfo.targetFactory()));
    }
    
}
