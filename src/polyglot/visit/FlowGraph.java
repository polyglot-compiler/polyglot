package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.*;
import java.util.*;

public class FlowGraph {
  protected Map peerMap;
  protected Term root;
  protected boolean forward;

  FlowGraph(Term root, boolean forward) {
    this.root = root;
    this.forward = forward;
    this.peerMap = new HashMap();
  }

  public Term startNode() { return forward ? root.entry() : root; }
  public Term finishNode() { return forward ? root : root.entry(); }
  public Term entryNode() { return root.entry(); }
  public Term exitNode() { return root; }
  public Term root() { return root; }
  public boolean forward() { return forward; }

  public Collection pathMaps() {
    return peerMap.values();
  }

  public Map pathMap(Node n) {
    return (Map) peerMap.get(new IdentityKey(n));
  }

  public Collection peers() {
    Collection c = new ArrayList();
    for (Iterator i = peerMap.values().iterator(); i.hasNext(); ) {
      Map m = (Map) i.next();
      for (Iterator j = m.values().iterator(); j.hasNext(); ) {
        c.add(j.next());
      }
    }
    return c;
  }

  public Peer peer(Term n, DataFlow df) {
    return peer(n, Collections.EMPTY_LIST, df);
  }

  public Collection peers(Term n) {
    IdentityKey k = new IdentityKey(n);
    Map pathMap = (Map) peerMap.get(k);
    return pathMap.values();
  }

  public Peer peer(Term n, List path_to_finally, DataFlow df) {
    IdentityKey k = new IdentityKey(n);
    Map pathMap = (Map) peerMap.get(k);
    if (pathMap == null) {
      pathMap = new HashMap();
      peerMap.put(k, pathMap);
    }

    ListKey lk = new ListKey(path_to_finally);
    Peer p = (Peer) pathMap.get(lk);
    if (p == null) {
      p = new Peer(n, path_to_finally);
      pathMap.put(lk, p);
    }
    return p;
  }

  /**
   * This class provides an identifying label for edges in the flow graph.
   * Thus, the condition of an if statement will have at least two edges
   * leaving it (in a forward flow graph): one will have the EdgeKey
   * FlowGraph.EDGE_KEY_TRUE, and is the flow that is taken when the condition
   * evaluates to true, and one will have the EdgeKey FlowGraph.EDGE_KEY_FALSE,
   * and is the flow that is taken when the condition evaluates to false. 
   * 
   * The differentiation of the flow graph edges allows for a finer grain
   * data flow analysis, as the dataflow equations can incorporate the 
   * knowledge that a condition is true or false on certain flow paths.
   */
  public static class EdgeKey {
      protected Object o;
      protected EdgeKey(Object o) {
          this.o = o;
      }
      public int hashCode() {
          return o.hashCode();
      }
      public boolean equals(Object other) {
          return (other instanceof EdgeKey) && 
                  (((EdgeKey)other).o.equals(this.o));
      }
      public String toString() {
          return "EdgeKey["+o+"]";
      }
  }
  
  /**
   * This class extends EdgeKey and is the key for edges that are
   * taken when an exception of type t is thrown. Thus, the flow from
   * line 2 in the example below to the catch block (line 4) would have an
   * ExceptionEdgeKey constructed with the Type representing 
   * NullPointerExceptions.
   * 
   * <pre>
   * ...
   * try {                                      // line 1
   *   o.foo();                                 // line 2
   * }                                          // line 3
   * catch (NullPointerException e) {           // line 4
   *   ...
   * }
   * ...
   * </pre>
   */
  public static class ExceptionEdgeKey extends EdgeKey {
      public ExceptionEdgeKey(Type t) {
          super(t);
      }

      public Type type() {
          return (Type) o;
      }

      public String toString() {
          return "ExceptionEdgeKey["+o+"]";
      }
  }
  
  /**
   * This EdgeKey is the EdgeKey for edges where the expression evaluates
   * to true.
   */
  public static final EdgeKey EDGE_KEY_TRUE = new EdgeKey("true");
  
  /**
   * This EdgeKey is the EdgeKey for edges where the expression evaluates
   * to false.
   */
  public static final EdgeKey EDGE_KEY_FALSE = new EdgeKey("false");

