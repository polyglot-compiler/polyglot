package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.*;
import java.util.*;

public class FlowGraph {
  Map peerMap;
  Computation root;
  boolean forward;
  boolean replicateFinally;

  FlowGraph(Computation root, boolean forward, boolean replicateFinally) {
    this.root = root;
    this.forward = forward;
    this.replicateFinally = replicateFinally;
    this.peerMap = new HashMap();
  }

  public Computation startNode() { return forward ? root.entry() : root; }
  public Computation finishNode() { return forward ? root : root.entry(); }
  public Computation entryNode() { return root.entry(); }
  public Computation exitNode() { return root; }
  public Computation root() { return root; }
  public boolean forward() { return forward; }
  public boolean replicateFinally() { return replicateFinally; }
  public Collection peers() { return peerMap.values(); }

  public Peer peer(Computation n, List path_to_finally, DataFlow df) {
    NodeKey k = new NodeKey(n, path_to_finally);
    Peer p = (Peer) peerMap.get(k);
    if (p == null) {
      DataFlow.Item item = df.createItem(this, n);
      p = new Peer(n, path_to_finally, item);
      peerMap.put(k, p);
    }
    return p;
  }

  class Peer {
    DataFlow.Item item;
    Computation node;
    List succs;
    List path_to_finally;

    public Peer(Computation node, List path_to_finally, DataFlow.Item item) {
      this.node = node;
      this.path_to_finally = path_to_finally;
      this.item = item;
      this.succs = new ArrayList();
    }

    public String toString() {
      return node + "[" + hashCode() + ": " + path_to_finally + "]";
    }
  }

  class NodeKey {
    Computation node;
    List path_to_finally;

    NodeKey(Computation node, List path_to_finally) {
      this.node = node;
      this.path_to_finally = path_to_finally;
    }

    public int hashCode() {
      return System.identityHashCode(node);
    }

    public boolean equals(Object other) {
      if (other instanceof NodeKey) {
          NodeKey k = (NodeKey) other;
          if (k.node != node) return false;
          if (k.path_to_finally.size() != path_to_finally.size()) return false;
          for (int i = 0; i < path_to_finally.size(); i++) {
            Computation kfrom = (Computation) k.path_to_finally.get(i);
            Computation from = (Computation) path_to_finally.get(i);
            if (kfrom != from) return false;
          }

          return true;
      }

      return false;
    }
  }
}
