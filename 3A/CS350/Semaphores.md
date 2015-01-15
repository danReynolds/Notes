# Semaphores
You need both locks and condition variables to solve many concurrency problems.

A semaphore is a third type of synchronization primitive. It is a single primitive for **all things** related to synchronization. A semaphore can be used as both a lock and a condition variable.

A **semaphore**  is an object with an integer value that can be manipulated with two routines: `sem_wait`, and `sem_post`.

Historically, and in OS161, these are called `P` for the dutch word *to probe*, and `V`, the dutch word for *to test*.

The initial value of the semaphore determines its behaviour, so before calling any other routine to interact with the semaphore, it must be initialized with some value:

		sem_t s;
		sem_init(&s, 0, 1);

## Build Later, Use Now
This covers the usage of semaphores, not building but they basically do:

		int sem_wait(sem_t &s) {
			// decrement the value of semaphore s by one
			// wait if value of semaphore s is negative
		}

		int sem_post(sem_t *s) {
			// increment the value of semaphore s by one
			// if there are one or more threads waiting, wake one
		}

`sem_wait`: Will return immediately if the value is still positive after decrementing. Otherwise, it will cause the caller to suspend execution and wait for a `post`.

If multiple threads call `sem_wait`, and the value is negative, they are all queued to be woken up.

`sem_post`: does not wait for some particular condition like `sem_wait`, instead it simply increments the value of the semaphore and then wakes up a thread.

The value of a semaphore, if negative represents the **number of waiting threads.**

Now in your critical section, put:

		sem_t m;
		sem_init(&m, 0, X); // initial value of its variable, X should be 1

		sem_wait(&m);
		// critical section
		sem_post(&m);

To make our semaphore function as a simple lock, the initial value passed in should be `1`.

The first time the code is executed, the semaphore's value is initially decremented, so if it was `1`, then it would now be zero. But since it is not negative, it does not wait.

Now if another thread ran the code, the `sem_wait` would decrement the value to -1 and therefore tell the semaphore to have the thread wait.

Then when a `post` occurs, the semaphore value is incremented by 1 and a thread is woken, if there is one to be woken.

## Binary Semaphores (Locks)
The above semaphore can be called a **Binary Semaphore** because a lock only cares about two states, locked or unlocked, and we can record this with a variable.

## Semaphores as Condition Variables
Semaphores are also useful when a thread wants to halt its progress waiting for a condition to become true.

For example, a thread may wish to wait for a list to become non-empty in order to remove an element from it.

		sem_t s;

		void * child(void *arg) {
			printf("child");
			sem_post(&s);
			return NULL;
		}

		int main(int argc, char *argv[]) {
			sem_init(&s, 0, X); // X should be 0, it should subtract and wait
			printf("Parent starts");
			Pthread_t c;
			Pthread_create(c, NULL, child, NULL);
			sem_wait(&s); // wait for child
			printf("parent done");
		}

A parent thread creates another thread, but instead of doing a `join` to wait for the child do wake it up, the parent calls `sem_wait(&s)`.

1. First Case
The parent is then put to sleep since the semaphore value is below zero. The child thread eventually calls post, incrementing the semaphore value back to zero and waking the sleeping parent thread.

2. Second Case
The child immediately interrupts the parent before the parent calls `sem_wait`. The child executes `sem_post`, so that the value of the semaphore is now 1. Now when the context switches back to the parent, when it runs `sem_wait` the value of the semaphore will be zero, but it won't put the parent to sleep because the value is not negative.

## Producer/Consumer (Bounded-Buffer) Problem
### Attempt 1
Introduce two semaphores `empty` and `full` which the threads will use to indicate when a buffer entry has been emptied or filled respectively.

		int buffer[MAX];
		int fill = 0;
		int use = 0;

		void put(int value) {
			buffer[fill] = value;
			fill = (fill + 1) % MAX;
		}

		int get() {
			int tmp = buffer[use];
			use = (use + 1) % MAX;
			return tmp;
		}

		sem_t empty;
		sem_t full;

		void *producer(void *arg) {
			int i;
			for (i = 0; i < loops; i++) {
				sem_wait(&empty);
				put(i);
				sem_post(&full);
			}
		}

		void *consumer(void *arg) {
			int i, tmp = 0;
			while (tmp != -1) {
				sem_wait(&full);
				tmp = get();
				sem_post(&empty);
			}

		int main(int argc, char *argv[]) {
			sem_init(&empty, 0, MAX);
			sem_init(&full, 0, 0);
		}

Setting `MAX` to 1 for the producer, we can have 1 buffer in the array and then have a simple 1 consumer, 1 producer setup on a single CPU.

If the consumer runs first, it will immediately decrement to -1 and wait because its semaphore, `full`, was setup with 0.

Then when the producer runs, it will run `sem_wait`, but `MAX` is set to 1, and so it will decrement to zero and still run the producer, putting in the value and running `sem_post` for the consumer, making its value go to 0. It then loops and runs `sem_wait` again, decrementing to -1 and going to sleep.

It then context switches to the consumer thread, which gets the value the producer put in, calls `sem_post` for the producers and loops, calling `sem_wait`, setting its value to -1 again and going back to sleep.
 
The producer gets control, having a value of 0 and immediately puts in a value to the buffer. It then calls `sem_post`, waking a consumer, which gets a value of 0 again, and when it gets the CPU, will get the value in the buffer, `sem_post` the producer again and go back to sleep.

This process continues again and again.

This example should work for a `MAX = 1` with any number of threads because of the use of the two semaphores. Consumers wake producers, producers wake consumers.

**But there is still a problem.** What if the `MAX = 10`, with multiple producers and multiple consumers? Now two producers can run at once!

This introduces a race condition:

If there are two producers A and B, both calling `put` at the same time, one puts a value in the buffers, and gets interrupted, the second then overwrites what it put in the buffer. Fill is incremented by each, but one of the buffers is not set and the other was set twice!

## Adding Mutual Exclusion












