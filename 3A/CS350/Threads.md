# Thread API

## Creating a Thread
To write multi-threaded programs, begin with thread creation.

When creating a thread, while these notes differ from how OS161 does it, there are general things you need and which will be discussed.

The thread comes with:
* A name, mostly for debugging
* A pointer to the process the thread is being made on
* A function pointer that points to the function the thread should start in. This is called the entrypoint for the thread.
* Data 1 and Data 2 that are passed to the entry point.

Data 1 is of type `void *`, a void pointer. This is  general purpose pointer. Having a void pointer as an argument allows the passing of *any* type of argument. It can act as an integer pointer, character, pointer, etc and store an address of any type of variable.

Meanwhile a function of type `void *` can return any type of result.

Void pointers are used for re-usability of pointers. If you have an int and a float and want to use a pointer for both, can make a void pointer.

To dereference it, first cast it as the desired class:
		
		int x = 0;
		void *ptr = &x;
		*((int*)ptr) == 0;

Then use it for a float:

		float y = 0;
		ptr = &y;
		*((float*)y) == 0;

You can package up a pointer to a structure in a void pointer, then return it, and unpackage it into the type of the structure again. It's like a general box you keep things in for transit.

## Thread Joining
Moving on, what if we want to wait for a thread to complete? This uses a **thread_join** routine.

As args, `thread_join` takes what thread to use and a pointer that is set by `thread_join` to point to the thread's return value when it finishes. This must be a pointer to a pointer because `thread_join` changes the value at the address of the passed in pointer to the address of the return value. This way we can change the memory address the original pointer contains to the address of the return value from the thread and that will be persisted when we return from thread_join.

A pointer to a pointer is used like in the following:

		int **a;
		int i = 5, j = 6;
		int* b = &i;
		int* c = &j;
		a = &b;
		*a = c;
		*b == 6 //true

a points to the address of b. This is saying go to the value at the address of a and set that to the c. So now the value at the address of b is the address of c, so when you dereference b you get 6.

Once the thread specified in the `thread_join` finishes running, the main thread that called `thread_join` returns and the value of the pointer passed in, which is also a pointer, can access the other thread's return value.

The thread should **never** return something allocated on the thread's stack. Use malloc and the heap instead.

When the thread finishes, it deallocates everything on the stack. Therefore allocate it on the heap.

Creating a thread by calling `thread_create` and then `thread_join` can equivalently be accomplished in a **procedure call.** Join is best used for making sure that work completed before exiting or moving on to another job.

## Locks
Locks provide mutual exclusion to a **critical section**. The most basic implementation would use a call to `lock` and then when finished, `unlock`.

When you have a region of code that is a critical section, protect it by using locks in order to make it operate as desired.

it could simply look like:

		declare lock;
		acquire_lock(&lock);
		# Now within the lock do whatever you want
		x = x + 2;
		release_lock(&lock);

The thread that calls this **only** acquires that lock if no other thread holds that lock when `acquire_lock()` is called. If it can, it will then enter the critical section.

If in fact, another thread currently holds that particular lock, the thread trying to grab that lock will **not return** from the call, **it will hang**, until it has acquired that lock. It will wait for the thread holding that lock has released it via the `release_lock()` call.

Many threads may be stuck waiting in the acquire lock function, all waiting for the thread with the lock to call `release_lock`.

Note that there isn't just one lock, the `acquire_lock(&lock)` command takes a lock and then any other thread trying to acquire that **same lock in memory** is going to hang. If it tried to acquire a different lock that is not already acquired it would be fine.

Locking can fail! Make methods that do what you want, like `Malloc` instead of `malloc` that call the latter and then check for an error. This keeps code clean and safe.

The locks should be **destroyed** when released and no longer needed.

There are usually also methods like `trylock`, which tries to acquire the lock and returns failure if it is already held.

`timedlock` does the same thing as `trylock` but doesn't return immediately if it fails, it will instead wait a certain amount of time to see if it can acquire the lock, otherwise it also fails.

There are times, however, when it is desirable to not timeout and actually get stuck in a lock acquisition routine forever.

## Condition Variables
The second major component of any threads library are **condition variables.** They are used when some kind of signaling must take place between threads, like if one thread is waiting for another to do something before it can continue and needs notification.

The condition variable, in this case called `initialized`, must have a lock associated with the condition and that must be held to use a `wait` or `signal`.

`wait` puts the calling thread to sleep so that it can wait to be woken by another thread's signal.

Here it is in action:

		acquire_lock(&lock);
		while (initialized == 0)
			wait(&init, &lock);
		release_lock(&lock);

This code first acquires the lock for waiting, and waits until the **condition variable** initialized is changed by another running thread.

The sleeping thread could be woken by another thread like this:

		acquire_lock(&lock);
		initialized = 1;
		signal(&init);
		release_lock(&lock);

When signalling, always make sure to acquire the lock, which will hang until some other thread releases the lock.

This ensures that we don't accidentally introduce race conditions. One **important** question to ask is how can the thread that wakes the sleeping thread acquire the lock if the sleeping thread has it and has gone to sleep?

This is possible because the `wait` call that puts the thread to sleep also takes as a parameter the `lock` which is immediately released by the wait call.

When the thread is woken, it then re-acquires the lock and must release it again at the end of execution outside of the while loop.

This ensures that any time code is executing in the waiting thread, it has the lock.

The second thing to notice is that instead of any if statement, the wait command is run in a while loop.

This will be discussed in detail more later, but it is generally the safe thing to do.

There are some cases where the waiting thread could be woken up when it shouldn't have been, so it is important to re-check that what we were actually waiting for, in this case the variable `initialized`, has actually been updated.

Waking up should be an indication that what you want *might have changed*, but that you should verify this by re-checking the condition variable.

While it is possible to use a flag instead to signal between the two threads:

		while (initialized == 0)
			; //do nothing

Meanwhile the signalling thread just changes initialized:

		initialized = 1;

This keeps the first thread spinning, wasting CPU instead of sleeping and this ad-hoc way of synchronizing threads is often buggy.

**Always use condition variables with locks and wait/signal calls.**

To sum up this basic thread introduction, remember the following:

* **Keep locking and signalling clean and simple**
* **Only talk between threads when necessary**
* **Initialize locks and condition variables**
* **Check return codes, check return codes, and check return codes!**
* **Pass and return variables on the heap or globally when working between stacks**
* **Stack variables are specific to a thread and cannot be accessed (easily) from another thread. Plus once the thread dies, its stack is remove and those variables are GONE.**
* **Always use condition variables with locking and waiting/signalling to signal between threads. Simple flags are dangerous.**





 
