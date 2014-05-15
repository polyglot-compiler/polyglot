/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/
package polyglot.util;

import java.io.Externalizable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Provides a method for generating a conservative approximation of the
 * serialVersionUID field. Hopefully, the values generated here will be less
 * sensitive to the particular compiler being used.
 */
public class SerialVersionUID implements Serializable {
    private static final long serialVersionUID = generate();

    public static long generate() {
        StackTraceElement caller = new Exception().getStackTrace()[1];
        try {
            Class<?> clazz = Class.forName(caller.getClassName());
            return generate(clazz);
        }
        catch (ClassNotFoundException e) {
            throw new Error(e);
        }
    }

    public static long generate(Class<?> clazz) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Add class name to hash.
            byte[] className = clazz.getName().getBytes("UTF-8");
            md.update(className);

            // Add super class's UID to hash.
            Class<?> superClass = clazz.getSuperclass();
            if (!Object.class.equals(superClass)) {
                ObjectStreamClass osc = ObjectStreamClass.lookup(superClass);
                md.update(getBytes(osc.getSerialVersionUID()));
            }

            // Add to hash a boolean indicating whether the class is an enum.
            md.update((byte) (clazz.isEnum() ? 1 : 0));

            // Add to hash a boolean indicating whether the class implements
            // Serializable and another boolean for whether the class implements
            // Externalizable.
            Class<?>[] interfaces = clazz.getInterfaces();
            boolean serializable = false;
            boolean externalizable = false;
            for (Class<?> iface : interfaces) {
                if (!serializable && iface.equals(Serializable.class))
                    serializable = true;
                if (!externalizable && iface.equals(Externalizable.class))
                    externalizable = true;
            }
            md.update((byte) (serializable ? 1 : 0));
            md.update((byte) (externalizable ? 1 : 0));

            // Go through fields in alphabetical order.
            Set<Field> fields = new TreeSet<>(new Comparator<Field>() {
                @Override
                public int compare(Field f1, Field f2) {
                    return f1.getName().compareTo(f2.getName());
                }
            });
            for (Field field : clazz.getDeclaredFields()) {
                fields.add(field);
            }
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)
                        || Modifier.isTransient(modifiers)) continue;

                // Add field name to hash.
                md.update(field.getName().getBytes("UTF-8"));

                // Add field type (if primitive) to hash.
                Class<?> fieldType = field.getClass();
                if (fieldType.isPrimitive()) {
                    md.update(fieldType.getName().getBytes("UTF-8"));
                }
            }

            // Abort if the class defines writeObject, readObject, writeReplace,
            // or readResolve.
            try {
                clazz.getDeclaredMethod("writeObject", ObjectOutputStream.class);
                try {
                    Field writeObjectVersion =
                            clazz.getDeclaredField("writeObjectVersionUID");
                    writeObjectVersion.setAccessible(true);
                    long ver = writeObjectVersion.getLong(clazz);
                    md.update(getBytes(ver));
                }
                catch (NoSuchFieldException e) {
                    throw new Error(clazz
                            + " defines writeObject(ObjectOutputStream) "
                            + "but not writeObjectVersionUID");
                }
                catch (IllegalAccessException e) {
                    throw new Error(e);
                }
            }
            catch (NoSuchMethodException e) {
            }
            try {
                clazz.getDeclaredMethod("readObject", ObjectInputStream.class);
                try {
                    Field readObjectVersion =
                            clazz.getDeclaredField("readObjectVersionUID");
                    readObjectVersion.setAccessible(true);
                    long ver = readObjectVersion.getLong(clazz);
                    md.update(getBytes(ver));
                }
                catch (NoSuchFieldException e) {
                    throw new Error(clazz
                            + " defines readObject(ObjectInputStream) "
                            + "but not readObjectVersionUID");
                }
                catch (IllegalAccessException e) {
                    throw new Error(e);
                }
            }
            catch (NoSuchMethodException e) {
            }
            try {
                clazz.getDeclaredMethod("writeReplace");
                try {
                    Field writeReplaceVersion =
                            clazz.getDeclaredField("writeReplaceVersionUID");
                    writeReplaceVersion.setAccessible(true);
                    long ver = writeReplaceVersion.getLong(clazz);
                    md.update(getBytes(ver));
                }
                catch (NoSuchFieldException e) {
                    throw new Error(clazz + " defines writeReplace() "
                            + "but not writeReplaceVersionUID");
                }
                catch (IllegalAccessException e) {
                    throw new Error(e);
                }
            }
            catch (NoSuchMethodException e) {
            }
            try {
                clazz.getDeclaredMethod("readResolve");
                try {
                    Field readResolveVersion =
                            clazz.getDeclaredField("readResolveVersionUID");
                    readResolveVersion.setAccessible(true);
                    long ver = readResolveVersion.getLong(clazz);
                    md.update(getBytes(ver));
                }
                catch (NoSuchFieldException e) {
                    throw new Error(clazz + " defines readResolve() "
                            + "but not readResolveVersionUID");
                }
                catch (IllegalAccessException e) {
                    throw new Error(e);
                }
            }
            catch (NoSuchMethodException e) {
            }

            byte[] md5 = md.digest();
            return longAt(md5, 0) ^ longAt(md5, 8);
        }
        catch (NoSuchAlgorithmException
               | UnsupportedEncodingException
               | SecurityException
               | IllegalArgumentException e) {
            throw new Error(e);
        }
    }

    /**
     * Returns the long that starts at the given position in the given byte array.
     */
    public static final long longAt(byte[] data, int pos) {
        return (long) (data[pos + 0] & 0xff) << 56
                | (long) (data[pos + 1] & 0xff) << 48
                | (long) (data[pos + 2] & 0xff) << 40
                | (long) (data[pos + 3] & 0xff) << 32
                | (long) (data[pos + 4] & 0xff) << 24
                | (long) (data[pos + 5] & 0xff) << 16
                | (long) (data[pos + 6] & 0xff) << 8
                | (long) (data[pos + 7] & 0xff) << 0;
    }

    public static final byte[] getBytes(long value) {
        byte[] data = new byte[8];
        data[0] = (byte) (0xff & value >> 56);
        data[1] = (byte) (0xff & value >> 48);
        data[2] = (byte) (0xff & value >> 40);
        data[3] = (byte) (0xff & value >> 32);
        data[4] = (byte) (0xff & value >> 24);
        data[5] = (byte) (0xff & value >> 16);
        data[6] = (byte) (0xff & value >> 8);
        data[7] = (byte) (0xff & value >> 0);
        return data;
    }
}
