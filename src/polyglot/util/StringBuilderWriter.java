package polyglot.util;

import java.io.IOException;
import java.io.Writer;

/**
 * A writer that writes to a StringBuilder
 */
public class StringBuilderWriter extends Writer {

    private final StringBuilder builder = new StringBuilder();

    @Override
    public void close() throws IOException {

    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        builder.append(cbuf, off, len);
    }

    public StringBuilder getBuilder() {
        return builder;
    }

}
