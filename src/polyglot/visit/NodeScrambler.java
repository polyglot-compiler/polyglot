package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;

import java.util.*;

// XXX document me
public class NodeScrambler extends NodeVisitor
{
  public FirstPass fp;

  HashMap pairs;
  LinkedList currentParents;
  boolean scrambled = false;
  CodeWriter cw;

  public NodeScrambler()
  {
    this.fp = new FirstPass();

    this.pairs = new HashMap();
    this.currentParents = new LinkedList();
    this.cw = new CodeWriter( System.err, 72);
  }

  public class FirstPass extends NodeVisitor 
  {
    public NodeVisitor enter( Node n)
    {
      pairs.put( n, currentParents.clone());
      
      currentParents.add( n);
      return this;
    }
    
    public Node leave( Node old, Node n, NodeVisitor v)
    {
      currentParents.remove( n);
      return n;
    }
  }

  public Node override( Node n)
  {
    if( coinFlip()) {
      Node m = potentialScramble( n);
      if( m == null) {
        /* No potential replacement. */
        return null;
      }
      else {
        scrambled = true;

        try {
          System.err.println( "Replacing:");
          n.dump( cw);
          cw.newline();
          cw.flush();
          System.err.println( "With:");
          m.dump( cw);
          cw.newline();
          cw.flush();
        }
        catch( Exception e)
        {
          e.printStackTrace();
          return null;
        }
        return m;
      }
    }
    else {
      return null;
    }  
  }

  protected boolean coinFlip()
  {
    if( scrambled) {
      return false;
    }
    else {
      if( Math.random() > 0.9) {
        return true;
      }
      else {
        return false;
      }
    }
  }

  protected Node potentialScramble( Node n)
  {
    Class required = Node.class;

    if( n instanceof SourceFileNode) {
      return null;
    }
    if( n instanceof ImportNode) {
      required = ImportNode.class;
    }
    else if( n instanceof TypeNode) {
      required = TypeNode.class;
    }
    else if( n instanceof ClassNode) {
      required = ClassNode.class;
    }
    else if( n instanceof ClassMember) {
      required = ClassMember.class;
    }
    else if( n instanceof FormalParameter) {
      required = FormalParameter.class;
    }
    else if( n instanceof Expression) {
      required = Expression.class;
    }
    else if( n instanceof BlockStatement) {
      required = BlockStatement.class;
    }
    else if( n instanceof CatchBlock) {
      required = CatchBlock.class;
    }
    else if( n instanceof VariableDeclarationStatement) {
      required = VariableDeclarationStatement.class;
    }
    else if( n instanceof Statement) {
      required = Statement.class;
    }

    LinkedList parents = (LinkedList)pairs.get( n);
    Iterator iter1 = pairs.keySet().iterator(), iter2;
    boolean isParent;

    while( iter1.hasNext()) {
      Node m = (Node)iter1.next();
      if( required.isAssignableFrom( m.getClass())) {

        isParent = false;
        iter2 = parents.iterator();
        while( iter2.hasNext()) {
          if( m == iter2.next()) {
            isParent = true;
          }
        }

        if( !isParent && m != n) {
          return m;
        }
      }
    }

    return null;
  }
}
