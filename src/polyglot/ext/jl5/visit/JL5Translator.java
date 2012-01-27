package polyglot.ext.jl5.visit;

import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.JL5Options;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.frontend.Job;
import polyglot.frontend.TargetFactory;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.visit.Translator;

public class JL5Translator extends Translator {

    private final boolean translateEnums;

    public JL5Translator(Job job, TypeSystem ts, NodeFactory nf,
            TargetFactory tf) {
        super(job, ts, nf, tf);
        translateEnums = ((JL5Options)job.extensionInfo().getOptions()).enumImplClass == null;
    }

    public void translateNode(Node n, CodeWriter w) {
        if (n instanceof ClassDecl) {
            if (translateEnums) {
                ClassDecl cd = (ClassDecl) n;
                if (cd.superClass() != null && cd.superClass().type().isClass() && cd.superClass().type().toClass().fullName().equals("java.lang.Enum")) {
                    // The super class is Enum, so this is really an enum declaration.
                    RemoveEnums.prettyPrintClassDeclAsEnum(cd, w, this);
                    return;
                }
            }
        }
        if (n instanceof TypeNode) {
            // Don't print out the type variable, print out its superclass.
            TypeNode tn = (TypeNode) n;
            Type type = ((JL5TypeSystem)ts).erasureType(tn.type()); 
            w.write(type.translate(this.context()));
            return;
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
}
