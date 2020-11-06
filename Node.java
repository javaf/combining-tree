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

  // 1. Lock head.
  // 2. Try deq.
  // 3. Unlock head.
  public synchronized boolean precombine()
    throws InterruptedException {
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
        throw new IllegalStateException(status+"");
    }
  }

  // 1. Ensure queue is not full
  // 2. Save data at tail.
  // 3. Increment tail.
  public synchronized T combine(T combined)
    throws InterruptedException {
    while (locked) wait();
    locked = true;
    value1 = combined;
    switch (status) {
      case FIRST:
        return value1;
      case SECOND:
        return value1 + value2;
      default:
        throw new IllegalStateException(status+"");
    }
  }
  
  // 1. Ensure queue is not empty.
  // 2. Return data at head.
  // 3. Increment head.
  public synchronized T op(T combined) {
    switch (status) {
      case ROOT:
        T prior = result;
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
        throw new IllegalStateException(status+"");
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
        throw new IllegalStateException(status+"");
    }
    notifyAll();
  }
}
