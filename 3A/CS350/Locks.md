# Locks
Locks are used to combat the problem of executing a series of instructions atomically.

Take the example of a critical section like `x = x + 1`.

Then to use a lock, add code around the critical section:

		lock mutex; // mutex stands for mutual exclusion
		lock(&mutex);
		balance = balance + 1;
		unlock(&mutex);

A lock is just a variable, so to use one, you must declare a lock variable of some kind. The lock variable holds the **state of the lock** at any instant in time.

The state of the lock variable is either available and not held by any thread, or acquired, and is held by exactly one thread that is (if done properly) executing a critical section.

If another thread then calls `lock` on the **same lock variable**, in this case `mutex`, it will not return while the lock is held by another thread.

The lock variable could hold other information too like *which thread holds the lock*, or a queue for ordering lock acquisition.

1. **Course-grainted Locking Strategy:** one big lock that is used any time any critical section is accessed
2. **Fine-grained Locking Strategy:** protect different data and data structures with different locks, thus allowing more threads to be in locked code at once.

## Building a Lock
How can we build an efficient lock? Efficient locks provide mutual exclusion at low cost, and also might attain a few other properties. What hardware support is needed? What OS support?

Over the years, a number of different hardware primitives have been added to the instruction set of various computer architectures. We will look at how to use them in order to build a mutual exclusion primitive like a lock.

## Evaluating Locks
Criteria for building a good lock:

1. **Mutual Exclusion:** It should provide mutual exclusion, preventing multiple threads from entering a critical section.
2. **Fairness:** does each thread contending for the lock get a fair show at acquiring it once it is free? Does any thread contending for the lock **starve**, never obtaining it when it gets released by other critical sections?
3. **Performance:** what is the time overhead added by using the lock? When no-one else is competing for it and one thread acquires and releases the lock, what is the overhead? If multiple threads are contending for the lock, are there performance issues? If there are **multiple CPUs** and threads on each are contending for the lock, how does it perform?

## Controlling Interrupts
One of the earliest solutions used to provide mutual exclusion was to just disable interrupts for critical sections. Now they're atomic! Easy, right?

The code was simply:

		void lock() { disableInterrupts(); }
		void unlock() { enableInterrupts(); }

This approach is delightfully simple, you don't have to work too hard to get why this works.

### The Downsides:
* This requires us to allow any thread to perform a *privileged* operation, namely turning interrupts off and on and trust that this ability is not abused. A bad program could call `lock` immediately and not release until its end, or even not release at all. This would require a **restart** to fix! Using interrupts requires too much faith in applications.
* The approach does not work on multiprocessors. It doesn't matter if interrupts are disabled since on multiple CPU's, multiple threads can run truly concurrently and both be in a critical section at the same time.
* The approach is very inefficient. Compared to normal instruction execution, code that masks or unmasks interrupts is slow.

Disabling interrupts can make sense for the OS sometimes, since it can trust itself not to abuse it unlike apps.

## Test and Set (Atomic Exchange)
Since disabling interrupts does not work on multiple processors, hardware support for locking was introduced.

### First Attempt: A simple Flag:

		void init(lock mutex) {
			mutex-> flag = 0;
		}
		
		void lock(lock mutex) {
			while(mutex->flag == 1)
				; //spin
			mutex-flag = 1;
		}

		void unlock(lock mutex) {
			mutex->flag = 0;
		}

Here we just test to see if the flag is set, and if it isn't, our thread can set the flag, indicating that it has acquired the lock. Any other thread that tries to acquire the lock will now have to wait.

This code has two flaws, correctness and performance:

1. There are correctness problems that could easily arise as a result of inputs. Remember, our locks need to be atomically sound too!

If thread1 is interrupted after the while loop but before setting the flag by thread2, and then thread2 acquires the lock successfully, being interrupted by thread1 in return, thread1 will also set the flag to 1 and both threads will have the lock at the same time.

**Always** think about concurrency as a **Malicious Scheduler**. Assume that threads will be interrupted at the **worst possible times.** As long as your synchronization primitives work at the most inopportune time, you're good!

2. The performance problem results from **spin-waiting**, running a loop until a condition is false wastes time and CPU waiting for another thread to release a lock. On a uniprocessor system, the thread that the waiter is waiting for cannot even run because the waiter continues its infinite loop!

## Second Attempt: Spin Lock with Test and Set:
While the previous attempt was a good direction, it is not possible to implement without some support from the hardware. Some systems provide an instruction to support the creation of simple locks based on the previous concept.

This hardware instruction is referred to as the `test-and-set`:

		int TestAndSet(int *ptr, int new) {
			int old = *ptr;
			*ptr = new;
			return old;
		}

This sequence of operations is performed atomically at the hardware level. It allows you to test what the old value was, by returning it, and sets the memory location to a new value.

