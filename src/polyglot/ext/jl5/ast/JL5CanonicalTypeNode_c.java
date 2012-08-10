package polyglot.ext.jl5.ast;

import java.util.LinkedHashSet;
import java.util.Set;

import polyglot.ast.Node;
import polyglot.ext.jl5.types.IntersectionType;
import polyglot.ext.jl5.types.JL5Context;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.RawClass;
import polyglot.ext.jl5.types.TypeVariable;
import polyglot.ext.jl5.types.WildCardType;
import polyglot.types.ArrayType;
import polyglot.types.ClassType;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;

public class JL5CanonicalTypeNode_c extends polyglot.ast.CanonicalTypeNode_c {

    public JL5CanonicalTypeNode_c(Position pos, Type type) {
        super(pos, makeRawIfNeeded(type, pos));
    }

    private static Type makeRawIfNeeded(Type type, Position pos) {
        if (type.isClass()) {
            JL5TypeSystem ts = (JL5TypeSystem) type.typeSystem();
            if (type instanceof JL5ParsedClassType
                    && !((JL5ParsedClassType) type).typeVariables().isEmpty()) {
                // needs to be a raw type
                return ts.rawClass((JL5ParsedClassType) type, pos);
            }
            if (type.toClass().isInnerClass()) {
                ClassType t = type.toClass();
                ClassType outer = type.toClass().outer();
                while (t.isInnerClass() && outer != null) {
                    if (outer instanceof RawClass) {
                        // an inner class of a raw class should be a raw class.
                        return ts.erasureType(type);
                    }
                    t = outer;
                    outer = outer.outer();

                }
            }
        }
        return type;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Type t = this.type();
        if (t instanceof JL5SubstClassType) {
            JL5SubstClassType st = (JL5SubstClassType) t;
            JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();

            // Check for rare types: e.g., Outer<String>.Inner, where Inner has uninstantiated type variables
            // See JLS 3rd ed. 4.8
            if (st.isInnerClass() && !st.base().typeVariables().isEmpty()) {
                // st is an inner class, with type variables. Make sure that
                // these type variables are acutally instantiated
                for (TypeVariable tv : st.base().typeVariables()) {
                    if (!st.subst().substitutions().keySet().contains(tv)) {
                        throw new SemanticException("\"Rare\" types are not allowed: cannot "
                                                            + "use raw class "
                                                            + st.name()
                                                            + " when the outer class "
                                                            + st.outer()
                                                            + " has instantiated type variables.",
                                                    position);
                    }
                }

            }
            if (!st.base().typeVariables().isEmpty()) {
                // check that arguments obey their bounds.
                //first we must perform capture conversion. see beginning of JLS 4.5            
                JL5SubstClassType capCT =
                        (JL5SubstClassType) ts.applyCaptureConversion(st);

                for (int i = 0; i < capCT.actuals().size(); i++) {
                    TypeVariable ai = capCT.base().typeVariables().get(i);
                    Type xi = capCT.actuals().get(i);

                    //require that arguments obey their bounds
                    if (!ts.isSubtype(xi,
                                      capCT.subst().substType(ai.upperBound()))) {
                        throw new SemanticException("Type argument "
                                + st.actuals().get(i)
                                + " is not a subtype of its declared bound "
                                + ai.upperBound(), position());
                    }
                }
            }
        }

        // check for uses of type variables in static contexts
        if (tc.context().inStaticContext()
                && !((JL5Context) tc.context()).inCTORCall()) {
            for (TypeVariable tv : findInstanceTypeVariables(t)) {
                throw new SemanticException("Type variable " + tv
                        + " cannot be used in a static context", this.position);

            }
        }
        ClassType currentClass = tc.context().currentClass();
        JL5Context jc = (JL5Context) tc.context();
        if (jc.inExtendsClause()) {
            currentClass = jc.extendsClauseDeclaringClass();
        }
        if (currentClass != null) {
            if (currentClass.isNested() && !currentClass.isInnerClass()) {
                // the current class is static.
                for (TypeVariable tv : findInstanceTypeVariables(t)) {
                    if (!tv.declaringClass().equals(currentClass)) {
                        throw new SemanticException("Type variable "
                                                            + tv
                                                            + " of class "
                                                            + tv.declaringClass()
                                                            + " cannot be used in a nested class",
                                                    this.position);
                    }
                }
            }

        }

        return super.typeCheck(tc);
    }

    private Set<TypeVariable> findInstanceTypeVariables(Type t) {
        Set<TypeVariable> s = new LinkedHashSet<TypeVariable>();
        findInstanceTypeVariables(t, s);
        return s;
    }

    private void findInstanceTypeVariables(Type t, Set<TypeVariable> tvs) {
        if (t instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) t;
            if (tv.declaredIn() == TypeVariable.TVarDecl.CLASS_TYPE_VARIABLE) {
                tvs.add(tv);
            }
        }
        if (t instanceof ArrayType) {
            ArrayType at = (ArrayType) t;
            findInstanceTypeVariables(at.base(), tvs);
        }
        if (t instanceof WildCardType) {
            WildCardType at = (WildCardType) t;
            findInstanceTypeVariables(at.upperBound(), tvs);
        }
        if (t instanceof JL5SubstClassType) {
            JL5SubstClassType ct = (JL5SubstClassType) t;
            for (ReferenceType at : ct.actuals()) {
                findInstanceTypeVariables(at, tvs);
            }
        }
        if (t instanceof IntersectionType) {
            IntersectionType it = (IntersectionType) t;
            for (Type at : it.bounds()) {
                findInstanceTypeVariables(at, tvs);
            }
        }
    }

}
