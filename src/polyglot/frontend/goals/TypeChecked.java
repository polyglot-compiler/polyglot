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


public class TypeChecked extends SourceFileGoal {
    boolean reached;
    
    public TypeChecked(Job job) {
        super(job);
        this.reached = false;
    }

    public Pass createPass(ExtensionInfo extInfo) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return new TypeCheckPass(this, new TypeChecker(job(), ts, nf));
    }

    public int distanceFromGoal() {
        if (Report.should_report(TOPICS, 3))
            Report.report(3, "checking " + this);

        if (this.reached) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "  ok (cached)");
            return 0;
        }
        
        if (! hasBeenRun()) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "  not run yet");
            return Integer.MAX_VALUE;
        }
        
        if (job().ast() == null) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "  null ast for " + job());
            return Integer.MAX_VALUE;
        }
        
        /*
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
*/
        
        // Now look for ambiguities in the AST.
        final int[] notOkCount = new int[] { 0 };
        
        job().ast().visit(new NodeVisitor() {
            public Node override(Node n) {
                if (! n.isTypeChecked()) {
                    if (Report.should_report(TOPICS, 3))
                        Report.report(3, "  not ok at " + n);
                    notOkCount[0]++;
                }
                
                return null;
            }
        });
        
        if (notOkCount[0] == 0) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "  ok");
            this.reached = true;
            return 0;
        }
        
        return notOkCount[0];
    }
    
    private static final Collection TOPICS = Arrays.asList(new String[] { Report.types, Report.frontend });
}
