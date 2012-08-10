package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.ClassBody;
import polyglot.ast.Expr;
import polyglot.ast.New;
import polyglot.ast.New_c;
import polyglot.ast.Node;
import polyglot.ast.Special;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.types.ClassType;
import polyglot.types.Context;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5New_c extends New_c implements JL5New {

    private List<TypeNode> typeArgs;

    public JL5New_c(Position pos, Expr outer, List<TypeNode> typeArgs,
            TypeNode objectType, List<Expr> args, ClassBody body) {
        super(pos, outer, objectType, args, body);
        this.typeArgs = typeArgs;
    }

    @Override
    public List<TypeNode> typeArgs() {
        return this.typeArgs;
    }

    @Override
    public JL5New typeArgs(List<TypeNode> typeArgs) {
        if (this.typeArgs == typeArgs) {
            return this;
        }
        JL5New_c n = (JL5New_c) this.copy();
        n.typeArgs = typeArgs;
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        JL5New_c n = (JL5New_c) super.visitChildren(v);
        List<TypeNode> targs = visitList(n.typeArgs, v);
        return n.typeArgs(targs);
    }

    @Override
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
            throws SemanticException {
        JL5New n = (JL5New) super.disambiguateOverride(parent, ar);
        // now do the type args
        return n.typeArgs(n.visitList(n.typeArgs(), ar));
    }

    @Override
    protected New findQualifier(AmbiguityRemover ar, ClassType ct)
            throws SemanticException {
        // Call super.findQualifier in order to perform its checks, but throw away the
        // qualifier that it finds. That is, just return this. Do not attempt to infer 
        // a qualifier if one is missing.
        super.findQualifier(ar, ct);
        return this;
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();

        if (!tn.type().isClass()) {
            throw new SemanticException("Must have a class for a new expression.",
                                        this.position());
        }

        List<Type> argTypes = new ArrayList<Type>(arguments.size());

        for (Expr e : this.arguments) {
            argTypes.add(e.type());
        }

        List<ReferenceType> actualTypeArgs =
                new ArrayList<ReferenceType>(typeArgs.size());
        for (TypeNode tn : this.typeArgs) {
            actualTypeArgs.add((ReferenceType) tn.type());
        }

        typeCheckFlags(tc);
        typeCheckNested(tc);

        if (this.body != null) {
            ts.checkClassConformance(anonType);
        }

        ClassType ct = tn.type().toClass();

        if (ct.isInnerClass()) {
            ClassType outer = ct.outer();
            JL5TypeSystem ts5 = (JL5TypeSystem) tc.typeSystem();
            if (outer instanceof JL5SubstClassType) {
                JL5SubstClassType sct = (JL5SubstClassType) outer;
                ct = (ClassType) sct.subst().substType(ct);
            }
            else if (qualifier == null
                    || (qualifier instanceof Special && ((Special) qualifier).kind() == Special.THIS)) {
                ct = ts5.instantiateInnerClassFromContext(tc.context(), ct);
            }
            else if (qualifier.type() instanceof JL5SubstClassType) {
                JL5SubstClassType sct = (JL5SubstClassType) qualifier().type();
                ct = (ClassType) sct.subst().substType(ct);
            }
        }

        if (!ct.flags().isInterface()) {
            Context c = tc.context();
            if (anonType != null) {
                c = c.pushClass(anonType, anonType);
            }
            ci =
                    ts.findConstructor(ct,
                                       argTypes,
                                       actualTypeArgs,
                                       c.currentClass());
        }
        else {
            ci = ts.defaultConstructor(this.position(), ct);
        }

        New n = this.constructorInstance(ci);

        if (anonType != null) {
            // The type of the new expression is the anonymous type, not the base type.
            ct = anonType;
        }

        return n.type(ct);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        printQualifier(w, tr);
        w.write("new ");

        // We need to be careful when pretty printing "new" expressions for
        // member classes.  For the expression "e.new C()" where "e" has
        // static type "T", the TypeNode for "C" is actually the type "T.C".
        // But, if we print "T.C", the post compiler will try to lookup "T"
        // in "T".  Instead, we print just "C".
        if (tn.type().isClass() && tn.type().toClass().isInnerClass()) {
            ClassType ct = tn.type().toClass();
            w.write(ct.name());
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
            print(tn, w, tr);
        }

        printArgs(w, tr);
        printBody(w, tr);
    }

    @Override
    protected ClassType findEnclosingClass(Context c, ClassType ct) {
        if (ct == anonType) {
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
        if (t.superType() != null) {
            Type sup = t.superType();
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
