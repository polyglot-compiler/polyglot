/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/
package polyglot.ext.jl5;

import polyglot.ast.NodeFactory;
import polyglot.ext.jl5.translate.JL5ToJLRewriter;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.visit.AnnotationChecker;
import polyglot.ext.jl5.visit.AutoBoxer;
import polyglot.ext.jl5.visit.JL5DefiniteAssignmentChecker;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.ext.jl5.visit.RemoveAnnotations;
import polyglot.ext.jl5.visit.RemoveEnums;
import polyglot.ext.jl5.visit.RemoveExtendedFors;
import polyglot.ext.jl5.visit.RemoveStaticImports;
import polyglot.ext.jl5.visit.RemoveVarArgsFlags;
import polyglot.ext.jl5.visit.RemoveVarargVisitor;
import polyglot.ext.jl5.visit.SimplifyExpressionsForBoxing;
import polyglot.ext.jl5.visit.TVCaster;
import polyglot.ext.jl5.visit.TypeErasureProcDecls;
import polyglot.frontend.CyclicDependencyException;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.JLExtensionInfo;
import polyglot.frontend.JLScheduler;
import polyglot.frontend.Job;
import polyglot.frontend.OutputPass;
import polyglot.frontend.Pass;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.CodeGenerated;
import polyglot.frontend.goals.EmptyGoal;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.VisitorGoal;
import polyglot.main.Options;
import polyglot.types.ParsedClassType;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;

public class JL5Scheduler extends JLScheduler {

    public JL5Scheduler(JLExtensionInfo extInfo) {
        super(extInfo);
    }

    public Goal AnnotationsResolved(ParsedClassType ct) {
        Goal g = AnnotationsResolved.create(this, ct);
        return internGoal(g);
    }

    public Goal AnnotationsResolved(Job job) {
        Goal g = AnnotationsResolved.create(this, job);
        return internGoal(g);
    }

