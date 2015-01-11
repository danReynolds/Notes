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

You provide what thread to use and a pointer that will point to the thread's return value.

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

The thread that calls this **only** acquires the lock if no other thread holds the lock when `acquire_lock()` is called. If it can, it will then enter the critical section.

If in fact, another thread currently hold the lock, the thread trying to grab the lock will **not return** from the call, **it will hang**, until it has acquired the lock. It will wait for the thread holding the lock has released it via the `release_lock()` call.

Many threads may be stuck waiting in the acquire lock function, all waiting for the thread with the lock to call `release_lock`.


