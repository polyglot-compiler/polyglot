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

    public boolean reached() {
        if (Report.should_report(TOPICS, 3))
            Report.report(3, "checking " + this);

        if (this.reached) {
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
        
        // Now look for ambiguities in the AST.
        final boolean[] allOk = new boolean[] { true };
        
        job().ast().visit(new NodeVisitor() {
            public Node override(Node n) {
                if (! allOk[0]) {
                    return n;
                }
                
                if (! n.isCanonical()) {
                    allOk[0] = false;
                }
                else if (n instanceof Expr) {
                    if (! ((Expr) n).constantValueSet()) {
                        allOk[0] = false;
                    }
                }
                else if (n instanceof VarInit) {
                    if (! ((VarInit) n).constantValueSet()) {
                        allOk[0] = false;
                    }
                }
                
                if (!allOk[0]) {
                    if (Report.should_report(TOPICS, 3))
                        Report.report(3, "  not ok at " + n);
                }
                
                return allOk[0] ? null : n;
            }
        });
        
        if (allOk[0]) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "  ok");
            this.reached = true;
            return true;
        }
        
        return false;
    }
    
    private static final Collection TOPICS = Arrays.asList(new String[] { Report.types, Report.frontend });
}