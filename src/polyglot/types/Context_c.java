/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import polyglot.main.Report;
import polyglot.util.CollectionUtil;
import polyglot.util.Enum;
import polyglot.util.InternalCompilerError;

/**
 * This class maintains a context for looking up named variables, types,
 * and methods.
 * It's implemented as a stack of Context objects.  Each Context
 * points to an outer context.  To enter a new scope, call one of the
 * pushXXX methods.  To leave a scope, just follow the outer() pointer.
 * NodeVisitors handle leaving scope automatically.
 * Each context object contains maps from names to variable, type, and
 * method objects declared in that scope.
 */
public class Context_c implements Context {
    protected Context outer;
    protected TypeSystem ts;

    public static class Kind extends Enum {
        public Kind(String name) {
            super(name);
        }
    }

    public static final Kind BLOCK = new Kind("block");
    public static final Kind CLASS = new Kind("class");
    public static final Kind CODE = new Kind("code");
    public static final Kind OUTER = new Kind("outer");
    public static final Kind SOURCE = new Kind("source");

    public Context_c(TypeSystem ts) {
        this.ts = ts;
        this.outer = null;
        this.kind = OUTER;
    }

    public boolean isBlock() {
        return kind == BLOCK;
    }

    public boolean isClass() {
        return kind == CLASS;
    }

    public boolean isCode() {
        return kind == CODE;
    }

    public boolean isOuter() {
        return kind == OUTER;
    }

    public boolean isSource() {
        return kind == SOURCE;
    }

    @Override
    public TypeSystem typeSystem() {
        return ts;
    }

