package polyglot.ext.jl5.types;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import polyglot.types.ClassType;
import polyglot.types.Context;
import polyglot.types.Context_c;
import polyglot.types.MethodInstance;
import polyglot.types.Named;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.VarInstance;
import polyglot.util.StringUtil;

public class JL5Context_c extends Context_c implements JL5Context {

    protected Map<String, TypeVariable> typeVars;

    protected TypeVariable typeVariable;

    protected Type switchType;
    protected ClassType declaringClass;

    protected boolean ctorCall;

    public static final Kind TYPE_VAR = new Kind("type-var");
    public static final Kind SWITCH = new Kind("switch");
    public static final Kind EXTENDS = new Kind("extends");

    public JL5Context_c(TypeSystem ts) {
        super(ts);
    }

    @Override
    public JL5TypeSystem typeSystem() {
        return (JL5TypeSystem) ts;
    }

    @Override
    public VarInstance findVariableSilent(String name) {
        VarInstance vi = findVariableInThisScope(name);
        if (vi != null) {
            return vi;
        }

        try {
            // might be static
            if (importTable() != null) {
                JL5ImportTable jit = (JL5ImportTable) importTable();
                for (String next : jit.singleStaticImports()) {
                    String id = StringUtil.getShortNameComponent(next);
                    if (name.equals(id)) {
                        Named nt =
                                ts.forName(StringUtil.getPackageComponent(next));
                        if (nt instanceof Type) {
                            Type t = (Type) nt;
                            try {
                                vi = ts.findField(t.toClass(), name);
                            }
                            catch (SemanticException e) {
                            }
                            if (vi != null) {
                                return vi;
                            }
                        }
                    }
                }
                if (vi == null) {
                    for (String next : jit.staticOnDemandImports()) {
                        Named nt = ts.forName(next);
                        if (nt instanceof Type) {
                            Type t = (Type) nt;
                            try {
                                vi = ts.findField(t.toClass(), name);
                            }
                            catch (SemanticException e) {
                            }
                            if (vi != null) return vi;
                        }
                    }
                }
            }
        }
        catch (SemanticException e) {
        }

        if (outer != null) {
            return outer.findVariableSilent(name);
        }
        return null;
    }

    @Override
    protected Context_c push() {
        JL5Context_c c = (JL5Context_c) super.push();
        c.typeVars = null;
        return c;
    }

    /**
     * pushes an additional static scoping level.
     */
    @Override
    public Context pushCTORCall() {
        JL5Context_c v = (JL5Context_c) push();
        v.staticContext = true;
        v.ctorCall = true;
        return v;
    }

    @Override
    public JL5Context pushTypeVariable(TypeVariable iType) {
        JL5Context_c v = (JL5Context_c) push();
        v.typeVariable = iType;
        v.kind = TYPE_VAR;
        return v;
    }

    @Override
    public TypeVariable findTypeVariableInThisScope(String name) {
        if (typeVariable != null && typeVariable.name().equals(name))
            return typeVariable;
        if (typeVars != null && typeVars.containsKey(name)) {
            return typeVars.get(name);
        }
        if (outer != null) {
            return ((JL5Context) outer).findTypeVariableInThisScope(name);
        }
        return null;
    }

    @Override
    public boolean inTypeVariable() {
        return kind == TYPE_VAR;
    }

    @Override
    public boolean inCTORCall() {
        return ctorCall;
    }

    @Override
    public String toString() {
        return super.toString() + "; type var: " + typeVariable
                + "; type vars: " + typeVars;
    }

    @Override
    public void addTypeVariable(TypeVariable type) {
        if (typeVars == null)
            typeVars = new LinkedHashMap<String, TypeVariable>();
        typeVars.put(type.name(), type);
    }

    @Override
    public Context pushSwitch(Type type) {
        JL5Context_c c = (JL5Context_c) push();
        c.switchType = type;
        c.kind = SWITCH;
        return c;
    }

    @Override
    public Context pushExtendsClause(ClassType declaringClass) {
        JL5Context_c c = (JL5Context_c) push();
        c.declaringClass = declaringClass;
        c.kind = EXTENDS;
        return c;
    }

    @Override
    public Type switchType() {
        return switchType;
    }

    @Override
    public MethodInstance findMethod(String name, List<? extends Type> argTypes)
            throws SemanticException {
        try {
            return super.findMethod(name, argTypes);
        }
        catch (SemanticException e) {
            // couldn't find the method.
            // try static imports.
            JL5ImportTable it = (JL5ImportTable) this.importTable();
            if (it != null && this.currentClass() != null) {
                ReferenceType rt = it.findTypeContainingMethodOrField(name);
                if (rt != null) {
                    try {
                        return ts.findMethod(rt,
                                             name,
                                             argTypes,
                                             this.currentClass());
                    }
                    catch (SemanticException f) {
                        // ignore this exception and throw the previous one.
                    }
                }
            }
            throw e;
        }
    }

    @Override
    public boolean inExtendsClause() {
        return this.kind == EXTENDS;
    }

    @Override
    public ClassType extendsClauseDeclaringClass() {
        return this.declaringClass;
    }

}
