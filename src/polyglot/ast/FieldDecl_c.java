package jltools.ext.jl.ast;

import jltools.ast.*;

import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;
import java.util.*;

/**
 * A <code>FieldDecl</code> is an immutable representation of the declaration
 * of a field of a class.
 */
public class FieldDecl_c extends Node_c implements FieldDecl
{
  Declarator decl;
  FieldInstance fi;
  InitializerInstance ii;

  public FieldDecl_c(Ext ext, Position pos, Flags flags, TypeNode type, String name, Expr init) {
    super(ext, pos);
    this.decl = new Declarator_c(flags, type, name, init);
  }

  /** Get the initializer instance of the initializer. */
  public InitializerInstance initializerInstance() {
    return ii;
  }

  /** Set the initializer instance of the initializer. */
  public FieldDecl initializerInstance(InitializerInstance ii) {
    FieldDecl_c n = (FieldDecl_c) copy();
    n.ii = ii;
    return n;
  }

  /** Get the type of the declaration. */
  public Type declType() {
    return decl.declType();
  }

  /** Get the flags of the declaration. */
  public Flags flags() {
    return decl.flags();
  }

  /** Set the flags of the declaration. */
  public FieldDecl flags(Flags flags) {
    FieldDecl_c n = (FieldDecl_c) copy();
    n.decl = decl.flags(flags);
    return n;
  }

  /** Get the type node of the declaration. */
  public TypeNode type() {
    return decl.type();
  }

  /** Set the type of the declaration. */
  public FieldDecl type(TypeNode type) {
    FieldDecl_c n = (FieldDecl_c) copy();
    n.decl = decl.type(type);
    return n;
  }

  /** Get the name of the declaration. */
  public String name() {
    return decl.name();
  }

  /** Set the name of the declaration. */
  public FieldDecl name(String name) {
    FieldDecl_c n = (FieldDecl_c) copy();
    n.decl = decl.name(name);
    return n;
  }

  /** Get the initializer of the declaration. */
  public Expr init() {
    return decl.init();
  }

  /** Set the initializer of the declaration. */
  public FieldDecl init(Expr init) {
    FieldDecl_c n = (FieldDecl_c) copy();
    n.decl = decl.init(init);
    return n;
  }

  /** Set the field instance of the declaration. */
  public FieldDecl fieldInstance(FieldInstance fi) {
    FieldDecl_c n = (FieldDecl_c) copy();
    n.fi = fi;
    return n;
  }

  /** Get the field instance of the declaration. */
  public FieldInstance fieldInstance() {
    return fi;
  }

  /**
   * Get the declarator.
   */
  protected Declarator decl() {
    return decl;
  }

  /**
   * Set the declarator.
   */
  protected FieldDecl decl(Declarator decl) {
    FieldDecl_c n = (FieldDecl_c) copy();
    n.decl = decl;
    return n;
  }

  /** Reconstruct the declaration. */
  protected FieldDecl_c reconstruct(TypeNode type, Expr init) {
    if (type() != type || init() != init) {
      FieldDecl_c n = (FieldDecl_c) copy();
      n.decl = (Declarator_c) decl.copy();
      n.decl = n.decl.type(type);
      n.decl = n.decl.init(init);
      return n;
    }

    return this;
  }

  /** Visit the children of the declaration. */
  public Node visitChildren(NodeVisitor v) {
    TypeNode type = (TypeNode) visitChild(type(), v);
    Expr init = (Expr) visitChild(init(), v);
    return reconstruct(type, init);
  }

  public Node buildTypesEnter_(TypeBuilder tb) throws SemanticException {
      tb.pushScope();
      return this;
  }

  public Node buildTypes_(TypeBuilder tb) throws SemanticException {
      tb.popScope();

      TypeSystem ts = tb.typeSystem();

      FieldDecl n;

      if (init() != null) {
          ClassType ct = tb.currentClass();
          Flags f = (flags().isStatic()) ? Flags.STATIC : Flags.NONE;
          InitializerInstance ii = ts.initializerInstance(init().position(),
                                                          ct, f);
          n = initializerInstance(ii);
      }
      else {
          n = this;
      }

      FieldInstance fi = ts.fieldInstance(n.position(), ts.Object(), Flags.NONE,
                                          ts.unknownType(position()), n.name());

      return n.fieldInstance(fi);
  }

  /** Build type objects for the declaration. */
  public Node disambiguateEnter_(AmbiguityRemover ar) throws SemanticException {
      if (ar.kind() == AmbiguityRemover.SUPER) {
          return bypassChildren();
      }
      else if (ar.kind() == AmbiguityRemover.SIGNATURES) {
          if (init() != null) {
              return init((Expr) init().bypass(true));
          }
      }

      return this;
  }

  public Node disambiguate_(AmbiguityRemover ar) throws SemanticException {
    if (ar.kind() == AmbiguityRemover.SIGNATURES) {
      Context c = ar.context();
      TypeSystem ts = ar.typeSystem();

      ParsedClassType ct = c.currentClass();

      FieldInstance fi = ts.fieldInstance(position(),
                                          ct, flags(), declType(), name());

      Flags flags = flags();

      if (ct.flags().isInterface()) {
        flags = flags.setPublic();
        flags = flags.setStatic();
        flags = flags.setFinal();
      }

      if (init() instanceof Lit && flags().isFinal()) {
        Object value = ((Lit) init()).objValue();
        fi = (FieldInstance) fi.constantValue(value);
      }

      return flags(fi.flags()).fieldInstance(fi);
    }

    return this;
  }

  public Node addMembersEnter_(AddMemberVisitor am) {
    ParsedClassType ct = am.context().currentClass();
    if (fi == null) {
        throw new InternalCompilerError("null field instance");
    }
    ct.addField(fi);
    return bypassChildren();
  }

  public void enterScope(Context c) {
      if (ii != null) {
          c.pushCode(ii);
      }
  }

  public void leaveScope(Context c) {
      if (ii != null) {
          c.popCode();
      }
  }

  /** Type check the declaration. */
  public Node typeCheck_(TypeChecker tc) throws SemanticException {
    TypeSystem ts = tc.typeSystem();

    try {
      ts.checkFieldFlags(flags());
    }
    catch (SemanticException e) {
      throw new SemanticException(e.getMessage(), position());
    }

    if (tc.context().currentClass().flags().isInterface()) {
        if (flags().isProtected() || flags().isPrivate()) {
            throw new SemanticException("Interface members must be public.",
                                        position());
        }
    }

    decl.typeCheck(tc);

    return this;
  }

  public Expr setExpectedType_(Expr child, ExpectedTypeVisitor tc)
      throws SemanticException
  {
      return decl.setExpectedType(child, tc);
  }

  public String toString() {
    return decl.toString();
  }

  public void translate_(CodeWriter w, Translator tr) {
    decl.translate(w, tr, true);
    w.write(";");
    w.newline(0);
  }

  public void dump(CodeWriter w) {
    super.dump(w);

    if (fi != null) {
      w.allowBreak(4, " ");
      w.begin(0);
      w.write("(instance " + fi + ")");
      w.end();
    }

    w.allowBreak(4, " ");
    w.begin(0);
    w.write("(declarator " + decl + ")");
    w.end();
  }
}
