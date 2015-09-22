/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/
package polyglot.ext.jl5.types;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import polyglot.ast.Lang;
import polyglot.types.ClassType;
import polyglot.types.Context;
import polyglot.types.Context_c;
import polyglot.types.FieldInstance;
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

    public JL5Context_c(Lang lang, TypeSystem ts) {
        super(lang, ts);
    }

    @Override
    public JL5TypeSystem typeSystem() {
        return (JL5TypeSystem) ts;
    }

    @Override
    public ClassType findFieldScope(String name) throws SemanticException {
        try {
            return super.findFieldScope(name);
        }
        catch (SemanticException e) {
            VarInstance vi = findVariableInStaticImport(name);
            if (vi instanceof FieldInstance)
                return ((FieldInstance) vi).container().toClass();
            throw e;
        }
    }

    @Override
    public VarInstance findVariableSilent(String name) {
        VarInstance vi = findVariableInThisScope(name);
        if (vi == null && outer != null) {
            vi = outer.findVariableSilent(name);
        }
        if (vi != null) {
            return vi;
        }

        // might be a static import
        return findVariableInStaticImport(name);
    }

    public VarInstance findVariableInStaticImport(String name) {
        try {
            if (importTable() != null) {
                VarInstance vi = null;
                JL5ImportTable jit = (JL5ImportTable) importTable();
                for (String next : jit.singleStaticImports()) {
                    String id = StringUtil.getShortNameComponent(next);
                    if (name.equals(id)) {
                        Named nt =
                                ts.forName(StringUtil.getPackageComponent(next));
                        if (nt instanceof Type) {
                            Type t = (Type) nt;
                            try {
                                vi =
                                        ts.findField(t.toClass(),
                                                     name,
                                                     t.toClass(),
                                                     true);
                            }
                            catch (SemanticException e) {
                            }
                            if (vi != null && vi.flags().isStatic()) {
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
                                vi =
                                        ts.findField(t.toClass(),
                                                     name,
                                                     t.toClass(),
                                                     true);
                            }
                            catch (SemanticException e) {
                            }
                            if (vi != null && vi.flags().isStatic()) {
                                return vi;
                            }
                        }
                    }
                }
            }
        }
        catch (SemanticException e) {
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
        if (typeVars == null) typeVars = new LinkedHashMap<>();
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
                for (ReferenceType rt : it.findTypesContainingMethodOrField(name)) {
                    try {
                        return ts.findMethod(rt,
                                             name,
                                             argTypes,
                                             this.currentClass(),
                                             false);
                    }
                    catch (SemanticException f) {
                        // ignore this exception and 
                        // try the next containing type.
                    }
                }
            }
            // couldn't find anything in the static imports.            
            // throw the original exception.
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
