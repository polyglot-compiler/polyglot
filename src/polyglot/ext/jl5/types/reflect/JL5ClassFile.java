package polyglot.ext.jl5.types.reflect;

import java.io.DataInputStream;
import java.io.IOException;

import javax.tools.FileObject;

import polyglot.frontend.ExtensionInfo;
import polyglot.types.reflect.Attribute;
import polyglot.types.reflect.ClassFile_c;
import polyglot.types.reflect.Field;
import polyglot.types.reflect.Method;

public class JL5ClassFile extends ClassFile_c {
    private JL5Signature signature;
    private Annotations runtimeVisibleAnnotations;
    private Annotations runtimeInvisibleAnnotations;

    public JL5ClassFile(FileObject classFileSource, byte[] code,
            ExtensionInfo ext) throws IOException {
        super(classFileSource, code, ext);
    }

    @Override
    public Method createMethod(DataInputStream in) throws IOException {
        Method m = new JL5Method(in, this);
        m.initialize();
        return m;
    }

    @Override
    public Field createField(DataInputStream in) throws IOException {
        Field f = new JL5Field(in, this);
        f.initialize();
        return f;
    }

    @Override
    public Attribute createAttribute(DataInputStream in, String name,
            int nameIndex, int length) throws IOException {
        if (name.equals("Signature")) {
            signature = new JL5Signature(this, in, nameIndex, length);
            return signature;
        }
        if (name.equals("RuntimeVisibleAnnotations")) {
            runtimeVisibleAnnotations =
                    new Annotations(this, in, nameIndex, length);
            return runtimeVisibleAnnotations;
        }
        if (name.equals("RuntimeInvisibleAnnotations")) {
            runtimeInvisibleAnnotations =
                    new Annotations(this, in, nameIndex, length);
            return runtimeInvisibleAnnotations;
        }
        return super.createAttribute(in, name, nameIndex, length);
    }

    public JL5Signature getSignature() {
        return signature;
    }

    public Annotations getRuntimeVisibleAnnotations() {
        return this.runtimeVisibleAnnotations;
    }

    public Annotations getRuntimeInvisibleAnnotations() {
        return this.runtimeInvisibleAnnotations;
    }

}
