# Multiprocessor Scheduling
To understand the new issues surrounding multiprocessor scheduling, we have to understand a new and fundamental difference between single-CPU and hardware and multi-CPU hardware.

This difference centers around the use of hardware **caches**.

In a system with a single CPU, there is a hierarchy of **hardware caches** that help the processor run programs faster.

Caches are small, fast memories that hold copies of popular data found in the main memory.

Main memory, in contrast, holds all of the data, but access to data is slower.

Consider a program that issues an explicit load instruction to fetch a value from memory and a simple system with only one CPU. The CPU has a small cache of 64KB and a large main memory.

In anticipation of data re-use, the processor may put a copy of the loaded data into a CPU cache.

Caching with multiple CPUs becomes much more complicated. If CPU 1 gets data D from address A, updates it to D`, it may just put it in the CPU 1 cache, rather than writing back to address A at this time, because it would be too costly.

Then if the program switches to run on CPU 2 instead, the CPU 2 cache will not have the value it is looking for, and any fetch from address A would get the old D since D` was just in CPU 1's cache and had not been written back yet.

This is the problem of **cache coherence**. The basic solution is provided by the hardware: by monitoring memory accesses, hardware can ensure that the view of a single shared memory is preserved.

One way to do this on a bus-based system is to use **bus-snooping**, each cache pays attention to memory updates by observing the bus that connects them to main memory.

When a CPU sees an update for a data item it holds in its cache, it will notice the change and either **invalidate** its copy, removing it from its own cache, or update it, putting the new value in its cache too.

## Synchronization
When accessing or updating shared data items or structures across CPUs, mutual exclusion primitives must be employed to guarantee correctness.

If there is a shared queue being accessed by multiple CPUs simultaneously, without locks, adding or removing elements from the queue concurrently will not work as expected.

Protecting these functions with locks can lead to significant performance hits as the number of CPUs grows.

## Cache Affinity
A process builds up a fair bit of state in the caches and TLBs when it runs. The next time the process runs, it is often better to run it on the same CPU, as it will run faster if some of its state is already present in the caches on that CPU.

If instead, the process runs on a different CPU, the performance of the process will be worse as it will have to reload the state each time it runs.

A multiprocessor scheduler should consider cache affinity, keeping processes with the cache they have already built a reputation with.

## Single-Queue Scheduling
The most basic approach to multi-processor scheduling is to apply the same principles used in single-processor scheduling.

This is called **single-queue multiprocessor scheduling (SQMS)**. It is the simplest approach, however it lacks **scalability.**

Locks are required to guard the single-queue, however, locks greatly reduce performance as the number of CPUs grow.

The second problem is cache affinity. If the single-queue scheduler keeps picking the next job to run from the globally shared queue, the processes end up moving from CPU to CPU, invalidating caches and having to rebuild their cache each time.

Most SQMS include some sort of affinity mechanism to try and make it more likely that processes will continue to run on the same CPU if possible.

This approach is straightforward to implement given an existing single-CPU scheduler, however it does not scale well due to synchronization overheads and does not preserver cache affinity.

## Multi-Queue Scheduling
Some systems opt for multiple queues, one per CPU. This is the **multi-queue multiprocessor scheduling (MQMS)**. In MQMS, each queue follows a particular scheduling discipline like round-robin. When a job enters the system, it is placed on exactly one scheduling queue, according to some heuristic.

Then it is scheduled independently, avoiding sharing information or the need to synchronize the queue.

While this appears like it might be much more scalable than using a single queue, it introduces the new problem of **load imbalance.**

If CPU 1's queue is near empty, while CPU 2's queue is near full, the jobs on the first queue will run much more frequently, which is unfair for the jobs on the second queue.

We could even have CPU 1's queue *empty* while CPU 2 runs multiple jobs on round-robin rotation.

The obvious solution to the problem is to move jobs around, called **job migration.** By migrating a job from one CPU to another, true load balance can be achieved.

How should the system decide to enact migrations? In a **work-stealing** approach, jobs will occasionally peak at another target queue to see how full it is. If the target queue is notably more full than the source queue, the source will *steal* some of its jobs to help balance the load.

It is important to not look around too often, as this will incur high cost and prevent scalability.

But if you don't look often enough, then it is possible to get severe load imbalances. Finding the correct threshold is difficult and not clear.

## Linux Multiprocessor Schedulers
No common solution has appeared in the Linux community.

The Completely Fair Scheduler (CFS) uses multiple queues, and a deterministic proportional-share approach.

## Conclusion
The single-queue approach (SQMS) is straightforward, but has difficulty scaling due to synchronization and cache affinity issues.

The multi-queue approach (MQMS) scales better and handles cache affinity, but has trouble with load imbalances and is more complicated.





