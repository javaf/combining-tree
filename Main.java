import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

class Main {
  static Queue<Integer> queue;
  static CombiningTree<Integer> tree;
  static int TH = 8, NUM = 10;

  // Each safe thread enqs N numbers and deqs N, adding
  // them to its own deqValues for checking; using
  // ArrayQueue.
  static Thread thread(int id) {
    return new Thread(() -> {
      try {
      for (int i=0; i<NUM; i++) {
        Integer r = tree.getAndOp(1,
          (Integer x, Integer y) -> x);
        queue.add(r);
      }
      } catch (InterruptedException e) {}
    });
  }

  // Checks if each thread dequeued N values, and they are
  // globally unique.
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

  static Thread[] startOps() {
    Thread[] t = new Thread[TH];
    for (int i=0; i<TH; i++)
      t[i] = thread(i);
    for (int i=0; i<TH; i++)
      t[i].start();
    return t;
  }

  static void awaitOps(Thread[] t) {
    try {
    for (int i=0; i<TH; i++)
      t[i].join();
    } catch (InterruptedException e) {}
  }

  public static void main(String[] args) {
    queue = new ConcurrentLinkedQueue<>();
    tree = new CombiningTree<>(3);
    tree.set(0);
    log("Starting "+TH+" threads doing ops ...");
    Thread[] t = startOps();
    awaitOps(t);
    log("\nWas valid? "+wasValid());
  }

  static void log(String x) {
    System.out.println(x);
  }
}
