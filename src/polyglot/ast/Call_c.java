package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import java.util.*;

/**
 * A <code>Call</code> is an immutable representation of a Java
 * method call.  It consists of a method name and a list of arguments.
 * It may also have either a Type upon which the method is being
 * called or an expression upon which the method is being called.
 */
public class Call_c extends Expr_c implements Call
{
  protected Receiver target;
  protected String name;
  protected List arguments;
  protected MethodInstance mi;

  public Call_c(Ext ext, Position pos, Receiver target, String name,
                List arguments) {
    super(ext, pos);
    this.target = target;
    this.name = name;
    this.arguments = TypedList.copyAndCheck(arguments, Expr.class, true);
  }

  /** Get the precedence of the call. */
  public Precedence precedence() {
    return Precedence.LITERAL;
  }

  /** Get the target object or type of the call. */
  public Receiver target() {
    return this.target;
  }

  /** Set the target object or type of the call. */
  public Call target(Receiver target) {
    Call_c n = (Call_c) copy();
    n.target = target;
    return n;
  }

  /** Get the name of the call. */
  public String name() {
    return this.name;
  }

  /** Set the name of the call. */
  public Call name(String name) {
    Call_c n = (Call_c) copy();
    n.name = name;
    return n;
  }

  /** Get the method instance of the call. */
  public MethodInstance methodInstance() {
    return this.mi;
  }

  /** Set the method instance of the call. */
  public Call methodInstance(MethodInstance mi) {
    Call_c n = (Call_c) copy();
    n.mi = mi;
    return n;
  }

  /** Get the arguments of the call. */
  public List arguments() {
    return this.arguments;
  }

  /** Set the arguments of the call. */
  public Call arguments(List arguments) {
    Call_c n = (Call_c) copy();
    n.arguments = TypedList.copyAndCheck(arguments, Expr.class, true);
    return n;
  }

  /** Reconstruct the call. */
  protected Call_c reconstruct(Receiver target, List arguments) {
    if (target != this.target || ! CollectionUtil.equals(arguments,
                                                         this.arguments)) {
      Call_c n = (Call_c) copy();
      n.target = target;
      n.arguments = TypedList.copyAndCheck(arguments, Expr.class, true);
      return n;
    }

    return this;
  }

  /** Visit the children of the call. */
  public Node visitChildren(NodeVisitor v) {
    Receiver target = null;

    if (this.target != null) {
      target = (Receiver) this.target.visit(v);
    }

    List arguments = new ArrayList(this.arguments.size());
    for (Iterator i = this.arguments.iterator(); i.hasNext(); ) {
      Expr n = (Expr) i.next();
      n = (Expr) n.visit(v);
      arguments.add(n);
    }

    return reconstruct(target, arguments);
  }

