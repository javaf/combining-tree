import java.util.function.*;


// A Combining Tree is an N-ary tree of nodes, that follows
// software combining to reduce memory contention while
// updating a shared value.
// 
// The shared value is placed at the root of the tree, and
// threads perfrom getAndOp() at the leaf nodes. Each leaf
// node handles N threads. The combined value is then
// propagated up the tree by an active thread. There can be
// several active threads, but eventually one active thread
// updates the root node. It then shares this message to all
// other threads behind it.
// 
// This was contention at a single memory location is avoided.
// However, i guess that such a design is normally useful
// only in hardware, as it becomes too slow in software.
// Useful for educational purposes.

class CombiningTree<T> {
  Node<T> root;
  Node<T>[] leaf;
  // root: root node
  // leaf: leaf nodes


  public CombiningTree(int depth) {
    this(depth, 2);
  }

  public CombiningTree(int depth, int ary) {
    Node<T>[] parent = createNodes(1, ary);
    root = parent[0];
    for (int i=1; i<depth; i++) {
      int n = (int) Math.pow(ary, i);
      leaf = createNodes(n, ary);
      for (int j=0; j<n; j++)
        leaf[j].parent = parent[j/ary];
      parent = leaf;
    }
  }

  @SuppressWarnings("unchecked")
  private Node<T>[] createNodes(int n, int ary) {
    Node<T>[] a = (Node<T>[]) new Node[n];
    for (int i=0; i<n; i++)
      a[i] = new Node<>(ary);
    return a;
  }


  // Get value of tree (at root).
  public T get() {
    return root.get();
  }

  // Set value of tree (at root).
  public void set(T x) {
    root.set(x);
  }


  // Gets current value, and then updates it.
  // x: value to OP with, op: binary op
  // 1. Select leaf index using thread id.
  // 2. Perform get & op at desired leaf.
  public T getAndOp(T x, BinaryOperator<T> op)
  throws InterruptedException {
    int i = (int) Thread.currentThread().getId(); // 1
    return getAndOp(x, op, i); // 2
  }

  // Gets current value, and then updates it.
  // x: value to OP with, op: binary op, i: leaf index
  // 1. Perform get & op at desired leaf (ensure in limit).
  public T getAndOp(T x, BinaryOperator<T> op, int i)
  throws InterruptedException {
    return leaf[i % leaf.length].getAndOp(x, op); // 1
  }
}
