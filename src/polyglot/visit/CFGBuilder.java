package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.*;
import polyglot.main.Report;
import java.util.*;

/**
 * Class used to construct a CFG.
 */
public class CFGBuilder implements Copy
{
    /** The flowgraph under construction. */
    FlowGraph graph;

    /** The type system. */
    TypeSystem ts;

    /**
     * The outer CFGBuilder.  We create a new inner CFGBuilder when entering a
     * loop or try-block and when entering a finally block.
     */
    CFGBuilder outer;

    /**
     * The innermost loop or try-block in lexical scope.  We maintain a stack
     * of loops and try-blocks in order to add edges for break and continue
     * statements and for exception throws.  When such a jump is encountered we
     * traverse the stack, searching for the target of the jump.
     */
    Stmt current_block;

    /**
     * List of terms on the path to the current finally block.  If we are
     * constructing a CFG for a finally block, this is the sequence of terms
     * that caused entry into this and lexically enclosing finally blocks.
     * We construct a unique subgraph for each such path. The list
     * is empty if this CFGBuilder is not constructing the CFG for a finally
     * block.
     */
    List path_to_finally;

    /** The data flow analysis for which we are constructing the graph. */
    DataFlow df;

    /**
     * Should we skip the catch blocks for the innermost try when building
     * edges for an exception throw?
     */
    boolean skipInnermostCatches;

    public CFGBuilder(TypeSystem ts, FlowGraph graph, DataFlow df) {
        this.ts = ts;
        this.graph = graph;
        this.df = df;
        this.path_to_finally = Collections.EMPTY_LIST;
        this.outer = null;
        this.current_block = null;
        this.skipInnermostCatches = false;
    }

    /** Get the type system. */
    public TypeSystem typeSystem() {
        return ts;
    }

    /** Copy the CFGBuilder. */
    public Object copy() {
        try {
            return (CFGBuilder) super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone() weirdness.");
        }
    }

    /**
     * Construct a new CFGBuilder with the a new innermost loop or
     * try-block <code>n</code>.
     */
    public CFGBuilder push(Stmt n) {
        return push(n, false);
    }

    /**
     * Construct a new CFGBuilder with the a new innermost loop or
     * try-block <code>n</code>, optionally skipping innermost catch blocks.
     */
    public CFGBuilder push(Stmt n, boolean skipInnermostCatches) {
        CFGBuilder v = (CFGBuilder) copy();
        v.outer = this;
        v.current_block = n;
        v.skipInnermostCatches = skipInnermostCatches;
        return v;
    }

    /** Get the CFGBuilder for the outer block. */
    public CFGBuilder outer() {
        return outer;
    }

    /**
     * Visit edges from a branch.  Simulate breaking/continuing out of
     * the loop, visiting any finally blocks encountered.
     */
    public void visitBranchTarget(Branch b) {
      Term last = b;
      CFGBuilder last_visitor = this;

      for (CFGBuilder v = this; v != null; v = v.outer()) {
        Term c = v.current_block;

        if (c instanceof Try) {
          Try tr = (Try) c;
          if (tr.finallyBlock() != null) {
            last_visitor = tryFinally(v, last, last_visitor, tr.finallyBlock());
            last = tr.finallyBlock();
          }
        }

        if (b.label() != null) {
          if (c instanceof Labeled) {
            Labeled l = (Labeled) c;
            if (l.label().equals(b.label())) {
              if (b.kind() == Branch.BREAK) {
                edge(last_visitor, last, l);
              }
              else {
                Stmt s = l.statement();
                if (s instanceof Loop) {
                  Loop loop = (Loop) s;
                  edge(last_visitor, last, loop.continueTarget());
                }
                else {
                  // Should be SemanticException
                  throw new InternalCompilerError("Can only continue loops.",
                                                  l.position());
                }
              }

              return;
            }
          }
        }
        else {
          if (c instanceof Loop) {
            Loop l = (Loop) c;
            if (b.kind() == Branch.CONTINUE) {
              edge(last_visitor, last, l.continueTarget());
            }
            else {
              edge(last_visitor, last, l);
            }

            return;
          }
          else if (c instanceof Switch && b.kind() == Branch.BREAK) {
            edge(last_visitor, last, c);
            return;
          }
        }
      }

      throw new InternalCompilerError("Branch target not found.", b.position());
    }

    /**
     * Visit edges for a return statement.  Simulate the return, visiting any
     * finally blocks encountered.
     */
    public void visitReturn(Return r) {
      Term last = r;
      CFGBuilder last_visitor = this;

      for (CFGBuilder v = this; v != null; v = v.outer()) {
        Term c = v.current_block;

        if (c instanceof Try) {
          Try tr = (Try) c;
          if (tr.finallyBlock() != null) {
            last_visitor = tryFinally(v, last, last_visitor, tr.finallyBlock());
            last = tr.finallyBlock();
          }
        }
      }

      // Add an edge to the exit node.
      edge(last_visitor, last, graph.exitNode());
    }

    static int counter = 0;

