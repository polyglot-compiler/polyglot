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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import polyglot.ast.Ambiguous;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ast.TypeNode_c;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.types.ClassType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Copy;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

public class AmbTypeInstantiation extends TypeNode_c implements Ambiguous {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected TypeNode base;
    protected List<TypeNode> typeArguments;

    public AmbTypeInstantiation(Position pos, TypeNode base,
            List<TypeNode> typeArguments) {
        super(pos);
        assert typeArguments != null;
        this.base = base;
        this.typeArguments = typeArguments;
    }

    @Override
    public String name() {
        return base.name();
    }

    public TypeNode base() {
        return base;
    }

    protected <N extends AmbTypeInstantiation> N base(N n, TypeNode base) {
        if (n.base == base) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.base = base;
        return n;
    }

    public List<TypeNode> typeArguments() {
        return typeArguments;
    }

    protected <N extends AmbTypeInstantiation> N typeArguments(N n,
            List<TypeNode> typeArguments) {
        if (CollectionUtil.equals(n.typeArguments, typeArguments)) return n;
        if (n == this) n = Copy.Util.copy(n);
        n.typeArguments = typeArguments;
        return n;
    }

    protected AmbTypeInstantiation reconstruct(TypeNode base,
            List<TypeNode> typeArguments) {
        AmbTypeInstantiation n = this;
        n = base(n, base);
        n = typeArguments(n, typeArguments);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        TypeNode base = visitChild(this.base, v);
        List<TypeNode> arguments = visitList(typeArguments, v);
        return reconstruct(base, arguments);
    }

    @Override
    public Node disambiguate(AmbiguityRemover sc) throws SemanticException {
        if (!shouldDisambiguate()) return this;

        checkRareType(sc);

        Map<TypeVariable, ReferenceType> typeMap = new LinkedHashMap<>();
        JL5ParsedClassType pct = handleBase(typeMap);

        Type baseType = base.type();
//        System.err.println("Base type is " + base);
//        System.err.println("typeArguments is " + typeArguments);
//        if (baseType instanceof JL5SubstClassType) {
//            System.err.println("  " + this.position);
//            System.err.println("    base type of " + this + " is " + base.type()+ " " + base.type().getClass());
//            System.err.println("   base type base is " + ((JL5SubstClassType)baseType).base());
//            System.err.println("   base type instantiation is " + ((JL5SubstClassType)baseType).subst() + "  " + ((JL5SubstClassType)baseType).subst().getClass());
//            System.err.println("   type args are is " + this.typeArguments);
//        }

        if (pct.pclass() == null || pct.pclass().formals().isEmpty()) {
            // The base class has no formals.
            // Then this node should not be created in the first place.
            throw new SemanticException("Cannot instantiate " + baseType
                    + " because it has no formals", position);
        }

        checkParamSize(pct);

        // if subst is not null, check that subst does not already define the formal type variables
        if (!typeMap.isEmpty()) {
            if (typeMap.keySet().containsAll(pct.typeVariables())) {
                throw new SemanticException("Cannot instantiate " + baseType
                        + " with arguments " + typeArguments, this.position());
            }
        }

        // add the new mappings 
        List<TypeVariable> formals = pct.typeVariables();
        for (int i = 0; i < typeArguments.size(); i++) {
            ReferenceType t = (ReferenceType) typeArguments.get(i).type();
            typeMap.put(formals.get(i), t);
        }

//        System.err.println("Instantiating " + pct + " with " + typeMap);
        JL5TypeSystem ts = (JL5TypeSystem) sc.typeSystem();
        Type instantiated = ts.subst(pct, typeMap);
        return sc.nodeFactory().CanonicalTypeNode(position, instantiated);
    }

    protected boolean shouldDisambiguate() {
        if (!base.isDisambiguated()) {
            return false;
        }
        for (TypeNode tn : typeArguments) {
            if (!tn.isDisambiguated()) {
                return false;
            }
        }
        return true;
    }

    protected void checkRareType(AmbiguityRemover ar) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) ar.typeSystem();
        Type baseType = base.type();
        if (baseType instanceof ClassType) {
            ClassType ct = (ClassType) baseType;
            if (ct.isInnerClass()) {
                ClassType outer = ct.outer();
                if (outer instanceof RawClass) {
                    ClassType currentClass = ar.context().currentClass();
                    ClassType outerBase = ((RawClass) outer).base();
                    if (!ts.typeEquals(currentClass, outerBase)
                            && !ts.isEnclosed(currentClass, outerBase)) {
                        // we are trying to create a "rare" class!
                        // That is, we are
                        // trying to instantiate a member class of
                        // a raw class.
                        // See JLS 3rd ed. 4.8
                        throw new SemanticException("\"Rare\" types are not allowed: cannot provide "
                                                            + "type arguments to member class "
                                                            + ct.name()
                                                            + " of raw class "
                                                            + ct.outer() + ".",
                                                    position);
                    }
                }
            }
        }
    }

    protected JL5ParsedClassType handleBase(
            Map<TypeVariable, ReferenceType> typeMap) {
        Type baseType = base.type();
        if (baseType instanceof JL5ParsedClassType)
            return (JL5ParsedClassType) baseType;
        if (baseType instanceof RawClass) return ((RawClass) baseType).base();
        if (baseType instanceof JL5SubstClassType) {
            JL5SubstClassType sct = (JL5SubstClassType) baseType;
            typeMap.putAll(sct.subst().substitutions());
            return sct.base();
        }
        throw new InternalCompilerError("Don't know how to handle " + baseType,
                                        position);
    }

    protected void checkParamSize(JL5ParsedClassType pct)
            throws SemanticException {
        int pctFormalSize = pct.pclass().formals().size();
        if (pctFormalSize != typeArguments.size()) {
            throw new SemanticException("Wrong number of type parameters for class "
                                                + pct,
                                        position);
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(base);
        sb.append("<");

        Iterator<TypeNode> iter = typeArguments.iterator();
        while (iter.hasNext()) {
            TypeNode tn = iter.next();
            sb.append(tn);

            if (iter.hasNext()) sb.append(", ");
        }
        sb.append(">");
        return sb.toString();
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        tr.lang().prettyPrint(base, w, tr);
        w.write("<");
        Iterator<TypeNode> iter = typeArguments.iterator();
        while (iter.hasNext()) {
            TypeNode tn = iter.next();
            tr.lang().prettyPrint(tn, w, tr);

            if (iter.hasNext()) {
                w.write(",");
                w.allowBreak(0, " ");
            }
        }
        w.write(">");
    }
}
