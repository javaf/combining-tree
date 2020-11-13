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
3-ary 3-depth Combining tree.
Starting 25 threads doing increments ...
30: done in 51ms
14: done in 60ms
13: done in 68ms
23: done in 53ms
12: done in 56ms
21: done in 54ms
32: done in 56ms
31: done in 56ms
22: done in 54ms
28: done in 7831ms
11: done in 7836ms
10: done in 7838ms
19: done in 7833ms
20: done in 7833ms
29: done in 7831ms
24: done in 8165ms
33: done in 8160ms
25: done in 8165ms
16: done in 8167ms
34: done in 8159ms
15: done in 8167ms
26: done in 11970ms
17: done in 11972ms
27: done in 12239ms
18: done in 12241ms
Total: 2500

Was valid? true
```

See [ArrayQueue.java] for code, [Main.java] for test, and [repl.it] for output.

[ArrayQueue.java]: https://repl.it/@wolfram77/array-queue#ArrayQueue.java
[Main.java]: https://repl.it/@wolfram77/array-queue#Main.java
[repl.it]: https://array-queue.wolfram77.repl.run


### references

- [The Art of Multiprocessor Programming :: Maurice Herlihy, Nir Shavit](https://dl.acm.org/doi/book/10.5555/2385452)
