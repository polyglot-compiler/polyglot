package efg.filemanager;

import java.net.URI;

import javax.tools.JavaFileObject;

import polyglot.filemanager.ExtFileObject;
import polyglot.frontend.Source_c;

public class NullSource extends Source_c {

    public NullSource(String name) {
        super(new ExtFileObject(URI.create(name), JavaFileObject.Kind.SOURCE),
              Kind.COMPILER_GENERATED);
    }
}
