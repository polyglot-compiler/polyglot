/*
 * Scheduler.java
 * 
 * Author: nystrom
 * Creation date: Feb 6, 2005
 */
package polyglot.ext.jl;

import polyglot.frontend.*;
import polyglot.frontend.goals.*;
import polyglot.types.FieldInstance;
import polyglot.types.ParsedClassType;

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
    
    public Goal AllMembersAdded(ParsedClassType ct) {
        return internGoal(new AllMembersAdded(ct));
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
        return internGoal(new TypesInitialized(job));
    }

    public Goal TypesInitializedForCommandLine() {
        return internGoal(new Barrier(this) {
            public Goal goalForJob(Job j) {
                return JLScheduler.this.TypesInitialized(j);
            }
        });
    }

    public Goal TypeChecked(Job job) {
        return internGoal(new TypeChecked(job));
    }

    public Goal ConstantsChecked(Job job) {
        return internGoal(new ConstantsCheckedForFile(job));
    }

    public Goal ReachabilityChecked(Job job) {
        return internGoal(new ReachabilityChecked(job));
    }

    public Goal ExceptionsChecked(Job job) {
        return internGoal(new ExceptionsChecked(job));
    }

    public Goal ExitPathsChecked(Job job) {
        return internGoal(new ExitPathsChecked(job));
    }

    public Goal InitializationsChecked(Job job) {
        return internGoal(new InitializationsChecked(job));
    }

    public Goal ConstructorCallsChecked(Job job) {
        return internGoal(new ConstructorCallsChecked(job));
    }

    public Goal ForwardReferencesChecked(Job job) {
        return internGoal(new ForwardReferencesChecked(job));
    }

    public Goal Serialized(Job job) {
        return internGoal(new Serialized(job));
    }

    public Goal CodeGenerated(Job job) {
        return internGoal(new CodeGenerated(job));
    }
}
