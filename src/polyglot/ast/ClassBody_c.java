package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;
import polyglot.frontend.*;
import java.util.*;

/**
 * A <code>ClassBody</code> represents the body of a class or interface
 * declaration or the body of an anonymous class.
 */
public class ClassBody_c extends Node_c implements ClassBody
{
    protected List members;

    public ClassBody_c(Del ext, Position pos, List members) {
        super(ext, pos);
        this.members = TypedList.copyAndCheck(members, ClassMember.class, true);
    }

    public List members() {
        return this.members;
    }

    public ClassBody members(List members) {
        ClassBody_c n = (ClassBody_c) copy();
        n.members = TypedList.copyAndCheck(members, ClassMember.class, true);
        return n;
    }

    public ClassBody addMember(ClassMember member) {
        ClassBody_c n = (ClassBody_c) copy();
        List l = new ArrayList(this.members.size() + 1);
        l.addAll(this.members);
        l.add(member);
        n.members = TypedList.copyAndCheck(l, ClassMember.class, true);
        return n;
    }

    protected ClassBody_c reconstruct(List members) {
        if (! CollectionUtil.equals(members, this.members)) {
            ClassBody_c n = (ClassBody_c) copy();
            n.members = TypedList.copyAndCheck(members,
                                               ClassMember.class, true);
            return n;
        }

        return this;
    }

    public Node visitChildren(NodeVisitor v) {
        List members = visitList(this.members, v);
        return reconstruct(members);
    }

    public Context enterScope(Context c) {
        return enterScope(c, true);
    }

    public Context enterScope(Context c, boolean inherit) {
        c = c.pushBlock();

        ClassType type = c.currentClass();

        addMembers(c, type, new HashSet(), inherit);

        return c;
    }

    protected void addMembers(Context c, ReferenceType type,
                              Set visited, boolean inherit) {
        Types.report(2, "addMembers(" + type + ")");

        if (visited.contains(type)) {
            return;
        }

        visited.add(type);

        if (inherit) {
            // Add supertype members first to ensure overrides work correctly.
            if (type.superType() != null) {
                if (! type.superType().isReference()) {
                    throw new InternalCompilerError(
                        "Super class \"" + type.superType() +
                        "\" of \"" + type + "\" is ambiguous.  " +
                        "An error must have occurred earlier.",
                        type.position());
                }

                addMembers(c, type.superType().toReference(), visited, true);
            }

            for (Iterator i = type.interfaces().iterator(); i.hasNext(); ) {
                Type t = (Type) i.next();

                if (! t.isReference()) {
                    throw new InternalCompilerError(
                        "Interface \"" + t + "\" of \"" + type +
                        "\" is ambiguous.  " +
                        "An error must have occurred earlier.",
                        type.position());
                }

                addMembers(c, t.toReference(), visited, true);
            }
        }

        for (Iterator i = type.methods().iterator(); i.hasNext(); ) {
            MethodInstance mi = (MethodInstance) i.next();
            c.addMethod(mi);
        }

        for (Iterator i = type.fields().iterator(); i.hasNext(); ) {
            FieldInstance fi = (FieldInstance) i.next();
            c.addVariable(fi);
        }

        if (type.isClass()) {
            for (Iterator i = type.toClass().memberClasses().iterator();
                 i.hasNext(); ) {
                MemberClassType mct = (MemberClassType) i.next();
                c.addType(mct);
            }
        }
    }

    public String toString() {
        return "{ ... }";
    }

    protected void duplicateFieldCheck(TypeChecker tc) throws SemanticException {
        ClassType type = tc.context().currentClass();
        TypeSystem ts = tc.typeSystem();

        ArrayList l = new ArrayList(type.fields());

        for (int i = 0; i < l.size(); i++) {
            FieldInstance fi = (FieldInstance) l.get(i);

            for (int j = i+1; j < l.size(); j++) {
                FieldInstance fj = (FieldInstance) l.get(j);

                if (fi.name().equals(fj.name())) {
                    throw new SemanticException("Duplicate field \"" + fj + "\".", fj.position());
                }
            }
        }
    }

    protected void duplicateConstructorCheck(TypeChecker tc) throws SemanticException {
        ClassType type = tc.context().currentClass();
        TypeSystem ts = tc.typeSystem();

        ArrayList l = new ArrayList(type.constructors());

        for (int i = 0; i < l.size(); i++) {
            ConstructorInstance ci = (ConstructorInstance) l.get(i);

            for (int j = i+1; j < l.size(); j++) {
                ConstructorInstance cj = (ConstructorInstance) l.get(j);

                if (ts.hasSameArguments(ci, cj)) {
                    throw new SemanticException("Duplicate constructor \"" + cj + "\".", cj.position());
                }
            }
        }
    }

