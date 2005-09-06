package polyglot.util;

import java.io.*;
import java.util.StringTokenizer;

/**
 * A <code>SimpleErrorQueue</code> handles outputing error messages.
 */
public class SimpleErrorQueue extends AbstractErrorQueue
{
    public SimpleErrorQueue() {
        super(Integer.MAX_VALUE, null);
    }

    public void displayError(ErrorInfo e) {
        System.err.println(e.getMessage());
    }
}
