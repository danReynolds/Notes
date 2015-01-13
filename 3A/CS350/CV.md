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

* *Signal()*: the signal call is used when a thread has changed something in the program and wants to wake a sleeping thread that was waiting on this condition, returning its state from `blocked` to `ready`.

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

The `wait()` command is responsible for releasing the lock held by the thread and putting it to sleep atomically. When the thread wakes up, it is also the responsibility of the `wait` command to acquire the lock again before resuming.

We wait in a while loop in case we are woken up and the condition is still not as desired.

This is done in order to mitigate possible race conditions when the thread puts itself to sleep:

1. When the parent creates the child thread and then calls join









 

