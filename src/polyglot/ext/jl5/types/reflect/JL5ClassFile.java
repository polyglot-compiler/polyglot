package polyglot.ext.jl5.types.reflect;

import java.io.DataInputStream;
import java.io.IOException;

import javax.tools.FileObject;

import polyglot.frontend.ExtensionInfo;
import polyglot.types.reflect.Attribute;
import polyglot.types.reflect.ClassFile_c;
import polyglot.types.reflect.Field;
import polyglot.types.reflect.InnerClasses;
import polyglot.types.reflect.Method;

public class JL5ClassFile extends ClassFile_c {
    private JL5Signature signature;

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
        if (name.equals("InnerClasses")) {
            innerClasses = new InnerClasses(in, nameIndex, length);
            return innerClasses;
        }
        if (name.equals("Signature")) {
            signature = new JL5Signature(this, in, nameIndex, length);
            return signature;
        }
        return null;
    }

    public JL5Signature getSignature() {
        return signature;
    }
}
