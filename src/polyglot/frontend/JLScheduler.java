/*
 * Scheduler.java
 * 
 * Author: nystrom
 * Creation date: Feb 6, 2005
 */
package polyglot.ext.jl;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.goals.*;
import polyglot.types.*;
import polyglot.types.FieldInstance;
import polyglot.types.ParsedClassType;
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
        // TODO Auto-generated constructor stub
    }
    
    public Goal TypeExists(String name) {
        return internGoal(new TypeExists(name));
    }
    
    public Goal MembersAdded(ParsedClassType ct) {
        return internGoal(new MembersAdded(ct));
    }
    
    public Goal SupertypesResolved(ParsedClassType ct) {
        return internGoal(new SupertypesResolved(ct));
    }
    
    public Goal SignaturesResolved(ParsedClassType ct) {
        return internGoal(new SignaturesResolved(ct));
    }

    public Goal FieldConstantsChecked(FieldInstance fi) {
        return internGoal(new FieldConstantsChecked(fi));
    }
    
    public Goal Parsed(Job job) {
        return internGoal(new Parsed(job));
    }
    
    public Goal TypesInitialized(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return internGoal(new VisitorGoal(job, new TypeBuilder(job, ts, nf)));
    }

    public Goal TypesInitializedForCommandLine() {
        return internGoal(new Barrier(this) {
            public Goal goalForJob(Job j) {
                return JLScheduler.this.TypesInitialized(j);
            }
        });
    }

    public Goal Disambiguated(Job job) {
        return internGoal(new Disambiguated(job));
    }

    public Goal TypeChecked(Job job) {
        return internGoal(new TypeChecked(job));
    }

    public Goal ConstantsChecked(Job job) {
        return internGoal(new ConstantsCheckedForFile(job));
    }

    public Goal ReachabilityChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return internGoal(new VisitorGoal(job, new ReachChecker(job, ts, nf)));
    }

    public Goal ExceptionsChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return internGoal(new VisitorGoal(job, new ExceptionChecker(job, ts, nf)));
    }

    public Goal ExitPathsChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return internGoal(new VisitorGoal(job, new ExitChecker(job, ts, nf)));
    }

    public Goal InitializationsChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return internGoal(new VisitorGoal(job, new InitChecker(job, ts, nf)));
    }

    public Goal ConstructorCallsChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return internGoal(new VisitorGoal(job, new ConstructorCallChecker(job, ts, nf)));
    }

    public Goal ForwardReferencesChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return internGoal(new VisitorGoal(job, new FwdReferenceChecker(job, ts, nf)));
    }

    public Goal Serialized(Job job) {
        return internGoal(new Serialized(job));
    }

    public Goal CodeGenerated(Job job) {
        return internGoal(new CodeGenerated(job));
    }
    
    public Goal InnerTranslated(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return internGoal(new VisitorGoal(job, new InnerTranslator(ts, nf)));
    }
}
