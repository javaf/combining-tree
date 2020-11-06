import java.nio.*;
import java.util.concurrent.locks.*;

// Array queue is a bounded lock-based FIFO queue using
// an array. It uses 2 separate locks for head and tail.

class ArrayQueue<T> {
  Lock headLock;
  Lock tailLock;
  T[] data;
  int head;
  int tail;
  // headLock: lock for enq() at head
  // tailLock: lock for deq() at tail
  // data: array of values in stack
  // head: front of queue (0)
  // tail: rear of queue (0)

  @SuppressWarnings("unchecked")
  public ArrayQueue(int capacity) {
    headLock = new ReentrantLock();
    tailLock = new ReentrantLock();
    data = (T[]) new Object[capacity+1];
    head = 0;
    tail = 0;
  }

  // 1. Lock tail.
  // 2. Try enq.
  // 3. Unlock tail.
  public void enq(T x) {
    try {
    tailLock.lock();   // 1
    tryEnq(x);         // 2
    } finally {
    tailLock.unlock(); // 3
    }
  }

  // 1. Lock head.
  // 2. Try deq.
  // 3. Unlock head.
  public T deq() {
    try {
    headLock.lock();   // 1
    return tryDeq();   // 2
    } finally {
    headLock.unlock(); // 3
    }
  }

  // 1. Ensure queue is not full
  // 2. Save data at tail.
  // 3. Increment tail.
  protected void tryEnq(T x) {
    int tail2 = (tail + 1) % data.length;  // 1, 3
    if (tail2 == head)                     // 1
      throw new BufferOverflowException(); // 1
    data[tail] = x; // 2
    tail = tail2;   // 3
  }

  // 1. Ensure queue is not empty.
  // 2. Return data at head.
  // 3. Increment head.
  protected T tryDeq() {
    if (head == tail)                       // 1
      throw new BufferUnderflowException(); // 1
    T x = data[head];                // 2
    head = (head + 1) % data.length; // 3
    return x;                        // 2
  }
}