This is enough to build a **Spin Lock**:

		typedef struct __lock_t {
			int flag;
		} lock_t;

		void init(lock_t *lock) {
			lock->flag = 0;
		}

		void lock(lock_t *lock) {
			while (TestAndSet(&lock->flag, 1) == 1)
				; // spin-wait
		}

		void unlock(lock_t *lock) {
			lock->flag = 0;
		}

Now since all of the locking only takes one line, it cannot be interrupted such that we reach undesired behaviour since it is an atomic action.

Now we have built a working mutual exclusion primitive!

This is a **spin lock**. It is the simplest type to build and spins using CPU cycles until the lock becomes available.

To work correctly, a **spin lock** requires a **preemptive scheduler**, one that will interrupt a thread via a timer, in order to run a different thread.

Without preemption, spin locks don't make sense on a single CPU, as a thread on a CPU will never relinquish it.

## Evaluating Spin Locks
How good is this solution? It is clearly correct as shown above. Is it **fair**? Can you guarantee that a waiting thread will ever enter the critical section?

This is not true, a spinning thread may spin forever and be starved because the thread is just waiting to be preempted by the scheduler and some other thread could always be picked ahead of it and acquire the lock.

The **performance** is especially poor for single CPU's. If 5 threads being rotated by the scheduler, and the first one run acquires the lock, the next 4 will each preempt each-other and spin until preempted. This wastes a lot of clock cycles.

On multiple CPU's, spin locks work well if the number of threads is about the number of CPU's. If Thread A on CPU 1 and Thread B on CPU 2 are both contending for a lock, if Thread A grabs the lock, and then B tries to, B will spin. However, Thread A should finish quickly, and Thread B will quickly acquire the lock and enter its own critical section.

## Attempt 3: Compare and Swap
	Another hardware primitive that some systems provide is the **compare and swap** instruction:

		int compareAndSwap(int *ptr, int expected, int new) {
			int actual = *ptr;
			if (actual == expected)
				*ptr = new;
			return actual;
		}

If the actual value is equal to what is expected, it updates to the new value, and either way returns the old value.

To build the lock:

		void lock(lock_t *lock) {
			while (compareAndSwap(&lock->flag, 0, 1) == 1)
				;spin
		}

If the lock is not currently acquired, it will set it to 1 and return 0, exiting the while loop.

If the lock is currently acquired, the compareAndSwap will do nothing and return 1, keeping the while loop going.

While testAndSet similarly returned the old value, it always updated, while compareAndSwap will only update if it succeeds.

## Attempt 4: Load-Linked and Store Conditional:
On MIPS the **load-linked** and **store-conditional** instructions can be used in tandem to build locks and other concurrent structures.

