/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

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
