# Condition Variables
Locks are not the only variables needed to build concurrent programs. There are many cases where a thread wishes to check whether a condition is true before continuing its execution.

A parent thread may wish to check whether a child thread has completed before continuing, waiting for a thread to complete is a 	`thread_join`.

If we want to see:

		parent: begin
		child
		parent: end

We could use a shared variable, but this would leave the parent spinning and wasting CPU time. Instead, is there some way to put the parent to sleep until the condition we are waiting for is true?

To **wait** for a condition to become true, a thread can use a **condition variable.** 

*A condition variable is an explicit queue that threads can put themselves on when some state of execution, some condition, is not as desired.*

In OS161, the **Condition Variable** is called the **Wait Channel** and threads are added to the wait channel while they are in the state of being blocked.

When another thread changes state, it can wake one or more waiting threads by **signaling them** to wake up.

A condition variable has two essential operations that define its use:

* *Wait()*: the wait call is executed when a thread wishes to put itself to sleep to wait to be unblocked by some condition it needs changed, changing the thread's state from `running` to `blocked`.

* *Signal()*: the signal call is used when a thread has changed something in the program and wants to wake a sleeping thread that was waiting on this condition, returning its state from `blocked` to `ready`. There is no `ready` state in OS161, instead the thread is just re-added to the run-queue to indicate it is runnable.

Here is an example of a parent waiting for a child:

		int done = 0;
		pthread_mutex_t m = PTHREAD_MUTEX_INITIALIZER; // the lock
		pthread_cond_t c = PTHREAD_COND_INITIALIZER; // the condition variable

		void thr_exit() {
			Pthread_mutex_lock(&m);
			done = 1;
			Pthread_cond_signal(&c);
			Pthread_mutex_unlock(&m);
		}

		void *child(void *arg) {
			thr_exit();
		}

		void thr_join() {
			Pthread_mutex_lock(&m);
			while (done == 0)
				Pthread_cond_wait(&c, &m);
			Pthread_mutex_unlock(&m);
		}

Here the parent thread must wait for done, the **state variable**, to be `1` before it makes sense to continue. It goes to sleep, waiting on the wait channel / condition variable queue until woken.

The `wait()` command is responsible for releasing the lock held by the thread and **putting it to sleep atomically**. When the thread wakes up, it is also the responsibility of the `wait` command to re-**acquire the lock** before resuming.

After the child thread has signalled the parent thread that it can be woken, the parent thread is spinning, trying to acquire the lock 

It is in a while loop in case it is woken up and the case it needs, `done == 1` is still not yet true.

`Wait` is such a powerful command so that it can mitigate possible race conditions when the thread puts itself to sleep:

1. On a uniprocessor, the parent creates the child, then calls `thread_join`, to wait for the child to finish the task it needs. It acquires the lock, checks if the child is done, if it isn't, then it goes to sleep by calling `wait`.

The child then runs, grabbing the lock, changing the data the parent needed, and eventually calling `signal` to wake the parent, before releasing the lock, however.

The `wait` command then tries to re-acquire the lock for the parent and waits for the child to release it, then it finishes what the parent needed to do in the critical section and also releases the lock.

2. The other scenario is that the child is created, and immediately executes, setting `done = 1` and calls the signal to wake a thread on the condition variable, there is none, and is done. The parent then runs `thread_join`, and immediately returns because the condition it needed has been satisfied.

Do we need to check the **state variable** the parent is waiting for? **Yes!!**

if we had:

		void thr_exit() {
			Pthread_mutex_lock(&m);
			Pthread_cond_signal(&c);
			Pthread_mutex_unlock(&m);
		}

		void thr_join() {
			Pthread_mutex_lock(&m);
			Pthread_cond_wait(&c, &m);
			Pthread_mutex_unlock(&m);
		}

Then if as soon as the parent creates the child, the child runs, it will signal an empty wait channel / condition variable. Then the parent will run `thr_join`, and wait unconditionally.

It will never be woken because the signal has already come and gone!

The state variable is essential to the process and is at the center of the lock, wait and signal.

Do we need to hold the **lock** for this to work?

		void thr_exit() {
			done = 1;
			Pthread_cond_signal(&c);
		}

		void thr_join() {
			while (done == 0)
				Pthread_cond_wait(&c, &m);
		}

Of course! Now the parent could be interrupted after the while loop, but then an interrupt occurs. The child sets the state variable to what the parent wants, signals the parent and then returns. Now the parent is dispatched again and immediately waits.

It will never be woken because the child has already signalled.

**Always hold the lock while signalling.** For simplicity, always hold the lock when signalling a thread it can wake.

The converse, **always hold the lock while waiting**, is **mandatory**. The syntax of the `wait` call demands that a thread holds the lock when it goes to sleep since `wait` automatically releases the lock when it goes to sleep, and re-acquires the lock when awoken.

## The Producer/Consumer (Bound Buffer) Problem
* **Producer Thread:** Generates data items and places them in a buffer,

* **Consumer Thread:** Grabs items from the buffer and consume them in some way.

In an HTTP server, a producer puts HTTP requests into a work queue, the bounded buffer, and consumer threads take requests out and process them.

It is also used when you pipe the output of programs together. In `grep foo file.txt | wc -l`, the two processes run concurrently, grep writes lines from the file and the pipe is connected to the count of the number of lines. 

