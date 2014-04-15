package polyglot.ext.jl5;

import java.util.List;

import polyglot.frontend.ExtensionInfo;
import polyglot.translate.JLOutputExtensionInfo;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.MethodInstance_c;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.TypeSystem_c;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public class CovarRetOutputExtensionInfo extends JLOutputExtensionInfo {

    public CovarRetOutputExtensionInfo(ExtensionInfo parent) {
        super(parent);
    }

    @Override
    public TypeSystem createTypeSystem() {
        return new CovarRetTypeSystem_c();
    }

    static protected class CovarRetTypeSystem_c extends TypeSystem_c {
        @Override
        public MethodInstance methodInstance(Position pos,
                ReferenceType container, Flags flags, Type returnType,
                String name, List<? extends Type> formals,
                List<? extends Type> throwTypes) {
            return new CovarRetMethodInstance_c(this,
                                                pos,
                                                container,
                                                flags,
                                                returnType,
                                                name,
                                                formals,
                                                throwTypes);
        }
    }

    static protected class CovarRetMethodInstance_c extends MethodInstance_c {

        private static final long serialVersionUID =
                SerialVersionUID.generate();

        public CovarRetMethodInstance_c(TypeSystem ts, Position pos,
                ReferenceType container, Flags flags, Type returnType,
                java.lang.String name, List<? extends Type> formals,
                List<? extends Type> throwTypes) {
            super(ts,
                  pos,
                  container,
                  flags,
                  returnType,
                  name,
                  formals,
                  throwTypes);
        }

        @Override
        public boolean canOverrideImpl(MethodInstance mj, boolean quiet)
                throws SemanticException {
            MethodInstance mi = this;

            // This is the only rule that has changed.
            if (!canOverrideReturnType(mi.returnType(), mj.returnType())) {
                if (quiet) return false;
                throw new SemanticException(mi.signature() + " in "
                                                    + mi.container()
                                                    + " cannot override "
                                                    + mj.signature() + " in "
                                                    + mj.container()
                                                    + "; incompatible "
                                                    + "return types: "
                                                    + mi.returnType()
                                                    + " is not a subtype of "
                                                    + mj.returnType(),
                                            mi.position());
            }

            // Force the return types to be the same and then let the super
            // class perform the remainder of the tests.
            MethodInstance tmpMj = (MethodInstance) mj.copy();
            tmpMj.setReturnType(mi.returnType());
            return super.canOverrideImpl(tmpMj, quiet);
        }

        protected boolean canOverrideReturnType(Type ri, Type rj) {
            if (ri.isPrimitive()) {
                return ri.equals(rj);
            }
            else if (ri.isReference()) {
                return ri.isSubtype(rj) || ri.isImplicitCastValid(rj);
            }
            else if (ri.isVoid()) {
                return rj.isVoid();
            }
            return ri.equals(rj);
        }
    }

}