In C, what these instruction look like would be:

		int LoadLinked(int *ptr) {
			return *ptr;
		}

		int StoreConditional(int* ptr, int value) {
			if (noone has updated *ptr since the loadlinked to this address) {
				*ptr = value;
				return 1; // success
			else {
				return 0; // fails
			}
		}

The loadLinked is just like the standard load instruction.

The key difference is with the `store-conditional` method, which only succeeds and updates the value if no intermittent store to the address has taken place. 

This way, we prevent critical sections like if we wanted two threads to both count to a 1000 for the same global variable. It would only succeed and store the updated value if no-one else has in the meantime updated the value.

Build a lock:

		void lock(lock_t* lock) {
			
			while(loadLinked(lock->flag) == 1 || storeConditional(lock->flag, 1) == 0)
				; // spin
		}

This solution is elegant, but doesn't improve the **fairness** of the locking system.

## Attempt 5: Fetch and Add:
The fetch-and-add hardware primitive increments instead of updating:

		int fetchAndAdd(int *ptr) {
			int old = *ptr;
			*ptr = old + 1;
			return old;
		}

This builds a more interesting **ticket lock**. Instead of using a single value, this solution uses a ticket and turn variable in combination to build a lock. When a thread wants to acquire the lock, it first does an atomic fetch-and-add on the ticket value.

That value is now considered the thread's turn. The globally shared `lock->turn` is then used to determine which thread's turn it is.

		typedef struct __lock_t {
			int ticket;
			int turn;
		}	lock_t;
		
		void lock_init(lock_t *lock) {
			lock-> ticket = 0;
			lock-> turn = 0;
		}

		void lock(lock_t *lock) {
			int myturn = FetchAndAdd(&lock->ticket);
			while (lock->turn != myturn)
				; //spin
		}

		void unlock(lock_t *lock) {
			FetchAndAdd(&lock->turn);
		}

Vincenzo's Deli!! With a **ticket lock**, everyone takes a number when they want to acquire the lock. They then spin until their number is up. When a thread releases the lock, it increments the turn.

This solution meets the **correctness** requirement, and unlike the previous examples, it also meets the **fairness** requirement. `**Ticket Locks** ensure that every thread gets to acquire the lock in the order that they asked for it.

All of the spinning solutions share the same problem, most prevalent on a single CPU:

**If one thread with the lock gets interrupted, the next thread will spin until it is preempted, wasting time and CPU.**

How can we develop a lock that doesn't needlessly waste time spinning on the CPU?

## Yield Baby Yield
Hardware support can create working, and with ticket locks, fairness. But performance is lacking.

The first solution to avoid spinning is to `thread_yield` and switch context to another thread when a thread knows it is about to spin.

The thread calls `yield` when it wants to give up the CPU.

A thread can be in one of three states:
1. Running
2. Ready
3. Blocked

Yield moves the thread from the running state to the ready state and dispatches another thread to run.

On a single processor, this fixes the performance problem. Now if one thread has the lock and is interrupted by another that tries to acquire the lock, it will yield and return context to the first thread. The first thread then finishes and there is no hanging.

If there are many threads contending for a lock, they all yield repeatedly until it hits the thread with the lock and that thread releases it:

		void lock() {
			while (TestAndSet(&flag, 1) == 1)
				yield();
		}

Assuming a round-robin scheduler, each of the 99 will execute this **run-and-yield** pattern before the thread holding the lock runs again.

While better than spinning, this **Yield-Baby-Yield** strategy is not optimal, as the time required for context switches is costly.

Additionally, a thread can get caught in an endless yield loop while other threads repeatedly enter and exit the critical section.

## Using Queues: Sleeping Instead of Spinning
The scheduler determines which thread runs next, if the scheduler makes a bad choice, so far a thread runs that must either **spin** or **yield**.

Either way, be it hanging, or context switching repeatedly, there is waste in time and CPU and only one type of spinning is far.

The real problem with the previous approach is that they leave too much to chance.

To solve this, exert control over who gets to acquire the lock next after the current holder releases it. Requires more OS support and a queue to keep track of who is waiting for the lock.

`park` will put a thread to sleep, and `unpark` will wake a particular thread designated by its ID. These commands will put a thread to sleep if it tries to acquire an in use lock and wakes it when the lock is free.

This strategy will combine the initial `test-and-set` **spin-lock** approach with a queue of lock waiters. The queue also helps to control who gets the lock next and avoids starvation.

		typedef struct __lock_t {
			int flag;
			int guard;
			queue_t *q;
		}

		void lock_init(lock_t *m) {
			m->flag = 0;
			m->guard = 0;
			queue_init(m->q);
		}

		void lock(lock_t *m) {
			while(TestAndset(&m->guard, 1) == 1)
				; // acquire guard lock by spinning
			if (m->flag == 0) {
				m->flag = 1; // lock is acquired
				m->guard = 0;
			}
			else {
				queue_add(m->q, gettid());
				m->guard = 0;
				park();
			}
		}

		void unlock(lock_t *m) {
			while TestAndSet(&m->guard, 1) == 1)
				; // acquire guard lock by spinning
			if (queue_empty(m->q))
				m->flag = 0; // let go of lock, no-one wants it
			else
				unpark(queue_remove(m->q)); // hold lock (for next thread)
			m->guard = 0;
		}

This **Guard Test-And-Set Queue** method combines a few techniques to get improvements in **performance** and **fairness**.

Firstly, it uses the idea of a **Guard** that is the new parameter for the familiar TestAndSet. While the guard is active (1), any other thread must **spin-lock** like normal. The difference is that we only spin-lock for the time it takes to atomically test and update the flag.

This way we don't have to **Test-And-Set** the flag, because no other code could manipulate the lock's flag while the lock's guard is active.

If the lock is available, the flag is changed and the thread acquires the lock. Otherwise the thread is added to the lock's queue.

Either way the guard is then lowered so that other threads can try to acquire the lock.

The `unlock` method similarly raises the guard, so that the next deserving thread in the queue does not check the flag too early and is not re-added to the queue while the current thread with the lock is in the process or releasing the lock.

This approach **does not avoid lock-spin entirely**, but the benefit is that the lock-spin isn't around the critical region, it is around the checking of the flag. That way, it only spins for 3 or 4 lines of code execution, which is a much smaller delay than if it was spinning for the duration of the transaction.

Notice that if the queue is empty, the flag is lowered, otherwise the **flag remains up**. This is because when the thread resume from `park` it is at the end of the `lock` method and cannot try to re-lock. Instead the lock from the previous thread is never lowered so the new thread is also considered locked immediately.

Additionally, there is a **perceived race condition** between the guard dropping in the `lock` method and the `park` method executing.

If there were two threads, one with the lock and one about to park, a switch at this exact moment to the thread with the lock would result in it unlocking, unparking a thread that has not yet been parked!

If the `unpark` call of a thread on the queue but not yet parked even worked (undefined behaviour), it would then immediately park itself, never to be awoken.

This problem is called the **wakeup/waiting race** and to avoid it, there must be further changes.

Include a third call, `setpark` that goes before the lowering of the guard that indicates that the thread is about to park. If the thread then switches context before it can park, when it resumes the park call that runs just returns immediately instead of sleeping.






















