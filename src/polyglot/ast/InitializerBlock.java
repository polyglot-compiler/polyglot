package jltools.ast;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;


/**
 * An <code>InitializerBlock</code> is an immutable representation of an
 * initializer block in a Java class (which appears outside of any
 * method).  Such a block is executed before the code for any of the
 * constructors.  Such a block can optionally be static, in which case
 * it is executed when the class is loaded.  
 */
public class InitializerBlock extends ClassMember 
{
  protected final BlockStatement block;
  protected final boolean isStatic;

  MethodTypeInstance mtiThis;

  /**
   * Creates a new <code>InitializerBlock</code>.
   */
  public InitializerBlock( BlockStatement block, boolean isStatic) 
  {
    this.block = block;
    this.isStatic = isStatic;
  }

  /**
   * Lazily reconstruct this node.
   */
  public InitializerBlock reconstruct( BlockStatement block, boolean isStatic)
  {
    if( this.block == block && this.isStatic == isStatic) {
      return this;
    }
    else {
      InitializerBlock n = new InitializerBlock( block, isStatic);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  /**
   * Returns true iff this block contains static code.
   */
  public boolean isStatic() 
  {
    return isStatic;
  }

  /**
   * Returns the <code>BlockStatement</code> which comprises this
   * <code>InitializerBlock</code>.
   */
  public BlockStatement getBlock() 
  {
    return block;
  }
    
  public Node visitChildren( NodeVisitor v) 
  {
    return reconstruct( (BlockStatement)block.visit( v), isStatic);
  }

  public void enterScope( LocalContext c)
  {
    c.enterMethod( new MethodTypeInstanceInitializer( c.getTypeSystem(), 
                                                      c.getCurrentClass(), 
                                                      isStatic) );
  }

  public void leaveScope( LocalContext c)
  {
    c.leaveMethod();
  }

  public Node readSymbols( SymbolReader sr)
  {
    return this;
  }

  public Node typeCheck( LocalContext c)
  {
    // FIXME; implement
    return this;
  }

  public Node exceptionCheck (ExceptionChecker ec ) 
  {
    SubtypeSet s = (SubtypeSet)ec.getThrowsSet();
    if (s.size() != 0 )
      ec.reportError("An initializer block may not throw"+ 
                     " any exceptions.", Annotate.getLineNumber(this));
    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write("{");
    w.newline(4);
    block.translate_block( c, w);
    w.newline(0);
    w.write("}");
  }

  public void dump( CodeWriter w)
  {
    w.write( "( INITIALIZER BLOCK ");
    if( isStatic) {
      w.write( "< static > ");
    }
    dumpNodeInfo( w);
    w.write( ")");
  }
}


  
