package jltools.types;

import jltools.util.InternalCompilerError;
import jltools.ast.NodeVisitor;

import java.util.*;

/**
 * A context to be used within the scope of a method body.  
 * It provides a convenient wrapper for the Type System.
 */
public class LocalContext implements TypeContext
{
  /**
   * Resolve anything that we don't know about to the type system
   */
  protected TypeSystem ts;
  /**
   * Contains the stack of inner scopes.
   */
  protected Stack /* of Scopes */ scopes;
  /**
   * the import table for the file
   */
  protected ImportTable itImports;
  /**
   * The current node visitor.
   */
  protected NodeVisitor visitor;

  public LocalContext( ImportTable itImports, TypeSystem ts,
                       NodeVisitor visitor) 
  {  
    this.itImports = itImports;
    this.ts = ts;
    this.visitor = visitor;
    
    scopes = new Stack();

    scopes.push(getTopScope());
  }

  /**
   * Returns whether the particular symbol is defined locally. If it 
   * isn't in this scope, we ask the parent scope, but don't traverse to 
   * enclosing classes.
   */
  public boolean isDefinedLocally(String s)
  {
    ListIterator iter = scopes.listIterator(scopes.size());

    while (iter.hasPrevious()) {
	Scope scope = (Scope) iter.previous();
	if (scope instanceof BlockScope) {
	  if (scope.getVariable(s) != null || scope.getType(s) != null) {
	    return true;
	  }
	}
	else {
	  break;
	}
    }

    return false;
  }

  /**
   * Gets the methodMatch with name with "name" and a list of argument 
   * types "argumentTypes" against Type "type". type may be null; 
   */
  public MethodTypeInstance getMethod(ReferenceType type, String methodName, List argumentTypes) 
    throws SemanticException
  {
    if (type == null) {
      ListIterator iter = scopes.listIterator(scopes.size());

      while (iter.hasPrevious()) {
	  Scope scope = (Scope) iter.previous();
	  type = (ReferenceType) scope.getMethodEnclosingType(methodName);
	  if (type != null) {
	    break;
	  }
      }

      if (type == null) {
	throw new SemanticException("Method " + methodName + " not found");
      }
    }

    return ts.getMethod( type, 
                         new MethodType( ts, methodName, argumentTypes), 
			 this );
  }

  /**
   * Gets the methodMatch with name with of a MethodNode m on object t 
   * type may be null; 
   */
  public MethodTypeInstance getMethod( ReferenceType t, MethodType m) 
    throws SemanticException
  {
    return ts.getMethod( t, m, this );
  }
  
  /**
   * Gets a local of a particular name.
   */  
  public LocalInstance getLocal(String fieldName) throws SemanticException
  {
    ListIterator iter = scopes.listIterator(scopes.size());

    while (iter.hasPrevious()) {
      Scope scope = (Scope) iter.previous();
      VariableInstance var = scope.getVariable(fieldName);
      if (var != null) {
        if (var instanceof LocalInstance) {
	  return (LocalInstance) var;
	}
	else {
	  break;
	}
      }
    }

    throw new SemanticException("Local " + fieldName + " not found");
  }

  /**
   * Gets a local or field of a particular name.
   */  
  public VariableInstance getVariable(String fieldName) throws SemanticException
  {
    ListIterator iter = scopes.listIterator(scopes.size());

    while (iter.hasPrevious()) {
      Scope scope = (Scope) iter.previous();
      VariableInstance var = scope.getVariable(fieldName);
      if (var != null) {
	return var;
      }
    }

    throw new SemanticException("Field or local " + fieldName + " not found");
  }

  public ClassType getFieldContainingClass(String fieldName)
    throws SemanticException
  {
    boolean found = false;

    ListIterator iter = scopes.listIterator(scopes.size());

    while (iter.hasPrevious()) {
      Scope scope = (Scope) iter.previous();

      if (! found) {
	VariableInstance var = scope.getVariable(fieldName);
	if (var != null) {
	  found = true;
	}
      }

      if (found) {
	if (scope instanceof ClassScope) {
	  return ((ClassScope) scope).getClassType();
	}
      }
    }

    throw new SemanticException("Field or local " + fieldName + " not found");
  }

  /**
   * Gets a field of a particular name.
   */  
  public FieldInstance getField(String fieldName) throws SemanticException
  {
    ListIterator iter = scopes.listIterator(scopes.size());

    while (iter.hasPrevious()) {
      Scope scope = (Scope) iter.previous();
      VariableInstance var = scope.getVariable(fieldName);
      if (var != null) {
        if (var instanceof FieldInstance) {
	  return (FieldInstance) var;
	}
	else {
	  break;
	}
      }
    }

    throw new SemanticException("Field " + fieldName + " not found");
  }

  /**
   * If <type> is a valid type in the given context, returns a
   * canonical form of that type.  
   **/
  public Type getType( Type type ) throws SemanticException {
    return ts.checkAndResolveType( type, this );
  }

