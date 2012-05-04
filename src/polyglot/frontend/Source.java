package polyglot.frontend;

import javax.tools.FileObject;

public interface Source extends FileObject {
    void setUserSpecified(boolean userSpecified);  
    boolean userSpecified();
}
