package efg;

import java.io.File;
import java.util.Set;

import polyglot.ext.jl5.JL5Options;
import polyglot.main.OptFlag;
import polyglot.main.OptFlag.Arg;
import polyglot.main.UsageError;

public class Options extends JL5Options {

    /**
     * The configuration file.
     */
    protected File confFile;

    public Options(ExtensionInfo extension) {
        super(extension);
        confFile = null;
    }

    @Override
    protected void populateFlags(Set<OptFlag<?>> flags) {
        flags.add(new OptFlag<String>("-conf", "<file>", "configuration file") {
            @Override
            public Arg<String> handle(String[] args, int index)
                    throws UsageError {
                return createArg(index + 1, args[index]);
            }
        });

        super.populateFlags(flags);
    }

    @Override
    protected void handleArg(Arg<?> arg) throws UsageError {
        if (arg.flag().ids().contains("-conf")) {
            confFile = new File((String) arg.value());
            if (!confFile.canRead()) {
                throw new UsageError("Unable to read configuration file '"
                        + arg.value() + "'");
            }
        }
        else super.handleArg(arg);
    }

    @Override
    protected void validateArgs() throws UsageError {
        super.validateArgs();

        if (OptFlag.lookup("-conf", arguments) == null) {
            throw new UsageError("Must specify a configuration file");
        }
    }

    @Override
    protected void postApplyArgs() {
        super.postApplyArgs();

        // Don't call javac.
        setOutputOnly(true);
    }

    public File confFile() {
        return confFile;
    }

}
