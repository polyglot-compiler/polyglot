/*
 * TypeChecked.java
 * 
 * Author: nystrom
 * Creation date: Dec 19, 2004
 */
package polyglot.frontend.goals;

import java.util.*;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.frontend.passes.TypeCheckPass;
import polyglot.main.Report;
import polyglot.types.ParsedClassType;
import polyglot.types.TypeSystem;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeChecker;


public class TypeChecked extends CyclicSourceFileGoal {
    public TypeChecked(Job job) {
        super(job);
    }

    public Pass createPass(ExtensionInfo extInfo) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return new TypeCheckPass(this, new TypeChecker(this, ts, nf));
    }

    public boolean reached() {
        if (Report.should_report(TOPICS, 3))
            Report.report(3, "checking " + this);

        if (super.reached()) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "  ok (cached)");
            return true;
        }
        
        if (! hasBeenRun()) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "  not run yet");
            return false;
        }
        
        if (job().ast() == null) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "  null ast for " + job());
            return false;
        }
        
        // Do a relatively quick test of the types contained in the AST.
        for (Iterator i = job().ast().typesBelow().iterator(); i.hasNext(); ) {
            ParsedClassType ct = (ParsedClassType) i.next();
            if (! ct.signaturesResolved()) {
                if (Report.should_report(TOPICS, 3))
                    Report.report(3, "  signatures for " + ct + " ambiguous");
                Scheduler scheduler = job().extensionInfo().scheduler();
                scheduler.addConcurrentDependency(this, scheduler.SignaturesResolved(ct));
                return false;
            }
        }
        
        // Now look for ambiguities in the AST.
        final boolean[] allOk = new boolean[] { true };
        
        job().ast().visit(new NodeVisitor() {
            public Node override(Node n) {
                if (! allOk[0]) {
                    return n;
                }
                
                if (! n.isCanonical()) {
                    if (Report.should_report(TOPICS, 3))
                        Report.report(3, "  not ok at " + n);
                    allOk[0] = false;
                    return n;
                }
                
                return null;
            }
        });
        
        if (allOk[0]) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "  ok");
            this.markReached();
        }
        
        return super.reached();
    }
    
    private static final Collection TOPICS = Arrays.asList(new String[] { Report.types, Report.frontend });
}
