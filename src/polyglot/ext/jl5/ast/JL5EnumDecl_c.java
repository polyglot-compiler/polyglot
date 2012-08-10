package polyglot.ext.jl5.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassMember;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Formal;
import polyglot.ast.Id;
import polyglot.ast.IntLit;
import polyglot.ast.Lit;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.types.ConstructorInstance;
import polyglot.types.Flags;
import polyglot.types.MemberInstance;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeChecker;

public class JL5EnumDecl_c extends JL5ClassDecl_c implements JL5EnumDecl {

    public JL5EnumDecl_c(Position pos, Flags flags,
            List<AnnotationElem> annotations, Id name, TypeNode superClass,
            List<TypeNode> interfaces, ClassBody body) {
        super(pos, flags, annotations, name, superClass, interfaces, body);
    }

    @Override
    public NodeVisitor typeCheckEnter(TypeChecker tc) throws SemanticException {
        // figure out if this should be an abstract type.
        // need to do this before any anonymous subclasses are typechecked.
        for (MethodInstance mi : type().methods()) {
            if (!mi.flags().isAbstract()) continue;

            // mi is abstract! First, mark the class as abstract.
            type().setFlags(type().flags().Abstract());
        }
        return super.typeCheckEnter(tc);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        if (flags().isAbstract()) {
            throw new SemanticException("Enum types cannot have abstract modifier",
                                        this.position());
        }
        if (flags().isPrivate() && !type().isNested()) {
            throw new SemanticException("Top level enum types cannot have private modifier",
                                        this.position());
        }
        if (flags().isFinal()) {
            throw new SemanticException("Enum types cannot have final modifier",
                                        this.position());
        }

        for (ConstructorInstance ci : type().constructors()) {
            if (!ci.flags().clear(Flags.PRIVATE).equals(Flags.NONE)) {
                throw new SemanticException("Modifier "
                                                    + ci.flags()
                                                        .clear(Flags.PRIVATE)
                                                    + " not allowed here",
                                            ci.position());
            }
        }

        ClassDecl n = (ClassDecl) super.typeCheck(tc);
        if (n.type().isMember()) {
            // it's a nested class
            n = this.flags(this.flags().Static());
            n.type().flags(n.type().flags().Static());
        }

        for (ClassMember m : this.body().members()) {
            if (m.memberInstance().flags().isAbstract()) {
                n = this.flags(this.flags().Abstract());
                n.type().flags(n.type().flags().Abstract());
                break;
            }
        }

        return n;
    }

    @Override
    protected Node addDefaultConstructorIfNeeded(TypeSystem ts, NodeFactory nf)
            throws SemanticException {
        JL5EnumDecl_c n =
                (JL5EnumDecl_c) super.addDefaultConstructorIfNeeded(ts, nf);
        return n.addEnumMethodsIfNeeded(ts, nf);
    }

    @Override
    protected Node addDefaultConstructor(TypeSystem ts, NodeFactory nf)
            throws SemanticException {
        ConstructorInstance ci = this.defaultCI;
        if (ci == null) {
            throw new InternalCompilerError("addDefaultConstructor called without defaultCI set");
        }

        // insert call to appropriate super constructor
        List<Lit> args = new ArrayList<Lit>(2);
        args.add(nf.NullLit(Position.compilerGenerated()));// XXX the right thing to do is change the type of java.lang.Enum instead of adding these dummy params
        args.add(nf.IntLit(Position.compilerGenerated(), IntLit.INT, 0));
        Block block =
                nf.Block(position().startOf(),
                         ((JL5NodeFactory) nf).ConstructorCall(position.startOf(),
                                                               ConstructorCall.SUPER,
                                                               null,
                                                               args,
                                                               true));

        //Default constructor of an enum is private 
        ConstructorDecl cd =
                nf.ConstructorDecl(body().position().startOf(),
                                   Flags.PRIVATE,
                                   name,
                                   Collections.<Formal> emptyList(),
                                   Collections.<TypeNode> emptyList(),
                                   block);
        cd = cd.constructorInstance(ci);
        return body(body.addMember(cd));

    }

    private Node addEnumMethodsIfNeeded(TypeSystem ts, NodeFactory nf) {
        if (enumMethodsNeeded()) {
            return addEnumMethods(ts, nf);
        }
        return this;
    }

    private boolean enumMethodsNeeded() {
        boolean valueOfMethodFound = false;
        boolean valuesMethodFound = false;
        // We added it to the type, check if it's in the class body.
        for (MemberInstance mi : this.type.members()) {
            if (mi instanceof MethodInstance) {
                MethodInstance md = (MethodInstance) mi;
                if (md.name().equals("valueOf")) {
                    valueOfMethodFound = true;
                }
                if (md.name().equals("values")) {
                    valuesMethodFound = true;
                }
            }
        }

        return !(valueOfMethodFound && valuesMethodFound);

    }

    protected Node addEnumMethods(TypeSystem ts, NodeFactory nf) {
        Flags flags = Flags.PUBLIC.set(Flags.STATIC.set(Flags.FINAL));

        // add values method
        JL5MethodInstance valuesMI =
                (JL5MethodInstance) ts.methodInstance(position(),
                                                      this.type(),
                                                      flags,
                                                      ts.arrayOf(this.type()),
                                                      "values",
                                                      Collections.<Type> emptyList(),
                                                      Collections.<Type> emptyList());
        this.type.addMethod(valuesMI);

        // add valueOf method
        JL5MethodInstance valueOfMI =
                (JL5MethodInstance) ts.methodInstance(position(),
                                                      this.type(),
                                                      flags,
                                                      this.type(),
                                                      "valueOf",
                                                      Collections.singletonList((Type) ts.String()),
                                                      Collections.<Type> emptyList());
        this.type.addMethod(valueOfMI);

        return this;
    }

}
