/*
 * NodeVisitor.java
 */

package jltools.ast;

/**
 * NodeVisitor
 *
 * Overview: A NodeVisitor is a traversal class for the AST, which implements
 *   the 'Visitor' design pattern. The contract is that every AST Node will
 *   have an accept method, which will dispatch to NodeVisitor's visitFoo
 *   method.
 *
 * Notes: 
 *    If you want to add nodes to the AST outside of jtools.ast, don't add
 *    them to NodeVisitor.  Instead, make a new visitor interface which
 *    extends NodeVisitor, and have your code look a little like:
 *         Node accept(NodeVistor v) {
 *             if (v instanceof SpecialVisitor)
 *                return v.visitSpecial(this);
 *             else {
 *                visitChildren(v);
 *                return this;
 *                // or maybe: return v.visitSupertypeOfThis(this);
 *                // or      : throw new Error("foo");
 *             }
 *         }
 **/
public abstract class NodeVisitor 
{
   public Node visitBefore(Node n)
   {
      return n;
   }

   public Node visitAfter(Node n)
   {
      return n;
   }
}
