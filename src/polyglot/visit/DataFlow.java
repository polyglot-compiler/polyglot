package polyglot.visit;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.*;
import java.util.*;

/**
 * Visitor which checks if local variables are defined on all paths.
 * Extend ContextVisitor to get its error handling features.
 */
public abstract class DataFlow extends ErrorHandlingVisitor
{
    boolean forward;
    boolean replicateFinally;

    public DataFlow(Job job, TypeSystem ts, NodeFactory nf, boolean forward, boolean replicateFinally) {
	super(job, ts, nf);
        this.forward = forward;
        this.replicateFinally = replicateFinally;
    }

    public interface Item {
        Item flow(Computation n);
        boolean meet(Item item);
        void check(FlowGraph graph, Computation n) throws SemanticException;
    }

    public abstract Item createItem(FlowGraph graph, Computation n);

    public void dataflow(FlowGraph graph) {
      Stack s = new Stack();

      for (Iterator i = graph.peers().iterator(); i.hasNext(); ) {
        s.push(i.next());
      }

      while (! s.isEmpty()) {
        FlowGraph.Peer p = (FlowGraph.Peer) s.pop();
        Item out = p.item.flow(p.node);

        for (Iterator i = p.succs.iterator(); i.hasNext(); ) {
          FlowGraph.Peer q = (FlowGraph.Peer) i.next();

          // System.out.println("// " + p.node + " -> " + q.node);

          // Edge p -> q.  Meet out[p] with in[q].  If the in[q] changes, push
          // q on the stack.
          Item in = q.item;

          // String old_in = in.toString();

          if (in.meet(out)) {
              if (s.search(q) == -1) {
                  s.push(q);
              }
          }

          // System.out.println("//     in[" + q.node + "] = " + old_in + " -> " + in);
        }
      }
    }

    protected FlowGraph initGraph(CodeDecl code, Computation root) {
        return new FlowGraph(root, forward, replicateFinally);
    }

    public Node leaveCall(Node n) throws SemanticException {
        if (n instanceof CodeDecl) {
            Block body = ((CodeDecl) n).body();

            if (body != null) {
                // Compute the successor of each child node.
                FlowGraph g = initGraph((CodeDecl) n, body);

                if (g != null) {
                    // Build the control flow graph.
                    CFGBuilder v = new CFGBuilder(ts, g, this);
                    v.visitGraph();

                    dataflow(g);

                    return ((CodeDecl) n).body(post(g, body));
                }
            }
        }

        return n;
    }

    public Block post(FlowGraph graph, Block root) throws SemanticException {
        for (Iterator i = graph.peers().iterator(); i.hasNext(); ) {
            FlowGraph.Peer p = (FlowGraph.Peer) i.next();
            p.item.check(graph, p.node);
        }

        return root;
    }
}
