/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import polyglot.frontend.SchedulerException;
import polyglot.main.Report;
import polyglot.types.Named;
import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;

/**
 * The <code>TypeEncoder</code> gives the ability to encode a polyglot 
 * <code>Type</code> as a Java string.
 * <p>
 * It uses a form of serialization to encode the <code>Type</code> into
 * a byte stream and then converts the byte stream to a standard Java string.
 * <p>
 * The difference between the encoder and a normal serialization process is
 * that in order to encode this type, we need to sever any links to other types
 * in the current environment. So any <code>ClassType</code> other than the 
 * the type being encoded is replaced in the stream with a 
 * <code>PlaceHolder</code> that contains the name of the class. To aid
 * in the decoding process, placeholders for member classes user their 
 * "mangled" name; non-member classes use their fully qualified name.
 */
public class TypeEncoder {
    protected TypeSystem ts;
    protected final boolean zip = true;
    protected final boolean base64 = true;
    protected final boolean test = false;
    protected Map<Object, Object> placeHolderCache;
    protected int depth;

    public TypeEncoder(TypeSystem ts) {
        this.ts = ts;
        this.placeHolderCache = null;
    }

    /**
     * Serialize a type object into a string.
     * @return String containing the encoded type object.
     * @param t The TypeObject to encode.
     * @throws IOException If the encoding fails.
     */
    public String encode(TypeObject t) throws IOException {
        ByteArrayOutputStream baos;
        ObjectOutputStream oos;

        if (Report.should_report(Report.serialize, 1)) {
            Report.report(1, "Encoding type " + t);
        }

        baos = new ByteArrayOutputStream();

        if (zip) {
            oos = new TypeOutputStream(new GZIPOutputStream(baos), ts, t);
        }
        else {
            oos = new TypeOutputStream(baos, ts, t);
        }

        oos.writeObject(t);
        oos.flush();
        oos.close();

        byte[] b = baos.toByteArray();

        if (Report.should_report(Report.serialize, 2)) {
            Report.report(2, "Size of serialization (with" + (zip ? "" : "out")
                    + " zipping) is " + b.length + " bytes");
        }

        String s;

        if (base64) {
            s = new String(Base64.encode(b));
        }
        else {
            StringBuffer sb = new StringBuffer(b.length);
            for (int i = 0; i < b.length; i++)
                sb.append((char) b[i]);
            s = sb.toString();
        }

        if (Report.should_report(Report.serialize, 2)) {
            Report.report(2,
                          "Size of serialization after conversion to string is "
                                  + s.length() + " characters");
        }

        if (test) {
            // Test it.
            try {
                String name = null;
                if (t instanceof Named) {
                    name = ((Named) t).fullName();
                }
                decode(s, name);
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new InternalCompilerError("Could not decode back to " + t
                        + ": " + e.getMessage(), e);
            }
        }

        return s;
    }

    /**
     * Decode a serialized type object.  If deserialization fails because
     * a type could not be resolved, the method returns null.  The calling
     * pass should abort in that case.
     * @param s String containing the encoded type object.
     * @return The decoded TypeObject, or null if deserialization fails.
     * @throws InvalidClassException If the string is malformed.
     */
    @SuppressWarnings("unused")
    public TypeObject decode(String s, String name)
            throws InvalidClassException {
        TypeInputStream ois = null;
        byte[] b;

        if (base64) {
            b = Base64.decode(s.toCharArray());
        }
        else {
            char[] source;
            source = s.toCharArray();
            b = new byte[source.length];
            for (int i = 0; i < source.length; i++)
                b[i] = (byte) source[i];
        }

        Map<Object, Object> oldCache = placeHolderCache;
        placeHolderCache = new HashMap<Object, Object>();
        if (oldCache != null) {
            placeHolderCache.putAll(oldCache);
        }

        if (Report.should_report(Report.serialize, 1))
            Report.report(1, "TypeEncoder depth " + depth + " at " + name);
        depth++;

        try {
            if (zip && !base64) {
                // The base64 decoder automatically unzips byte streams, so
                // we only need an explicit GZIPInputStream if we are not
                // using base64 encoding.
                ois =
                        new TypeInputStream(new GZIPInputStream(new ByteArrayInputStream(b)),
                                            ts,
                                            placeHolderCache);
            }
            else {
                ois =
                        new TypeInputStream(new ByteArrayInputStream(b),
                                            ts,
                                            placeHolderCache);
            }

            TypeObject o = (TypeObject) ois.readObject();

            if (ois.deserializationFailed()) {
                return null;
            }

            return o;
        }
        catch (InvalidClassException e) {
            throw e;
        }
        catch (IOException e) {
            throw new InternalCompilerError("IOException thrown while "
                    + "decoding serialized type info: " + e.getMessage(), e);
        }
        catch (ClassNotFoundException e) {
            throw new InternalCompilerError("Unable to find one of the classes "
                                                    + "for the serialized type info: "
                                                    + e.getMessage(),
                                            e);
        }
        catch (SchedulerException e) {
            throw new InternalCompilerError("SchedulerException thrown while "
                    + "decoding serialized type info: " + e.getMessage(), e);
        }
        finally {
            placeHolderCache = oldCache;
            depth--;
            if (ois != null) {
                try {
                    ois.close();
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
