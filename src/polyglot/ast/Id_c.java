/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2006-2007 Polyglot project group, Cornell University
 * Copyright (c) 2006-2007 IBM Corporation
 * 
 */

package polyglot.ast;

import polyglot.visit.*;
import polyglot.util.*;

/**
 * An <code>AmbExpr</code> is an ambiguous AST node composed of a single
 * identifier that must resolve to an expression.
 */
public class Id_c extends Node_c implements Id
{
  protected String id;

  public Id_c(Position pos, String id) {
    super(pos);
    assert(id != null);
    assert(StringUtil.isNameShort(id));
    this.id = id;
  }

  // Override to make Name.equals(String) a compile-time error
  public final void equals(String s) { }

  /** Get the name of the expression. */
  public String id() {
    return this.id;
  }

  /** Set the name of the expression. */
  public Id id(String id) {
    Id_c n = (Id_c) copy();
    n.id = id;
    return n;
  }

  /** Write the name to an output file. */
  public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
    w.write(id);
  }

  public String toString() {
    return id;
  }

  public void dump(CodeWriter w) {
    w.write("(Id \"" + id + "\")");
  }
  
  public Node copy(NodeFactory nf) {
      return nf.Id(this.position, this.id);
  }

}