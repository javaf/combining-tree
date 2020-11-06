import java.nio.*;
import java.util.*;
import java.util.concurrent.locks.*;

// Array queue is a bounded lock-based FIFO queue using
// an array. It uses 2 separate locks for head and tail.

class CombiningTree<T> {
  // headLock: lock for enq() at head
  // tailLock: lock for deq() at tail
  // data: array of values in stack
  // head: front of queue (0)
  // tail: rear of queue (0)

  @SuppressWarnings("unchecked")
  public CombiningTree(int width) {
    Node<T>[] nodes = new Node[width - 1];
    nodes[0] = new Node<>();
    for (int i=1; i<nodes.length; i++)
      nodes[i] = new Node<>(nodes[(i-1)/2]);
    Node<T>[] leaf = new Node[(width+1)/2];
    for (int i=0; i<leaf.length; i++)
      leaf[i] = nodes[nodes.length-i-1];
  }

  // 1. Lock tail.
  // 2. Try enq.
  // 3. Unlock tail.
  public int getAndIncrement() {
    Stack<Node<T>> stack = new Stack<>();
    Node<T> myLeaf = leaf[ThreadID.get()/2];
    Node node = myLeaf;
    // precombining phase
    while (node.precombine())
      node = node.parent;
    Node stop = node;
    // combining phase
    node = myLeaf;
    int combined = 1;
    while (node != stop) {
      combined = node.combine(combined);
      stack.push(node);
      node = node.parent;
    }
    // operation phase
    int prior = stop.op(combined);
    // distribution phase
    while (!stack.empty()) {
      node = stack.pop();
      node.distribute();
    }
    return prior;
  }
}
