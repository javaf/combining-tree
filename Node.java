import java.util.function.*;

// N-ary Node is the basic block of an N-ary Combining tree.
// It has N places for values, allowing N threads to put in
// their value. The first thread to put its value in the node
// becomes the "active thread", while all others become
// "passive threads". Once a node is full (or timeout), the
// active thread combines the node values and pushes it to
// the parent node. The value it recieves back from parent
// node is the distributed among all threads with proper
// values. Passive threads just wait for these values to be
// distributed, and take the one for them.

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
  // FREE: indicates that values are still being inserted
  // PUSHING: node is full, combined value being pushed
  // PULLED: pulled value from parent being given to threads
  // TIMEOUT: maximum time active thread waits for node to fill
  // parent: parent node
  // state: either FREE, PUSHING, or PULLING
  // count: number of values in node
  // value: storage place for values
  // size: max. no. of values allowed (arity of node, eg 2)
  

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


  // Gets node value (only for root node).
  public synchronized T get() {
    return value[0];
  }

  // Sets node value (only for root node).
  public synchronized void set(T x) {
    value[0] = x;
    count = 1;
  }


  // Gets current value, and then updates it.
  // x: value to OP (accumulate), op: binary operator
  // 1. Perform get & op based on 3 possible cases.
  // 1a. Root node
  // 1b. Active thread (first to visit node)
  // 1c. Passive thread (visits later)
  public synchronized T getAndOp(T x,
  BinaryOperator<T> op)
  throws InterruptedException {
    if (parent==null) return getAndOpRoot(x, op); // 1a
    if (count==0) return getAndOpActive(x, op);   // 1b
    return getAndOpPassive(x, op); // 1c
  }

  // Performs get & op for root node.
  // x: value to OP (accumulate), op: binary operator
  // 1. Get old value, by combining (a).
  // 2. Empty the node.
  // 3. Insert a OP x
  // 3. Return old value.
  private synchronized T getAndOpRoot(T x,
  BinaryOperator<T> op)
  throws InterruptedException {
    T a = combine(op);
    count = 0;
    insert(op.apply(a, x));
    return a;
  }

  // Performs get & op for active thread.
  // x: value to OP (accumulate), op: binary operator
  // 1. Insert value.
  // 2. Wait until node is full, or timeout.
  // 3. We have the values, so start pushing.
  // 4. Combine values into one with OP.
  // 5. Push combined value to parent.
  // 6. Distribute recieved value for all threads.
  // 7. Start the pulling process.
  // 8. Decrement count (we have our pulled value).
  // 9. Return pulled value.
  private synchronized T getAndOpActive(T x,
  BinaryOperator<T> op)
  throws InterruptedException {
    insert(x); // 1
    waitUntilFull(TIMEOUT); // 2
    state = PUSHING;   // 3
    T a = combine(op); // 4
    T r = parent.getAndOp(a, op); // 5
    distribute(r, op); // 6
    state = PULLING;   // 7
    count--;  // 8
    return r; // 9
  }

  // Performs get & op for passive thread.
  // x: value to OP (accumulate), op: binary operator
  // 1. Insert value.
  // 2. Wait until active thread has pulled value.
  // 3. Decrement count, one pulled value processed.
  // 4. If count is 0, the node is free.
  // 5. Return value of this thread.
  private synchronized T getAndOpPassive(T x,
  BinaryOperator<T> op)
  throws InterruptedException {
    int i = insert(x); // 1
    while (state!=PULLING) wait(); // 2
    if (--count==0) state = FREE;  // 3, 4
    return value[i]; // 5
  }


  // Inserts a value in the node (for a thread).
  // x: value to insert
  // 1. Wait unit node is free.
  // 2. Get index to place value in.
  // 3. Increment number of values in node.
  // 4. Place the value.
  // 5. If node is full, notify active thread.
  // 6. Return index where value was placed.
  public synchronized int insert(T x)
  throws InterruptedException {
    while (state!=FREE) wait(); // 1
    int i = count++; // 2, 3
    value[i] = x;    // 4
    if (count==size) notifyAll(); // 5
    return i; // 6
  }


  // Combine all values in the node (put by threads).
  // op: binary operator
  // 1. If OP is "+", combine will return sum.
  public synchronized T combine(BinaryOperator<T> op) {
    T a = value[0]; // 1
    for (int i=1; i<count; i++)  // 1
      a = op.apply(a, value[i]); // 1
    return a; // 1
  }


  // Distribute pulled value to all threads.
  // r: pulled value, op: binary operator
  // T0: active thread, T1...: passive threads
  // 1. T0 receives r
  // 2. T1 recieves r OP v0.
  // 3. T2 recieves r OP v0 OP v1 ...
  public synchronized void distribute(T r,
  BinaryOperator<T> op) {
    for (int i=0; i<count; i++) { // 1
      T x = value[i];             // 1
      value[i] = r;               // 1
      r = op.apply(r, x);  // 2
    } // 3
  }


  // Wait until node is full, or timeout.
  // 1. Get start time.
  // 2. If node is full, exit.
  // 3. Otherwise, wait with timeout.
  // 4. On waking up, check current time.
  // 5. Reduce timeout by the elapsed time.
  // 6. If timeout done, exit (else retry 2).
  private void waitUntilFull(long w)
  throws InterruptedException {
    long t0 = System.currentTimeMillis(); //1
    while (count < size) { // 2
      wait(w);             // 3
      long t = System.currentTimeMillis(); // 4
      w -= t - t0;     // 5
      if (w<=0) break; // 6
    }
  }
}
