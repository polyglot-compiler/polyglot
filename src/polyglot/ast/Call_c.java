package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
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

  public Call_c(Position pos, Receiver target, String name,
                List arguments) {
    super(pos);
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

  public ProcedureInstance procedureInstance() {
      return methodInstance();
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

  /** Get the actual arguments of the call. */
  public List arguments() {
    return this.arguments;
  }

  /** Set the actual arguments of the call. */
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
    Receiver target = (Receiver) visitChild(this.target, v);
    List arguments = visitList(this.arguments, v);
    return reconstruct(target, arguments);
  }

  public Node buildTypes(TypeBuilder tb) throws SemanticException {
    Call_c n = (Call_c) super.buildTypes(tb);

    TypeSystem ts = tb.typeSystem();

    List l = new ArrayList(arguments.size());
    for (int i = 0; i < arguments.size(); i++) {
      l.add(ts.unknownType(position()));
    }

    MethodInstance mi = ts.methodInstance(position(), ts.Object(),
                                          Flags.NONE,
                                          ts.unknownType(position()),
                                          name, l,
                                          Collections.EMPTY_LIST);
    return n.methodInstance(mi);
  }

  /** Type check the call. */
  public Node typeCheck(TypeChecker tc) throws SemanticException {
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
        r = nf.CanonicalTypeNode(position(), mi.container()).type(mi.container());

      } else {
        // The field is non-static, so we must prepend with "this", but we
        // need to determine if the "this" should be qualified.  Get the
        // enclosing class which brought the method into scope.  This is
        // different from fi.container().  fi.container() returns a super type
        // of the class we want.
        ClassType scope = c.findMethodScope(name);

        if (! ts.equals(scope, c.currentClass())) {
          r = nf.This(position(),
                      nf.CanonicalTypeNode(position(), scope)).type(scope);
        }
        else {
          r = nf.This(position()).type(scope);
        }
      }

      call = target(r);
    }
    else {
      call = this;
    }

    return call.methodInstance(mi).type(mi.returnType());
  }

  public Type childExpectedType(Expr child, AscriptionVisitor av)
  {
      if (child == target) {
          return mi.container();
      }

      Iterator i = this.arguments.iterator();
      Iterator j = mi.formalTypes().iterator();

      while (i.hasNext() && j.hasNext()) {
          Expr e = (Expr) i.next();
          Type t = (Type) j.next();

          if (e == child) {
              return t;
          }
      }

      return child.type();
  }

  public String toString() {
    String s = (target != null ? target.toString() + "." : "") + name + "(";

    int count = 0;

    for (Iterator i = arguments.iterator(); i.hasNext(); ) {
        if (count++ > 2) {
            s += "...";
            break;
        }

        Expr n = (Expr) i.next();
        s += n.toString();

        if (i.hasNext()) {
            s += ", ";
        }
    }

    s += ")";
    return s;
  }

  /** Write the expression to an output file. */
  public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
    if (target instanceof Expr) {
      printSubExpr((Expr) target, w, tr);
      w.write(".");
    }
    else if (target != null) {
      print(target, w, tr);
      w.write(".");
    }

    w.write(name + "(");
    w.begin(0);
		
    for(Iterator i = arguments.iterator(); i.hasNext();) {
      Expr e = (Expr) i.next();
      print(e, w, tr);

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

  public Term entry() {
      if (target instanceof Expr) {
          return ((Expr) target).entry();
      }
      return listEntry(arguments, this);
  }

  public List acceptCFG(CFGBuilder v, List succs) {
      if (target instanceof Expr) {
          Expr t = (Expr) target;
          v.visitCFG(t, listEntry(arguments, this));
      }

      v.visitCFGList(arguments, this);

      return succs;
  }

  /** Check exceptions thrown by the call. */
  public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
    if (mi == null) {
      throw new InternalCompilerError(position(),
                                      "Null method instance after type "
                                      + "check.");
    }

    return super.exceptionCheck(ec);
  }


  public List throwTypes(TypeSystem ts) {
    List l = new LinkedList();

    l.addAll(mi.throwTypes());
    l.addAll(ts.uncheckedExceptions());

    if (target instanceof Expr && ! (target instanceof Special)) {
      l.add(ts.NullPointerException());
    }

    return l;
  }
}
