package jltools.visit;

import jltools.ast.*;
import java.util.*;

/**
 * The <code>BlockFlattener</code> runs over the AST and, as the name
 * suggests, flattens blocks whenever possible.
 **/
public class BlockFlattener extends NodeVisitor {

  protected NodeFactory nf;

  /**
   * Creates a visitor for flattening blocks.
   *
   * @param job  The job in which this visitor is being executed.
   **/
  public BlockFlattener(NodeFactory nf) {
    this.nf = nf;
  }

  public Node leave( Node old, Node n, NodeVisitor v ) {
    if ( !(n instanceof Block || n instanceof Labeled) ) {
      return n;
    }

    // If we have a labeled block consisting of just one statement, then
    // flatten the block and label the statement instead.
    if ( n instanceof Labeled ) {
      Labeled l = (Labeled)n;
      if ( !(l.statement() instanceof Block) ) {
        return n;
      }

      Block b = (Block)l.statement();
      if ( b.statements().size() != 1 ) {
        return n;
      }

      return nf.Labeled( l.position(), l.label(),
                         (Stmt)b.statements().get(0) );
    }

    // Flatten any blocks that may be contained in this one.
    Block b = (Block)n;
    List stmtList = new LinkedList();
    for ( Iterator it = b.statements().iterator(); it.hasNext(); ) {
      Stmt stmt = (Stmt)it.next();
      if ( stmt instanceof Block ) {
        stmtList.addAll( ((Block)stmt).statements() );
      } else {
        stmtList.add( stmt );
      }
    }

    if ( b instanceof SwitchBlock ) {
      return nf.SwitchBlock( b.position(), stmtList );
    }

    return nf.Block( b.position(), stmtList );
  }
}