    /** Visit the AST, constructing the CFG. */
    public void visitGraph() {
        String name = StringUtil.getShortNameComponent(df.getClass().getName());
        name += counter++;

	if (Report.should_report(Report.cfg, 2))
	    Report.report(2, "digraph " + name + " {");

        // create peers for the entry and exit nodes.
        graph.peer(graph.entryNode(), Collections.EMPTY_LIST, df);
        graph.peer(graph.exitNode(), Collections.EMPTY_LIST, df);

        this.visitCFG(graph.root(), Collections.EMPTY_LIST);

	if (Report.should_report(Report.cfg, 2))
	    Report.report(2, "}");
    }

    /** Utility function to visit all edges in a list. */
    public void visitCFGList(List elements, Term after) {
        Term prev = null;

        for (Iterator i = elements.iterator(); i.hasNext(); ) {
            Term c = (Term) i.next();

            if (prev != null) {
                visitCFG(prev, c.entry());
            }

            prev = c;
        }

        if (prev != null) {
            visitCFG(prev, after);
        }
    }

    /**
     * Create edges for a node <code>a</code> with a single successor
     * <code>succ</code>.
     */
    public void visitCFG(Term a, Term succ) {
        visitCFG(a, Collections.singletonList(succ));
    }

    /**
     * Create edges for a node <code>a</code> with successors
     * <code>succs</code>.
     */
    public void visitCFG(Term a, List succs) {
      if (Report.should_report(Report.cfg, 2))
	Report.report(2, "// node " + a + " -> " + succs);

      succs = a.acceptCFG(this, succs);

      for (Iterator i = succs.iterator(); i.hasNext(); ) {
          Term out = (Term) i.next();
          edge(a, out);
      }

      if (a instanceof Thrower) {
        Thrower t = (Thrower) a;

        for (Iterator i = t.throwTypes(ts).iterator(); i.hasNext(); ) {
          Type type = (Type) i.next();
          visitThrow(t, type);
        }
      }

      // Every statement can throw an error.
      // This is probably too inefficient.
      if ((a instanceof Stmt && ! (a instanceof CompoundStmt)) ||
          (a instanceof Block && ((Block) a).statements().isEmpty())) {

          visitThrow(a, ts.Error());
      }
    }

    /**
     * Create edges for an exception thrown from term <code>t</code>.
     */
    public void visitThrow(Term t, Type type) {
      Term last = t;
      CFGBuilder last_visitor = this;

      for (CFGBuilder v = this; v != null; v = v.outer()) {
        Term c = v.current_block;

        if (c instanceof Try) {
          Try tr = (Try) c;

          if (! v.skipInnermostCatches) {
            for (Iterator i = tr.catchBlocks().iterator(); i.hasNext(); ) {
              Catch cb = (Catch) i.next();

              // definite catch
              if (type.isImplicitCastValid(cb.catchType())) {
                edge(last_visitor, last, cb.entry());
                return;
              }

              // possible catch
              if (type.isCastValid(type)) {
                edge(last_visitor, last, cb.entry());
              }
            }
          }

          if (tr.finallyBlock() != null) {
            last_visitor = tryFinally(v, last, last_visitor, tr.finallyBlock());
            last = tr.finallyBlock();
          }
        }
      }

      // If not caught, do _not_ insert a node from the thrower to exit.
      // edge(last_visitor, last, graph.exitNode());
    }

    /** Create edges for a try finally block. */
    public CFGBuilder tryFinally(CFGBuilder v, Term last,
                                 CFGBuilder last_visitor, Term f) {
        CFGBuilder v_ = v.outer().enterFinally(last);
        v_.edge(last_visitor, last, f.entry());
        v_.visitCFG(f, Collections.EMPTY_LIST);
        return v_;
    }

    /** Enter a finally block. */
    public CFGBuilder enterFinally(Term from) {
      CFGBuilder v = (CFGBuilder) copy();
      v.path_to_finally = new ArrayList(path_to_finally.size()+1);
      v.path_to_finally.add(from);
      return v;
    }

    /**
     * Add an edge to the CFG from <code>p</code> to <code>q</code>.
     */
    public void edge(Term p, Term q) {
      edge(this, p, q);
    }

    /**
     * Add an edge to the CFG from <code>p</code> to <code>q</code>.
     *
     * @param p_visitor The visitor used to create p ("this" is the visitor
     *                  that created q) 
     * @param p The predecessor node in the forward graph
     * @param q The successor node in the forward graph
     */
    public void edge(CFGBuilder p_visitor, Term p, Term q) {
      if (Report.should_report(Report.cfg, 2))
	Report.report(2, "//     edge " + p + " -> " + q);

      FlowGraph.Peer pp = graph.peer(p, p_visitor.path_to_finally, df);
      FlowGraph.Peer pq = graph.peer(q, path_to_finally, df);

      if (Report.should_report(Report.cfg, 2)) {
	Report.report(2,
			pp.hashCode() + " [ label = \"" +
			StringUtil.escape(pp.toString()) + "\" ];");
	Report.report(2,
			pq.hashCode() + " [ label = \"" +
			StringUtil.escape(pq.toString()) + "\" ];");
      }

      if (graph.forward()) {
	if (Report.should_report(Report.cfg, 2))
	    Report.report(2, pp.hashCode() + " -> " + pq.hashCode() + ";");
        pp.succs.add(pq);
        pq.preds.add(pp);
      }
      else {
	if (Report.should_report(Report.cfg, 2))
        Report.report(2, pq.hashCode() + " -> " + pp.hashCode() + ";");
        pq.succs.add(pp);
        pp.preds.add(pq);
      }
    }
}
