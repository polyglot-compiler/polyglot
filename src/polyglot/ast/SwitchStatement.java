/*
 * SwitchStatement.java
 */

package jltools.ast;

import jltools.util.TypedList;
import jltools.util.TypedListIterator;
import jltools.util.CodeWriter;
import jltools.types.LocalContext;
import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;

/**
 * Overview: A SwitchStatement is a mutable representation of a Java
 * swtich/case statement.  A Switch statement has an expression which
 * is evaluated to determine where to branch to, an a list of labels
 * and block statements which are conditionally evaluated.  One of the
 * labels, rather than having a constant expression, may be lablled
 * default.
 */

public class SwitchStatement extends Statement {
    
   /**
    * Overview: An element of a switch statement.  This can either be
    * a case label, or a BlockStatement.
    */
   public static abstract class SwitchElement {
      public abstract SwitchElement copy();
      public abstract SwitchElement deepCopy();
   }
  
   /**
    * Overview: A case statement or a default label in a switch block.  
    */
   public static class CaseStatement extends SwitchElement {
      /**
       * Effects: Creates a new CaseStement with <expr> as the value
       * for the case.
       */
      public CaseStatement(Expression expr) {
         this.expr = expr;
         this.def = false;
      }

      /**
       * Effects: Creates a new CaseStatement which represents a default Label.
       */
      public CaseStatement() {
         this.def = true;
      }

      /**
       * Effects: Returns true iff this CaseStatement represents a
       * default label.
       */
      public boolean isDefault() {
         return def;
      }

      /**
       * Effects: Returns the expresion associated with this
       * CaseStatement if it is not a default, otherwise returns null.
       */
      public Expression getExpression() {
         if (def) {
            return null;
         }
         else {
            return expr;
         }
      }

      /**
       * Effects: Sets the expression associated with this case to be
       * <newExpr>.
       */
      public void setExpression(Expression newExpr) {
         expr = newExpr;
      }

      /**
       * Effects: If <def> is true, sets this CaseStatement to
       * represent a defualt label, else sets it to represent a typical
       * case.
       */
      public void setDefault(boolean def) {
         this.def = def;
      }

      public SwitchElement copy() {
         if (def) {
            return new CaseStatement();
         }
         else {
            return new CaseStatement(expr);
         }
      }

      public SwitchElement deepCopy() {
         if (def) {
            return new CaseStatement();
         }
         else {
            return new CaseStatement((Expression)expr.deepCopy());
         }
      }
  
      private boolean def;
      private Expression expr;
   }

   public static class SwitchBlock extends SwitchElement {
      /**
       * Effects: Creates a new SwitchBlock which contains <block>.
       */
      public SwitchBlock (BlockStatement block) {
         this.block = block;
      }
      
      public SwitchBlock () {
         this( new BlockStatement());
      }

      /** 
       * Effects: Returns the BlockStatement contained in this switch block.
       */
      public BlockStatement getBlock() {
         return block;
      }

      /**
       * Effects: Sets the block statement associated with this to be <newBlock>.
       */
      public void setBlock(BlockStatement newBlock) {
         block = newBlock;
      }

      public SwitchElement copy() {
         return new SwitchBlock(block);
      }

      public SwitchElement deepCopy() {
         return new SwitchBlock((BlockStatement) block.deepCopy());
      }
    
      private BlockStatement block;
   }

   /**
    * Requires: <switchElems> contains only elements of type SwitchElement.
    *
    * Effects: Creates a new SwitchStatement which is conditioned on
    * <expr> and contains the elements of <switchElems> in order.
    */
   public SwitchStatement(Expression expr, List switchElems) {
      this.expr = expr;
      TypedList.check(switchElems, SwitchElement.class);
      this.switchElems = new ArrayList(switchElems);
   }

   /**
    * Effects: Returns the Expression which this SwitchStatement is
    * conditioned on.
    */
   public Expression getExpression() {
      return expr;
   }

   /**
    * Effects: Sets the Expression upon which this is conditioned to be
    * <newExpr>.
    */
   public void setExpression(Expression newExpr) {
      expr = newExpr;
   }

   /**
    * Effects: Adds <sw> to the list of SwitchElements of this.
    */
   public void addSwitchElement(SwitchElement sw) {
      switchElems.add(sw);
   }

   /**
    * Effects: Returns the SwitchElement at position <pos>.  Throws an
    * IndexOutOfBoundsException when <pos> is not valid.
    */
   public SwitchElement getSwitchElementAt(int pos) {
      return (SwitchElement) switchElems.get(pos);
   }

   /**
    * Effects: Removes the SwitchElement at position <pos>.  Throws an
    * IndexOutOfBoundsException if <pos> is not valid.
    */
   public void removeSwitchElement(int pos) {
      switchElems.remove(pos);
   }

   /**
    * Effects: Returns TypedListIterator which will return the
    * SwitchElements of this in order.
    */
   public TypedListIterator switchElements() {
      return new TypedListIterator (switchElems.listIterator(),
                                    SwitchElement.class,
                                    false);
   }


   void visitChildren(NodeVisitor vis)
   {
      expr = (Expression) expr.visit(vis);
      for (ListIterator it = switchElems.listIterator(); it.hasNext(); ) {
	 SwitchElement se = (SwitchElement) it.next();
	 if (se instanceof CaseStatement) {
	    CaseStatement cs = (CaseStatement) se;
	    cs.expr = (Expression) cs.expr.visit(vis);
	 }
	 else {
	    SwitchBlock sb = (SwitchBlock) se;
	    sb.block = (BlockStatement) sb.block.visit(vis);
	 }
      }
   }

   public Node typeCheck(LocalContext c)
   {
      // FIXME: implement
      return this;
   }

   public void translate(LocalContext c, CodeWriter w)
   {
      SwitchElement se;
      CaseStatement cs;
      SwitchBlock cb;

      w.write("switch (");
      expr.translate(c, w);
      w.write(") {");
      w.beginBlock();

      for (ListIterator it = switchElems.listIterator(); it.hasNext(); )
      {
         se = (SwitchElement) it.next();
         if (se instanceof CaseStatement)
         {
            cs = (CaseStatement)se;
            if (cs.isDefault())
               w.write("default: ");
            else
            {
               w.write("case ");
               cs.getExpression().translate(c, w);
               w.write(": " );
            }
         }
         else if (se instanceof SwitchBlock)
         {
           ((SwitchBlock)se).getBlock().translate(c, w);
         }

         if (it.hasNext()) {
           w.newline(0);
         }
      }
      w.endBlock();      
      w.write("}");
   }

   public Node dump( CodeWriter w)
   {
      w.write( "( SWITCH ");
      dumpNodeInfo( w);
      w.write( ")");
      return null;
   }

   public Node copy() {
      List newSwitchElems = new ArrayList(switchElems.size());
      for(ListIterator it=switchElems.listIterator(); it.hasNext(); ) {
         newSwitchElems.add(((SwitchElement) it.next()).copy());
      }
      SwitchStatement ss = new SwitchStatement(expr,
                                               newSwitchElems);
      ss.copyAnnotationsFrom(this);
      return ss;
   }
  
   public Node deepCopy() {
      List newSwitchElems = new ArrayList(switchElems.size());
      for(ListIterator it=switchElems.listIterator(); it.hasNext(); ) {
         newSwitchElems.add(((SwitchElement) it.next()).deepCopy());
      }
      SwitchStatement ss = new SwitchStatement((Expression) expr.deepCopy(),
                                               newSwitchElems);
      ss.copyAnnotationsFrom(this);
      return ss;
   }
  
   private Expression expr;
   private List switchElems;

}