    @Override
    public Object copy() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone() weirdness.");
        }
    }

    protected Context_c push() {
        Context_c v = (Context_c) this.copy();
        v.outer = this;
        v.types = null;
        v.vars = null;
        return v;
    }

    /**
     * The import table for the file
     */
    protected ImportTable it;
    protected Kind kind;
    protected ClassType type;
    protected ParsedClassType scope;
    protected CodeInstance code;
    protected Map<String, Named> types;
    protected Map<String, VarInstance> vars;
    protected boolean inCode;

    /**
     * Is the context static?
     */
    protected boolean staticContext;

    /** @deprecated */
    @Deprecated
    @Override
    public Resolver outerResolver() {
        return ts.systemResolver();
    }

    @Override
    public ImportTable importTable() {
        return it;
    }

    /** The current package, or null if not in a package. */
    @Override
    public Package package_() {
        return importTable().package_();
    }

    /** Return the code def that defines the local variable or type with the given name. */
    @Override
    public CodeInstance definingCodeDef(String name) {
        if ((isBlock() || isCode())
                && (findVariableInThisScope(name) != null || findInThisScope(name) != null)) {
            return currentCode();
        }

        if (outer == null) {
            return null;
        }

        return outer.definingCodeDef(name);
    }

    /**
     * Returns whether the particular symbol is defined locally.  If it isn't
     * in this scope, we ask the parent scope, but don't traverse to enclosing
     * classes.
     */
    @Override
    public boolean isLocal(String name) {
        if (isClass()) {
            return false;
        }

        if ((isBlock() || isCode())
                && (findVariableInThisScope(name) != null || findInThisScope(name) != null)) {
            return true;
        }

        if (isCode()) {
            return false;
        }

        if (outer == null) {
            return false;
        }

        return outer.isLocal(name);
    }

    /**
     * Looks up a method with name "name" and arguments compatible with
     * "argTypes".
     */
    @Override
    public MethodInstance findMethod(String name, List<? extends Type> argTypes)
            throws SemanticException {
        if (Report.should_report(TOPICS, 3))
            Report.report(3, "find-method " + name + argTypes + " in " + this);

        // Check for any method with the appropriate name.
        // If found, stop the search since it shadows any enclosing
        // classes method of the same name.
        if (this.currentClass() != null
                && ts.hasAccessibleMethodNamed(this.currentClass(),
                                               name,
                                               this.currentClass())) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "find-method " + name + argTypes + " -> "
                        + this.currentClass());

            // Found a class which has a method of the right name.
            // Now need to check if the method is of the correct type.
            return ts.findMethod(this.currentClass(),
                                 name,
                                 argTypes,
                                 this.currentClass());
        }

        if (outer != null) {
            return outer.findMethod(name, argTypes);
        }

        throw new SemanticException("Method " + name + " not found.");
    }

    /**
     * Gets a local of a particular name.
     * 
     * @return
     * 		     the local instance
     * @throws SemanticException
     *           if there is no such local 
     */
    @Override
    public LocalInstance findLocal(String name) throws SemanticException {
        LocalInstance vi = findLocalSilent(name);

        if (vi == null)
            throw new SemanticException("Local " + name + " not found.");

        return vi;
    }

    /**
     * Gets a local of a particular name.
     * 
     * @return the local instance, or null if none exists
     */
    @Override
    public LocalInstance findLocalSilent(String name) {
        VarInstance vi = findVariableSilent(name);

        if (vi instanceof LocalInstance) {
            return (LocalInstance) vi;
        }

        return null;
    }

    /**
     * Finds the class which added a field to the scope.
     */
    @Override
    public ClassType findFieldScope(String name) throws SemanticException {
        if (Report.should_report(TOPICS, 3))
            Report.report(3, "find-field-scope " + name + " in " + this);

        VarInstance vi = findVariableInThisScope(name);

        if (vi instanceof FieldInstance) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "find-field-scope " + name + " in " + vi);
            return type;
        }

        if (vi == null && outer != null) {
            return outer.findFieldScope(name);
        }

        throw new SemanticException("Field " + name + " not found.");
    }

    /** Finds the class which added a method to the scope.
     */
    @Override
    public ClassType findMethodScope(String name) throws SemanticException {
        if (Report.should_report(TOPICS, 3))
            Report.report(3, "find-method-scope " + name + " in " + this);

        if (this.currentClass() != null
                && ts.hasMethodNamed(this.currentClass(), name)) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3,
                              "find-method-scope " + name + " -> "
                                      + this.currentClass());
            return this.currentClass();
        }

        if (outer != null) {
            return outer.findMethodScope(name);
        }

        throw new SemanticException("Method " + name + " not found.");
    }

    /**
     * Gets a field of a particular name.
     */
    @Override
    public FieldInstance findField(String name) throws SemanticException {
        VarInstance vi = findVariableSilent(name);

        if (vi instanceof FieldInstance) {
            FieldInstance fi = (FieldInstance) vi;

            if (!ts.isAccessible(fi, this)) {
                throw new SemanticException("Field " + name
                        + " not accessible.");
            }

            if (Report.should_report(TOPICS, 3))
                Report.report(3, "find-field " + name + " -> " + fi);
            return fi;
        }

        throw new NoMemberException(NoMemberException.FIELD, "Field " + name
                + " not found.");
    }

    /**
     * Gets a local or field of a particular name.
     */
    @Override
    public VarInstance findVariable(String name) throws SemanticException {
        VarInstance vi = findVariableSilent(name);

        if (vi != null) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "find-var " + name + " -> " + vi);
            return vi;
        }

        throw new SemanticException("Variable " + name + " not found.");
    }

    /**
     * Gets a local or field of a particular name.
     */
    @Override
    public VarInstance findVariableSilent(String name) {
        if (Report.should_report(TOPICS, 3))
            Report.report(3, "find-var " + name + " in " + this);

        VarInstance vi = findVariableInThisScope(name);

        if (vi != null) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "find-var " + name + " -> " + vi);
            return vi;
        }

        if (outer != null) {
            return outer.findVariableSilent(name);
        }

        return null;
    }

    protected String mapsToString() {
        return "types=" + types + " vars=" + vars;
    }

    @Override
    public String toString() {
        return "(" + kind + " " + mapsToString() + " " + outer + ")";
    }

    @Override
    public Context pop() {
        return outer;
    }

    /**
     * Finds the definition of a particular type.
     */
    @Override
    public Named find(String name) throws SemanticException {
        if (Report.should_report(TOPICS, 3))
            Report.report(3, "find-type " + name + " in " + this);

        if (isOuter()) return ts.systemResolver().find(name);
        if (isSource()) return it.find(name);

        Named type = findInThisScope(name);

        if (type != null) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "find " + name + " -> " + type);
            return type;
        }

        if (outer != null) {
            return outer.find(name);
        }

        throw new SemanticException("Type " + name + " not found.");
    }

    /**
     * Push a source file scope.
     */
    @Override
    public Context pushSource(ImportTable it) {
        Context_c v = push();
        v.kind = SOURCE;
        v.it = it;
        v.inCode = false;
        v.staticContext = false;
        return v;
    }

    /**
     * Pushes on a class scoping.
     * @param classScope The class whose scope is being entered.  This is
     * the object associated with the class declaration and is returned by
     * currentClassScope.  This is a mutable class type since for some
     * passes (e.g., addMembers), the object returned by currentClassScope
     * is modified.
     * @param type The type to be returned by currentClass().  For JL, this
     * type is the same as classScope.  For other languages, it may differ
     * since currentClassScope might not represent a type.
     * @return A new context with a new scope and which maps the short name
     * of type to type.
     */
    @Override
    public Context pushClass(ParsedClassType classScope, ClassType type) {
        if (Report.should_report(TOPICS, 4))
            Report.report(4,
                          "push class " + classScope + " "
                                  + classScope.position());
        Context_c v = push();
        v.kind = CLASS;
        v.scope = classScope;
        v.type = type;
        v.inCode = false;
        v.staticContext = false;

        if (!type.isAnonymous()) {
            v.addNamed(type);
        }

        return v;
    }

    /**
     * pushes an additional block-scoping level.
     */
    @Override
    public Context pushBlock() {
        if (Report.should_report(TOPICS, 4)) Report.report(4, "push block");
        Context_c v = push();
        v.kind = BLOCK;
        return v;
    }

    /**
     * pushes an additional static scoping level.
     */
    @Override
    public Context pushStatic() {
        if (Report.should_report(TOPICS, 4)) Report.report(4, "push static");
        Context_c v = push();
        v.staticContext = true;
        return v;
    }

    /**
     * enters a method
     */
    @Override
    public Context pushCode(CodeInstance ci) {
        if (Report.should_report(TOPICS, 4))
            Report.report(4, "push code " + ci + " " + ci.position());
        Context_c v = push();
        v.kind = CODE;
        v.code = ci;
        v.inCode = true;
        v.staticContext = ci.flags().isStatic();
        return v;
    }

    /**
     * Gets the current method
     */
    @Override
    public CodeInstance currentCode() {
        return code;
    }

    /**
     * Return true if in a method's scope and not in a local class within the
     * innermost method.
     */
    @Override
    public boolean inCode() {
        return inCode;
    }

    /** 
     * Returns whether the current context is a static context.
     * A statement of expression occurs in a static context if and only if the
     * inner-most method, constructor, instance initializer, static initializer,
     * field initializer, or explicit constructor statement enclosing the 
     * statement or expressions is a static method, static initializer, the 
     * variable initializer of a static variable, or an explicity constructor 
     * invocation statment. (Java Language Spec, 2nd Edition, 8.1.2)
     */
    @Override
    public boolean inStaticContext() {
        return staticContext;
    }

    /**
     * Gets current class
     */
    @Override
    public ClassType currentClass() {
        return type;
    }

    /**
     * Gets current class
     */
    @Override
    public ParsedClassType currentClassScope() {
        return scope;
    }

    /**
     * Adds a symbol to the current scoping level.
     */
    @Override
    public void addVariable(VarInstance vi) {
        if (Report.should_report(TOPICS, 3))
            Report.report(3, "Adding " + vi + " to context.");
        addVariableToThisScope(vi);
    }

    /**
     * Adds a method to the current scoping level.
     * Actually, this does nothing now.
     * @deprecated
     */
    @Deprecated
    @Override
    public void addMethod(MethodInstance mi) {
        if (Report.should_report(TOPICS, 3))
            Report.report(3, "Adding " + mi + " to context.");
    }

    /**
     * Adds a named type object to the current scoping level.
     */
    @Override
    public void addNamed(Named t) {
        if (Report.should_report(TOPICS, 3))
            Report.report(3, "Adding type " + t + " to context.");
        addNamedToThisScope(t);
    }

    public Named findInThisScope(String name) {
        Named t = null;
        if (types != null) {
            t = types.get(name);
        }
        if (t == null && isClass()) {
            if (!this.type.isAnonymous() && this.type.name().equals(name)) {
                return this.type;
            }
            else {
                try {
                    return ts.findMemberClass(this.type, name, this.type);
                }
                catch (SemanticException e) {
                }
            }
        }
        return t;
    }

    public void addNamedToThisScope(Named type) {
        if (types == null) types = new HashMap<String, Named>();
        types.put(type.name(), type);
    }

    public ClassType findMethodContainerInThisScope(String name) {
        if (isClass() && ts.hasMethodNamed(this.currentClass(), name)) {
            return this.type;
        }
        return null;
    }

    public VarInstance findVariableInThisScope(String name) {
        VarInstance vi = null;
        if (vars != null) {
            vi = vars.get(name);
        }
        if (vi == null && isClass()) {
            try {
                return ts.findField(this.type, name, this.type);
            }
            catch (SemanticException e) {
                return null;
            }
        }
        return vi;
    }

    public void addVariableToThisScope(VarInstance var) {
        if (vars == null) vars = new HashMap<String, VarInstance>();
        vars.put(var.name(), var);
    }

    private static final Collection<String> TOPICS =
            CollectionUtil.list(Report.types, Report.context);

}
