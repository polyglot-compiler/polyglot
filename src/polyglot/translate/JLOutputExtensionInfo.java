package polyglot.translate;

import java.util.Collections;
import java.util.List;

import polyglot.frontend.EmptyPass;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.JLExtensionInfo;
import polyglot.frontend.JLScheduler;
import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.Goal;
import polyglot.frontend.goals.SourceFileGoal;
import polyglot.main.OptFlag.Arg;
import polyglot.main.Options;
import polyglot.main.UsageError;
import polyglot.util.InternalCompilerError;

public class JLOutputExtensionInfo extends JLExtensionInfo {
    final protected ExtensionInfo parent;

    public JLOutputExtensionInfo(ExtensionInfo parent) {
        this.parent = parent;
    }

    @Override
    public Scheduler createScheduler() {
        return new JLOutputScheduler(this);
    }

    static protected class JLOutputOptions extends Options {

        public JLOutputOptions(ExtensionInfo extension) {
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
        Options opt = new JLOutputOptions(this);
        // filter the parent's options by the ones this extension understands
        List<Arg<?>> arguments = parentOpts.filterArgs(opt.flags());
        try {
            opt.processArguments(arguments, Collections.<String> emptySet());
        }
        catch (UsageError e) {
            throw new InternalCompilerError("Got usage error while configuring output extension",
                                            e);
        }
        return opt;
    }

    static protected class JLOutputScheduler extends JLScheduler {
        public JLOutputScheduler(ExtensionInfo extInfo) {
            super(extInfo);
        }

        @Override
        public Goal Parsed(Job job) {
            return internGoal(new SourceFileGoal(job) {
                @Override
                public Pass createPass(ExtensionInfo extInfo) {
                    return new EmptyPass(this);
                }
            });
        }
    }
}