    public Goal CastsInserted(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new TVCaster(job, ts, nf));
        try {
            g.addPrerequisiteGoal(PreRemoveJava5isms(job), this);
            g.addPrerequisiteGoal(TypeChecked(job), this);
            g.addPrerequisiteGoal(AnnotationCheck(job), this);
            g.addPrerequisiteGoal(TypeClosure(job), this);
            g.addPrerequisiteGoal(AutoBoxing(job), this);
            g.addPrerequisiteGoal(RemoveExtendedFors(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);

    }

    public Goal AutoBoxing(Job job) {
        JL5TypeSystem ts = (JL5TypeSystem) extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new AutoBoxer(job, ts, nf));
        try {
            g.addPrerequisiteGoal(PreRemoveJava5isms(job), this);
            g.addPrerequisiteGoal(RemoveVarArgs(job), this);
            g.addPrerequisiteGoal(RemoveExtendedFors(job), this);
            g.addPrerequisiteGoal(SimplifyExpressionsForBoxing(job), this);
            g.addPrerequisiteGoal(AnnotationCheck(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);

    }

    public Goal TypeErasureProcDecls(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new TypeErasureProcDecls(job, ts, nf));
        try {
            g.addPrerequisiteGoal(PreRemoveJava5isms(job), this);
            g.addPrerequisiteGoal(CastsInserted(job), this);
            g.addPrerequisiteGoal(AutoBoxing(job), this);
            g.addPrerequisiteGoal(RemoveExtendedFors(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);

    }

    public Goal RemoveVarArgs(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new RemoveVarargVisitor(job, ts, nf));
        try {
            g.addPrerequisiteGoal(PreRemoveJava5isms(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    public Goal SimplifyExpressionsForBoxing(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new SimplifyExpressionsForBoxing(nf, ts));
        try {
            g.addPrerequisiteGoal(PreRemoveJava5isms(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    public Goal RemoveEnums(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new RemoveEnums(job, ts, nf));
        try {
            g.addPrerequisiteGoal(PreRemoveJava5isms(job), this);
            g.addPrerequisiteGoal(RemoveStaticImports(job), this);
            g.addPrerequisiteGoal(RemoveVarArgs(job), this);
            g.addPrerequisiteGoal(AutoBoxing(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    public Goal RemoveVarArgsFlags(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new RemoveVarArgsFlags(job, ts, nf));
        try {
            g.addPrerequisiteGoal(PreRemoveJava5isms(job), this);
            g.addPrerequisiteGoal(RemoveEnums(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    public Goal RemoveExtendedFors(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new RemoveExtendedFors(job, ts, nf));
        try {
            g.addPrerequisiteGoal(PreRemoveJava5isms(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);

    }

    public Goal RemoveStaticImports(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new RemoveStaticImports(job, ts, nf));
        try {
            g.addPrerequisiteGoal(PreRemoveJava5isms(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);

    }

    public Goal RemoveAnnotations(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new RemoveAnnotations(job, ts, nf));
        try {
            g.addPrerequisiteGoal(PreRemoveJava5isms(job), this);
            g.addPrerequisiteGoal(RemoveStaticImports(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);

    }

    public Goal PreRemoveJava5isms(Job job) {
        Goal g = new EmptyGoal(job, "PreRemoveJava5isms");
        try {
            g.addPrerequisiteGoal(TypeChecked(job), this);
            g.addPrerequisiteGoal(AnnotationCheck(job), this);
            // make sure we serialize before we start changing things.
            g.addPrerequisiteGoal(Serialized(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    public Goal RemoveJava5isms(Job job) {
        Options opts = extInfo.getOptions();
        boolean typecheckResult = !(opts instanceof JL5Options
                && ((JL5Options) opts).skip524checks);
        Goal g = typecheckResult
                ? internGoal(new VisitorGoal(job,
                                             new JL5ToJLRewriter(job,
                                                                 extInfo,
                                                                 extInfo.outputExtensionInfo())))
                : new EmptyGoal(job, "RemoveJava5isms");
        try {
            g.addPrerequisiteGoal(PreRemoveJava5isms(job), this);
            g.addPrerequisiteGoal(CastsInserted(job), this);
            g.addPrerequisiteGoal(TypeErasureProcDecls(job), this);
            g.addPrerequisiteGoal(RemoveVarArgs(job), this);
            g.addPrerequisiteGoal(AutoBoxing(job), this);
            g.addPrerequisiteGoal(RemoveEnums(job), this);
            g.addPrerequisiteGoal(RemoveVarArgsFlags(job), this);
            g.addPrerequisiteGoal(RemoveExtendedFors(job), this);
            g.addPrerequisiteGoal(RemoveStaticImports(job), this);
            g.addPrerequisiteGoal(RemoveAnnotations(job), this);

        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }

        return internGoal(g);
    }

    public Goal AnnotationCheck(Job job) {

        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job, new AnnotationChecker(job, ts, nf));
        try {
            g.addPrerequisiteGoal(TypeChecked(job), this);
            g.addPrerequisiteGoal(AnnotationsResolved(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);

    }

    public Goal TypeClosure(Job job) {
        Goal g = internGoal(new VisitorGoal(job,
                                            new polyglot.visit.TypeClosure(extInfo.nodeFactory()
                                                                                  .lang())));
        try {
            g.addPrerequisiteGoal(TypeChecked(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    @Override
    public Goal CodeGenerated(Job job) {
        Options opts = extInfo.getOptions();
        if (opts instanceof JL5Options && ((JL5Options) opts).removeJava5isms) {
            Goal g = ((JL5Options) opts).skip524checks
                    ? JL5CodeGenerated.create(this, job)
                    : new EmptyGoal(job, "CodeGenerated");
            try {
                g.addPrerequisiteGoal(RemoveJava5isms(job), this);
            }
            catch (CyclicDependencyException e) {
                throw new InternalCompilerError(e);
            }
            return g;
        }
        else return JL5CodeGenerated.create(this, job);
    }

    @Override
    public Goal Validated(Job job) {
        Goal g = super.Validated(job);
        try {
            g.addPrerequisiteGoal(AnnotationCheck(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    @Override
    public boolean runToCompletion() {
        boolean complete = super.runToCompletion();
        Options opts = extInfo.getOptions();
        if (complete && ((JL5Options) opts).removeJava5isms) {
            ExtensionInfo outExtInfo = extInfo.outputExtensionInfo();

            // Create a goal to compile every source file.
            for (Job job : outExtInfo.scheduler().jobs()) {
                Job newJob =
                        outExtInfo.scheduler().addJob(job.source(), job.ast());
                outExtInfo.scheduler()
                          .addGoal(outExtInfo.getCompileGoal(newJob));
            }
            cleanup();
            return outExtInfo.scheduler().runToCompletion();
        }
        return complete;
    }

    protected void cleanup() {
        extInfo.cleanup();
        inWorklist.clear();
        worklist.clear();
        jobs.clear();
        goals.clear();
        runCount.clear();
    }

    private static class JL5CodeGenerated extends CodeGenerated {
        public static Goal create(Scheduler scheduler, Job job) {
            return scheduler.internGoal(new JL5CodeGenerated(job));
        }

        /**
         * @param job The job to compile.
         */
        protected JL5CodeGenerated(Job job) {
            super(job);
        }

        @Override
        public Pass createPass(ExtensionInfo extInfo) {
            TypeSystem ts = extInfo.typeSystem();
            NodeFactory nf = extInfo.nodeFactory();
            return new OutputPass(this, new JL5Translator(job(),
                                                          ts,
                                                          nf,
                                                          extInfo.targetFactory()));
        }
    }

    @Override
    public Goal InitializationsChecked(Job job) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        Goal g = new VisitorGoal(job,
                                 new JL5DefiniteAssignmentChecker(job, ts, nf));
        try {
            g.addPrerequisiteGoal(ReachabilityChecked(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

}
