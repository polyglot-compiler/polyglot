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
    Stmt current_block;
    CFGBuilder outer;
    TypeSystem ts;
    List path_to_finally;
    FlowGraph graph;
    DataFlow df;
    boolean skipCatches;

    public CFGBuilder(TypeSystem ts, FlowGraph graph, DataFlow df) {
        this.ts = ts;
        this.graph = graph;
        this.df = df;
        this.path_to_finally = Collections.EMPTY_LIST;
        this.outer = null;
        this.current_block = null;
        this.skipCatches = false;
    }

    public TypeSystem typeSystem() {
        return ts;
    }

    public Object copy() {
        try {
            return (CFGBuilder) super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone() weirdness.");
        }
    }

    public CFGBuilder push(Stmt n) {
        return push(n, false);
    }

    public CFGBuilder push(Stmt n, boolean skipCatches) {
        CFGBuilder v = (CFGBuilder) copy();
        v.outer = this;
        v.current_block = n;
        v.skipCatches = skipCatches;
        return v;
    }

    public CFGBuilder pop() {
        return outer;
    }

    /**
     * Visit edges from a branch.  Simulate breaking/continuing out of
     * the loop, visiting any finally blocks encountered.
     */
    public void visitBranchTarget(Branch b) {
      Computation last = b;
      CFGBuilder last_visitor = this;

      for (CFGBuilder v = this; v != null; v = v.pop()) {
        Computation c = v.current_block;

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
      Computation last = r;
      CFGBuilder last_visitor = this;

      for (CFGBuilder v = this; v != null; v = v.pop()) {
        Computation c = v.current_block;

        if (c instanceof Try) {
          Try tr = (Try) c;
          if (tr.finallyBlock() != null) {
            last_visitor = tryFinally(v, last, last_visitor, tr.finallyBlock());
            last = tr.finallyBlock();
          }
        }
      }

      // Add an edge to the exit node.
      edge(last, graph.exitNode());
    }

    static int counter = 0;

    public void visitGraph() {
        String name = StringUtil.getShortNameComponent(df.getClass().getName());
        name += counter++;

	if (Report.should_report("cfg",1))
	    Report.report(1, "digraph " + name + " {");

        // create peers for the entry and exit nodes.
        graph.peer(graph.entryNode(), Collections.EMPTY_LIST, df);
        graph.peer(graph.exitNode(), Collections.EMPTY_LIST, df);

        this.visitCFG(graph.root(), Collections.EMPTY_LIST);

	if (Report.should_report("cfg",1))
	    Report.report(1, "}");
    }


    /** Utility function to visit all edges in a list. */
    public void visitCFGList(List elements, Computation after) {
        Computation prev = null;

        for (Iterator i = elements.iterator(); i.hasNext(); ) {
            Computation c = (Computation) i.next();

            if (prev != null) {
                visitCFG(prev, c.entry());
            }

            prev = c;
        }

        if (prev != null) {
            visitCFG(prev, after);
        }
    }

    public void visitCFG(Computation a, Computation succ) {
        visitCFG(a, Collections.singletonList(succ));
    }

    public void visitCFG(Computation a, List succs) {
      if (Report.should_report("cfg",1))
	Report.report(1, "// node " + a + " -> " + succs);

      succs = a.acceptCFG(this, succs);

      for (Iterator i = succs.iterator(); i.hasNext(); ) {
          Computation out = (Computation) i.next();
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

    public void visitThrow(Computation t, Type type) {
      Computation last = t;
      CFGBuilder last_visitor = this;

      for (CFGBuilder v = this; v != null; v = v.pop()) {
        Computation c = v.current_block;

        if (c instanceof Try) {
          Try tr = (Try) c;

          if (! skipCatches) {
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
      // edge(last, graph.exitNode());
    }

    public CFGBuilder tryFinally(CFGBuilder v, Computation last,
                                 CFGBuilder last_visitor, Computation f) {
        CFGBuilder v_ = v.pop().enterFinally(last);
        v_.edge(last_visitor, last, f.entry());
        v_.visitCFG(f, Collections.EMPTY_LIST);
        return v_;
    }

    public CFGBuilder enterFinally(Computation from) {
      if (graph.replicateFinally()) {
        CFGBuilder v = (CFGBuilder) copy();
        v.path_to_finally = new ArrayList(path_to_finally.size()+1);
        v.path_to_finally.add(from);
        return v;
      }
      else {
        return this;
      }
    }

    public void edge(Computation p, Computation q) {
      edge(this, p, q);
    }

    /**
     * @param p_visitor The visitor used to create p ("this" is the visitor
     *                  that created q) 
     * @param p The predecessor node in the forward graph
     * @param q The successor node in the forward graph
     */
    public void edge(CFGBuilder p_visitor, Computation p, Computation q) {
      if (Report.should_report("cfg", 1))
	Report.report(1, "//     edge " + p + " -> " + q);

      FlowGraph.Peer pp = graph.peer(p, p_visitor.path_to_finally, df);
      FlowGraph.Peer pq = graph.peer(q, path_to_finally, df);

      if (Report.should_report("cfg", 1)) {
	Report.report(1,
			pp.hashCode() + " [ label = \"" +
			StringUtil.escape(pp.toString()) + "\" ];");
	Report.report(1,
			pq.hashCode() + " [ label = \"" +
			StringUtil.escape(pq.toString()) + "\" ];");
      }

      if (graph.forward()) {
	if (Report.should_report("cfg", 1))
	    Report.report(1, pp.hashCode() + " -> " + pq.hashCode() + ";");
        pp.succs.add(pq);
      }
      else {
	if (Report.should_report("cfg", 1))
        Report.report(1, pq.hashCode() + " -> " + pp.hashCode() + ";");
        pq.succs.add(pp);
      }
    }
}
