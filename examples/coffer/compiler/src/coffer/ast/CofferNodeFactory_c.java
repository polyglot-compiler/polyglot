package polyglot.ext.coffer.ast;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.coffer.types.*;
import polyglot.ext.coffer.extension.*;
import polyglot.types.Flags;
import polyglot.types.Package;
import polyglot.types.Type;
import polyglot.types.Qualifier;
import polyglot.util.*;
import java.util.*;

/**
 * NodeFactory for coffer extension.
 */
public class CofferNodeFactory_c extends NodeFactory_c
                             implements CofferNodeFactory
{
    public Del defaultExt() { return new CofferDel_c(); }

    /*
    public CanonicalTypeNode CanonicalTypeNode(Position pos, Type t) {
        return new CanonicalTypeNode_c(new TypeNodeDel_c(), pos, t);
    }
    */

    public Assign Assign(Position pos, Expr left, Assign.Operator op, Expr right) {
        return (Assign) super.Assign(pos, left, op, right).del(new AssignDel_c());
    }

    public Local Local(Position pos, String name) {
        return (Local) super.Local(pos, name).del(new LocalDel_c());
    }

    public Special Special(Position pos, Special.Kind kind, TypeNode outer) {
        return (Special) super.Special(pos, kind, outer).del(new SpecialDel_c());
    }

    public LocalDecl LocalDecl(Position pos, Flags flags, TypeNode type, String name, Expr init) {
        return (LocalDecl) super.LocalDecl(pos, flags, type, name, init).del(new LocalDeclDel_c());
    }

    public Call Call(Position pos, Receiver target, String name, List args) {
        return (Call) super.Call(pos, target, name, args).del(new CallDel_c());
    }

    public New New(Position pos, Expr outer, TypeNode objectType, List args, ClassBody body) {
        return (New) super.New(pos, outer, objectType, args, body).del(new NewDel_c());
    }

    public New TrackedNew(Position pos, Expr outer, KeyNode key, TypeNode objectType, List args, ClassBody body) {
        return New(pos, outer, TrackedTypeNode(key.position(), key, objectType), args, body);
    }

    public ConstructorCall ConstructorCall(Position pos, ConstructorCall.Kind kind, Expr outer, List args) {
        return (ConstructorCall) super.ConstructorCall(pos, kind, outer, args).del(new ConstructorCallDel_c());
    }

    public Free Free(Position pos, Expr expr) {
        return new Free_c(new FreeDel_c(), pos, expr);
    }

    public TrackedTypeNode TrackedTypeNode(Position pos, KeyNode key, TypeNode base) {
        return new TrackedTypeNode_c(defaultExt(), pos, key, base);
    }

    public AmbKeySetNode AmbKeySetNode(Position pos, List keys) {
        return new AmbKeySetNode_c(defaultExt(), pos, keys);
    }

    public CanonicalKeySetNode CanonicalKeySetNode(Position pos, KeySet keys) {
        return new CanonicalKeySetNode_c(defaultExt(), pos, keys);
    }

    public KeyNode KeyNode(Position pos, Key key) {
        return new KeyNode_c(defaultExt(), pos, key);
    }

    public ClassDecl ClassDecl(Position pos, Flags flags, String name,
                               TypeNode superClass, List interfaces,
                               ClassBody body)
    {
        return CofferClassDecl(pos, flags, name, null,
                              superClass, interfaces, body);
    }

    public CofferClassDecl CofferClassDecl(Position pos, Flags flags,
                                         String name, KeyNode key,
                                         TypeNode superClass, List interfaces,
                                         ClassBody body)
    {
        return new CofferClassDecl_c(defaultExt(), pos, flags, name, key,
                                    superClass, interfaces, body);
    }

    public ThrowConstraintNode ThrowConstraintNode(Position pos, TypeNode tn, KeySetNode keys) {
        return new ThrowConstraintNode_c(defaultExt(), pos, tn, keys);
    }

    public MethodDecl MethodDecl(Position pos, Flags flags,
                                 TypeNode returnType, String name,
                                 List argTypes, List excTypes, Block body)
    {
        List l = new LinkedList();

        for (Iterator i = excTypes.iterator(); i.hasNext(); ) {
            TypeNode tn = (TypeNode) i.next();
            l.add(ThrowConstraintNode(tn.position(), tn, null));
        }

        return CofferMethodDecl(pos, flags, returnType, name, argTypes,
                               null, null, l, body);

    }

    public ConstructorDecl ConstructorDecl(Position pos, Flags flags,
                                           String name, List argTypes,
                                           List excTypes, Block body)
    {
        List l = new LinkedList();

        for (Iterator i = excTypes.iterator(); i.hasNext(); ) {
            TypeNode tn = (TypeNode) i.next();
            l.add(ThrowConstraintNode(tn.position(), tn, null));
        }

        return CofferConstructorDecl(pos, flags, name, argTypes,
                                     null, null, l, body);
    }

    public CofferMethodDecl CofferMethodDecl(Position pos, Flags flags,
                                              TypeNode returnType, String name,
                                              List argTypes,
                                              KeySetNode entryKeys,
                                              KeySetNode returnKeys,
                                              List throwConstraints,
                                              Block body)
    {
        return new CofferMethodDecl_c(new ProcedureDeclDel_c(), pos, flags, returnType, name, argTypes,
                                     entryKeys, returnKeys, throwConstraints, body);
    }

    public CofferConstructorDecl CofferConstructorDecl(Position pos,
                                                        Flags flags,
                                                        String name,
                                                        List argTypes,
                                                        KeySetNode entryKeys,
                                                        KeySetNode returnKeys,
                                                        List throwConstraints,
                                                        Block body)
    {
        return new CofferConstructorDecl_c(new ProcedureDeclDel_c(), pos, flags, name, argTypes,
                                          entryKeys, returnKeys, throwConstraints, body);
    }
}

