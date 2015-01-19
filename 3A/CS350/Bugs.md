# Common Concurrency Problems
## What Type of Bugs Exist?
The two major non-deadlock bugs are **atomicity violation** bugs and **order violation bugs**.

### Atomicity-Violation Bugs

Here is a simple one in MySQL:

		Thread1::
			if (thd->proc_info) {
				...
				fputs(thd->proc_info, ...) {
				...
				}
			}

		Thread2::
			thd->proc_info = NULL;

If thread1 runs and gets past the if statement, and then context switches to thread 2, which sets the `proc_info` to `NULL`, then when context switches back to thread1, it will `fputs` `NUll`!

Atomicity violation bugs are therefore any bug where a code region is intended to be atomic, but the atomicity is not enforced during execution.

This could of course be fixed with a lock.

### Order-Violation Bugs

		Thread1::
		void init() {
			...
			mThread = PR_CreateThread(mMain, ...);
			...
		}

		Thread2::
		void mMain() {
			...
			mState = mThread->State;
		}

In this order violation bug, the code in thread2 assumes that thread1 has already been run, creating `mThread`. But if Thread2 runs first, `mThread1` will not have been created and an error will occur.

An order violation bug is therefore a bug that occurs when the order between two groups of memory execution is flipped. A should be done before B, but instead B is run before A and the correct order is not enforced during execution.

A condition variable is good at enforcing order, `while mThread == NULL` have Thread2 sleep, then have `Thread1` wake `Thread2` afterwards.

Most of the time **Non-Deadlock Bugs** are a result of Atomicity, or Order issues.

## Deadlock Bugs
Deadlock occurs when a thread A is holding a lock 1 and is waiting for a lock 2. Now if the thread B that can release lock 2 needs to first acquire lock 1, neither thread can run.

		Thread1:		Thread2:
		lock(L1);		Lock(L2);
		lock(L2);		Lock(L1);

## Why Do Deadlocks Occur?
In large code bases, complex dependencies arise between components. The design of locking strategies in large systems must be carefully done to avoid deadlock in the case of circular dependencies.

Encapsulation often results in deadlock. Modularity can hide code that would reveal a potential locking conflict. 

### Conditions for Deadlock
Four conditions need to hold for deadlock to occur:

1. **Mutual Exclusion:** Threads claim exclusive control of resources tat they require (acquire lock).

2. **Hold and Wait:** Threads hold resources allocated to them that they have acquired while waiting for additional locks they wish to acquire. (Nested locks where hold the outer but not the inner).

3. **No Preemption:** Locks cannot be forcibly removed from threads that are holding them.

4. **Circular Wait:** There exists a circular chain of threads such that each thread holds one of the locks that are being requested by the next thread in the chain.

## Preventing Deadlock
### Circular Wait
The most practical prevention technique is to write locking code that never induces a circular wait. Provide a **total ordering** on lock acquisition.

For example, if there are only two locks in the system, L1, L2, we can prevent deadlock by always acquiring L1 before L2 (**never change the order locks are acquired**). Then if someone wants to acquire L2, they must first have L1, which means that noone else can have L2.

### Hold-And-Wait
The hold-and-wait requirement for deadlock can be avoided by acquiring all locks at once, atomically:

		lock(prevention);
		lock(L1);
		lock(l2);
		...
		unlock(prevention);

This wraps all grabbing of locks in a **global prevention lock.** Now there can never be circular dependencies because if anyone holds L1 or L2, they also hold the prevention lock and if someone wants L1 or L2 they must be available, since if they were not available, the prevention lock would not have been available.

This solution is problematic and decreases concurrency by requiring all locks immediately, using a global lock, rather than just using locks when they are needed to protect critical sections.

### No Preemption
Multiple lock acquisition can be perilous because while waiting for one lock, often a thread holds another. This can be solved using a `trylock`, available in many libraries which will grab the lock if it is available or return `-1` indicating that the lock is held right now and that you should try again later, **instead of putting the thread to sleep.**

		top:
			lock(L1);
			if (trylock(L2) == -1) {
				unlock(L1);
				goto top;
			}

Now if the second lock you want is held, just unlock the first one you held so that you never sleep with an acquired lock.

This does introduce the problem of **livelock.** It is possible, though unlikely that two threads could both be repeatedly attempting this sequence and repeatedly failing to acquire both locks.

This is also problematic because jumping back to previous code may have unwanted ramifications.

### Mutual Exclusion
The final prevention technique would be to avoid the need for mutual exclusion at all. 

There have been data structures built that are actually **wait-free.** The idea is simple:

using powerful hardware instructions, you can build data structures in a manner such that they will not require locking.

for example:

		void AtomicIncrement(int *value, int amount) {
			do {
				int old = *value;
			} while (CompareAndSwap(value, old, old + amount) == 0);
		}

This will increment a value atomically, because it is relying on a hardware instruction `CompareAndSwap` that is performed atomically. CompareAndSwap only updates the value with the new value, in this case we want the new value to be `old + amount`, **if** the old value is equal to some expected value.

## Deadlock Avoidance Via Scheduling
Instead of deadlock prevention, in some scenarios it is better to use deadlock **avoidance.**

Avoidance requires some global knowledge of which locks various threads might grab during their execution, and subsequently schedules said threads in a way such that no deadlock can occur.

In Deadlock Prevention, we change how locks are held to avoid all 4 conditions being satisfied, but in Deadlock Avoidance, we manipulate how the threads are run, not how the locks are acquired.

If T1 and T2 grab L1 and L2 in some order, and T3 grabs L2 and T4 grabs no locks, then it is clear to the scheduler that the only way deadlock could occur would be if T1 and T2 were run at the same time.

Therefore to avoid deadlock, the scheduler just makes sure that T1 and T2 don't run at once. One must finish before the other can start.

It is okay for T1 and T3 to run at the same time because T3 will just sleep until T1 is done, and T1 would sleep until T3 is done, but they won't ever cause each other to sleep at the same time.

Deadlock Avoidance is only ever really useful in very limited environments when one has full knowledge of the entire set of tasks that must be run and the locks that they need.

Also, this approach limits concurrency, as threads can only run in certain ways, sometimes not running anything in order to wait for a contentious thread to finish.

> Not everything worth doing is worth doing well.

This is a terrific Engineering maxim. If a bad thing happens rarely, certainly one should not spend a great deal of effort to prevent it, particularly if the cost of the bad thing is small.

## Detect and Recover
A final way to handle deadlock is to allow deadlocks to occur occasionally and then take some action to deal with it once detected.

For example, if an OS freezes once a year, you would just reboot it and continue on. if deadlocks are rare, such a non-solution is pragmatic.

Many database systems employ deadlock detection and recovery techniques. A deadlock detector runs periodically, building a resource graph and checking it for cycles. In the event of a cycle, the system needs to be restarted.

# Summary
* Atomicity Bugs = Needs Locking (something should have been atomic)
* Order Bugs = Needs Condition Variables (something should have waited for a certain state)

Be careful. Use locks only when you truly must.











