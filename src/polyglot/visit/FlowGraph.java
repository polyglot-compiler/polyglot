package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.*;
import java.util.*;

public class FlowGraph {
  Map peerMap;
  Term root;
  boolean forward;

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

  static class Peer {
    DataFlow.Item inItem;  // Input Item for dataflow analysis
    DataFlow.Item outItem; // Output Item for dataflow analysis
    Term node;
    List succs; // List of successor Peers
    List preds; // List of predecessor Peers
    List path_to_finally;

    public Peer(Term node, List path_to_finally) {
      this.node = node;
      this.path_to_finally = path_to_finally;
      this.inItem = null;
      this.outItem = null;
      this.succs = new ArrayList();
      this.preds = new ArrayList();
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
            Peer q = (Peer)i.next();
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
