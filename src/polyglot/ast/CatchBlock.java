package jltools.ast;

import jltools.util.*;
import jltools.types.*;
import jltools.visit.*;


/**
 * A <code>CatchBlock</code> represents one half of a <code>try... catch</code>
 * statement. Specifically, the second half. The example below demonstrates a
 * catch block with parameter <code>ioe</code> of type <code>IOException</code>
 * that prints out the stack trace of the exception.
 * <pre><code>
 * ...
 * catch( IOException ioe) 
 * {
 *   ioe.printStackTrace();
 * }
 * </code></pre>
 */
public class CatchBlock extends Node 
{
  protected final FormalParameter fp;
  protected final BlockStatement block;
  
  /**
   * Creates a new <code>CatchBlock</code> to 
   * <formalParameter> and BlockStatement <block>.
   */
  public CatchBlock( Node ext, FormalParameter fp, BlockStatement block) 
  {
    this.ext = ext;
    this.fp = fp;
    this.block = block;
  }

    public CatchBlock( FormalParameter fp, BlockStatement block) {
	this(null, fp, block);
    }

  /**
   * Lazily reconstruct this node.
   * <p>
   * If the arguments are pointer identical the fields of the current node,
   * then the current node is returned untouched. Otherwise a new node is
   * constructed with the new fields and all annotations from this node are
   * copied over.
   *
   * @param fp The new type and name of the expression to be caught.
   * @param block The set of statements to be executed if the given 
   *  throwable is caught.
   * @return An <code>CatchBlock<code> with the given parameter and block of
   *  statements.
   */
  public CatchBlock reconstruct( Node ext, FormalParameter fp, BlockStatement block)
  {
    if( this.fp == fp && this.block == block && this.ext == ext) {
      return this;
    }
    else {
      CatchBlock n = new CatchBlock( ext, fp, block);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }
  
    public CatchBlock reconstruct( FormalParameter fp, BlockStatement block) {
	return reconstruct(this.ext, fp, block);
    }

  /**
   * Returns the <code>FormalParameter</code> associated with this 
   * <code>CatchBlock</code>.
   */
  public FormalParameter getFormalParameter() 
  {
    return fp;
  }
 
  /**
   * Returns the type of the <code>FormalParameter</code> which this
   * <code>CatchBlock</code> catches.
   */
  public Type getCatchType()
  {
    return fp.getParameterType();
  }

  /**
   * Returns the <code>BlockStatement</code> for this <code>catch</code>
   * statement.
   */
  public BlockStatement getBlockStatement() 
  {
    return block;
  }

  /* 
   * Visit the children of this node.
   *
   * @pre Required that <code>fp.visit</code> returns an object of type
   *  <code>FormalParameter</code> and that <code>block.visit</code> returns
   *  an object of type <code>BlockStatement</code>.
   * @post Returns <code>this</code> if no changes are made, otherwise a copy
   *  is made and returned.
   */
  public Node visitChildren( NodeVisitor v) 
  {
    return reconstruct( Node.condVisit(this.ext, v), (FormalParameter)fp.visit( v),
                        (BlockStatement)block.visit( v));
  }
  
  public Node readSymbols( SymbolReader sr)
  {
    return this;
  }

  public void enterScope( LocalContext c)
  {
    c.pushBlock();
  }

  public void leaveScope( LocalContext c)
  {
    c.popBlock();
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    ClassType throwable = (ClassType)c.getTypeSystem().getThrowable();

    if( !fp.getParameterType().descendsFrom( throwable) &&
          !fp.getParameterType().equals( throwable)) {
      throw new SemanticException(
                "Can only catch objects whose type descends from Throwable.");
    }
    return this;
  }

  // FIXME implement flowCheck

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write("catch(");
    fp.translate_block(c, w);
    w.write(")");

    enterScope(c);
    block.translate_substmt(c, w);
    leaveScope(c);
  }
  
  public void dump( CodeWriter w)
  {
    w.write( "CATCH BLOCK ");
    dumpNodeInfo( w);
  }
}