  /**
   * Finds the definition of a particular type
   */
  public Type getType(String name) throws SemanticException {
    ListIterator iter = scopes.listIterator(scopes.size());

    while (iter.hasPrevious()) {
      Scope scope = (Scope) iter.previous();
      Type type = scope.getType(name);
      if (type != null) {
	return type;
      }
    }

    throw new SemanticException("Type " + name + " not found");
  }

  /**
   * Returns the current type system
   */
  public TypeSystem getTypeSystem()
  {
    return ts;
  }

  protected TopScope getTopScope()
  {
    return new JavaTopScope();
  }

  protected ClassScope getClassScope(ClassType c)
  {
    return new JavaClassScope(c);
  }

  protected BlockScope getBlockScope()
  {
    return new JavaBlockScope();
  }

  protected MethodScope getMethodScope(MethodTypeInstance m)
  {
    return new JavaMethodScope(m);
  }

  /**
   * Pushes on a class  scoping
   */
  public void pushClass( ClassType c)
  {
    if ( c == null)
      throw new InternalCompilerError("Tried to push a null class.");
    if ( c.getSuperType() instanceof AmbiguousType)
      throw new InternalCompilerError("Tried to push a class with an " +
				      "ambiguous supertype: \"" +
				      c.getSuperType().getTypeString() + "\".");

    ClassScope scope = getClassScope(c);
    scopes.push( scope );

    LinkedList typeQueue = new LinkedList();
    Set visitedTypes = new HashSet();
    typeQueue.addLast(c);

    while (!typeQueue.isEmpty()) {
      c = (ClassType)typeQueue.removeFirst();
      if (visitedTypes.contains(c))
	continue;

	for (Iterator iter = c.getMethods().iterator(); iter.hasNext(); ) {
	    MethodTypeInstance mti = (MethodTypeInstance) iter.next();
	    if (scope.getMethodEnclosingType(mti.getName()) == null) {
	      scope.putMethodEnclosingType(mti.getName(), c);
	    }
	}
	for (Iterator iter = c.getFields().iterator(); iter.hasNext(); ) {
	    FieldInstance fi = (FieldInstance) iter.next();
	    if (scope.getVariable(fi.getName()) == null) {
	      scope.putVariable(fi.getName(), fi);
	    }
	}
	for (Iterator iter = c.getInnerClasses().iterator(); iter.hasNext(); ) {
	    ClassType t = (ClassType) iter.next();
	    if (! t.isLocal() && ! t.isAnonymous()) {
	      if (scope.getType(t.getShortName()) == null) {
		scope.putType(t.getShortName(), t);
	      }
	    }
	}

	if (scope.getType(c.getShortName()) == null) {
	  scope.putType(c.getShortName(), c);
	}

	visitedTypes.add(c);
	if (c.getSuperType() != null)
	  typeQueue.addLast(c.getSuperType());
	for (Iterator i = c.getInterfaces().iterator(); i.hasNext(); ) {
	  Object iface = i.next();
	  if (iface != null)
	    typeQueue.addLast(iface);
	}
    }
  }

  /**
   * Pops the most recently pushed class scoping
   */
  public void popClass()
  {
    try {
      if (scopes.peek() instanceof ClassScope) {
	ClassScope classScope = (ClassScope) scopes.pop();

	// Handle local classes -- insert in new innermost scope after
	// we've processed the class body.
	if (classScope.getClassType().isLocal() &&
	    ! classScope.getClassType().isAnonymous()) {
	  try {
	    Scope top = (Scope) scopes.peek();
	    if (! (top instanceof BlockScope)) {
	      throw new InternalCompilerError("Local class not in block scope: " + top);
	    }
	    else {
	      ((BlockScope) top).putType(
			classScope.getClassType().getShortName(),
			classScope.getClassType());
	    }
	  }
	  catch (EmptyStackException ese ) { 
	    throw new InternalCompilerError("Local class found in top scope");
	  }
	}
      }
      else {
	  throw new InternalCompilerError("Innermost scope is not a class scope");
      }
    }
    catch (EmptyStackException ese ) { 
      throw new InternalCompilerError("No more class scopes to pop");
    }
  }

  /**
   * pushes an additional block-scoping level.
   */
  public void pushBlock()
  {
    scopes.push( getBlockScope() );
  }

  /**
   * Removes a scoping level 
   */
  public void popBlock()
  {
    try {
      if (scopes.peek() instanceof BlockScope) {
	  scopes.pop();
      }
      else {
	  throw new InternalCompilerError("Innermost scope is not a block scope!");
      }
    }
    catch (EmptyStackException ese ) { 
      throw new InternalCompilerError("No more block scopes to pop!");
    }
  }

  /**
   * enters a method
   */
  public void enterMethod(MethodTypeInstance mti)
  {
    scopes.push( getMethodScope(mti) );
  }

  /**
   * leaves a method
   */
  public void leaveMethod()
  {
    try {
      if (scopes.peek() instanceof MethodScope) {
	  scopes.pop();
      }
      else {
	  throw new InternalCompilerError("Innermost scope is not a method scope!");
      }
    }
    catch (EmptyStackException ese ) { 
      throw new InternalCompilerError("No more method scopes to pop!");
    }
  }

