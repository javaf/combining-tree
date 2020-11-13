A Combining Tree is an N-ary tree of nodes, that follows
software combining to reduce memory contention while
updating a shared value.

The shared value is placed at the root of the tree, and
threads perfrom `getAndOp()` at the leaf nodes. Each leaf
node handles N threads. The combined value is then
propagated up the tree by an active thread. There can be
several active threads, but eventually one active thread
updates the root node. It then shares this message to all
other threads behind it.

This was contention at a single memory location is avoided.
However, i guess that such a design is normally useful
only in hardware, as it becomes too slow in software.
Useful for educational purposes.

```java
CombiningTree.getAndOp(x, op):
Gets current value, and then updates it.
x: value to OP with, op: binary op
1. Select leaf index using thread id.
2. Perform get & op at desired leaf.
```

```java
CombiningTree.getAndOp(x, op, i):
Gets current value, and then updates it.
x: value to OP with, op: binary op, i: leaf index
1. Perform get & op at desired leaf (ensure in limit).
```

```java
Node.getAndOp(x, op)
Gets current value, and then updates it.
x: value to OP (accumulate), op: binary operator
1. Wait until node is free.
2. Perform get & op based on 3 possible cases.
2a. Root node
2b. Active thread (first to visit node)
2c. Passive thread (visits later)
```

```java
Node.getAndOpRoot(x, op)
Performs get & op for root node.
x: value to OP (accumulate), op: binary operator
1. Get old value, by combining (a).
2. Empty the node.
3. Insert a OP x
3. Return old value.
```

```java
Node.getAndOpActive(x, op)
Performs get & op for active thread.
x: value to OP (accumulate), op: binary operator
1. Insert value.
2. Wait until node is full, or timeout.
3. We have the values, so start pushing.
4. Combine values into one with OP.
5. Push combined value to parent.
6. Distribute recieved value for all threads.
7. Start the pulling process.
8. Decrement count (we have our pulled value).
9. Return pulled value.
```

```java
Node.getAndOpPassive(x, op)
Performs get & op for passive thread.
x: value to OP (accumulate), op: binary operator
1. Insert value.
2. Wait until active thread has pulled value.
3. Decrement count, one pulled value processed.
4. If count is 0, the node is free.
5. Return value of this thread.
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

See [CombiningTree.java], [Node.java] for code, [Main.java] for test, and [repl.it] for output.

[CombiningTree.java]: https://repl.it/@wolfram77/combining-tree#CombiningTree.java
[Node.java]: https://repl.it/@wolfram77/combining-tree#Node.java
[Main.java]: https://repl.it/@wolfram77/combining-tree#Main.java
[repl.it]: https://combining-tree.wolfram77.repl.run


### references

- [The Art of Multiprocessor Programming :: Maurice Herlihy, Nir Shavit](https://dl.acm.org/doi/book/10.5555/2385452)
