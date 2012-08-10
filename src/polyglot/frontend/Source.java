package polyglot.frontend;

import javax.tools.FileObject;

/**
 * Interface for defining source files
 */
public interface Source extends FileObject {
    void setUserSpecified(boolean userSpecified);

    boolean userSpecified();

    String name();

    String path();
}
