package polyglot.main;

import polyglot.main.OptFlag.Arg;

public class UnhandledArgument extends UsageError {
    final protected Arg<?> arg;

    public UnhandledArgument(Arg<?> arg) {
        super("Unhandled argument: " + arg.flag().ids());
        this.arg = arg;
    }

    public Arg<?> argument() {
        return arg;
    }
}
