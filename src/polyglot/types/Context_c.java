package polyglot.ext.jl.types;

import java.util.HashMap;
import java.util.List;
import java.util.Collection;
import java.util.Map;

import polyglot.main.Report;
import polyglot.types.ClassType;
import polyglot.types.CodeInstance;
import polyglot.types.Context;
import polyglot.types.FieldInstance;
import polyglot.types.ImportTable;
import polyglot.types.LocalInstance;
import polyglot.types.MethodInstance;
import polyglot.types.Named;
import polyglot.types.NoMemberException;
import polyglot.types.ParsedClassType;
import polyglot.types.ReferenceType;
import polyglot.types.Resolver;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.types.VarInstance;
import polyglot.util.CollectionUtil;
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
public class Context_c implements Context
{
    protected Context outer;
    protected TypeSystem ts;
    
    public Context_c(TypeSystem ts) {
        this.ts = ts;
        this.outer = null;
        this.kind = OUTER;
    }

    public TypeSystem typeSystem() {
        return ts;
    }

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
        v.methods = null;
        v.vars = null;
        return v;
    }

    /**
     * The import table for the file
     */
    protected ImportTable it;
    protected int kind;
    protected ClassType type;
    protected ParsedClassType scope;
    protected CodeInstance code;
    protected Map types;
    protected Map methods;
    protected Map vars;
    protected boolean inCode;

    /**
     * Is the context static?
     */
    protected boolean staticContext;

    public static final int BLOCK = 0;
    public static final int CLASS = 1;
    public static final int CODE  = 2;
    public static final int OUTER = 3;
    public static final int SOURCE = 4;

    private String kindStr() {
        if (kind == BLOCK) return "block";
        if (kind == CLASS) return "class";
        if (kind == CODE) return "code";
        if (kind == OUTER) return "outer";
        if (kind == SOURCE) return "source";
        return "unknown-scope";
    }

    public Resolver outerResolver() {
        if (it != null) {
            return it;
        }
        return ts.systemResolver();
    }

    public ImportTable importTable() {
        return it;
    }

    /**
     * Returns whether the particular symbol is defined locally.  If it isn't
     * in this scope, we ask the parent scope, but don't traverse to enclosing
     * classes.
     */
    public boolean isLocal(String name) {
        if (kind == CLASS) {
            return false;
        }
        
        if ((kind == BLOCK || kind == CODE) &&
            (findVariableInThisScope(name) != null || findInThisScope(name) != null)) {
            return true;
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
    public MethodInstance findMethod(String name, List argTypes) throws SemanticException {
        if (Report.should_report(TOPICS, 3))
          Report.report(3, "find-method " + name + argTypes + " in " + this);

        ReferenceType rt = findMethodContainerInThisScope(name);

        if (rt != null) {
            if (Report.should_report(TOPICS, 3))
              Report.report(3, "find-method " + name + argTypes + " -> " + rt);

            // Found a class which has a method of the right name.
            // Now need to check if the method is of the correct type.
            return ts.findMethod(rt, name, argTypes, this.currentClass());
        }

        if (outer != null) {
            return outer.findMethod(name, argTypes);
        }

        throw new SemanticException("Method " + name + " not found.");
    }

    /**
     * Gets a local of a particular name.
     */
    public LocalInstance findLocal(String name) throws SemanticException {
	VarInstance vi = findVariableSilent(name);

	if (vi instanceof LocalInstance) {
	    return (LocalInstance) vi;
	}

        throw new SemanticException("Local " + name + " not found.");
    }

    /**
     * Finds the class which added a field to the scope.
     */
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
    public ClassType findMethodScope(String name) throws SemanticException {
        if (Report.should_report(TOPICS, 3))
          Report.report(3, "find-method-scope " + name + " in " + this);

        ClassType container = findMethodContainerInThisScope(name);

        if (container != null) {
            if (Report.should_report(TOPICS, 3))
              Report.report(3, "find-method-scope " + name + " -> " + container);
            return type;
        }

        if (outer != null) {
            return outer.findMethodScope(name);
        }

        throw new SemanticException("Method " + name + " not found.");
    }

    /**
     * Gets a field of a particular name.
     */
    public FieldInstance findField(String name) throws SemanticException {
	VarInstance vi = findVariableSilent(name);

	if (vi instanceof FieldInstance) {
	    FieldInstance fi = (FieldInstance) vi;

	    if (! ts.isAccessible(fi, this)) {
                throw new SemanticException("Field " + name + " not accessible.");
	    }

            if (Report.should_report(TOPICS, 3))
              Report.report(3, "find-field " + name + " -> " + fi);
	    return fi;
	}

        throw new NoMemberException(NoMemberException.FIELD, "Field " + name + " not found.");
    }

    /**
     * Gets a local or field of a particular name.
     */
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
        return "types=" + types + " vars=" + vars + " methods=" + methods;
    }

    public String toString() {
        return "(" + kindStr() + " " + mapsToString() + " " + outer + ")";
    }

    public Context pop() {
        return outer;
    }

    /**
     * Finds the definition of a particular type.
     */
    public Named find(String name) throws SemanticException {
        if (Report.should_report(TOPICS, 3))
          Report.report(3, "find-type " + name + " in " + this);

        if (kind == OUTER) return outerResolver().find(name);
        if (kind == SOURCE) return it.find(name);

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
    public Context pushSource(ImportTable it) {
        Context_c v = push();
        v.kind = SOURCE;
        v.it = it;
        v.inCode = false;
        v.staticContext = false;
        return v;
    }

    /**
     * Pushes on a class scoping
     */
    public Context pushClass(ParsedClassType c, ClassType t) {
        if (Report.should_report(TOPICS, 4))
          Report.report(4, "push class " + c + " " + c.position());
        Context_c v = push();
        v.kind = CLASS;
        v.scope = c;
        v.type = t;
        v.inCode = false;
        v.staticContext = false;
        return v;
    }

    /**
     * pushes an additional block-scoping level.
     */
    public Context pushBlock() {
        if (Report.should_report(TOPICS, 4))
          Report.report(4, "push block");
        Context_c v = push();
        v.kind = BLOCK;
        return v;
    }

    /**
     * pushes an additional static scoping level.
     */
    public Context pushStatic() {
        if (Report.should_report(TOPICS, 4))
          Report.report(4, "push static");
        Context_c v = push();
        v.staticContext = true;
        return v;
    }

    /**
     * enters a method
     */
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
    public CodeInstance currentCode() {
        return code;
    }

    /**
     * Return true if in a method's scope and not in a local class within the
     * innermost method.
     */
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
    public boolean inStaticContext() {
        return staticContext;
    }

    /**
     * Gets current class
     */
    public ClassType currentClass() {
        return type;
    }

    /**
     * Gets current class
     */
    public ParsedClassType currentClassScope() {
        return scope;
    }

    /**
     * Adds a symbol to the current scoping level.
     */
    public void addVariable(VarInstance vi) {
        if (Report.should_report(TOPICS, 3))
          Report.report(3, "Adding " + vi + " to context.");
        addVariableToThisScope(vi);
    }

    /**
     * Adds a method to the current scoping level.
     */
    public void addMethod(MethodInstance mi) {
        if (Report.should_report(TOPICS, 3))
          Report.report(3, "Adding " + mi + " to context.");
        addMethodContainerToThisScope(mi);
    }

    /**
     * Adds a named type object to the current scoping level.
     */
    public void addNamed(Named t) {
        if (Report.should_report(TOPICS, 3))
          Report.report(3, "Adding type " + t + " to context.");
        addNamedToThisScope(t);
    }

    public Named findInThisScope(String name) {
        if (types == null) return null;
        return (Named) types.get(name);
    }

    public void addNamedToThisScope(Named type) {
        if (types == null) types = new HashMap();
        types.put(type.name(), type);
    }

    public ClassType findMethodContainerInThisScope(String name) {
        if (methods == null) return null;
        return (ClassType) methods.get(name);
    }

    public void addMethodContainerToThisScope(MethodInstance mi) {
        if (methods == null) methods = new HashMap();
        methods.put(mi.name(), mi.container());
    }

    public VarInstance findVariableInThisScope(String name) {
        if (vars == null) return null;
        return (VarInstance) vars.get(name);
    }

    public void addVariableToThisScope(VarInstance var) {
        if (vars == null) vars = new HashMap();
        vars.put(var.name(), var);
    }

    private static final Collection TOPICS = 
                CollectionUtil.list(Report.types, Report.context);

}
