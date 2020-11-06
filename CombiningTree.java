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

  // 1. Lock head.
  // 2. Try deq.
  // 3. Unlock head.
  public synchronized boolean precombine() {
    while (locked) wait();
    switch (status) {
      case IDLE:
      status = Status.FIRST;
      return true;
      case FIRST:
      locked = true;
      status = Status.SECOND;
      return false;
      case ROOT:
      return false;
      default:
      throw new RuntimeException("unexpected Node state "+status);
    }
  }

  // 1. Ensure queue is not full
  // 2. Save data at tail.
  // 3. Increment tail.
  public synchronized int combine(int combined) {
    while (locked) wait();
    locked = true;
    value1 = combined;
    switch(status) {
      case FIRST:
      return value1;
      case SECOND:
      return value1 + value2;
      default:
      throw new RuntimeException("unexpected Node state "+status);
    }
  }

  // 1. Ensure queue is not empty.
  // 2. Return data at head.
  // 3. Increment head.
  public synchronized int op(int combined) {
    switch (status) {
      case ROOT:
      int prior = result;
      result += combined;
      return prior;
      case SECOND:
      value2 = combined;
      locked = false;
      notifyAll(); // wake up waiting threads
      while (status != Status.RESULT) wait();
      locked = false;
      notifyAll();
      status = Status.IDLE;
      return result;
      default:
      throw new RuntimeException("unexpected Node state");
    }
  }

  public synchronized void distribute(int prior) {
    switch (status) {
      case FIRST:
      status = Status.IDLE;
      locked = false;
      break;
      case SECOND:
      result = prior + value1;
      status = Status.RESULT;
      break;
      default:
      throw new RuntimeException("unexpected Node state");
    }
    notifyAll();
  }
}
