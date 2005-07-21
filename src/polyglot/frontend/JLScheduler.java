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
        // TODO Auto-generated constructor stub
    }
    
    public Goal TypeExists(String name) {
        return internGoal(new TypeExists(name));
    }
    
    public Goal MembersAdded(ParsedClassType ct) {
        Goal g = internGoal(new MembersAdded(ct));
        try {
            if (ct.job() != null) {
                addPrerequisiteDependency(g, Parsed(ct.job()));
                addConcurrentDependency(g, TypesInitialized(ct.job()));
            }
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return g;
    }
    
    public Goal SupertypesResolved(ParsedClassType ct) {
        Goal g = internGoal(new SupertypesResolved(ct));
        try {
            if (ct.job() != null) {
                addPrerequisiteDependency(g, TypesInitialized(ct.job()));
                addConcurrentDependency(g, Disambiguated(ct.job()));
            }
            addPrerequisiteDependency(g, MembersAdded(ct));
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return g;
    }
    
    public Goal SignaturesResolved(ParsedClassType ct) {
        Goal g = internGoal(new SignaturesResolved(ct));
        try {
            if (ct.job() != null) {
                addPrerequisiteDependency(g, TypesInitialized(ct.job()));
                addConcurrentDependency(g, Disambiguated(ct.job()));
            }
            addPrerequisiteDependency(g, SupertypesResolved(ct));
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return g;
    }

    public Goal FieldConstantsChecked(FieldInstance fi) {
        Goal g = internGoal(new FieldConstantsChecked(fi));
        try {
            ReferenceType container = fi.container();
            if (container instanceof ParsedTypeObject) {
                ParsedTypeObject ct = (ParsedTypeObject) container;
                if (ct.job() != null) {
                    addConcurrentDependency(g, ConstantsChecked(ct.job()));
                }
            }
            if (container instanceof ParsedClassType) {
                ParsedClassType ct = (ParsedClassType) container;
                addPrerequisiteDependency(g, SignaturesResolved(ct));
            }
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return g;
    }
    
    public Goal Parsed(Job job) {
        Goal g = internGoal(new Parsed(job));
        return g;
    }
    
    public Goal TypesInitialized(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = internGoal(new VisitorGoal(job, new TypeBuilder(job, ts, nf)));
        try {
            addPrerequisiteDependency(g, Parsed(job));
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return g;
    }
    
    public Goal TypesInitializedForCommandLine() {
        return internGoal(new Barrier(this) {
            public Goal goalForJob(Job j) {
                return JLScheduler.this.TypesInitialized(j);
            }
        });
    }
    
    public Goal Disambiguated(Job job) {
        Goal g = internGoal(new Disambiguated(job));
        try {
            addPrerequisiteDependency(g, TypesInitializedForCommandLine());
            addPrerequisiteDependency(g, TypesInitialized(job));
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return g;
    }
    
    public Goal TypeChecked(Job job) {
        Goal g = internGoal(new TypeChecked(job));
        try {
            addPrerequisiteDependency(g, Disambiguated(job));
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return g;
    }
    
    public Goal ConstantsChecked(Job job) {
        Goal g = internGoal(new ConstantsCheckedForFile(job));
        try {
            addPrerequisiteDependency(g, TypeChecked(job));
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return g;
    }
    
    public Goal ReachabilityChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = internGoal(new VisitorGoal(job, new ReachChecker(job, ts, nf)));
        try {
            addPrerequisiteDependency(g, TypeChecked(job));
            addPrerequisiteDependency(g, ConstantsChecked(job));
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return g;
        
    }
    
    public Goal ExceptionsChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = internGoal(new VisitorGoal(job, new ExceptionChecker(job, ts, nf)));
        try {
            addPrerequisiteDependency(g, TypeChecked(job));
            addPrerequisiteDependency(g, ReachabilityChecked(job));
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return g;
    }
    
    public Goal ExitPathsChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g =internGoal(new VisitorGoal(job, new ExitChecker(job, ts, nf)));
        try {
            addPrerequisiteDependency(g, ReachabilityChecked(job));
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return g;
    }
    
    public Goal InitializationsChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = internGoal(new VisitorGoal(job, new InitChecker(job, ts, nf)));
        try {
            addPrerequisiteDependency(g, ReachabilityChecked(job));
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return g;
    }
    
    public Goal ConstructorCallsChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = internGoal(new VisitorGoal(job, new ConstructorCallChecker(job, ts, nf)));
        try {
            addPrerequisiteDependency(g, ReachabilityChecked(job));
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return g; 
    }
    
    public Goal ForwardReferencesChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = internGoal(new VisitorGoal(job, new FwdReferenceChecker(job, ts, nf)));
        try {
            addPrerequisiteDependency(g, ReachabilityChecked(job));
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return g;
    }
    
    public Goal Serialized(Job job) {
        Goal g = internGoal(new Serialized(job));
        try {
            addPrerequisiteDependency(g, TypeChecked(job));
            addPrerequisiteDependency(g, ConstantsChecked(job));
            addPrerequisiteDependency(g, ReachabilityChecked(job));
            addPrerequisiteDependency(g, ExceptionsChecked(job));
            addPrerequisiteDependency(g, ExitPathsChecked(job));
            addPrerequisiteDependency(g, InitializationsChecked(job));
            addPrerequisiteDependency(g, ConstructorCallsChecked(job));
            addPrerequisiteDependency(g, ForwardReferencesChecked(job));
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return g;
    }
    
    public Goal CodeGenerated(Job job) {
        Goal g = internGoal(new CodeGenerated(job));
        try {
            addPrerequisiteDependency(g, Serialized(job));
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return g;
    }
}