  /**
   * Gets the current method
   */
  public MethodTypeInstance getCurrentMethod() 
  {
    ListIterator iter = scopes.listIterator(scopes.size());

    while (iter.hasPrevious()) {
      Scope scope = (Scope) iter.previous();

      if (scope instanceof MethodScope) {
	MethodScope s = (MethodScope) scope;
	return s.getMethod();
      }
    }

    return null;
    // throw new InternalCompilerError("Not in method scope");
  }

  /**
   * Return true if in a method's scope and not in a local class within the
   * innermost method.
   */
  public boolean inMethodScope() 
  {
    ListIterator iter = scopes.listIterator(scopes.size());

    while (iter.hasPrevious()) {
      Scope scope = (Scope) iter.previous();

      if (scope instanceof MethodScope) {
	return true;
      }
      if (scope instanceof ClassScope) {
	return false;
      }
    }

    return false;
  }

  /**
   * Gets current class
   */
  public ClassType getCurrentClass() {
    ListIterator iter = scopes.listIterator(scopes.size());

    while (iter.hasPrevious()) {
      Scope scope = (Scope) iter.previous();

      if (scope instanceof ClassScope) {
	ClassScope s = (ClassScope) scope;
	return s.getClassType();
      }
    }

    return null;
    // throw new InternalCompilerError("Not in class scope");
  }

  /**
   * Adds a symbol to the current scoping level
   */
  public void addSymbol( String name, VariableInstance vi) 
    throws SemanticException
  {
    try {
      Scope scope = (Scope) scopes.peek();
      if (scope.getVariable(name) == null) {
	scope.putVariable(name, vi);
      }
      else {
	throw new SemanticException ( "Symbol \"" + name + 
				       "\" already defined in this block.");
      }
    }
    catch (EmptyStackException ese ) { 
      throw new InternalCompilerError("Scope stack is empty!");
    }
  }

  public ImportTable getImportTable() {
    return itImports;
  }

  public NodeVisitor getVisitor() {
    return visitor;
  }

  protected interface Scope {
    Type getType(String name);
    void putType(String name, Type type);

    ReferenceType getMethodEnclosingType(String name);
    void putMethodEnclosingType(String name, ReferenceType method);

    VariableInstance getVariable(String name);
    void putVariable(String name, VariableInstance var);
  }

  protected interface TopScope extends Scope {
  }

  protected interface BlockScope extends Scope {
  }

  protected interface ClassScope extends Scope {
    ClassType getClassType();
  }

  protected interface MethodScope extends Scope {
    MethodTypeInstance getMethod();
  }

  protected abstract class HashScope implements Scope {
    Map types;		// Map from name to type
    Map methods;	// Map from name to class type enclosing the method
			// (This isn't a map to the method type, since it
			// could be overloaded)
    Map variables;	// Map from name to variable instance

    public HashScope() {
	types = new HashMap();
	methods = new HashMap();
	variables = new HashMap();
    }

    public Type getType(String name) {
	return (Type) types.get(name);
    }

    public void putType(String name, Type type) {
	types.put(name, type);
    }

    public ReferenceType getMethodEnclosingType(String name) {
	return (ReferenceType) methods.get(name);
    }

    public void putMethodEnclosingType(String name, ReferenceType method) {
	methods.put(name, method);
    }

    public VariableInstance getVariable(String name) {
	return (VariableInstance) variables.get(name);
    }

    public void putVariable(String name, VariableInstance var) {
	variables.put(name, var);
    }
  }

  protected class JavaBlockScope extends HashScope implements BlockScope {
    public String toString() {
	return "BlockScope";
    }
  }

  protected class JavaMethodScope extends HashScope implements MethodScope {
    private MethodTypeInstance mti;

    public JavaMethodScope(MethodTypeInstance mti) {
	this.mti = mti;
    }

    public MethodTypeInstance getMethod() {
	return mti;
    }

    public String toString() {
	return "MethodScope " + mti.getTypeString();
    }
  }

  protected class JavaClassScope extends HashScope implements ClassScope {
    private ClassType clazz;

    public JavaClassScope(ClassType clazz) {
	this.clazz = clazz;
    }

    public ClassType getClassType()
    {
	return clazz;
    }

    public String toString() {
	return "ClassScope " + clazz.getTypeString();
    }
  }

  protected class JavaTopScope implements TopScope {
    public Type getType(String name) {
	try {
	  Type t = itImports.findClass(name);
	  return t;
	}
	catch (SemanticException e2) {
	  try {
	    itImports.findPackage(name);
	    Type t = new PackageType(ts, name);
	    return t;
	  }
	  catch (NoClassException e) {
	    return null;
	  }
	}
    }

    public void putType(String name, Type type) {
	throw new InternalCompilerError("Cannot insert type in top scope");
    }

    public ReferenceType getMethodEnclosingType(String name) {
	return null;
    }

    public void putMethodEnclosingType(String name, ReferenceType method) {
	throw new InternalCompilerError("Cannot insert method in top scope");
    }

    public VariableInstance getVariable(String name) {
	return null;
    }

    public void putVariable(String name, VariableInstance var) {
	throw new InternalCompilerError("Cannot insert variable in top scope");
    }

    public String toString() {
	return "TopScope";
    }
  }
}
