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

package polyglot.visit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassMember;
import polyglot.ast.FieldDecl;
import polyglot.ast.IntLit;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.main.Report;
import polyglot.main.Version;
import polyglot.types.ClassType;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.InitializerInstance;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.Position;
import polyglot.util.TypeEncoder;

/**
 * Visitor which serializes class objects and adds a field to the class
 * containing the serialization.
 */
public class ClassSerializer extends NodeVisitor {
    /**
     * The maximum number of characters that will be assigned to an encoded type
     * string field. More characters than this will be broken up over several
     * fields.
     */
    private static final int MAX_ENCODED_TYPE_INFO_STRING_LENGTH = 8192;

    protected TypeEncoder te;
    protected ErrorQueue eq;
    protected long time;
    protected TypeSystem ts;
    protected NodeFactory nf;
    protected Version ver;

    public ClassSerializer(TypeSystem ts, NodeFactory nf, long time,
            ErrorQueue eq, Version ver) {
        this.ts = ts;
        this.nf = nf;
        this.te = new TypeEncoder(ts);
        this.eq = eq;
        this.time = time;
        this.ver = ver;
    }

    @Override
    public Node override(Node n) {
        // Stop at class members. We only want to encode top-level classes.
        if (n instanceof ClassMember && !(n instanceof ClassDecl)) {
            return n;
        }

        return null;
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        if (!(n instanceof ClassDecl)) {
            return n;
        }

        ClassDecl cd = (ClassDecl) n;
        ClassBody body = cd.body();

        List<ClassMember> l = createSerializationMembers(cd);

        for (ClassMember m : l) {
            body = body.addMember(m);
        }

        return cd.body(body);
    }

    public List<ClassMember> createSerializationMembers(ClassDecl cd) {
        return createSerializationMembers(cd.type());
    }

    public List<ClassMember> createSerializationMembers(ClassType ct) {
        try {
            List<ClassMember> newMembers = new ArrayList<ClassMember>(3);

            // HACK: force class members to get created from lazy class
            // initializer.
            ct.memberClasses();
            ct.constructors();
            ct.methods();
            ct.fields();
            ct.interfaces();
            ct.superType();

            // Only serialize top-level and member classes.
            if (!ct.isTopLevel() && !ct.isMember()) {
                return Collections.emptyList();
            }

            /* Add the compiler version number. */
            String suffix = ver.name();

            // Check if we've already serialized.
            if (ct.fieldNamed("jlc$CompilerVersion$" + suffix) != null
                    || ct.fieldNamed("jlc$SourceLastModified$" + suffix) != null
                    || ct.fieldNamed("jlc$ClassType$" + suffix) != null) {

                eq.enqueue(ErrorInfo.SEMANTIC_ERROR,
                           "Cannot serialize class information "
                                   + "more than once.");

                return Collections.emptyList();
            }

            Flags flags = Flags.PUBLIC.set(Flags.STATIC).set(Flags.FINAL);

            FieldDecl f;
            FieldInstance fi;
            InitializerInstance ii;

            /* Add the compiler version number. */
            String version =
                    ver.major() + "." + ver.minor() + "." + ver.patch_level();

            Position pos = Position.compilerGenerated();

            fi =
                    ts.fieldInstance(pos,
                                     ct,
                                     flags,
                                     ts.String(),
                                     "jlc$CompilerVersion$" + suffix);
            fi.setConstantValue(version);
            ii = ts.initializerInstance(pos, ct, Flags.STATIC);
            f =
                    nf.FieldDecl(fi.position(),
                                 fi.flags(),
                                 nf.CanonicalTypeNode(fi.position(), fi.type()),
                                 nf.Id(fi.position(), fi.name()),
                                 nf.StringLit(pos, version).type(ts.String()));

            f = f.fieldInstance(fi);
            f = f.initializerInstance(ii);
            newMembers.add(f);

            fi =
                    ts.fieldInstance(pos,
                                     ct,
                                     flags,
                                     ts.Long(),
                                     "jlc$SourceLastModified$" + suffix);
            fi.setConstantValue(new Long(time));
            ii = ts.initializerInstance(pos, ct, Flags.STATIC);
            f =
                    nf.FieldDecl(fi.position(),
                                 fi.flags(),
                                 nf.CanonicalTypeNode(fi.position(), fi.type()),
                                 nf.Id(fi.position(), fi.name()),
                                 nf.IntLit(pos, IntLit.LONG, time)
                                   .type(ts.Long()));

            f = f.fieldInstance(fi);
            f = f.initializerInstance(ii);
            newMembers.add(f);

            // output the encoded type info, over several fields if needed.
            String encodedTypeInfo = te.encode(ct);
            int etiStart = 0;
            int etiEnd = 0;
            int numberETIFields = 0;
            do {
                etiEnd = encodedTypeInfo.length();
                if (etiEnd - etiStart > MAX_ENCODED_TYPE_INFO_STRING_LENGTH) {
                    etiEnd = etiStart + MAX_ENCODED_TYPE_INFO_STRING_LENGTH;
                }
                // add an additional suffix to distinguish fields.
                String additionalFieldSuffix =
                        numberETIFields == 0 ? "" : ("$" + numberETIFields);
                String encoded = encodedTypeInfo.substring(etiStart, etiEnd);
                fi =
                        ts.fieldInstance(pos,
                                         ct,
                                         flags,
                                         ts.String(),
                                         "jlc$ClassType$" + suffix
                                                 + additionalFieldSuffix);
                fi.setConstantValue(encoded);
                ii = ts.initializerInstance(pos, ct, Flags.STATIC);

                f =
                        nf.FieldDecl(fi.position(),
                                     fi.flags(),
                                     nf.CanonicalTypeNode(fi.position(),
                                                          fi.type()),
                                     nf.Id(fi.position(), fi.name()),
                                     nf.StringLit(pos, encoded)
                                       .type(ts.String()));

                f = f.fieldInstance(fi);
                f = f.initializerInstance(ii);
                newMembers.add(f);

                numberETIFields++;
                etiStart = etiEnd;
            } while (etiEnd != encodedTypeInfo.length());

            return newMembers;
        }
        catch (IOException e) {
            if (Report.should_report(Report.serialize, 1)) e.printStackTrace();
            eq.enqueue(ErrorInfo.IO_ERROR,
                       "Unable to serialize class information.");
            return Collections.emptyList();
        }
    }
}
