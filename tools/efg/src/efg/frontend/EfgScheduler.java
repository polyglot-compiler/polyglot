package efg.frontend;

import efg.ExtensionInfo;
import efg.filemanager.NullSource;
import efg.translate.ExtFactoryGenerator;
import efg.visit.EFGInfoCollector;
import polyglot.ast.SourceFile;
import polyglot.ext.jl7.JL7ExtensionInfo;
import polyglot.ext.jl7.JL7Scheduler;
import polyglot.frontend.CyclicDependencyException;
import polyglot.frontend.Job;
import polyglot.frontend.Scheduler;
import polyglot.frontend.Source;
import polyglot.frontend.goals.Barrier;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.VisitorGoal;
import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;

public class EfgScheduler extends JL7Scheduler {
    protected ExtensionInfo extInfo;

    public EfgScheduler(ExtensionInfo extInfo) {
        super(extInfo);
        this.extInfo = extInfo;
    }

    public Goal ValidationBarrier(Job job) {
        Goal g = new Barrier("Validated", this) {
            @Override
            public Goal goalForJob(Job job) {
                return Validated(job);
            }
        };

        return internGoal(g);
    }

    public Goal EfgInfoCollected(Job job) {
        Goal g = new VisitorGoal(job,
                                 new EFGInfoCollector(job,
                                                      extInfo,
                                                      extInfo.typeSystem(),
                                                      extInfo.nodeFactory()));
        try {
            g.addPrerequisiteGoal(ValidationBarrier(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }

        return internGoal(g);
    }

    public Goal EfgInfoCollectionBarrier(Job job) {
        Goal g = new Barrier("EFGInfoCollected", this) {
            @Override
            public Goal goalForJob(Job job) {
                return EfgInfoCollected(job);
            }
        };

        return internGoal(g);
    }

    public Goal EfgInfoValidated(Job job) {
        Goal g = new EfgInfoValidationGoal(job);
        try {
            g.addPrerequisiteGoal(EfgInfoCollectionBarrier(job), this);
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e);
        }
        return internGoal(g);
    }

    @Override
    public boolean runToCompletion() {
        if (!super.runToCompletion()) return false;

        JL7ExtensionInfo outExtInfo = extInfo.outputExtensionInfo();

        try {
            // Create a goal to compile each generated source.
            Scheduler outScheduler = outExtInfo.scheduler();
            ExtFactoryGenerator efg =
                    new ExtFactoryGenerator(extInfo, outExtInfo);
            Job[] jobs =
                    new Job[] { addExtFactoryJob(efg, outExtInfo, outScheduler),
                            addAbstractExtFactoryJob(efg,
                                                     outExtInfo,
                                                     outScheduler) };
            for (Job job : jobs) {
                if (job != null) {
                    outScheduler.addGoal(outExtInfo.getCompileGoal(job));
                }
            }

            cleanup();

            return outExtInfo.scheduler().runToCompletion();
        }
        catch (SemanticException e) {
            throw new InternalCompilerError(e);
        }
    }

    /**
     * Generates the ExtFactory AST, creates a job for it, and adds the job to
     * the given scheduler.
     *
     * @return the added job.
     */
    protected Job addExtFactoryJob(ExtFactoryGenerator efg,
            JL7ExtensionInfo outExtInfo, Scheduler outScheduler) {
        Source source = new NullSource(efg.extFactorySimpleName() + "."
                + outExtInfo.defaultFileExtension());

        SourceFile ast = efg.genExtFactory().source(source);
        return outScheduler.addJob(source, ast);
    }

    /**
     * Generates the AbstractExtFactory AST, creates a job for it, and adds
     * the job to the given scheduler.
     *
     * @return the added job.
     */
    protected Job addAbstractExtFactoryJob(ExtFactoryGenerator efg,
            polyglot.frontend.ExtensionInfo outExtInfo,
            Scheduler outScheduler) {
        Source source = new NullSource(efg.abstractExtFactorySimpleName() + "."
                + outExtInfo.defaultFileExtension());

        SourceFile ast = efg.genAbstractExtFactory().source(source);
        return outScheduler.addJob(source, ast);
    }
}
