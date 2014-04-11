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
package polyglot.ext.jl5.types.reflect;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import polyglot.ext.jl5.types.AnnotationElementValue;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.ext.jl5.types.JL5ClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.Type;
import polyglot.types.reflect.Attribute;
import polyglot.types.reflect.ClassFile;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

public class Annotations extends Attribute {

    protected DataInputStream in;
    protected ClassFile cls;
    protected Position position;
    protected Annotation[] annotations;

    /**
     * Structure: (JVM Spec Java SE 7th edition)
     * annotations_attribute {
     *   u2         attribute_name_index;
     *   u4         attribute_length;
     *   u2         num_annotations;
     *   annotation annotations[num_annotations];
     * }
     * 
     * annotation {
     *       u2 type_index;
     *       u2 num_element_value_pairs;
     *       {   u2            element_name_index;
     *           element_value value;
     *       } element_value_pairs[num_element_value_pairs];
     * }
     * 
     * element_value {
     *   u1 tag;
     *   union {
     *       u2 const_value_index;
     *       {   u2 type_name_index;
     *           u2 const_name_index;
     *       } enum_const_value;
     *       u2 class_info_index;
     *       annotation annotation_value;
     *       {   u2            num_values;
     *           element_value values[num_values];
     *       } array_value;
     *   } value;
     * }
     *        
     */

    Annotations(ClassFile clazz, DataInputStream in, int nameIndex, int length)
            throws IOException {
        super(nameIndex, length);
        this.cls = clazz;
        int numAnnotations = in.readUnsignedShort();
        this.annotations = new Annotation[numAnnotations];
        for (int i = 0; i < numAnnotations; i++) {
            this.annotations[i] = new Annotation(clazz, in);
        }
    }

    public Map<Type, Map<String, AnnotationElementValue>> toAnnotationElems(
            JL5ClassFileLazyClassInitializer init, JL5TypeSystem ts) {

        Position pos = init.position();

        Map<Type, Map<String, AnnotationElementValue>> m =
                new LinkedHashMap<>();
        for (Annotation a : this.annotations) {
            String typeString =
                    (String) (cls.getConstants()[a.typeIndex]).value();
            Type type = init.typeForString(typeString);

            m.put(type, a.createAnnotationElementValues(init, ts, pos));
        }

        return m;
    }

    class Annotation implements ElementValue {
        int typeIndex;
        Map<String, ElementValue> elementValuePairs;
        private ClassFile cls;

        public Annotation(ClassFile clazz, DataInputStream in)
                throws IOException {
            this.cls = clazz;
            this.typeIndex = in.readUnsignedShort();
            int numElementValuePairs = in.readUnsignedShort();

            this.elementValuePairs = new LinkedHashMap<>();

            for (int i = 0; i < numElementValuePairs; i++) {
                int elementNameIndex = in.readUnsignedShort();
                ElementValue val = readElementValue(clazz, in);
                String elementName =
                        (String) clazz.getConstants()[elementNameIndex].value();
                elementValuePairs.put(elementName, val);
            }
        }

        public Map<String, AnnotationElementValue> createAnnotationElementValues(
                JL5ClassFileLazyClassInitializer init, JL5TypeSystem ts,
                Position pos) {

            Map<String, AnnotationElementValue> m = new LinkedHashMap<>();

            for (String key : elementValuePairs.keySet()) {
                m.put(key,
                      elementValuePairs.get(key).toAnnotationElementValue(init,
                                                                          ts,
                                                                          pos));
            }
            return m;
        }

        private ElementValue readElementValue(ClassFile clazz,
                DataInputStream in) throws IOException {
            char tag = (char) in.readUnsignedByte();
            switch (tag) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 's':
                int constValueIndex = in.readUnsignedShort();
                // return a value of the appropriate type.
                return new ElementValueConstant(Character.toUpperCase(tag),
                                                constValueIndex);

            case 'e':
                // enum constant
                int typeName = in.readUnsignedShort();
                int constName = in.readUnsignedShort();
                return new ElementValueEnumConstant(typeName, constName);

            case 'c':
                // class
                int classInfo = in.readUnsignedShort();
                return new ElementValueClassConstant(classInfo);

            case '@':
                // annotation
                return new Annotation(clazz, in);

            case '[':
                // annotation
                int numValues = in.readUnsignedShort();
                ElementValue[] vals = new ElementValue[numValues];
                for (int i = 0; i < numValues; i++) {
                    vals[i] = readElementValue(clazz, in);
                }
                return new ElementValueArray(vals);

            default:
                throw new InternalCompilerError("Don't know how to deal with "
                        + tag);
            }
        }

