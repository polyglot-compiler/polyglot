package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.*;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.visit.JL5Translator;
import polyglot.types.ClassType;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class JL5New_c extends New_c implements JL5New {

    private List<TypeNode> typeArgs;

    public JL5New_c(Position pos, Expr outer, List<TypeNode> typeArgs,
            TypeNode objectType, List argTypes, ClassBody body) {
        super(pos, outer, objectType, argTypes, body);
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
        JL5New_c n = (JL5New_c)this.copy();
        n.typeArgs = typeArgs;
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        JL5New_c n = (JL5New_c)super.visitChildren(v);
        List targs = visitList(n.typeArgs, v);
        return n.typeArgs(targs);
    }
    
    @Override
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar) throws SemanticException {
        JL5New n = (JL5New) super.disambiguateOverride(parent, ar);
        // now do the type args
        n = (JL5New) n.typeArgs(n.visitList(n.typeArgs(), ar));
        return n;
    }
    
    @Override
    protected New findQualifier(AmbiguityRemover ar, ClassType ct) throws SemanticException {
        // If we're instantiating a non-static member class, add a "this"
        // qualifier.
        NodeFactory nf = ar.nodeFactory();
        JL5TypeSystem ts = (JL5TypeSystem)ar.typeSystem();
        Context c = ar.context();
        
        // Search for the outer class of the member.  The outer class is
        // not just ct.outer(); it may be a subclass of ct.outer().
        Type outer = null;
        
        String name = ct.name();
        ClassType t = c.currentClass();
        
        // We're in one scope too many.
        if (t == anonType) {
            t = t.outer();
        }
        
        while (t != null) {
            try {
                //t = ts.staticTarget(t).toClass();
                ClassType mt = ts.findMemberClass(t, name, c.currentClass());
                if (ct instanceof JL5SubstClassType) {
                    ct = ((JL5SubstClassType)ct).base();
                }
                
                if (ts.equals(ts.toRawType(mt), ts.toRawType(ct))) {
                    outer = t;
                    break;
                }
            }
            catch (SemanticException e) {
            }
            
            t = t.outer();
        }
        
        if (outer == null) {
            throw new SemanticException("Could not find non-static member class \"" +
                                        name + "\".", position());
        }
        
        // Create the qualifier.
        Expr q;

        if (outer.equals(c.currentClass())) {
            q = nf.This(position().startOf());
        }
        else {
            q = nf.This(position().startOf(),
                        nf.CanonicalTypeNode(position(),
                                             outer));
        }
        
        q = q.type(outer);
        return qualifier(q);
    }


    
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        
        if (!tn.type().isClass()) {
            throw new SemanticException("Must have a class for a new expression.", this.position());
        }

        List argTypes = new ArrayList(arguments.size());
        
        for (Iterator i = this.arguments.iterator(); i.hasNext(); ) {
            Expr e = (Expr) i.next();
            argTypes.add(e.type());
        }
        
        List<Type> actualTypeArgs = new ArrayList(typeArgs.size());
        for (Iterator i = this.typeArgs.iterator(); i.hasNext(); ) {
            TypeNode tn = (TypeNode) i.next();
            actualTypeArgs.add(tn.type());
        }

        typeCheckFlags(tc);
        typeCheckNested(tc);
        
        if (this.body != null) {
            ts.checkClassConformance(anonType);
        }
        
        ClassType ct = tn.type().toClass();
        
        if (! ct.flags().isInterface()) {
            Context c = tc.context();
            if (anonType != null) {
                c = c.pushClass(anonType, anonType);
            }
            ci = ts.findConstructor(ct, argTypes, actualTypeArgs, c.currentClass());
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
        if (qualifier != null) {
            ClassType ct = tn.type().toClass();
            w.write(ct.name());
            if (ct instanceof JL5SubstClassType) {
                boolean printParams = true;
                if (tr instanceof JL5Translator) {
                    JL5Translator jtr = (JL5Translator)tr;
                    printParams = !jtr.removeJava5isms();
                }
                if (printParams) {
                    JL5SubstClassType jsct = (JL5SubstClassType)ct;
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

}
