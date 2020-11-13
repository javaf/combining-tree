import java.util.function.*;


class Node<T> {
  static final int FREE = 0;
  static final int PUSHING = 1;
  static final int PULLING = 2;
  static final long TIMEOUT = 100;
  Node<T> parent;
  int state;
  int count;
  T[] value;
  int size;

  
  public Node() {
    this(2);
  }

  @SuppressWarnings("unchecked")
  public Node(int n) {
    value = (T[]) new Object[n];
    parent = null;
    state = FREE;
    count = 0;
    size = n;
  }


  public synchronized T getAndOp(T x, BinaryOperator<T> op)
  throws InterruptedException {
    if (parent==null) return getAndOpRoot(x, op);
    if (count==0) return getAndOpActive(x, op);
    return getAndOpPassive(x, op);
  }

  private synchronized T getAndOpRoot(T x, BinaryOperator<T> op)
  throws InterruptedException {
    T a = combine(op);
    count = 0;
    insert(op.apply(a, x));
    return a;
  }

  private synchronized T getAndOpActive(T x, BinaryOperator<T> op)
  throws InterruptedException {
    insert(x);
    waitUntilFull(TIMEOUT);
    state = PUSHING;
    T a = combine(op);
    T r = parent.getAndOp(a, op);
    distribute(r, op);
    state = PULLING;
    return r;
  }

  private synchronized T getAndOpPassive(T x, BinaryOperator<T> op)
  throws InterruptedException {
    int i = insert(x);
    while (state!=PULLING) wait();
    if (--count==0) state = FREE;
    return value[i];
  }


  public synchronized int insert(T x)
  throws InterruptedException {
    while (state!=FREE) wait();
    int i = count++;
    value[i] = x;
    if (count==size) notifyAll();
    return i;
  }


  public synchronized T combine(BinaryOperator<T> op) {
    T a = value[0];
    for (int i=1; i<count; i++)
      a = op.apply(a, value[i]);
    return a;
  }

  public synchronized void distribute(T r, BinaryOperator<T> op) {
    for (int i=0; i<count; i++) {
      value[i] = r;
      r = op.apply(r, value[i]);
    }
  }


  private void waitUntilFull(long w)
  throws InterruptedException {
    long t0 = System.currentTimeMillis();
    while (count < size) {
      wait(w);
      long t = System.currentTimeMillis();
      w -= t - t0;
    }
  }
}
