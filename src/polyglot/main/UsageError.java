package jltools.main;

/** An exception used to indicate a command-line usage error. */
public class UsageError extends Exception {
    public UsageError(String s) { super(s); }
}
