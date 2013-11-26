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
package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import polyglot.ast.Expr;
import polyglot.ast.New;
import polyglot.ast.NewOps;
import polyglot.ast.Node;
import polyglot.ast.Node_c;
import polyglot.ast.Special;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5ClassType;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.ParsedClassType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5NewExt extends JL5Ext {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected List<TypeNode> typeArgs;

    public List<TypeNode> typeArgs() {
        return this.typeArgs;
    }

    public New typeArgs(List<TypeNode> typeArgs) {
        if (this.typeArgs == typeArgs) {
            return (New) this.node();
        }
        New n = (New) this.node().copy();
        JL5NewExt ext = (JL5NewExt) JL5Ext.ext(n);
        ext.typeArgs = typeArgs;
        return n;
    }

    public Node visitChildren(NodeVisitor v) {
        JL5NewExt ext = (JL5NewExt) JL5Ext.ext(this.node());

        List<TypeNode> typeArgs = this.node().visitList(ext.typeArgs(), v);

        Node newN = this.node().visitChildren(v);
        JL5NewExt newext = (JL5NewExt) JL5Ext.ext(newN);

        if (!CollectionUtil.equals(typeArgs, newext.typeArgs())) {
            // the type args changed! Let's update the node.
            if (newN == this.node()) {
                // we need to create a copy.
                newN = (Node) newN.copy();
                newext = (JL5NewExt) JL5Ext.ext(newN);
            }
            else {
                // the call to super.visitChildren(v) already
                // created a copy of the node (and thus of its extension).
            }
            newext.typeArgs = typeArgs;
        }
        return newN;
    }

    public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
            throws SemanticException {
        New n = (New) this.node().disambiguateOverride(parent, ar);
        JL5NewExt ext = (JL5NewExt) JL5Ext.ext(n);
        // now do the type args
        return ext.typeArgs(n.visitList(ext.typeArgs(), ar));
    }

    public TypeNode findQualifiedTypeNode(AmbiguityRemover ar, ClassType outer,
            TypeNode objectType) throws SemanticException {
        if (objectType instanceof AmbTypeInstantiation) {
            JL5TypeSystem ts = (JL5TypeSystem) ar.typeSystem();
            JL5NodeFactory nf = (JL5NodeFactory) ar.nodeFactory();
            Context c = ar.context();

            // Check for visibility of inner class, but ignore result
            ts.findMemberClass(outer, objectType.name(), c.currentClass());

            if (outer instanceof ParsedClassType) {
                ParsedClassType opct = (ParsedClassType) outer;
                c = c.pushClass(opct, opct);
                objectType = (TypeNode) objectType.visit(ar.context(c));
                return objectType;
            }
            else if (outer instanceof JL5SubstClassType) {
                JL5SubstClassType osct = (JL5SubstClassType) outer;
                c = c.pushClass(osct.base(), osct.base());
                objectType = (TypeNode) objectType.visit(ar.context(c));
                if (!objectType.isDisambiguated()) return objectType;

                Map<TypeVariable, ReferenceType> substMap =
                        new LinkedHashMap<TypeVariable, ReferenceType>(osct.subst()
                                                                           .substitutions());
                Type type = objectType.type();
                if (type instanceof JL5SubstClassType) {
                    JL5SubstClassType tsct = (JL5SubstClassType) type;
                    substMap.putAll(tsct.subst().substitutions());
                    type = tsct.base();
                }
                return nf.CanonicalTypeNode(type.position(),
                                            ts.subst(type, substMap));
            }
            else if (outer instanceof RawClass) {
                throw new SemanticException("The member type " + outer + "."
                        + objectType
                        + " must be qualified with a parameterized type,"
                        + " since it is not static");
            }
        }
        return ((NewOps) this.node()).findQualifiedTypeNode(ar,
                                                            outer,
                                                            objectType);
    }

    public New findQualifier(AmbiguityRemover ar, ClassType ct)
            throws SemanticException {
        // Call super.findQualifier in order to perform its checks, but throw away the
        // qualifier that it finds. That is, just return this node. Do not attempt to infer 
        // a qualifier if one is missing.
        ((NewOps) this.node()).findQualifier(ar, ct);
        return (New) this.node();
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException {
        New n = (New) this.node();
        JL5NewExt ext = (JL5NewExt) JL5Ext.ext(n);
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();

        if (!n.objectType().type().isClass()) {
            throw new SemanticException("Must have a class for a new expression.",
                                        n.position());
        }

        List<Type> argTypes = new ArrayList<Type>(n.arguments().size());

        for (Expr e : n.arguments()) {
            argTypes.add(e.type());
        }

        List<ReferenceType> actualTypeArgs =
                new ArrayList<ReferenceType>(ext.typeArgs().size());
        for (TypeNode tn : ext.typeArgs()) {
            actualTypeArgs.add((ReferenceType) tn.type());
        }

        ((NewOps) this.node()).typeCheckFlags(tc);
        ((NewOps) this.node()).typeCheckNested(tc);

        if (n.body() != null) {
            ts.checkClassConformance(n.anonType());
        }

        ClassType ct = n.objectType().type().toClass();

        if (ct.isInnerClass()) {
            ClassType outer = ct.outer();
            JL5TypeSystem ts5 = (JL5TypeSystem) tc.typeSystem();
            if (outer instanceof JL5SubstClassType) {
                JL5SubstClassType sct = (JL5SubstClassType) outer;
                ct = (ClassType) sct.subst().substType(ct);
            }
            else if (n.qualifier() == null
                    || (n.qualifier() instanceof Special && ((Special) n.qualifier()).kind() == Special.THIS)) {
                ct = ts5.instantiateInnerClassFromContext(tc.context(), ct);
            }
            else if (n.qualifier().type() instanceof JL5SubstClassType) {
                JL5SubstClassType sct =
                        (JL5SubstClassType) n.qualifier().type();
                ct = (ClassType) sct.subst().substType(ct);
            }
        }

        ConstructorInstance ci;
        if (!ct.flags().isInterface()) {
            Context c = tc.context();
            if (n.anonType() != null) {
                c = c.pushClass(n.anonType(), n.anonType());
            }
            ci =
                    ts.findConstructor(ct,
                                       argTypes,
                                       actualTypeArgs,
                                       c.currentClass());
        }
        else {
            ci = ts.defaultConstructor(n.position(), ct);
        }

        n = n.constructorInstance(ci);

        if (n.anonType() != null) {
            // The type of the new expression is the anonymous type, not the base type.
            ct = n.anonType();
        }

        return n.type(ct);
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        New n = (New) this.node();
        JL5NewExt ext = (JL5NewExt) JL5Ext.ext(n);

        ((NewOps) this.node()).printQualifier(w, tr);
        w.write("new ");

        if (ext.typeArgs() != null && !ext.typeArgs().isEmpty()) {
            w.write("<");
            Iterator<TypeNode> it = ext.typeArgs().iterator();
            while (it.hasNext()) {
                TypeNode tn = it.next();
                ((Node_c) n).print(tn, w, tr);
                if (it.hasNext()) {
                    w.write(",");
                    w.allowBreak(0, " ");
                }
            }
            w.write(">");
            w.allowBreak(0, " ");
        }

        // We need to be careful when pretty printing "new" expressions for
        // member classes.  For the expression "e.new C()" where "e" has
        // static type "T", the TypeNode for "C" is actually the type "T.C".
        // But, if we print "T.C", the post compiler will try to lookup "T"
        // in "T".  Instead, we print just "C".
        if (n.qualifier() != null && n.objectType().type() != null) {
            w.write(n.objectType().name());
            ClassType ct = n.objectType().type().toClass();
            if (ct instanceof JL5SubstClassType) {
                boolean printParams = true;
                if (tr instanceof JL5Translator) {
                    JL5Translator jtr = (JL5Translator) tr;
                    printParams = !jtr.removeJava5isms();
                }
                if (printParams) {
                    JL5SubstClassType jsct = (JL5SubstClassType) ct;
                    jsct.printParams(w);
                }
            }
        }
        else {
            ((Node_c) n).print(n.objectType(), w, tr);
        }

        ((NewOps) this.node()).printArgs(w, tr);
        ((NewOps) this.node()).printBody(w, tr);
    }

    public ClassType findEnclosingClass(Context c, ClassType ct) {
        New n = (New) this.node();
        JL5NewExt ext = (JL5NewExt) JL5Ext.ext(n);

        if (ct == n.anonType()) {
            // we need to find ct, is an anonymous class, and so 
            // the enclosing class is the current class.
            return c.currentClass();
        }

        JL5TypeSystem ts = (JL5TypeSystem) ct.typeSystem();
        ClassType t = findEnclosingClassFrom(c.currentClass(), c, ct);
        if (t == null) {
            // couldn't find anything suitable using the JL5ParsedClassType. Try using the raw class.
            t =
                    findEnclosingClassFrom(ts.rawClass((JL5ParsedClassType) c.currentClass()),
                                           c,
                                           ct);
        }
        return t;
    }

    ClassType findEnclosingClassFrom(ClassType t, Context c, ClassType ct) {
        JL5TypeSystem ts = (JL5TypeSystem) ct.typeSystem();
        String name = ct.name();
        while (t != null) {
            try {
                ClassType mt = ts.findMemberClass(t, name, c.currentClass());
                if (mt != null) {
                    // get the class directly from t, so that substitution works properly...
                    mt = findMemberClass(name, t);
                    if (mt == null) {
                        throw new InternalCompilerError("Couldn't find member class "
                                + name + " in " + t);
                    }
                    if (ts.isImplicitCastValid(mt, ct)) {
                        return t;
                    }
                    if (ts.isImplicitCastValid(ts.erasureType(mt),
                                               ts.erasureType(ct))) {
                        return (ClassType) ts.erasureType(t);
                    }
                }
            }
            catch (SemanticException e) {
            }

            t = t.outer();
        }
        return null;
    }

    private ClassType findMemberClass(String name, ClassType t) {
        ClassType mt = t.memberClassNamed(name);
        if (mt != null) {
            return mt;
        }
        for (Type sup : ((JL5ClassType) t).superclasses()) {
            if (sup instanceof ClassType) {
                mt = findMemberClass(name, sup.toClass());
                if (mt != null) {
                    return mt;
                }
            }
        }

        for (Type sup : t.interfaces()) {
            if (sup instanceof ClassType) {
                mt = findMemberClass(name, sup.toClass());
                if (mt != null) {
                    return mt;
                }
            }
        }
        return null;
    }
}