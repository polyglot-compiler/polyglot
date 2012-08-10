package polyglot.ext.jl5.visit;

import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Receiver;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.JL5Options;
import polyglot.ext.jl5.types.JL5ClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.frontend.Job;
import polyglot.frontend.TargetFactory;
import polyglot.types.ArrayType;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.visit.Translator;

public class JL5Translator extends Translator {

    private final boolean translateEnums;

    private final boolean removeJava5isms;

    public JL5Translator(Job job, TypeSystem ts, NodeFactory nf,
            TargetFactory tf) {
        super(job, ts, nf, tf);
        translateEnums =
                ((JL5Options) job.extensionInfo().getOptions()).translateEnums;
        removeJava5isms =
                ((JL5Options) job.extensionInfo().getOptions()).removeJava5isms;
    }

    public boolean removeJava5isms() {
        return this.removeJava5isms;
    }

    public void translateNode(Node n, CodeWriter w) {
        if (n instanceof ClassDecl) {
            if (removeJava5isms && translateEnums) {
                ClassDecl cd = (ClassDecl) n;
                if (cd.superClass() != null
                        && cd.superClass().type().isClass()
                        && cd.superClass()
                             .type()
                             .toClass()
                             .fullName()
                             .equals("java.lang.Enum")) {
                    // The super class is Enum, so this is really an enum declaration.
                    RemoveEnums.prettyPrintClassDeclAsEnum(cd, w, this);
                    return;
                }
            }
        }
        if (n instanceof TypeNode) {
            TypeNode tn = (TypeNode) n;
            if (removeJava5isms) {
                // Print out the erasure type
                Type t = tn.type();
                Type erastype = ((JL5TypeSystem) ts).erasureType(t);
                w.write(translateType(erastype));
                return;
            }
            else {
                w.write(tn.type().translate(this.context()));
                return;
            }
        }
//        if (n instanceof TypeNode && ((TypeNode)n).type() instanceof TypeVariable) {
//            // Don't print out the type variable, print out its superclass.
//            TypeNode tn = (TypeNode) n;
//            TypeVariable tv = (TypeVariable) tn.type();
//            translateNode(tn.type(tv.erasureType()), w);
//            return;
//        }

        n.del().prettyPrint(w, this);
    }

    private String translateType(Type t) {
        if (t instanceof JL5SubstClassType) {
            // For C<T1,...,Tn>, just print C.
            JL5SubstClassType jct = (JL5SubstClassType) t;
            return jct.base().translate(this.context);
        }
        else if (t instanceof ArrayType) {
            ArrayType at = (ArrayType) t;
            return translateType(at.base()) + "[]";
        }
        else {
            return t.translate(this.context());
        }
    }

    public void printReceiver(Receiver target, CodeWriter w) {
        if (target == null) {
            return;
        }
        if (target instanceof TypeNode) {
            Type t = ((TypeNode) target).type();
            if (t instanceof JL5ClassType) {
                JL5ClassType ct = (JL5ClassType) t;
                ct.translateAsReceiver(this.context());
            }
        }
        this.translateNode(target, w);
    }
}
