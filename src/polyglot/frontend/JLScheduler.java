/*
 * Scheduler.java
 * 
 * Author: nystrom
 * Creation date: Feb 6, 2005
 */
package polyglot.ext.jl;

import java.util.*;
import java.util.ArrayList;
import java.util.Collections;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.goals.*;
import polyglot.types.*;
import polyglot.types.FieldInstance;
import polyglot.types.ParsedClassType;
import polyglot.util.InternalCompilerError;
import polyglot.visit.*;
import polyglot.visit.ExceptionChecker;
import polyglot.visit.ReachChecker;

/**
 * Comment for <code>Scheduler</code>
 *
 * @author nystrom
 */
public class JLScheduler extends Scheduler {
    /**
     * @param extInfo
     */
    public JLScheduler(ExtensionInfo extInfo) {
        super(extInfo);
    }
    
    public Goal TypeExists(String name) {
        return internGoal(new TypeExists(name));
    }
    
    public Goal MembersAdded(ParsedClassType ct) {
        Goal g = internGoal(new MembersAdded(ct));
        return g;
    }

    public Goal SupertypesResolved(ParsedClassType ct) {
        Goal g = internGoal(new SupertypesResolved(ct));
        return g;
    }

    public Goal SignaturesResolved(ParsedClassType ct) {
        Goal g = internGoal(new SignaturesResolved(ct));
        return g;
    }

    public Goal FieldConstantsChecked(FieldInstance fi) {
        Goal g = internGoal(new FieldConstantsChecked(fi));
        return g;
    }
    
    public Goal Parsed(Job job) {
        return internGoal(new Parsed(job));
    }
    
    public Goal TypesInitialized(final Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = internGoal(new VisitorGoal(job, new TypeBuilder(job, ts, nf)) {
            public Collection prerequisiteGoals(Scheduler scheduler) {
                List l = new ArrayList();
                l.addAll(super.prerequisiteGoals(scheduler));
                l.add(Parsed(job));
                return l;
            }
        });
        return g;
 }
    
    public Goal TypesInitializedForCommandLine() {
        return internGoal(new Barrier("TYPES_INIT_BARRIER", this) {
            public Goal goalForJob(Job j) {
                return JLScheduler.this.TypesInitialized(j);
            }
//            
//            public Collection jobs() {
//                return JLScheduler.this.commandLineJobs();
//            }
        });
    }
    
    public Goal ImportTableInitialized(final Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = internGoal(new VisitorGoal(job, new InitImportsVisitor(job, ts, nf)) {
            public Collection prerequisiteGoals(Scheduler scheduler) {
                List l = new ArrayList();
                l.addAll(super.prerequisiteGoals(scheduler));
                l.add(TypesInitializedForCommandLine());
                l.add(TypesInitialized(job));
                return l;
            }
        });
        return g;
    }
    
    public Goal Disambiguated(final Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = internGoal(new VisitorGoal(job, new AmbiguityRemover(job, ts, nf)) {
            public Collection prerequisiteGoals(Scheduler scheduler) {
                List l = new ArrayList();
                l.addAll(super.prerequisiteGoals(scheduler));
                l.add(ImportTableInitialized(job));
                return l;
            }
        });
        return g;
    }
    
    public Goal TypeChecked(final Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = internGoal(new VisitorGoal(job, new TypeChecker(job, ts, nf)) {
            public Collection prerequisiteGoals(Scheduler scheduler) {
                List l = new ArrayList();
                l.addAll(super.prerequisiteGoals(scheduler));
                l.add(Disambiguated(job));
                return l;
            }
        });
        return g;
    }
    
    public Goal ConstantsChecked(final Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = internGoal(new VisitorGoal(job, new ConstantChecker(job, ts, nf)) {
            public Collection prerequisiteGoals(Scheduler scheduler) {
                List l = new ArrayList();
                l.addAll(super.prerequisiteGoals(scheduler));
                l.add(TypeChecked(job));
                return l;
            }
        });
        return g;
    }
    
    public Goal ReachabilityChecked(final Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = internGoal(new VisitorGoal(job, new ReachChecker(job, ts, nf)) {
            public Collection prerequisiteGoals(Scheduler scheduler) {
                List l = new ArrayList();
                l.addAll(super.prerequisiteGoals(scheduler));
                l.add(TypeChecked(job));
                l.add(ConstantsChecked(job));
                return l;
            }
        });
        return g;
    }
    
    public Goal ExceptionsChecked(final Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = internGoal(new VisitorGoal(job, new ExceptionChecker(job, ts, nf)) {
            public Collection prerequisiteGoals(Scheduler scheduler) {
                List l = new ArrayList();
                l.addAll(super.prerequisiteGoals(scheduler));
                l.add(TypeChecked(job));
                l.add(ReachabilityChecked(job));
                return l;
            }
        });
        return g;
    }
    
    public Goal ExitPathsChecked(final Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = internGoal(new VisitorGoal(job, new ExitChecker(job, ts, nf)) {
            public Collection prerequisiteGoals(Scheduler scheduler) {
                List l = new ArrayList();
                l.addAll(super.prerequisiteGoals(scheduler));
                l.add(ReachabilityChecked(job));
                return l;
            }
        });
        return g;
    }
    
    public Goal InitializationsChecked(final Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = internGoal(new VisitorGoal(job, new InitChecker(job, ts, nf)) {
            public Collection prerequisiteGoals(Scheduler scheduler) {
                List l = new ArrayList();
                l.addAll(super.prerequisiteGoals(scheduler));
                l.add(ReachabilityChecked(job));
                return l;
            }
        });
        return g;
    }
    
    public Goal ConstructorCallsChecked(final Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = internGoal(new VisitorGoal(job, new ConstructorCallChecker(job, ts, nf)) {
            public Collection prerequisiteGoals(Scheduler scheduler) {
                List l = new ArrayList();
                l.addAll(super.prerequisiteGoals(scheduler));
                l.add(ReachabilityChecked(job));
                return l;
            }
        });
        return g; 
    }
    
    public Goal ForwardReferencesChecked(final Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = internGoal(new VisitorGoal(job, new FwdReferenceChecker(job, ts, nf)) {
            public Collection prerequisiteGoals(Scheduler scheduler) {
                List l = new ArrayList();
                l.addAll(super.prerequisiteGoals(scheduler));
                l.add(ReachabilityChecked(job));
                return l;
            }
        });
        return g;
    }
    
    public Goal Serialized(Job job) {
        Goal g = internGoal(new Serialized(job));
        return g;
    }
    
    public Goal CodeGenerated(Job job) {
        Goal g = internGoal(new CodeGenerated(job));
        return g;
    }
}
