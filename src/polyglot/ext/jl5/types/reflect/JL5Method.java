package polyglot.ext.jl5.types.reflect;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

import polyglot.types.reflect.Attribute;
import polyglot.types.reflect.ClassFile;
import polyglot.types.reflect.Constant;
import polyglot.types.reflect.Exceptions;
import polyglot.types.reflect.Method;

public class JL5Method extends Method {
    protected JL5Signature signature;
    /**
     * Record whether an annotation has a default value.
     */
    protected boolean defaultVal;

    public JL5Method(DataInputStream in, ClassFile clazz) {
        super(in, clazz);
//        System.err.println("JL5Method created for " + clazz);
    }

    @Override
    public void initialize() throws IOException {
        modifiers = in.readUnsignedShort();

        name = in.readUnsignedShort();
        type = in.readUnsignedShort();
//        System.err.println("JL5Method.initialize() for " + clazz );

        int numAttributes = in.readUnsignedShort();

        attrs = new Attribute[numAttributes];

        for (int i = 0; i < numAttributes; i++) {
            int nameIndex = in.readUnsignedShort();
            int length = in.readInt();

            Constant name = clazz.getConstants()[nameIndex];
//            System.err.println("    " + name.value());

            if (name != null) {
                if ("Exceptions".equals(name.value())) {
                    exceptions = new Exceptions(clazz, in, nameIndex, length);
                    attrs[i] = exceptions;
                }
                if ("Synthetic".equals(name.value())) {
                    synthetic = true;
                }
                if ("AnnotationDefault".equals(name.value())) {
                    defaultVal = true;
                }
                if ("Signature".equals(name.value())) {
                    signature = new JL5Signature(clazz, in, nameIndex, length);
                    attrs[i] = signature;
                }
            }

            if (attrs[i] == null) {
                long n = in.skip(length);
                if (n != length) {
                    throw new EOFException();
                }
            }
        }
        this.in = null; // RMF 7/23/2008 - Don't need the input stream any more,
                        // so don't hang onto it
    }

    public JL5Signature getSignature() {
        return signature;
    }

    public boolean hasDefaultVal() {
        return defaultVal;
    }

}
