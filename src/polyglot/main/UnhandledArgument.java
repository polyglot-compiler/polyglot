package polyglot.main;

import polyglot.main.OptFlag.Arg;
import polyglot.util.SerialVersionUID;

public class UnhandledArgument extends UsageError {
    private static final long serialVersionUID = SerialVersionUID.generate();

    final protected Arg<?> arg;

    public UnhandledArgument(Arg<?> arg) {
        super("Unhandled argument: " + arg.flag().ids());
        this.arg = arg;
    }

    public Arg<?> argument() {
        return arg;
    }
}
