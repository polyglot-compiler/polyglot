/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2007 Polyglot project group, Cornell University
 * Copyright (c) 2006-2007 IBM Corporation
 * 
 */

package polyglot.ast;

import polyglot.ast.Local;
import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ast.Precedence;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.VarInstance;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;
import polyglot.visit.CFGBuilder;
import java.util.List;

/** 
 * A local variable expression.
 */
public class Local_c extends Expr_c implements Local
{
  protected Id name;
  protected LocalInstance li;

  public Local_c(Position pos, Id name) {
    super(pos);
    assert(name != null);
    this.name = name;
  }

  /** Get the precedence of the local. */
  public Precedence precedence() { 
    return Precedence.LITERAL;
  }

  /** Get the name of the local. */
  public Id id() {
    return this.name;
  }
  
  /** Set the name of the local. */
  public Local id(Id name) {
      Local_c n = (Local_c) copy();
      n.name = name;
      return n;
  }
  
  /** Get the name of the local. */
  public String name() {
      return this.name.id();
  }
  
  /** Set the name of the local. */
  public Local name(String name) {
      return id(this.name.id(name));
  }

  /** Return the access flags of the variable. */
  public Flags flags() {
    return li.flags();
  }

  /** Get the local instance of the local. */
  public VarInstance varInstance() {
    return li;
  }

  /** Get the local instance of the local. */
  public LocalInstance localInstance() {
    return li;
  }

  /** Set the local instance of the local. */
  public Local localInstance(LocalInstance li) {
    if (li == this.li) return this;
    Local_c n = (Local_c) copy();
    n.li = li;
    return n;
  }

  /** Reconstruct the expression. */
  protected Local_c reconstruct(Id name) {
      if (name != this.name) {
          Local_c n = (Local_c) copy();
          n.name = name;
          return n;
      }
      
      return this;
  }
  
  /** Visit the children of the constructor. */
  public Node visitChildren(NodeVisitor v) {
      Id name = (Id) visitChild(this.name, v);
      return reconstruct(name);
  }

  public Node buildTypes(TypeBuilder tb) throws SemanticException {
      Local_c n = (Local_c) super.buildTypes(tb);

      TypeSystem ts = tb.typeSystem();

      LocalInstance li = ts.localInstance(position(), Flags.NONE,
                                          ts.unknownType(position()), name.id());
      return n.localInstance(li);
  }

  /** Type check the local. */
  public Node typeCheck(TypeChecker tc) throws SemanticException {
    Context c = tc.context();
    LocalInstance li = c.findLocal(name.id());
    
    // if the local is defined in an outer class, then it must be final
    if (!c.isLocal(li.name())) {
        // this local is defined in an outer class
        if (!li.flags().isFinal()) {
            throw new SemanticException("Local variable \"" + li.name() + 
                    "\" is accessed from an inner class, and must be declared " +
                    "final.",
                    this.position());                     
        }
    }
    
    return localInstance(li).type(li.type());
  }

  /**
   * Return the first (sub)term performed when evaluating this
   * term.
   */
  public Term entry() {
      return this;
  }

  /**
   * Visit this term in evaluation order.
   */
  public List acceptCFG(CFGBuilder v, List succs) {
      return succs;
  }

  public String toString() {
    return name.toString();
  }

  /** Write the local to an output file. */
  public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
      tr.print(this, name, w);
  }

  /** Dumps the AST. */
  public void dump(CodeWriter w) {
    super.dump(w);

    if (li != null) {
	w.allowBreak(4, " ");
	w.begin(0);
	w.write("(instance " + li + ")");
	w.end();
    }
  }
  
  public boolean constantValueSet() {
      return li != null && li.constantValueSet();
  }

  public boolean isConstant() {
    return li != null && li.isConstant();
  }

  public Object constantValue() {
    if (! isConstant()) return null;
    return li.constantValue();
  }
  
  public Node copy(NodeFactory nf) {
      return nf.Local(this.position, this.name);
  }


}