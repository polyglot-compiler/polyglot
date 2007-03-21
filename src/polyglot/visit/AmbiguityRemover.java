/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.visit;

import java.util.*;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.frontend.Job;
import polyglot.frontend.MissingDependencyException;
import polyglot.frontend.goals.Goal;
import polyglot.main.Report;
import polyglot.types.*;
import polyglot.util.*;

/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself.
 */
public class AmbiguityRemover extends DisambiguationDriver
{
    protected boolean visitSigs;
    protected boolean visitBodies;
    
    public AmbiguityRemover(Job job, TypeSystem ts, NodeFactory nf) {
        this(job, ts, nf, true, true);
    }
    
    public AmbiguityRemover(Job job, TypeSystem ts, NodeFactory nf, boolean visitSigs, boolean visitBodies) {
        super(job, ts, nf);
        this.visitSigs = visitSigs;
        this.visitBodies = visitBodies;
    }
    
    public Node override(Node parent, Node n) {
        if (! visitSigs && n instanceof ClassMember && ! (n instanceof ClassDecl)) {
            return n;
        }
        if ((! visitBodies || ! visitSigs) && parent instanceof ClassMember) {
            if (parent instanceof FieldDecl && ((FieldDecl) parent).init() == n) {
                return n;
            }
            if (parent instanceof CodeDecl && ((CodeDecl) parent).body() == n) {
                return n;
            }
        }
        
        try {
            if (Report.should_report(Report.visit, 2))
                Report.report(2, ">> " + this + "::override " + n + " (" + n.getClass().getName() + ")");
            
            Node m = n.del().disambiguateOverride(parent, this);
            
            if (Report.should_report(Report.visit, 2))
                Report.report(2, "<< " + this + "::override " + n + " -> " + m + (m != null ? (" (" + m.getClass().getName() + ")") : ""));
            
            return m;
        }
        catch (MissingDependencyException e) {
            if (Report.should_report(Report.frontend, 3))
                e.printStackTrace();
            Scheduler scheduler = job.extensionInfo().scheduler();
            Goal g = scheduler.currentGoal();
            scheduler.addDependencyAndEnqueue(g, e.goal(), e.prerequisite());
            g.setUnreachableThisRun();
            if (this.rethrowMissingDependencies) {
                throw e;
            }
            return n;
        }
        catch (SemanticException e) {
            if (e.getMessage() != null) {
                Position position = e.position();
                
                if (position == null) {
                    position = n.position();
                }
                
                this.errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR,
                                     e.getMessage(), position);
            }
            else {
                // silent error; these should be thrown only
                // when the error has already been reported 
            }
            
            return n;
        }    
    }
    
    protected NodeVisitor enterCall(Node n) throws SemanticException {
        if (Report.should_report(Report.visit, 2))
            Report.report(2, ">> " + this + "::enter " + n + " (" + n.getClass().getName() + ")");
        
        AmbiguityRemover v = (AmbiguityRemover) n.del().disambiguateEnter(this);
        
        if (Report.should_report(Report.visit, 2))
            Report.report(2, "<< " + this + "::enter " + n+ " (" + n.getClass().getName() + ")" + " -> " + v);
        
        return v;
    }
    
    protected Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        if (Report.should_report(Report.visit, 2))
            Report.report(2, ">> " + this + "::leave " + n + " (" + n.getClass().getName() + ")");

        Node m = n.del().disambiguate((AmbiguityRemover) v);

        if (Report.should_report(Report.visit, 2))
            Report.report(2, "<< " + this + "::leave " + n + " -> " + m + (m != null ? (" (" + m.getClass().getName() + ")") : ""));
        
        return m;
    }
    
    public HaltingVisitor bypass(Collection c) {
        throw new InternalCompilerError("AmbiguityRemover does not support bypassing. " +
                                        "Implement any required functionality using " +
                                        "Node.disambiguateOverride(Node, AmbiguityRemover).");
    }
    public HaltingVisitor bypass(Node n) {
        throw new InternalCompilerError("AmbiguityRemover does not support bypassing. " +
                                        "Implement any required functionality using " +
                                        "Node.disambiguateOverride(Node, AmbiguityRemover).");
    }
   
    public HaltingVisitor bypassChildren(Node n) {
        throw new InternalCompilerError("AmbiguityRemover does not support bypassing. " +
                                        "Implement any required functionality using " +
                                        "Node.disambiguateOverride(Node, AmbiguityRemover).");
    }

    public boolean isASTDisambiguated(Node n) {
        return astAmbiguityCount(n) == 0;
    }

    protected static class AmbChecker extends NodeVisitor {
        public int notOkCount;

        public Node override(Node parent, Node n) {
            final Collection TOPICS = Arrays.asList(new String[] { Report.types, Report.frontend, "disam-check" });

            // Don't check if New is disambiguated; this is handled
            // during type-checking.
            if (n instanceof New) {
                return n;
            }

            if (! n.isDisambiguated()) {
                if (Report.should_report(TOPICS, 3))
                    Report.report(3, "  not ok at " + n + " (" + n.getClass().getName() + ")");
                notOkCount++;
            }
            
            return null;
        }
    }
    
    public static int astAmbiguityCount(Node n) {
        AmbChecker ac = new AmbChecker();
        n.visit(ac);
        return ac.notOkCount;
    }
}
