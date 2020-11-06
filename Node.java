class Node<T> {
  Node<T> parent;
  boolean locked;
  Status status;
  T value1;
  T value2;
  T result;

  public Node() {
    status = Status.ROOT;
  }

  public Node(Node<T> par) {
    parent = par;
    status = Status.IDLE;
  }
}
