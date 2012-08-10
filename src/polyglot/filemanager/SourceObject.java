package polyglot.filemanager;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

import polyglot.util.StringBuilderWriter;

/**
 * This class represents a source object to be kept in memory.
 */
public class SourceObject extends SimpleJavaFileObject {

    private final StringBuilderWriter sbw = new StringBuilderWriter();

    public SourceObject(URI u, Kind k) {
        super(u, k);
    }

    @Override
    public Writer openWriter() throws IOException {
        return sbw;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return sbw.getBuilder();
    }
}
