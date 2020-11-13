import java.util.function.*;


class CombiningTree<T> {
  Node<T>[] leaf;


  public CombiningTree(int depth) {
    this(depth, 2);
  }

  public CombiningTree(int depth, int ary) {
    Node<T>[] parent = createNodes(1, ary);
    for (int i=1; i<depth; i++) {
      int n = (int) Math.pow(ary, i);
      leaf = createNodes(n, ary);
      for (int j=0; j<n; j++)
        leaf[j].parent = parent[j/ary];
    }
  }

  @SuppressWarnings("unchecked")
  private Node<T>[] createNodes(int n, int ary) {
    Node<T>[] a = (Node<T>[]) new Node[n];
    for (int i=0; i<n; i++)
      a[i] = new Node<>(ary);
    return a;
  }


  public T getAndOp(T x, BinaryOperator<T> op)
  throws InterruptedException {
    int i = (int) Thread.currentThread().getId();
    return getAndOp(x, op, i);
  }

  public T getAndOp(T x, BinaryOperator<T> op, int i)
  throws InterruptedException {
    return leaf[i % leaf.length].getAndOp(x, op);
  }
}
