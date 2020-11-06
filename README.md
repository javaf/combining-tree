Array queue is a bounded lock-based FIFO queue using
an array. It uses 2 separate locks for head and tail.

```java
enq():
1. Lock tail.
2. Try enq.
3. Unlock tail.
```

```java
deq():
1. Lock head.
2. Try deq.
3. Unlock head.
```

```java
tryEnq():
1. Ensure queue is not full
2. Save data at tail.
3. Increment tail.
```

```java
tryDeq():
1. Ensure queue is not empty.
2. Return data at head.
3. Increment head.
```

```bash
## OUTPUT
Starting 10 threads with sequential queue
2: failed enq
5: failed deq
6: failed deq
7: failed deq
8: failed deq
1: failed deq
4: failed deq
9: failed deq
1: dequeued 0/1000 values
2: dequeued 0/1000 values
4: dequeued 0/1000 values
5: dequeued 698/1000 values
6: dequeued 0/1000 values
7: dequeued 0/1000 values
8: dequeued 0/1000 values
9: dequeued 0/1000 values
Was LIFO? false

Starting 10 threads with array queue
Was LIFO? true
```

See [ArrayQueue.java] for code, [Main.java] for test, and [repl.it] for output.

[ArrayQueue.java]: https://repl.it/@wolfram77/array-queue#ArrayQueue.java
[Main.java]: https://repl.it/@wolfram77/array-queue#Main.java
[repl.it]: https://array-queue.wolfram77.repl.run


### references

- [The Art of Multiprocessor Programming :: Maurice Herlihy, Nir Shavit](https://dl.acm.org/doi/book/10.5555/2385452)
