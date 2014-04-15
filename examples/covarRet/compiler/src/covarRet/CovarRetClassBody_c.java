package covarRet;

import java.util.List;

import polyglot.ast.ClassBody_c;
import polyglot.ast.ClassMember;
import polyglot.types.ClassType;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;

/**
 * A <code>ClassBody</code> represents the body of a class or interface
 * declaration or the body of an anonymous class.
 *
 * The covariant return extension overrides the overrideMethodCheck method
 * to allow the return type of a method to be a subclass of the return type
 * declared in a superclass.
 */
public class CovarRetClassBody_c extends ClassBody_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public CovarRetClassBody_c(Position pos, List<ClassMember> members) {
        super(pos, members);
    }

    protected void overrideMethodCheck(TypeChecker tc) throws SemanticException {
        ClassType type = tc.context().currentClass();
        TypeSystem ts = tc.typeSystem();

        for (MethodInstance mi : type.methods()) {
            Type t = type.superType();

            while (t instanceof ReferenceType) {
                ReferenceType rt = (ReferenceType) t;
                t = rt.superType();

                for (MethodInstance mj : rt.methods()) {
                    if (!mi.name().equals(mj.name())
                            || !mi.hasFormals(mj.formalTypes()) /*(qixin) ts.hasSameArguments(mi, mj) */
                            || !ts.isAccessible(mj, tc.context())) {

                        continue;
                    }

                    // This condition is the only change from the superclass!
                    if (!ts.isSubtype(mi.returnType(), mj.returnType())) {
                        throw new SemanticException("Cannot override " + mj
                                + " in " + rt + " with " + mi + " in " + type
                                + "; overridden method returns "
                                + mi.returnType()
                                + ", which is not a subtype of "
                                + mj.returnType() + ".", mi.position());
                    }

                    if (!ts.throwsSubset(mi, mj)) {
                        throw new SemanticException("Cannot override "
                                                            + mj
                                                            + " in "
                                                            + rt
                                                            + " with "
                                                            + mi
                                                            + " in "
                                                            + type
                                                            + "; throws more exceptions than overridden method.",
                                                    mi.position());
                    }

                    if (mi.flags().moreRestrictiveThan(mj.flags())) {
                        throw new SemanticException("Cannot override "
                                                            + mj
                                                            + " in "
                                                            + rt
                                                            + " with "
                                                            + mi
                                                            + " in "
                                                            + type
                                                            + "; overridden method is more restrictive.",
                                                    mi.position());
                    }

                    if (!mi.flags().isStatic() && mj.flags().isStatic()) {
                        throw new SemanticException("Cannot override "
                                                            + mj
                                                            + " in "
                                                            + rt
                                                            + " with "
                                                            + mi
                                                            + " in "
                                                            + type
                                                            + "; overridden method is static.",
                                                    mi.position());
                    }

                    if (mj.flags().isFinal()) {
                        throw new SemanticException("Cannot override "
                                                            + mj
                                                            + " in "
                                                            + rt
                                                            + " with "
                                                            + mi
                                                            + " in "
                                                            + type
                                                            + "; overridden method is final.",
                                                    mi.position());
                    }
                }
            }
        }
    }
}
