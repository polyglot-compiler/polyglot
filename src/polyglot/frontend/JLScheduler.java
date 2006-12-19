/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * Scheduler.java
 * 
 * Author: nystrom
 * Creation date: Feb 6, 2005
 */
package polyglot.frontend;

import java.util.*;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.goals.*;
import polyglot.frontend.goals.Disambiguated;
import polyglot.frontend.goals.TypeChecked;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.visit.*;

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
        return TypeExists.create(this, name);
    }
    
    public Goal MembersAdded(ParsedClassType ct) {
        Goal g = MembersAdded.create(this, ct);
        return g;
    }

    public Goal SupertypesResolved(ParsedClassType ct) {
        Goal g = SupertypesResolved.create(this, ct);
        return g;
    }

    public Goal SignaturesResolved(ParsedClassType ct) {
        Goal g = SignaturesResolved.create(this, ct);
        return g;
    }

    public Goal FieldConstantsChecked(FieldInstance fi) {
        Goal g = FieldConstantsChecked.create(this, fi);
        return g;
    }
    
    public Goal Parsed(Job job) {
        return Parsed.create(this, job);
    }
    
    public Goal TypesInitialized(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = TypesInitialized.create(this, job, ts, nf);
        return g;
    }
    
    public Goal TypesInitializedForCommandLine() {
        return TypesInitializedForCommandLine.create(this);
    }
    
    public Goal ImportTableInitialized(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = ImportTableInitialized.create(this, job, ts, nf);
        return g;
    }
    
    public Goal SignaturesDisambiguated(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = SignaturesDisambiguated.create(this, job, ts, nf);
        return g;
    }

    public Goal SupertypesDisambiguated(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = SupertypesDisambiguated.create(this, job, ts, nf);
        return g;
    }
    
    public Goal Disambiguated(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = Disambiguated.create(this, job, ts, nf);
        return g;
    }
    
    public Goal TypeChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = TypeChecked.create(this, job, ts, nf);
        return g;
    }
    
    public Goal ConstantsChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = ConstantsChecked.create(this, job, ts, nf);
        return g;
    }
    
    public Goal ReachabilityChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = ReachabilityChecked.create(this, job, ts, nf);
        return g;
    }
    
    public Goal ExceptionsChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = ExceptionsChecked.create(this, job, ts, nf);
        return g;
    }
    
    public Goal ExitPathsChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = ExitPathsChecked.create(this, job, ts, nf);
        return g;
    }
    
    public Goal InitializationsChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = InitializationsChecked.create(this, job, ts, nf);
        return g;
    }
    
    public Goal ConstructorCallsChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = ConstructorCallsChecked.create(this, job, ts, nf);
        return g; 
    }
    
    public Goal ForwardReferencesChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = ForwardReferencesChecked.create(this, job, ts, nf);
        return g;
    }
    
    public Goal Serialized(Job job) {
        Goal g = Serialized.create(this, job);
        return g;
    }
    
    public Goal CodeGenerated(Job job) {
        Goal g = CodeGenerated.create(this, job);
        return g;
    }
}
