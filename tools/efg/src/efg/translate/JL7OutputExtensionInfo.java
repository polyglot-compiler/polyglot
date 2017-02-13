package efg.translate;

import java.util.Collections;
import java.util.List;

import polyglot.ext.jl5.JL5Options;
import polyglot.ext.jl7.JL7ExtensionInfo;
import polyglot.ext.jl7.JL7Scheduler;
import polyglot.frontend.CyclicDependencyException;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.JLExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.EmptyGoal;
import polyglot.frontend.goals.Goal;
import polyglot.main.OptFlag.Arg;
import polyglot.main.Options;
import polyglot.main.UsageError;
import polyglot.translate.JLOutputExtensionInfo;
import polyglot.util.InternalCompilerError;

/**
 * Modelled on {@link JLOutputExtensionInfo}.
 */
public class JL7OutputExtensionInfo extends JL7ExtensionInfo {
    final protected ExtensionInfo parent;

    public JL7OutputExtensionInfo(ExtensionInfo parent) {
        this.parent = parent;
    }

    @Override
    public Scheduler createScheduler() {
        return new JL7OutputScheduler(this);
    }

    static protected class JL7OutputOptions extends JL5Options {
        public JL7OutputOptions(ExtensionInfo extension) {
            super(extension);
        }

        /**
         * Skip checks regarding source files.
         */
        @Override
        protected void validateArgs() throws UsageError {
        }
    }

    @Override
    protected Options createOptions() {
        Options parentOpts = parent.getOptions();
        Options opt = new JL7OutputOptions(this);

        // Filter the parent's options by the ones this extension understands.
        List<Arg<?>> arguments = parentOpts.filterArgs(opt.flags());
        try {
            opt.processArguments(arguments, Collections.<String> emptySet());
        }
        catch (UsageError e) {
            throw new InternalCompilerError("Got usage error while configuring "
                    + "output extension", e);
        }

        return opt;
    }

    protected static class JL7OutputScheduler extends JL7Scheduler {
        public JL7OutputScheduler(JLExtensionInfo extInfo) {
            super(extInfo);
        }

        @Override
        public Goal Parsed(Job job) {
            return internGoal(new EmptyGoal(job, "Parsed"));
        }

        @Override
        public Goal Serialized(Job job) {
            // Don't serialize. Replace serialized goal with an empty goal.
            Goal g = new EmptyGoal(job, "Serialized");
            try {
                g.addPrerequisiteGoal(Validated(job), this);
                g.addPrerequisiteGoal(AnnotationCheck(job), this);
            }
            catch (CyclicDependencyException e) {
                throw new InternalCompilerError(e);
            }

            return internGoal(g);
        }

    }
}