  /**
   * This EdgeKey is the EdgeKey for edges where the flow is not suitable 
   * for EDGE_KEY_TRUE, EDGE_KEY_FALSE or an 
   * ExceptionEdgeKey, such as the edges from a switch
   * statement to its cases and
   * the flow from a sink node in the control flow graph.
   */
  public static final EdgeKey EDGE_KEY_OTHER = new EdgeKey("other");

  /**
   * This class represents an edge in the flow graph. The target of the edge
   * is either the head or the tail of the edge, depending on how the Edge is 
   * used. Thus, the target field in Edges in the collection Peer.preds is the
   * source Peer, while the target field in Edges in the collection Peer.succs 
   * is the destination Peer of edges.
   * 
   * Each Edge has an EdgeKey, which identifies when flow uses that edge in 
   * the flow graph. See EdgeKey for more information.
   */
  public static class Edge {
      protected Edge(EdgeKey key, Peer target) {
          this.key = key;
          this.target = target;
      }
      protected EdgeKey getKey() {
          return key;
      }
      protected Peer getTarget() {
          return target;
      }
      private EdgeKey key;
      private Peer target;
      public String toString() {
          return "(" + key + ")" + target;
      }
      
  }
  
  public static class Peer {
    protected DataFlow.Item inItem;  // Input Item for dataflow analysis
    protected Map outItems; // Output Items for dataflow analysis, a map from EdgeKeys to DataFlowlItems
    protected Term node;
    protected List succs; // List of successor Edges 
    protected List preds; // List of predecessor Edges 
    protected List path_to_finally;
    /**
     * Set of all the different EdgeKeys that occur in the Edges in the 
     * succs. This Set is lazily constructed, as needed, by the 
     * method succEdgeKeys()
     */     
    private Set succEdgeKeys;

    public Peer(Term node, List path_to_finally) {
      this.node = node;
      this.path_to_finally = path_to_finally;
      this.inItem = null;
      this.outItems = null;
      this.succs = new ArrayList();
      this.preds = new ArrayList();
      this.succEdgeKeys = null;
    }

    public List succs() { return succs; }
    public List preds() { return preds; }
    public Term node()  { return node; }

    public String toString() {
      return node + "[" + hashCode() + ": " + path_to_finally + "]";
    }
    
    public Set succEdgeKeys() {
        if (this.succEdgeKeys == null) {
            // the successor edge keys have not yet been calculated. do it
            // now.
            this.succEdgeKeys = new HashSet();
            for (Iterator iter = this.succs.iterator(); iter.hasNext(); ) {
                Edge e = (Edge)iter.next();
                this.succEdgeKeys.add(e.getKey());
            }
            if (this.succEdgeKeys.isEmpty()) {
                // There are no successors for this node. Add in the OTHER
                // edge key, so that there is something to map the output
                // item from...
                this.succEdgeKeys.add(FlowGraph.EDGE_KEY_OTHER);
            }
        }
        return this.succEdgeKeys;
    }
  }

  protected static class ListKey {
    protected List list;

    ListKey(List list) {
      this.list = list;
    }

    public int hashCode() {
      return list.hashCode();
    }

    public boolean equals(Object other) {
      if (other instanceof ListKey) {
          ListKey k = (ListKey) other;
          return CollectionUtil.equals(list, k.list);
      }

      return false;
    }
  }
  
  public String toString() {
    
    StringBuffer sb = new StringBuffer();
    Set todo = new HashSet(this.peers());
    LinkedList queue = new LinkedList(this.peers(this.startNode()));
    
    while (!queue.isEmpty()) {
        Peer p = (Peer)queue.removeFirst();
        todo.remove(p);
//        sb.append(StringUtil.getShortNameComponent(p.node.getClass().getName()) + " ["+p.node+"]" + "\n");
        sb.append(p.node+" (" + p.node.position()+ ")\n");
        for (Iterator i = p.succs.iterator(); i.hasNext(); ) {
            Edge e = (Edge)i.next();
            Peer q = e.getTarget();
            sb.append("    -> " + q.node+" (" + q.node.position()+ ")\n");
            //sb.append("  " + StringUtil.getShortNameComponent(q.node.getClass().getName()) + " ["+q.node+"]" + "\n");
            if (todo.contains(q) && !queue.contains(q)) {
                queue.addLast(q);
            }
        }
        
        if (queue.isEmpty() && !todo.isEmpty()) {
            sb.append("\n\n***UNREACHABLE***\n");
            queue.addAll(todo);
            todo = Collections.EMPTY_SET;
        }
    }
    
    return sb.toString();
  }
}
