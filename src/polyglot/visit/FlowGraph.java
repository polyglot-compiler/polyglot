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

  public Peer peer(Computation n, DataFlow df) {
    return peer(n, Collections.EMPTY_LIST, df);
  }

  public Collection peers(Computation n) {
    IdentityKey k = new IdentityKey(n);
    Map pathMap = (Map) peerMap.get(k);
    return pathMap.values();
  }

  public Peer peer(Computation n, List path_to_finally, DataFlow df) {
    IdentityKey k = new IdentityKey(n);
    Map pathMap = (Map) peerMap.get(k);
    if (pathMap == null) {
      pathMap = new HashMap();
      peerMap.put(k, pathMap);
    }

    ListKey lk = new ListKey(path_to_finally);
    Peer p = (Peer) pathMap.get(lk);
    if (p == null) {
      DataFlow.Item item = df.createItem(this, n);
      p = new Peer(n, path_to_finally, item);
      pathMap.put(lk, p);
    }
    return p;
  }

  static class Peer {
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

  static class ListKey {
    List list;

    ListKey(List list) {
      this.list = list;
    }

    public int hashCode() {
      return list.hashCode();
    }

    public boolean equals(Object other) {
      if (other instanceof ListKey) {
          ListKey k = (ListKey) other;
          if (k.list.size() != list.size())
            return false;
          for (int i = 0; i < list.size(); i++) {
            Object kfrom = k.list.get(i);
            Object from = list.get(i);
            if (kfrom != from)
              return false;
          }

          return true;
      }

      return false;
    }
  }
}
