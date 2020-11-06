import java.util.*;

class Main {
  static Deque<Integer> queue;
  static ArrayQueue<Integer> concurrentQueue;
  static List<Integer>[] deqValues;
  static int TH = 10, NUM = 1000;

  // Each unsafe thread enqs N numbers and deqs N, adding
  // them to its own deqValues for checking; using Java's
  // sequential queue implementation, ArrayDeque.
  static Thread unsafe(int id, int x, int N) {
    return new Thread(() -> {
      String action = "enq";
      try {
      for (int i=0, y=x; i<N; i++)
        queue.addLast(y++);
      Thread.sleep(1000);
      action = "deq";
      for (int i=0; i<N; i++)
        deqValues[id].add(queue.removeFirst());
      }
      catch (Exception e) { log(id+": failed "+action); }
    });
  }

  // Each safe thread enqs N numbers and deqs N, adding
  // them to its own deqValues for checking; using
  // ArrayQueue.
  static Thread safe(int id, int x, int N) {
    return new Thread(() -> {
      String action = "enq";
      try {
      for (int i=0, y=x; i<N; i++)
        concurrentQueue.enq(y++);
      Thread.sleep(1000);
      action = "deq";
      for (int i=0; i<N; i++)
        deqValues[id].add(concurrentQueue.deq());
      }
      catch (Exception e) { log(id+": failed "+action);
      e.printStackTrace(); }
    });
  }

  // Checks if each thread dequeued N values, and they are
  // globally unique.
  static boolean wasLIFO(int N) {
    Set<Integer> set = new HashSet<>();
    boolean passed = true;
    for (int i=0; i<TH; i++) {
      int n = deqValues[i].size();
      if (n != N) {
        log(i+": dequeued "+n+"/"+N+" values");
        passed = false;
      }
      for (Integer x : deqValues[i])
        if (set.contains(x)) {
          log(i+": has duplicate value "+x);
          passed = false;
        }
      set.addAll(deqValues[i]);
    }
    return passed;
  }

  @SuppressWarnings("unchecked")
  static void testThreads(boolean safe) {
    queue = new ArrayDeque<>();
    concurrentQueue = new ArrayQueue<>(TH*NUM);
    deqValues = new List[TH];
    for (int i=0; i<TH; i++)
      deqValues[i] = new ArrayList<>();
    Thread[] threads = new Thread[TH];
    for (int i=0; i<TH; i++) {
      threads[i] = safe?
        safe(i, i*NUM, NUM) :
        unsafe(i, i*NUM, NUM);
      threads[i].start();      
    }
    try {
    for (int i=0; i<TH; i++)
      threads[i].join();
    }
    catch (Exception e) {}
  }

  public static void main(String[] args) {
    log("Starting "+TH+" threads with sequential queue");
    testThreads(false);
    log("Was LIFO? "+wasLIFO(NUM));
    log("");
    log("Starting "+TH+" threads with array queue");
    testThreads(true);
    log("Was LIFO? "+wasLIFO(NUM));
    log("");
  }

  static void log(String x) {
    System.out.println(x);
  }
}
