package jltools.ast;

import jltools.util.*;
import jltools.types.*;


/**
 * FIXME
 */
public class ClassDeclarationStatement extends Statement 
{
  protected final ClassNode classNode;

  /**
   * Creates a new <code>ClassDeclarationStatement</code> for the class
   * defined in <code>ClassNode</code>.
   */
  public ClassDeclarationStatement(Node ext, ClassNode classNode) 
  {
    this.ext = ext;
    this.classNode = classNode;
  }

    public ClassDeclarationStatement(ClassNode classNode) {
	this(null, classNode);
    }

  /**
   * Lazily reconstruct this node.
   * <p>
   * If the arguments are pointer identical the fields of the current node,
   * then the current node is returned untouched. Otherwise a new node is
   * constructed with the new fields and all annotations from this node are
   * copied over.
   *
   * @param classNode The new class definition.
   * @return An <code>ClassDeclarationStatement<code> defining the given class.
   */
  public ClassDeclarationStatement reconstruct( Node ext, ClassNode classNode) 
  {
    if( this.classNode == classNode && this.ext == ext) {
      return this;
    }
    else {
      ClassDeclarationStatement n = new ClassDeclarationStatement( ext, classNode);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

    public ClassDeclarationStatement reconstruct( ClassNode classNode) {
	return reconstruct(this.ext, classNode);
    }

  /**
   * Returns the <code>ClassNode</code> declared by <code>this</code>.
   */
  public ClassNode getClassNode() 
  {
    return classNode;
  }

  /**
   * Visit the child of this node.
   *
   * @pre Requires that <code>classNode.visit</code> returns an object of 
   *  type <code>ClassNode</code>.
   * @post Returns <code>this</code> if the result of 
   *  <code>classNode.visit</code> is pointer identical to 
   *  <code>classNode</code>. Otherwise returns a new 
   *  <code>ClassDeclarationStatement</code> which defines the new class.
   */
  public Node visitChildren( NodeVisitor v) 
  {
    return reconstruct( Node.condVisit(this.ext, v), (ClassNode)classNode.visit( v));
  }

  public Node typeCheck( LocalContext c)
  {
    // FIXME: implement;
    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    classNode.translate( c, w);
  }
  
  public void dump( CodeWriter w)
  {
    w.write( "( CLASS DECLARATION");
    dumpNodeInfo( w);
    w.write( ")");
  }
}
