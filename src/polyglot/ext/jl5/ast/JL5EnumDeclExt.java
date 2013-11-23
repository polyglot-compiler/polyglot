package polyglot.ext.jl5.ast;

import java.util.Collections;

import polyglot.ast.ClassDecl;
import polyglot.ast.ClassDecl_c;
import polyglot.ast.ClassMember;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Formal;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Node_c;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5Flags;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.ConstructorInstance;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

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

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        ClassDecl n = (ClassDecl) super.buildTypes(tb);
        JL5EnumDeclExt ext = (JL5EnumDeclExt) JL5Ext.ext(n);

        if (n.type().isMember()) {
            // it's a nested class
            n = n.flags(n.flags().Static());
            n.type().flags(n.type().flags().Static());
        }

        try {
            JL5TypeSystem ts = (JL5TypeSystem) tb.typeSystem();
            return ext.addEnumMethodTypesIfNeeded(ts);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public NodeVisitor typeCheckEnter(TypeChecker tc) throws SemanticException {
        ClassDecl n = (ClassDecl) this.node();
        // figure out if this should be an abstract type.
        // need to do this before any anonymous subclasses are typechecked.
        for (MethodInstance mi : n.type().methods()) {
            if (!mi.flags().isAbstract()) continue;

            // mi is abstract! First, mark the class as abstract.
            n.type().setFlags(n.type().flags().Abstract());
        }
        return this.superDel().typeCheckEnter(tc);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        ClassDecl n = (ClassDecl) this.node();
        if (n.flags().isAbstract()) {
            throw new SemanticException("Enum types cannot have abstract modifier",
                                        n.position());
        }
        if (n.flags().isPrivate() && !n.type().isNested()) {
            throw new SemanticException("Top level enum types cannot have private modifier",
                                        n.position());
        }
        if (n.flags().isFinal()) {
            throw new SemanticException("Enum types cannot have final modifier",
                                        n.position());
        }

        for (ConstructorInstance ci : n.type().constructors()) {
            if (!JL5Flags.clearVarArgs(ci.flags().clear(Flags.PRIVATE))
                         .equals(Flags.NONE)) {
                throw new SemanticException("Modifier "
                                                    + ci.flags()
                                                        .clear(Flags.PRIVATE)
                                                    + " not allowed here",
                                            ci.position());
            }
        }

        // set the supertype appropraitely
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        if (ts.rawClass((JL5ParsedClassType) ts.Enum()).equals(n.type()
                                                                .superType())) {
            // the super class is currently a raw Enum.
            // instantiate Enum to on this type.
            n.type()
             .superType(ts.instantiate(n.position(),
                                       (JL5ParsedClassType) ts.Enum(),
                                       Collections.singletonList(n.type())));
        }

        n = (ClassDecl) super.typeCheck(tc);
        if (n.type().isMember()) {
            // it's a nested class
            n = n.flags(n.flags().Static());
            n.type().flags(n.type().flags().Static());
        }

        for (ClassMember m : n.body().members()) {
            if (m.memberInstance().flags().isAbstract()
                    && m instanceof MethodDecl) {
                n.type().flags(n.type().flags().Abstract());
                break;
            }
        }

        return n;
    }

    public Node addDefaultConstructor(TypeSystem ts, NodeFactory nf,
            ConstructorInstance defaultCI) throws SemanticException {
        ClassDecl n = (ClassDecl) this.node();
        ConstructorInstance ci = defaultCI;
        if (ci == null) {
            throw new InternalCompilerError("addDefaultConstructor called without defaultCI set");
        }

        //Default constructor of an enum is private 
        ConstructorDecl cd =
                nf.ConstructorDecl(n.body().position().startOf(),
                                   Flags.PRIVATE,
                                   n.name(),
                                   Collections.<Formal> emptyList(),
                                   Collections.<TypeNode> emptyList(),
                                   nf.Block(n.position().startOf()));
        cd = cd.constructorInstance(ci);
        return n.body(n.body().addMember(cd));

    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        ClassDecl n = (ClassDecl) this.node();
        prettyPrintHeader(w, tr);

        boolean hasEnumConstant = false;
        for (ClassMember m : n.body().members()) {
            if (m instanceof EnumConstantDecl) {
                hasEnumConstant = true;
                break;
            }
        }

        if (!hasEnumConstant) w.write(";");
        ((Node_c) n).print(n.body(), w, tr);
        ((ClassDecl_c) n).prettyPrintFooter(w, tr);
    }

}
