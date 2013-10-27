package polyglot.ext.jl5.ast;

import java.util.Collections;

import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.types.Flags;
import polyglot.types.Type;
import polyglot.types.TypeSystem;

public class JL5EnumDeclExt extends JL5ClassDeclExt {

    public ClassDecl addValueOfMethodType(TypeSystem ts) {
        ClassDecl n = (ClassDecl) this.node();
        Flags flags = Flags.PUBLIC.set(Flags.STATIC.set(Flags.FINAL));

        // add valueOf method
        JL5MethodInstance valueOfMI =
                (JL5MethodInstance) ts.methodInstance(n.position(),
                                                      n.type(),
                                                      flags,
                                                      n.type(),
                                                      "valueOf",
                                                      Collections.singletonList((Type) ts.String()),
                                                      Collections.<Type> emptyList());
        n.type().addMethod(valueOfMI);

        return n;
    }

    public ClassDecl addValuesMethodType(TypeSystem ts) {
        ClassDecl n = (ClassDecl) this.node();
        Flags flags = Flags.PUBLIC.set(Flags.STATIC.set(Flags.FINAL));

        // add values method
        JL5MethodInstance valuesMI =
                (JL5MethodInstance) ts.methodInstance(n.position(),
                                                      n.type(),
                                                      flags.set(Flags.NATIVE),
                                                      ts.arrayOf(n.type()),
                                                      "values",
                                                      Collections.<Type> emptyList(),
                                                      Collections.<Type> emptyList());
        n.type().addMethod(valuesMI);

        return n;
    }

    public Node addEnumMethodTypesIfNeeded(TypeSystem ts) {
        ClassDecl n = (ClassDecl) this.node();
        JL5EnumDeclExt ext = (JL5EnumDeclExt) JL5Ext.ext(n);

        JL5ParsedClassType ct = (JL5ParsedClassType) n.type();
        if (ct.enumValueOfMethodNeeded()) {
            n = ext.addValueOfMethodType(ts);
        }
        if (ct.enumValuesMethodNeeded()) {
            n = ext.addValuesMethodType(ts);
        }
        return n;
    }

}
