package polyglot.ext.jl5.types.reflect;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

import polyglot.types.reflect.Attribute;
import polyglot.types.reflect.ClassFile;
import polyglot.types.reflect.Constant;
import polyglot.types.reflect.ConstantValue;
import polyglot.types.reflect.Field;

public class JL5Field extends Field {
    protected JL5Signature signature;

    public JL5Field(DataInputStream in, ClassFile clazz) throws IOException {
        super(in, clazz);
    }

    @Override
    public void initialize() throws IOException {
        modifiers = in.readUnsignedShort();

        name = in.readUnsignedShort();
        type = in.readUnsignedShort();

        int numAttributes = in.readUnsignedShort();

        attrs = new Attribute[numAttributes];

        for (int i = 0; i < numAttributes; i++) {
            int nameIndex = in.readUnsignedShort();
            int length = in.readInt();

            Constant name = clazz.getConstants()[nameIndex];

            if (name != null) {
                if ("ConstantValue".equals(name.value())) {
                    constantValue = new ConstantValue(in, nameIndex, length);
                    attrs[i] = constantValue;
                }
                if ("Synthetic".equals(name.value())) {
                    synthetic = true;
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
        this.in = null; // RMF 7/23/2008 - Don't need the input stream any more, so don't hang onto it
    }

    public JL5Signature getSignature() {
        return signature;
    }

}
