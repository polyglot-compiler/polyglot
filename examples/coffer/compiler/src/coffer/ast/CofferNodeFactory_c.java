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

/** An implementation of the <code>CofferNodeFactory</code> interface. 
 */
public class CofferNodeFactory_c extends ExtNodeFactory_c implements CofferNodeFactory
{
    public CofferNodeFactory_c() {
        super(new NodeFactory_c());
    }

    public Node extNode(Node n) {
        return n.ext(new CofferExt_c());
    }

    public Assign extAssign(Assign n) {
        return (Assign) super.extAssign(n).del(new AssignDel_c()).ext(new AssignExt_c());
    }

    public Local extLocal(Local n) {
        return (Local) super.extLocal(n).ext(new LocalExt_c());
    }

    public Special extSpecial(Special n) {
        return (Special) super.extSpecial(n).ext(new SpecialExt_c());
    }

    public LocalDecl extLocalDecl(LocalDecl n) {
        return (LocalDecl) super.extLocalDecl(n).ext(new LocalDeclExt_c());
    }

    public ConstructorCall extConstructorCall(ConstructorCall n) {
        return (ConstructorCall) extProcedureCall(super.extConstructorCall(n));
    }

    public Call extCall(Call n) {
        return (Call) extProcedureCall(super.extCall(n));
    }

    public ProcedureCall extProcedureCall(ProcedureCall n) {
        return (ProcedureCall) n.ext(new ProcedureCallExt_c());
    }

    public New extNew(New n) {
        return (New) super.extNew(n).ext(new NewExt_c());
    }

    public New TrackedNew(Position pos, Expr outer, KeyNode key, TypeNode objectType, List args, ClassBody body) {
        return New(pos, outer, TrackedTypeNode(key.position(), key, objectType), args, body);
    }

    public Free extFree(Free n) {
        return (Free) extStmt(n).ext(new FreeExt_c());
    }

    public Free Free(Position pos, Expr expr) {
        return extFree(new Free_c(null, null, pos, expr));
    }

    public TrackedTypeNode extTrackedTypeNode(TrackedTypeNode n) {
        return (TrackedTypeNode) extTypeNode(n);
    }

    public TrackedTypeNode TrackedTypeNode(Position pos, KeyNode key, TypeNode base) {
        return extTrackedTypeNode(new TrackedTypeNode_c(null, null, pos, key, base));
    }

    public AmbKeySetNode extAmbKeySetNode(AmbKeySetNode n) {
        return (AmbKeySetNode) extNode(n);
    }

    public AmbKeySetNode AmbKeySetNode(Position pos, List keys) {
        return extAmbKeySetNode(new AmbKeySetNode_c(null, null, pos, keys));
    }

    public CanonicalKeySetNode extCanonicalKeySetNode(CanonicalKeySetNode n) {
        return (CanonicalKeySetNode) extNode(n);
    }

    public CanonicalKeySetNode CanonicalKeySetNode(Position pos, KeySet keys) {
        return extCanonicalKeySetNode(new CanonicalKeySetNode_c(null, null, pos, keys));
    }

    public KeyNode extKeyNode(KeyNode n) {
        return (KeyNode) extNode(n);
    }

    public KeyNode KeyNode(Position pos, Key key) {
        return extKeyNode(new KeyNode_c(null, null, pos, key));
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
        return (CofferClassDecl) extClassDecl(new CofferClassDecl_c(null, null, pos, flags, name, key,
                                    superClass, interfaces, body));
    }

    public ThrowConstraintNode extThrowConstraintNode(ThrowConstraintNode n) {
        return (ThrowConstraintNode) extNode(n);
    }

    public ThrowConstraintNode ThrowConstraintNode(Position pos, TypeNode tn, KeySetNode keys) {
        return extThrowConstraintNode(new ThrowConstraintNode_c(null, null, pos, tn, keys));
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

    public ProcedureDecl extProcedureDecl(ProcedureDecl n) {
        return (ProcedureDecl) n.ext(new ProcedureDeclExt_c());
    }

    public CofferMethodDecl CofferMethodDecl(Position pos, Flags flags,
                                              TypeNode returnType, String name,
                                              List argTypes,
                                              KeySetNode entryKeys,
                                              KeySetNode returnKeys,
                                              List throwConstraints,
                                              Block body)
    {
        return (CofferMethodDecl) extMethodDecl(new CofferMethodDecl_c(null, null, pos, flags, returnType, name, argTypes,
                                     entryKeys, returnKeys, throwConstraints, body));
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
        return (CofferConstructorDecl) extConstructorDecl(new CofferConstructorDecl_c(null, null, pos, flags, name, argTypes,
                                          entryKeys, returnKeys, throwConstraints, body));
    }
}
