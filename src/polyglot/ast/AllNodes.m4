include(`ASTNode.m4')

dnl TODO: FlagsNode

NODES_ARE_MUTABLE
dnl =================================================================
AST_NODE(`AmbiguousExpression', `abstract', `Expression')
NO_CONSTRUCTOR`'NO_COPY`'NO_MERGE`'NO_ACCEPT

/**
 * AmbiguousExpression
 *
 * Overview: An AmbiguousExpression represents any ambiguous Java expression,
 *    such as "a.b.c".
 **/
GENERATE

dnl =================================================================
AST_NODE(`AmbiguousNameExpression', `concrete', `AmbiguousExpression')
AST_VARS(`*J names:String')

/**
 * AmbiguousNameExpression
 *
 * Overview: An AmbiguousNameExpression represents an ambiguous
 *    expression composed of a series of period-separated identifiers.
 *
 * Notes: In standard Java, an AmbiguousNameExpression will be one of:
 *     -- field-name{.nonstatic-field-name}*
 *     -- class-name.static-field-name{.nonstatic-field-name}*....
 *
 * Since we can identify locals at parse-time, we make it invariant
 * that the first component of an AmbiguousNameExpression is _not_ a local.
 *
 * In order to resolve the ambiguity, the spec requires that we inspect the
 * first identifier to determine whether it's a field.  If not, we look for
 * the longest possible prefix that's a class name.
 **/
START_CLASS
dnl ======> Extra constructor
  /**
   * Requires: strng is not empty, and does not begin or end with a '.'
   * Effects: creates a new AmbiguousNameExpression for the identifier in
   *   <strng>
   */
  public AmbiguousNameExpression(String strng) {
    names = new TypedList(new ArrayList(4), String.class, false);
    Enumeration enum = new java.util.StringTokenizer(strng, ".");
    while (enum.hasMoreElements())
      names.add(enum.nextElement());
  }
END_CLASS

dnl =================================================================
AST_NODE(`ArrayIndexExpression', `concrete', `Expression')
AST_VARS(`!A base:Expression', `!A index:Expression')

/**
 * Overview: An ArrayIndexExpression is a representation of an
 * access of an array member.  For instance foo[i] accesses the i'th
 * member of foo.  An ArrayIndexExpression consists of a base
 * Expression which evaulates to an array, and an index expression
 * which evaluates to an int indicating the index of the array to be
 * accessed.
 */
GENERATE

dnl =================================================================
AST_NODE(`ArrayInitializerExpression', `concrete', `Expression')
AST_VAR(`*A children:Expression')

/**
 * ArrayInitializerExpression
 *
 * Overview: An ArrayInitializerExpression is a representation of
 *   an ArrayInitializer, such as { 3, 1, { 4, 1, 5 } }
 **/
GENERATE

dnl =================================================================
AST_NODE(`BinaryExpression', `concrete', `Expression')
AST_VAR(`!A left:Expression', 
        `!P operator:int', 
        `!A right:Expression')

/**
 * BinaryExpression
 * 
 * Overview: A BinaryExpression represents a Java binary expression, a
 * pair of expressions combined with an operator.
 **/
START_CLASS

    public static final int ASSIGN         = 0; // = operator
    public static final int GT             = 1; // > operator
    public static final int LT             = 2; // < opereator
    public static final int EQUAL          = 3; // == operator
    public static final int LE             = 4; // <= operator
    public static final int GE             = 5; // >= operator
    public static final int NE             = 6; // != operator
    public static final int LOGIC_OR       = 7; // || operator
    public static final int LOGIC_AND      = 8; // && operator
    public static final int MULT           = 9; // * operator
    public static final int DIV            = 10; // / operator
    public static final int BIT_OR         = 11; // | operator
    public static final int BIT_AND        = 12; // & operator
    public static final int BIT_XOR        = 13; // ^ operator
    public static final int MOD            = 14; // % operator
    public static final int LSHIFT         = 15; // << operator
    public static final int RSHIFT         = 16; // >> operator
    public static final int RUSHIFT        = 17; // >>> operator
    public static final int PLUSASSIGN     = 18; // += operator
    public static final int SUBASSIGN      = 19; // -= operator
    public static final int MULTASSIGN     = 20; // *= operator
    public static final int DIVASSIGN      = 21; // /= operator
    public static final int ANDASSIGN      = 22; // &= operator
    public static final int ORASSIGN       = 23; // |= operator
    public static final int XORASSIGN      = 24; // ^= operator
    public static final int MODASSIGN      = 25; // %= operator
    public static final int LSHIFTASSIGN   = 26; // <<= operator
    public static final int RSHIFTASSIGN   = 27; // >>= operator
    public static final int RUSHIFTASSIGN  = 28; // >>>= operator

    // Largest operator used.
    public static final int MAX_OPERATOR   = RUSHIFTASSIGN; 
END_CLASS

dnl =================================================================
AST_NODE(`BlockStatement', `concrete', `Statement')
AST_VARS(`*A children:Statement')
NO_VISITCHILDREN

/**
 * BlockStatement
 *
 * Overview: A BlockStatement represents a Java block statement -- a 
 *   sequence of statements.
 **/
START_CLASS

  /**
   * Effects:
   *    Visits the children of this in order with <v>.  If <v> returns
   *    null, the statement is elided. 
   *
   *    If <flatten> is true, all BlockStatements have their contents
   *    contents are interpolated into this statement.
   **/
ifelse(`MUTABLE', `true', `
  public void visitChildren(NodeVisitor vis) {
    visitChildren(vis, false);
  }

  public void visitChildren(NodeVisitor vis, boolean flatten) {
    for (ListIterator it = children.listIterator(); it.hasNext(); ) {
      Node node = (Node) it.next();
      Node newNode = node.accept(v);
      if (newNode == null) {
	// Remove the node.
	it.remove();
	continue;
      } else if (flatten && newNode instanceof BlockStatement) {
	// Interpolate the subnodes.
	it.remove();
	BlockStatement bs = (BlockStatement) newNode;
	for (Iterator bsIt = bs.statements.iterator(); bsIt.hasNext(); ) {
	  it.add(bsIt.next());
	}
      } else if (node != newNode) {
	// The node changed.
	it.set(newNode);
      }
    }    
  }',`
  public Node visitChildren(NodeVisitor vis) {
    return visitChildren(vis, false);
  }

  public Node visitChildren(NodeVisitor vis, boolean flatten) {
    ArrayList newChildren = new ArrayList(children.size());
    for (ListIterator it = children.listIterator(); it.hasNext(); ) {
      Node node = (Node) it.next();
      Node newNode = node.accept(v);
      if (newNode == null) {
	// Remove the node.
	continue;
      } else if (flatten && newNode instanceof BlockStatement) {
	// Interpolate the subnodes.
	BlockStatement bs = (BlockStatement) newNode;
	for (Iterator bsIt = bs.statements.iterator(); bsIt.hasNext(); ) {
	  newChildren.add(bsIt.next());
	}
      } else {
	newChildren.add(newNode);
      }
    }    
    return merge(newChildren);
  }')
  
END_CLASS

dnl =================================================================
AST_NODE(`BranchStatement', `concrete', `Statement')
AST_VARS(`!P type:int',
         `?J label:String')

/**
 * BranchStatement
 *
 * Overview: A BranchStatement is a representation of a branch
 * statment in Java (a break or continue).  It consists of a type
 * corresponding to either break or continue and an optional label
 * specifing where to branch to.
 */
START_CLASS

  public static final int BREAK     = 0; // break statement
  public static final int CONTINUE  = 1; // continue statement
  public static final int MAX_TYPE = CONTINUE; // largest type used.

  public BranchStatement(int type) {
    this(type, null);
  }
END_CLASS

dnl =================================================================
AST_NODE(`CastExpression', `concrete', `Expression')
AST_VAR(`!A castType:TypeNode', `!A expression:Expression')

/**
 * CastExpression
 * 
 * Overview: A CastExpression is a representation of a casting
 *   operation.  It consists of an Expression being cast and a TypeNode
 *   being cast to.
 **/ 
GENERATE

dnl =================================================================
AST_NODE(`CatchBlock', `concrete', `Node')
AST_VARS(`!A formal:FormalParameter', `!A body:BlockStatement')
NO_COPY NO_VISITCHILDREN

/**
 * Overview: Represents a pair of BlockStatements and
 * FormalParameters which represent a catch block.
 **/
GENERATE

dnl =================================================================
AST_NODE(`ClassDeclarationStatement', `concrete', `Statement')
AST_VARS(`!A classNode:ClassNode')

/**
 * ClassDeclarationStatement
 *
 * Overeview: A ClassDeclarationStatement is a representation
 * of the declaration of a class.  It consists of a ClassNode
 * representing the delcared class.
 */
GENERATE

dnl =================================================================
AST_NODE(`ClassMember', `abstract', `Node')
NO_CONSTRUCTOR`'NO_COPY`'NO_MERGE

/**
 * ClassMember
 *
 * Overview: A ClassMember is a method, a field, an initializer block,
 *   or another class declaration.  It is any node that may occur directly
 *   inside a class declaration.
 **/
GENERATE

dnl =================================================================
AST_NODE(`ClassNode', `concrete', `ClassMember')
AST_VARS(`!A flags:FlagsNode', 
         `!J name:String',
         `?A superType:TypeNode',
         `*A interfaces:TypeNode',
         `*A members:ClassMember')

/**
 * ClassNode
 *
 * Overeview: A ClassDeclarationStatement is a representation
 * of the declaration of a class.  It consists of a class name, an
 * instance of AccessFlags, an optional super class, a list of
 * Interfaces, and a list of ClassMembers.
 **/
GENERATE

dnl =================================================================
AST_NODE(`ConstructorCallStatement', `concrete', `Statement')
AST_VARS(`?A primary:Expression',
         `!P type:int',
         `*A arguments:Expression')

/**
 * ConstructorCallStatement
 *
 * Overview: A ConstructorCallStatement is a representation of
 *    a direct call to a constructor of a class in the form of
 *    super(...)  or this(...).  It consists of a type of the call
 *    (either super or this) and a list of expressions to be
 *    parameters of the call.  A constructor call statement may also
 *    contain an expression providing the context in which it is
 *    executed. XXXX is this right?  should it be a type?
 **/
START_CLASS
  public static final int SUPER   = 0; 
  public static final int THIS    = 1;
END_CLASS

dnl =================================================================
AST_NODE(`DoStatement', `concrete', `Statement')
AST_VARS(`!A statement:Statement',
         `!A condition:Expression')

/**
 * DoStatement
 *
 * Overview: A representation of a Java language do
 *   statment.  Contains a statement to be executed and an expression
 *   to be tested indicating whether to reexecute the statement.
 **/ 
GENERATE

dnl =================================================================
AST_NODE(`Expression', `abstract', `Node')
NO_CONSTRUCTOR`'NO_COPY`'NO_MERGE`'NO_ACCEPT

/**
 * An expression is any Java expression.
 **/
GENERATE

dnl =================================================================
AST_NODE(`ExpressionStatement', `concrete', `Statement')
AST_VARS(`!A expresssion:Expression')

/**
 * Represents a statement containing a single expression.
 **/ 
GENERATE

dnl =================================================================
AST_NODE(`FieldExpression', `concrete', `Expression')
AST_VARS(`?A target:Node', `!J name:String')
NO_CONSTRUCTOR`'NO_SET

/**
 * Requires: <target> is either a TypeNode or an Expression, or null.
 *
 * Overview: A FieldExpression is a mutable representation of a Java field
 * access.  It consists of field name and may also have either a Type
 * or an Expression containing the field being accessed.
 **/
START_CLASS

  public FieldExpression(Node target, String name) {
    setTarget(target);
    this.name = name;
  }
  public void setName(String newName) {
    name = newName;
  }

  public void setTarget(Node target) {
    if (target != null && ! (target instanceof TypeNode ||
			     target instanceof Expression))
     throw new Error("Target of a field access must be a type or expression.");

    this.target = target;
  }

END_CLASS

dnl =================================================================
AST_NODE(`FieldNode', `concrete', `ClassMember')
AST_VARS(`!A flags:FlagsNode', `!A declare:VariableDeclarationStatement')

/**
 * FieldNode
 *
 * Overview: A FieldNode is a representation of the
 * declaration of a field of a class.  It consists of a set of
 * AccessFlags and a VariableDeclarationStatement.
 */
GENERATE

dnl =================================================================
AST_NODE(`FloatLiteral', `concrete', `Literal')
AST_VARS(`!P d:double', `!P b:isDouble')
NO_CONSTRUCTOR`'NO_GET`'NO_SET

dnl TODO: Get this right.
GENERATE

dnl =================================================================
AST_NODE(`FlagsNode', `concrete', `Node')
AST_VARS(`!J flags:AccessFlags')
import jltools.types.AccessFlags;

/**
 * This node wraps a set of access flags.
 **/
GENERATE

dnl =================================================================
AST_NODE(`ForStatement', `concrete', `Statement')
AST_VARS(`?A initializer:Statement',
         `?A condition:Expression',
         `*A incrementors:Expression',
         `?A body:Statement')

/**
 * ForStatement
 *
 * Overview: A representation of a Java language For
 *   statment.  Contains a statement to be executed and an expression
 *   to be tested indicating whether to reexecute the statement.
 **/ 
GENERATE

dnl =================================================================
AST_NODE(`FormalParameter', `concrete', `Node')
AST_VARS(`!A type:TypeNode',
         `!J name:String',
         `!P flags:FlagsNode')

/**
 * Overview: A FormalParameter is mutable representation of a pair of
 * values, a type and a variable declarator id, used as formal
 * parameters such as in method declarations and catch blocks.
 *
 * Note: Only the "final" flag is legain in current versions of Java.
 **/
GENERATE

dnl =================================================================
AST_NODE(`IfStatement', `concrete', `Statement')
AST_VARS(`!A condition:Expression',
         `!A thenStatement:Statement',
         `?A elseStatement:Statement')
/**
 * IfStatement
 *
 * Overview: A representation of a Java language if statement.
 *    Contains an expression whose value is tested, a then statement, and
 *    optionally an else statement.
 **/
GENERATE

dnl =================================================================
AST_NODE(`ImportNode', `concrete', `Node')
AST_VARS(`!P kind:int',
         `!J imports:String')

/**
 * Overview: An ImportNode is a mutable representation of a Java
 * import statement.  It consists of the string representing the item
 * being imported and a type which is either indicating that a class
 * is being imported, or that an entire package is being imported.
 **/
START_CLASS
  /** Indicates that a single class is being imported. */
  public static final int CLASS = 0;
  /** Indicates that an entire package is being imported. */
  public static final int PACKAGE = 1;
END_CLASS

dnl =================================================================
AST_NODE(`InitializerBlock', `concrete', `ClassMember')
AST_VARS(`!A body:BlockStatement',
         `!P isStatic:boolean')

/**
 * Overview: An InitializerBlock is a representation of an
 * initializer block in a Java class (which appears outside of any
 * method).  Such a block is executed before the code for any of the
 * constructors.  Such a block can optionally be static, in which case
 * it is executed when the class is created.  
 **/
GENERATE

dnl =================================================================
AST_NODE(`InstanceofExpression', `concrete', `Expression')
AST_VARS(`!A expr:Expression',
         `!A type:TypeNode')

/**
 * InstanceofExpression
 *
 * Overview: An InstanceofExpression is a representation of
 *   the use of the instanceof operator in Java such as "<expression>
 *   instanceof <type>".
 **/
GENERATE

dnl =================================================================

dnl TODO: IntegerLiteral. (Get it right!)

dnl =================================================================