The `grep` process is the producer and the `wc` is the consumer.

		int buffer;
		int count = 0;
		void put(int value) {
			assert(count == 0);
			count = 1;
			buffer = value;
		}

		int get() {
			assert(count == 1);
			count = 0;
			return buffer;
		}

		cond_t cond;
		mutex_t mutex;

		void *producer(void *arg) {
			int i;
			int loops = (int) arg;
			for (i=0; i < loops; i++) {
				Pthread_mutex_lock(&mutex);
				if (count == 1)
					Pthread_cond_wait(&cond, &mutex);
				put(i);
				Pthread_cond_signal(&cond);
				Pthread_mutex_unlock(&mutex);
			}
		}

		void *consumer(void *arg) {
			int i;
			for (i = 0; i < loops; i++) {
				Pthread_mutex_lock(&mutex);
				if (count == 0)
					Pthread_cond_wait(&cond, &mutex);
				get();
				Pthread_cond_signal(&cond);
				Pthread_mutex_unlock(&mutex);
			}
		}

The above, however, is **broken**. While it makes sense and works for **one consumer and one producer**, if there is more than one of these threads, like two consumers, it has 2 problems:

1. Or, what if you have one sleeping, then one is produced, and signals, waking up the thread and placing it on the ready queue, but as the lock is released by the producer, a different consumer grabs the lock, and then gets the value.

The original one has been woken up and thinks it is no longer blocked because it is an `if`, and it will then try and `get` the value, even though it is at 0.

This is the **Mesa semantics** of a signal: a signal to wake a thread does not necessitate the thread runs immediately and there is no guarantee that the state it desires will *still* be true when it does run.

The contrast, the **Hoare semantics**, provides this guarantee that a signalled thread will have run immediately and have its desired condition true.

Which one do you think is easier? The world pretty much only does Mesa. **Always use while loops.**

### Fix 1: Change the IF to a while!
In both the consumer and the producer.

2. The second bug is encountered in the case there are two consumers and one producer. If both consumers go to sleep, the only runnable thread is the producer which then puts a value in the buffer and signals a wake up.

A consumer wakes up, takes the variable, and signals its own wakeup.

If the condition variable / wait channel is managed like a queue, then it will wake the other consumer thread since that one is the next in the queue!

Then that consumer checks the buffer, which is empty, and goes to sleep. Now all three threads are asleep and noone can wake anyone!

Signalling should only allow you to wake up a thread that is not your type, aka: a consumer can wake a producer. A producer can wake a consumer.

The solution here is to use two condition variables, two wait channels:

		cond_t empty, fill;

Where a producer will only wake a consumer:

		PThread_cond_signal(&fill);

And a consumer will only wake a producer:

		Pthread_cond_signal(&empty);

Producer threads wait on the condition variable empty. They will be placed on the empty channel and be woken when it is again empty.

Consumers will sleep on the fill channel and will be woken when the condition that the buffer is filled is met.

## Fix 2: Use Two Condition Variables
Now a consumer never accidentally wakes a consumer, and a producer never wakes a producer.

Notice there are two **condition variables**, not necessarily two **state variables.**

We can then accommodate multiple buffers by using a buffer array and changing the condition that a producer sleeps if the count is at the max number of buffers and a consumer only sleeps if all the buffers are empty.

## Covering Conditions
When a thread calls into memory allocation code, it might have to wait in order for more memory to become free.

When a thread frees memory, it signals that more memory is free.

Consider the following scenario:

There are zero bytes free, and two threads A and B, both request 100 bytes and 10 bytes respectively.

Both go to sleep because the state variable, the number of free bytes, is less than the number they each want allocated.

Now assume that a third thread calls `free(50)`. Like the previous example, we should wake a thread that wants to allocate memory, since we now have some free. But which one do we wake?

If we wake the first, we **still** don't have enough free memory!

The solution is to not signal using `pthread_cond_signal`, rather use `pthread_cond_broadcast`, the equivalent of `wake_all` in OS161 vs `wake_one`.

This wakes up all threads, which since they are using **Mesa semantics**, will check to see if they should have been woken up, and if they shouldn't have, go back to sleep.

It's like your roommate running around the house waking everyone up and asking if each roommate has class at 8:30. One might, and it does wake up, but the others don't until 10 so they doze off.

The downside is the performance impact, since more threads are woken up than is necessary.

		cond_t c;
		mutex_t m;

		void allocate(int size) {
			Pthread_mutex_lock(&m);
			while (bytesLeft < size)
				Pthread_mutex_wait(&c, &m);
			bytesLeft -= size;
			Pthread_mutex_unlock(&m);
		}

		void free(void *ptr, int size) {
			Pthread_mutex_lock(&m);
			bytesLeft += size;
			Pthread_cond_broadcast(&c);
			Pthread_mutex_unlock(&m);
		}

This is a **covering condition** as it covers all the cases where a thread needs to wake up. The cost is that threads that don't need to be woken up are woken up.

Note, we could have used this earlier with the consumer/producer method, instead of having two wait channels / condition variables, just have one and wake up everyone.

But in that situation, a better solution, using two condition variables / wait channels was possible.

Usually, you shouldn't have to broadcast, but if you deal with situations like memory allocation, it is sometimes necessary.

## Conclusion
Condition variables are another important synchronization primitive in addition to locks. They allow threads to sleep while they wait for some condition to become true.

Sleeping is better than yielding because we don't waste re-running threads that won't yet be ready.














 

