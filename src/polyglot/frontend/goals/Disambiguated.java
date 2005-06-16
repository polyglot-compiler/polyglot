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
import polyglot.frontend.passes.DisambiguatorPass;
import polyglot.frontend.passes.TypeCheckPass;
import polyglot.main.Report;
import polyglot.types.ParsedClassType;
import polyglot.types.TypeSystem;
import polyglot.visit.*;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeChecker;


public class Disambiguated extends SourceFileGoal {
    boolean reached;

    public Disambiguated(Job job) {
        super(job);
        this.reached = false;
    }

    public Pass createPass(ExtensionInfo extInfo) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return new DisambiguatorPass(this, new AmbiguityRemover(job(), ts, nf));
    }

    /**
     * Return true if the disambiguation pass has been run at least once
     * (this forces dependent classes to get loaded) and isDisambiguated()
     * is true for all nodes in the AST (except possibly those within a
     * local or anonymous class).
     */
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
        
        /* XXX breaks for anonymous classes.
        // Do a relatively quick test of the types declared in the AST.
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
        int count = AmbiguityRemover.astAmbiguityCount(job.ast());

        if (count == 0) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "  ok");
            this.reached = true;
            return 0;
        }

        return count;
    }
    
    private static final Collection TOPICS = Arrays.asList(new String[] { Report.types, Report.frontend });
}
