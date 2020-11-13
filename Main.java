import java.util.*;
import java.util.concurrent.*;


class Main {
  static Queue<Integer> queue;
  static CombiningTree<Integer> tree;
  static int TH = 25, ARY = 3, NUM = 100;
  // queue: used to store old (get) values (unique check)
  // tree: combining tree where threads do increment ops
  // TH: number of threads
  // ARY: arity of combining tree
  // NUM: number of increment ops each thread performs


  // Each thread performs increment op using the combining
  // tree, and saves old (get) values in the queue, for
  // uniqueness check later.
  static Thread thread(int id) {
    return new Thread(() -> {
      try {
      long start = System.currentTimeMillis();
      for (int i=0; i<NUM; i++) {
        Integer r = tree.getAndOp(1,
          (Integer x, Integer y) -> x + y);
        queue.add(r);
        Thread.yield();
      }
      long stop = System.currentTimeMillis();
      log(id()+": done in "+(stop-start)+"ms");
      } catch (InterruptedException e) {}
    });
  }

  // Check if total sum is as expected.
  // Check if all old values are unique.
  static boolean wasValid() {
    int a = tree.get().intValue();
    if (a != TH*NUM) return false;
    Set<Integer> s = new HashSet<>();
    while (queue.size()>0) {
      Integer n = queue.remove();
      if (s.contains(n)) return false;
      s.add(n);
    }
    return true;
  }

  // Setup the combining tree for threads.
  static void setupTreeAndQueue() {
    int depth = (int) Math.ceil(Math.log(TH)/Math.log(ARY));
    tree = new CombiningTree<>(depth, ARY);
    tree.set(0);
  }

  // Setup the queue for storing old values.
  static void setupQueue() {
    queue = new ConcurrentLinkedQueue<>();
  }


  // Start threads doing increments using tree.
  static Thread[] startOps() {
    Thread[] t = new Thread[TH];
    for (int i=0; i<TH; i++)
      t[i] = thread(i);
    for (int i=0; i<TH; i++)
      t[i].start();
    return t;
  }

  // Wait until all threads done with increments.
  static void awaitOps(Thread[] t) {
    try {
    for (int i=0; i<TH; i++)
      t[i].join();
    } catch (InterruptedException e) {}
  }


  public static void main(String[] args) {
    setupTree();
    setupQueue();
    log(ARY+"-ary "+depth+"-depth Combining tree.");
    log("Starting "+TH+" threads doing increments ...");
    Thread[] t = startOps();
    awaitOps(t);
    log("Total: "+tree.get());
    log("\nWas valid? "+wasValid());
  }


  static void log(String x) {
    System.out.println(x);
  }

  static long id() {
    return Thread.currentThread().getId();
  }
}