  /** Type check the call. */
  public Node typeCheck_(TypeChecker tc) throws SemanticException {
    TypeSystem ts = tc.typeSystem();
    Context c = tc.context();

    ReferenceType targetType = null;

    /* By default, we're not in a static context.  But if the
     * target of the method is a type name, or if the target isn't
     * specified, and we're inside a static method, then we're
     * in a static context. */
    boolean staticContext = false;

    if (target instanceof TypeNode) {
      TypeNode tn = (TypeNode) target;
      Type t = tn.type();

      staticContext = true;

      if (t.isReference()) {
        targetType = t.toReference();
      } else {
        throw new SemanticException("Cannot invoke static method \"" + name
                                    + "\" on non-reference type " + t + ".",
                                    tn.position());
      }
    } else if (target instanceof Expr) {
      Expr e = (Expr) target;

      if (e.type().isReference()) {
        targetType = e.type().toReference();
      } else {
        throw new SemanticException("Cannot invoke method \"" + name + "\" on "
                                    + "an expression of non-reference type "
                                    + e.type() + ".", e.position());
      }
    } else if (target != null) {
      throw new SemanticException("Receiver of method invocation must be a "
                                  + "reference type.",
                                  target.position());
    } else { // target == null
      CodeInstance ci = c.currentCode();
      if (ci.flags().isStatic()) {
        staticContext = true;
      }
    }

    List argTypes = new ArrayList(arguments.size());

    for (Iterator i = arguments.iterator(); i.hasNext(); ) {
      Expr e = (Expr) i.next();
      argTypes.add(e.type());
    }

    MethodInstance mi;

    if (targetType != null) {
      mi = ts.findMethod(targetType, name, argTypes, c);
    }
    else {
      mi = c.findMethod(name, argTypes);
    }

    if (staticContext && !mi.flags().isStatic()) {
      Type containingClass;
      if (targetType == null) {
        containingClass = c.findMethodScope(name);
      } else {
        containingClass = targetType;
      }
      throw new SemanticException("Cannot call non-static method " + name
                                  + " of " + containingClass + " in static "
                                  + "context.");
    }

    // If we found a method, the call must type check, so no need to check
    // the arguments here.

    // Now we should set the target if it is not null.
    Call call;

    if (target == null) {
      Receiver r;

      NodeFactory nf = tc.nodeFactory();

      if (mi.flags().isStatic()) {
        r = nf.CanonicalTypeNode(position(), ts.staticTarget(mi.container()));
      } else {
        // The field is non-static, so we must prepend with "this", but we
        // need to determine if the "this" should be qualified.  Get the
        // enclosing class which brought the method into scope.  This is
        // different from fi.container().  fi.container() returns a super type
        // of the class we want.
        ParsedClassType scope = c.findMethodScope(name);

        if (! scope.isSame(c.currentClass())) {
          r = nf.This(position(),
                      nf.CanonicalTypeNode(position(),
                                           ts.staticTarget(scope)));
        }
        else {
          r = nf.This(position());
        }
      }

      r = (Receiver) r.visit(tc);

      call = target(r);
    }
    else {
      call = this;
    }

    return call.methodInstance(mi).type(mi.returnType());
  }

  /** Check exceptions thrown by the call. */
  public Node exceptionCheck_(ExceptionChecker ec) throws SemanticException {
    if (mi == null) {
      throw new InternalCompilerError(position(),
                                      "Null method instance after type "
                                      + "check.");
    }

    for (Iterator i = mi.exceptionTypes().iterator(); i.hasNext();) {
      Type t = (Type) i.next();
      ec.throwsException(t);
    }

    // We may throw a null pointer exception except when the target
    // is "this" or "super".
    TypeSystem ts = ec.typeSystem();

    if (target instanceof Expr && ! (target instanceof Special)) {
      ec.throwsException(ts.NullPointerException());
    }

    return this;
  }

  public String toString() {
    return (target != null ? target.toString() + "." : "") + name + "(...)";
  }
	
  /** Write the expression to an output file. */
  public void translate_(CodeWriter w, Translator tr) {
    if (target instanceof Expr) {
      translateSubexpr((Expr) target, w, tr);
      w.write(".");
    }
    else if (target != null) {
      target.ext().translate(w, tr);
      w.write(".");
    }

    w.write(name + "(");
    w.begin(0);
		
    for(Iterator i = arguments.iterator(); i.hasNext();) {
      Expr e = (Expr) i.next();
      e.ext().translate(w, tr);

      if (i.hasNext()) {
        w.write(",");
        w.allowBreak(0, " ");
      }
    }

    w.end();
    w.write(")");
  }

  /** Dumps the AST. */
  public void dump(CodeWriter w) {
    super.dump(w);

    if ( mi != null ) {
      w.allowBreak(4, " ");
      w.begin(0);
      w.write("(instance " + mi + ")");
      w.end();
    }

    w.allowBreak(4, " ");
    w.begin(0);
    w.write("(name " + name + ")");
    w.end();

    w.allowBreak(4, " ");
    w.begin(0);
    w.write("(arguments " + arguments + ")");
    w.end();
  }
}
