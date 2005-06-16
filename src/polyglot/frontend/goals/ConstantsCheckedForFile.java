/*
 * TypeChecked.java
 * 
 * Author: nystrom
 * Creation date: Dec 19, 2004
 */
package polyglot.frontend.goals;

import java.util.*;
import java.util.Iterator;

import polyglot.ast.*;
import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.passes.ConstantCheckPass;
import polyglot.main.Report;
import polyglot.types.ParsedClassType;
import polyglot.types.TypeSystem;
import polyglot.visit.ConstantChecker;
import polyglot.visit.NodeVisitor;


public class ConstantsCheckedForFile extends SourceFileGoal {
    boolean reached;
    
    public ConstantsCheckedForFile(Job job) {
        super(job);
        this.reached = false;
    }

    public Pass createPass(ExtensionInfo extInfo) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return new ConstantCheckPass(this, new ConstantChecker(job(), ts, nf));
    }

    public int distanceFromGoal() {
        final Collection TOPICS = new ArrayList(ConstantsCheckedForFile.TOPICS);
        TOPICS.add("const-check");
    
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
        
        // Now look for ambiguities in the AST.
        final int[] notOkCount = new int[] { 0 };
        
        job().ast().visit(new NodeVisitor() {
            public Node override(Node n) {
                boolean ok = true;
                if (! n.isTypeChecked()) {
                    // We should depend on type checking, so there's no need
                    // to add any new dependencies.
                    ok = false;
                }
                else if (n instanceof Expr) {
                    if (! ((Expr) n).constantValueSet()) {
                        ok = false;
                    }
                }
                else if (n instanceof VarInit) {
                    if (! ((VarInit) n).constantValueSet()) {
                        ok = false;
                    }
                }
                
                if (! ok) {
                    notOkCount[0]++;
                    if (Report.should_report(TOPICS, 3))
                        Report.report(3, "  not ok at " + n + " (" + n.getClass().getName() + ")");
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