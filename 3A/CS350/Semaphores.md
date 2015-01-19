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
The filling of the buffer and incrementing of the index into the buffer is a critical section.

We can attempt to fix this by putting a semaphore lock around the critical sections:

		sem_t mutex;
		sem_init(&mutex, 0, 1);

		void *producer(void *arg) {
			int i;
			for (i = 0; i < loops; i++) {
				sem_wait(&mutex); // our new mutex
				sem_wait(&empty);
				put(i);
				sem_post(&full);
				sem_post(&mutex); // prevent double producing at once
			}
		}

And a similar thing for the consumer. This is problematic though, because it can cause **deadlock.**

**Deadlock** occurs when every thread is waiting and noone is active.

If a thread acquires the `mutex`, indicating that it will be the producer adding something, and then the buffer is full, it will go to sleep.

It can only be woken by a consumer, so a consumer comes along to take one off the values from the full buffer, but it cant because the producer when to sleep still holding the mutex.

The consumer cannot get a buffer until the producer releases the mutex, and the producer cannot release the mutex until a consumer gets a buffer.

This is **deadlock.**

Fortunately, it is easily fixed:

		void *producer(void *arg) {
			int i;
			for (i = 0; i < loops; i++) {
				sem_wait(&empty);
				sem_wait(&mutex); // our new mutex in its PROPER position
				put(i);
				sem_post(&mutex); // prevent double producing at once
				sem_post(&full);
			}
		}

Now we only take the `mutex` if we are going to actually `put` or `get`.

Now if the same situation arises, and a producer goes to produce but it can't because the buffer is full, then it immediately `sem_wait`'s. The consumer comes along, and is able to `sem_post`, so it acquires the free `mutex` with `sem_wait`.

Now we have a simple, working `bounded_buffer` with semaphores for `locks` and `condition variables`.

## Reader-Writer Locks
There are special types of locks that don't conform to the typical critical section scenario.

With file reading/writing, only one thread can write to a file at a time, so a critical section makes sense, but multiple should be able to read from a file.

A solution to this problem is a **reader-writer lock:**

		typedef struct _rwlock_t {
			sem_t lock;         // basic lock to block critical sections
			sem_t writelock;    // write lock
			int readers;
		} rwlock_t;

		void rwlock_init(rwlock_t *rw) {
			rw->readers = 0;
			sem_init(&rw->lock, 0, 1);
			sem_init(&rw->writelock, 0, 1);
		}

		void rwlock_acquire_readlock(rwlock_t *rw) {
			sem_wait(&rw->lock); // only let one acquire readlock at a time
			rw->readers++;
			if (rw->readers == 1)
				sem_wait(&rw->writelock);
			sem_post(&rw->lock);
		}

		void rwlock_release_readlock(rwlock_t *rw) {
			sem_wait(&rw->lock);
			rw->readers--;
			if (rw->readers == 0)
				sem_post(&rw->writelock);
			sem_post(&rw->lock);
		}

		void rwlock_acquire_writelock(rwlock_t *rw) {
			sem_wait(&rw->writelock);
		}

		void rwlock_release_writelock(rwlock_t *rw) {
			sem_post(&rw->writelock);
		}

If some thread wants to update a data structure, it should call the new synchronization primitives:

		rwlock_acquire_writelock()
		rwlock_release_writelock()

These primitives just use a `writelock` semaphore that ensures only one writer can acquire the lock and enter the critical section at a time.

When acquiring the `readlock`, the first reader must also acquire the `write-lock`, by calling `sem_wait` on the `writelock` semaphore.

This way, once a reader is reading, more readers are allowed to read too, but any writer that tries to acquire the write lock must wait for **all** readers to be finished reading.

The last reader to exit will have to call `sem_post` for the `writelock`, allowing writes again.

This approach **works** but has some downsides:

there are fairness concerns. It is easy for **readers** to **starve writers.** To improve this, **think of a way to prevent more readers from entering the lock once a writer is waiting.**

Perhaps once a writer is waiting, readers wait? And then when already reading readers finish, writer goes in?

## Hill's Law
Simple and dumb approaches can work wonders. With locking, sometimes a simple `spin-lock` works the best because it is easy to implement and fast.

While something like the `reader-writer` system sounds cool, they are more complex and complex can mean **slow.** 

> Big and dumb is better

Hill's Law.

## Dining Problem
One of the most famous concurrency problems is the **dining philosopher's problem**. It is fun, but not especially useful.

Assume there are 5 philosophers sitting around a table.

Between each pair of philosophers is a single fork.

A philosopher can think, in which case they use no forks, or a philosopher can eat, in which case they use the fork to both the **left and right of them.**

Each philosopher basically does:

		while(1) {
			think();
			getforks();
			eat();
			putforks();
		}

	The challenge is to write the routines `getforks` and `putforks` such that there is no deadlock, no philosopher starves, and concurrency is high.

Some helper functions to achieve this goal are:

		int left(int p) { return p; }
		int right(int p) { return (p + 1) %5; }

which provide philosophers with their appropriate left and right fork.

If you create a semaphore for each fork, then you can just have:

		sem_t forks[5]
	
		void getforks() {
			sem_wait(forks[left(p)]);
			sem_wait(forks[right(p)]);
		}

		void putforks() {
			sem_post(forks[left(p)]);
			sem_post(forks[right(p)]);
		}

And each fork's semaphore acts as a simple lock.

This solution is **broken** and has **deadlock.**

If a philosopher grabs the fork on their left, and then the thread switches, and another philosopher grabs a fork on their left, and so on, when one of them context switches back, all the forks could be occupied and they cannot grab a right fork, leaving all threads sleeping.

This can easily be fixed by saying that one philosopher, let's pick the last one, picks up forks `right` **then** `left`.

Now They cannot all pick up their left and all wait for rights, because if the first 4 pick up their left forks, the last philosopher will pick up his right fork first and then have to wait, switching back to different philosophers, and when it switches to the one to the last philosopher's left, he will be able to pick up his right.

He will then put his down and post to the philosopher to his left that that philosopher can now use his right fork.

## Zemaphores
Let's build a modern semaphore! It only uses one lock and one condition variable, plus a state variable that tracks the value of the semaphore:

		typedef struct __Zem_t {
				int value;
				pthread_cond_t cond;
				pthread_mutex_t lock;
		} Zem_t;

		// only one thread can call this
		void Zem_init(Zem_t *s, int value) {
			s->value = value;
			Cond_init(&s->cond);
			Mutex_init(&s->lock);
		}

		void Zem_wait(Zem_t *s) { 
			Mutex_lock(&s->lock);
			while (s->value <= 0) {
				Cond_wait(&s->cond, &s->lock);
			s->value--;
			Mutex_unlock(&s->lock);
		}

		void Zem_post(Zem_t *s) {
			Mutex_lock(&s->lock);
			s->value++;
			Cond signal(&s->cond);
			Mutex_unlock(&s->lock);
		}

The locks around `wait` and `post` ensures that only one `wait` and `post` can happen at a time, atomically.

One small difference from Dijkstra semaphores is that this one doesn't go negative, it decreases the count in the `Zem_wait` while greater than zero. This is how Linux semaphores currently function.

In conclusion, semaphores are a powerful and flexible primitive for writing concurrent programs. Some programmers use them exclusively due to their simplicity and utility.























