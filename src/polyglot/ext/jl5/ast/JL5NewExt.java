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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import polyglot.ast.Expr;
import polyglot.ast.JLang;
import polyglot.ast.Lang;
import polyglot.ast.New;
import polyglot.ast.NewOps;
import polyglot.ast.Node;
import polyglot.ast.Special;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5ClassType;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.ParsedClassType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/** Extends {@code New} with the ability to supply type arguments to the constructor.
 */
public class JL5NewExt extends JL5ProcedureCallExt implements NewOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public JL5NewExt() {
        this(null);
    }

    public JL5NewExt(List<TypeNode> typeArgs) {
        super(typeArgs);
    }

    @Override
    public New node() {
        return (New) super.node();
    }

    @Override
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
            throws SemanticException {
        New n = (New) superLang().disambiguateOverride(this.node(), parent, ar);
        // now do the type args
        n = typeArgs(n, visitList(typeArgs, ar));
        return n;
    }

    @Override
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
                        new LinkedHashMap<>(osct.subst().substitutions());
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
        return superLang().findQualifiedTypeNode(this.node(),
                                                 ar,
                                                 outer,
                                                 objectType);
    }

    @Override
    public Expr findQualifier(AmbiguityRemover ar, ClassType ct)
            throws SemanticException {
        // Call super.findQualifier in order to perform its checks, but throw away the
        // qualifier that it finds. That is, just return this node. Do not attempt to infer 
        // a qualifier if one is missing.
        superLang().findQualifier(this.node(), ar, ct);
        return this.node().qualifier();
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        New n = this.node();
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();

        if (!n.objectType().type().isClass()) {
            throw new SemanticException("Must have a class for a new expression.",
                                        n.position());
        }

        List<Type> argTypes = new ArrayList<>(n.arguments().size());

        for (Expr e : n.arguments()) {
            argTypes.add(e.type());
        }

        List<ReferenceType> actualTypeArgs = actualTypeArgs();

        superLang().typeCheckFlags(this.node(), tc);
        superLang().typeCheckNested(this.node(), tc);

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
                                       c.currentClass(),
                                       n.body() == null);
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

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        printQualifier(w, tr);
        w.write("new ");

        super.prettyPrint(w, tr);

        // We need to be careful when pretty printing "new" expressions for
        // member classes.  For the expression "e.new C()" where "e" has
        // static type "T", the TypeNode for "C" is actually the type "T.C".
        // But, if we print "T.C", the post compiler will try to lookup "T"
        // in "T".  Instead, we print just "C".
        New n = this.node();
        if (n.qualifier() != null && n.objectType().type() != null) {
            ((JLang) tr.lang()).printShortObjectType(this.node(), w, tr);
        }
        else {
            print(n.objectType(), w, tr);
        }

        printArgs(w, tr);
        printBody(w, tr);
    }

    @Override
    public ClassType findEnclosingClass(Context c, ClassType ct) {
        New n = this.node();

        if (n.anonType() != null) {
            // we need to find ct, is an anonymous class, and so 
            // the enclosing class is the current class.
            return c.currentClass();
        }

        JL5TypeSystem ts = (JL5TypeSystem) ct.typeSystem();
        ClassType t = findEnclosingClassFrom(c.currentClass(), c, ct);
        if (t == null) {
            // couldn't find anything suitable using the JL5ParsedClassType. Try using the raw class.
            JL5ParsedClassType curClass = (JL5ParsedClassType) c.currentClass();
            if (ts.canBeRaw(curClass))
                t = findEnclosingClassFrom(ts.rawClass(curClass), c, ct);
        }
        return t;
    }

    private ClassType findEnclosingClassFrom(ClassType t, Context c,
            ClassType ct) {
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

    @Override
    public void typeCheckFlags(TypeChecker tc) throws SemanticException {
        superLang().typeCheckFlags(this.node(), tc);
    }

    @Override
    public void typeCheckNested(TypeChecker tc) throws SemanticException {
        superLang().typeCheckNested(this.node(), tc);
    }

    @Override
    public void printQualifier(CodeWriter w, PrettyPrinter tr) {
        superLang().printQualifier(this.node(), w, tr);
    }

    @Override
    public void printShortObjectType(CodeWriter w, PrettyPrinter tr) {
        New n = this.node();
        superLang().printShortObjectType(n, w, tr);
        ClassType ct = n.objectType().type().toClass();
        if (ct instanceof JL5SubstClassType) {
            JL5SubstClassType jsct = (JL5SubstClassType) ct;
            jsct.printParams(w);
        }
    }

    @Override
    public void printBody(CodeWriter w, PrettyPrinter tr) {
        superLang().printBody(this.node(), w, tr);
    }

    @Override
    public boolean constantValueSet(Lang lang) {
        return superLang().constantValueSet(node(), lang);
    }

    @Override
    public boolean isConstant(Lang lang) {
        return superLang().isConstant(node(), lang);
    }

    @Override
    public Object constantValue(Lang lang) {
        return superLang().constantValue(node(), lang);
    }
}