    protected void duplicateMethodCheck(TypeChecker tc) throws SemanticException {
        ClassType type = tc.context().currentClass();
        TypeSystem ts = tc.typeSystem();

        ArrayList l = new ArrayList(type.methods());

        for (int i = 0; i < l.size(); i++) {
            MethodInstance mi = (MethodInstance) l.get(i);

            for (int j = i+1; j < l.size(); j++) {
                MethodInstance mj = (MethodInstance) l.get(j);

                if (isSameMethod(ts, mi, mj)) {
                    throw new SemanticException("Duplicate method \"" + mj + "\".", mj.position());
                }
            }
        }
    }

    protected boolean isSameMethod(TypeSystem ts, MethodInstance mi,
                                   MethodInstance mj) {
        return ts.isSameMethod(mi, mj);
    }

    protected void overrideMethodCheck(TypeChecker tc) throws SemanticException {
        ClassType type = tc.context().currentClass();
        TypeSystem ts = tc.typeSystem();

        for (Iterator i = type.methods().iterator(); i.hasNext(); ) {
            MethodInstance mi = (MethodInstance) i.next();

            Type t = type.superType();

            while (t instanceof ReferenceType) {
                ReferenceType rt = (ReferenceType) t;
                t = rt.superType();

                for (Iterator j = rt.methods().iterator(); j.hasNext(); ) {
                    MethodInstance mj = (MethodInstance) j.next();

                    if (! mi.name().equals(mj.name()) ||
                        ! ts.hasSameArguments(mi, mj) ||
                        ! ts.isAccessible(mj, tc.context())) {

                        continue;
                    }

                    if (! ts.isSame(mi.returnType(), mj.returnType())) {
                        throw new SemanticException("Cannot override " + mj + " in " + rt + " with " + mi + " in " + type + "; overridden method returns " + mi.returnType() + " not " + mj.returnType() + ".", mi.position());
                    }

                    if (! ts.throwsSubset(mi, mj)) {
                        throw new SemanticException("Cannot override " + mj + " in " + rt + " with " + mi + " in " + type + "; throws more exceptions than overridden method.", mi.position());
                    }

                    if (mi.flags().moreRestrictiveThan(mj.flags())) {
                        throw new SemanticException("Cannot override " + mj + " in " + rt + " with " + mi + " in " + type + "; overridden method is more restrictive.", mi.position());
                    }

                    if (! mi.flags().isStatic() && mj.flags().isStatic()) {
                        throw new SemanticException("Cannot override " + mj + " in " + rt + " with " + mi + " in " + type + "; overridden method is static.", mi.position());
                    }

                    if (mj.flags().isFinal()) {
                        throw new SemanticException("Cannot override " + mj + " in " + rt + " with " + mi + " in " + type + "; overridden method is final.", mi.position());
                    }
                }
            }
        }
    }

    protected void abstractMethodCheck(TypeChecker tc) throws SemanticException {
        ClassType type = tc.context().currentClass();
        TypeSystem ts = tc.typeSystem();

        // FIXME: check that we implement methods of interfaces and abstract
        // superclasses.
        if (type.flags().isAbstract() || type.flags().isInterface()) {
            return;
        }

        // Check for abstract methods.
        for (Iterator i = type.methods().iterator(); i.hasNext(); ) {
            MethodInstance mi = (MethodInstance) i.next();

            if (mi.flags().isAbstract()) {
                // Clear all flags for the error message.
                MethodInstance x = mi.flags(mi.flags().clear());
                throw new SemanticException("Class \"" + type +
                                            "\" should be declared abstract; " +
                                            "it does not implement " + x + ".",
                                            type.position());
            }
        }
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException {
        duplicateFieldCheck(tc);
        duplicateConstructorCheck(tc);
        duplicateMethodCheck(tc);
        overrideMethodCheck(tc);
        abstractMethodCheck(tc);

        return this;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (members.isEmpty()) {
            w.write("{ }");
        } else {
            w.write("{");
            w.newline(4);
            w.begin(0);

            for (Iterator i = members.iterator(); i.hasNext(); ) {
                ClassMember member = (ClassMember) i.next();
                printBlock(member, w, tr);
                if (i.hasNext()) {
                    w.newline(0);
                    w.newline(0);
                }
            }

            w.end();
            w.newline(0);
            w.write("}");
        }
    }
}