        @Override
        public AnnotationElementValue toAnnotationElementValue(
                JL5ClassFileLazyClassInitializer init, JL5TypeSystem ts,
                Position pos) {
            String typeName = (String) cls.getConstants()[typeIndex].value();
            Type type = init.typeForString(typeName);
            if (!type.isClass()) {
                throw new InternalCompilerError("Type " + type + " ("
                        + typeName + ") is not a class.");
            }
            return ts.AnnotationElementValueAnnotation(pos,
                                                       type,
                                                       createAnnotationElementValues(init,
                                                                                     ts,
                                                                                     pos));
        }
    }

    static abstract interface ElementValue {
        AnnotationElementValue toAnnotationElementValue(
                JL5ClassFileLazyClassInitializer init, JL5TypeSystem ts,
                Position pos);
    }

    class ElementValueConstant implements ElementValue {
        char type;
        int constValueIndex;

        public ElementValueConstant(char type, int constValueIndex) {
            this.type = type;
            this.constValueIndex = constValueIndex;
        }

        @Override
        public AnnotationElementValue toAnnotationElementValue(
                JL5ClassFileLazyClassInitializer init, JL5TypeSystem ts,
                Position pos) {
            return ts.AnnotationElementValueConstant(pos,
                                                     init.typeForString(String.valueOf(type)),
                                                     cls.getConstants()[constValueIndex].value());
        }
    }

    class ElementValueEnumConstant implements ElementValue {
        int typeIndex;
        int constNameIndex;

        public ElementValueEnumConstant(int typeIndex, int constNameIndex) {
            this.typeIndex = typeIndex;
            this.constNameIndex = constNameIndex;
        }

        @Override
        public AnnotationElementValue toAnnotationElementValue(
                JL5ClassFileLazyClassInitializer init, JL5TypeSystem ts,
                Position pos) {
            String typeName = (String) cls.getConstants()[typeIndex].value();
            String constName =
                    (String) cls.getConstants()[constNameIndex].value();
            Type type = init.typeForString(typeName);
            if (!type.isClass()) {
                throw new InternalCompilerError("Type " + type + " ("
                        + typeName + ") is not a class.");
            }
            JL5ClassType ct = (JL5ClassType) type;
            EnumInstance ei = ct.enumConstantNamed(constName);
            if (ei == null) {
                System.err.println("Class is " + ct);
                System.err.println("   " + ct.fields());
                System.err.println("   XXX" + ct.enumConstants());
                System.err.println("   " + ct.enumConstantNamed("METHOD"));
                throw new InternalCompilerError("No enum constant named "
                        + constName + " in " + type);
            }
            return ts.AnnotationElementValueConstant(pos, type, ei);
        }
    }

    class ElementValueClassConstant implements ElementValue {
        int classInfo;

        public ElementValueClassConstant(int classInfo) {
            this.classInfo = classInfo;
        }

        @Override
        public AnnotationElementValue toAnnotationElementValue(
                JL5ClassFileLazyClassInitializer init, JL5TypeSystem ts,
                Position pos) {
            return ts.AnnotationElementValueConstant(pos,
                                                     ts.Class(),
                                                     init.typeForString((String) cls.getConstants()[classInfo].value()));
        }
    }

    class ElementValueArray implements ElementValue {
        ElementValue[] vals;

        public ElementValueArray(ElementValue[] vals) {
            this.vals = vals;
        }

        @Override
        public AnnotationElementValue toAnnotationElementValue(
                JL5ClassFileLazyClassInitializer init, JL5TypeSystem ts,
                Position pos) {
            List<AnnotationElementValue> l = new ArrayList<>();
            for (ElementValue v : vals) {
                l.add(v.toAnnotationElementValue(init, ts, pos));
            }
            return ts.AnnotationElementValueArray(pos, l);
        }
    }
}
