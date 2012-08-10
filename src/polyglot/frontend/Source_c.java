package polyglot.frontend;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.tools.FileObject;
import javax.tools.ForwardingFileObject;

public class Source_c extends ForwardingFileObject<FileObject> implements
        FileSource {
    protected boolean user_specified;

    protected Source_c(FileObject f, boolean userSpecified) {
        super(f);
        this.user_specified = userSpecified;
    }

    @Override
    public void setUserSpecified(boolean userSpecified) {
        this.user_specified = userSpecified;
    }

    @Override
    public boolean userSpecified() {
        return user_specified;
    }

    @Override
    public String name() {
        return getName();
    }

    @Override
    public String path() {
        return toUri().getPath();
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        return new InputStreamReader(openInputStream());
    }

    @Override
    public String toString() {
        return toUri().getPath();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FileObject) {

            FileObject fo = (FileObject) o;
            return toUri().equals(fo.toUri());
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return toUri().hashCode();
    }
}
