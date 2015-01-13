# Lock-Based Concurrent Data Structures
Adding locks to a data structure to make it usable by threads makes the structure **thread safe**.

When given a particular data structure, how should locks be added to it in order to make it work **correctly**? How do we add locks such that the data structure yields **high performance**, enabling many threads to access the structure concurrently?

## Data Structure 1: Concurrent Counters
A counter is a simple data structure that can be made thread-safe. It could look like this:

		typedef struct __counter_t {
			int value;
		} counter_t;

		void init(counter_t *c) {
			c->value = 0;
		}

		void increment(counter_t *c) {
			c->value++;
		}

...

This structure can be made **thread safe** like so:

		typedef struct __counter_t {
			pthread_lock_t lock;
			int value;
		} counter_t;

		void init(counter_t *c) {
			c->value = 0;
			Pthread_mutex_init(&c->lock, NULL);
		}

		void increment(counter_t *c) {
			Pthread_mutex_lock(&c->lock);
			c->value++;
			Pthread_mutex_unlock(&c->lock);
		}

		...

We lock the `increment`, `decrement` (not shown but self-explanatory) and accessor (same) so that the instruction to increase/decrease/access, which requires multiple assembly instructions, complete atomically.

It follows a common, simple pattern for concurrent data structures:

It adds a single lock which is acquired when calling a routine that manipulates the data structure that is released when returning from the call.

This is a **working** concurrent data structure, but it is slow. A graph shows that the time to complete a number of interactions with the concurrent counter increases significantly with more threads.

A single thread completed a million calls to increment in 0.03 seconds, while with only two threads it can take 5 seconds.

Making it so that scaling the number of threads the data structure will run on does not change the time to execute the task is called **perfect scaling**.

One thread scalability solution for the counter data structure is called a **sloppy counter**. The sloppy counter works by representing a single logical counter using multiple **local counters**, one per CPU, and a **global counter**.

A machine with 4 CPU's would have 4 local counters, and one global counter and they all have locks.

When a thread running on a core wants to increment the counter, it increments its local counter, and access to this counter is synchronized by the local lock.

Since each CPU has its own local counter, threads across CPU's can update local counters without contention.

To keep the global counter up to date, the local values are periodically transferred to the global counter by acquiring the global lock and incrementing it by the local counter's value, resetting the local counter to zero.

The local-to-global transfer occurs after the local counters reach a certain value, a threshold called `S` for sloppiness. The smaller the `S`, the worse the sloppy counter's performance and the more it resembles the first solution.

The larger the `S` the better the performance, but the global counter will be increasingly off from the actual total of the local counters.

So if `S=1024`, each local counter is going to update the global counter after it reaches 1024, and since they are all doing it at their own rate separately, and the lock for the global counter will only be held for a very brief time, usually at different times from when the other LC's are at 1024.

This makes it very fast, giving 4 threads on 4 CPU's the ability to get to a million *nearly as quickly as it takes one CPU to get to a million!*

This counter could look like the following:

		typedef struct __counter_t {
			int global; // counter for the global 
			pthread_mutex_glock; // lock for global counter
			int local[NUMCPUS]; // array of local counters
			pthread_mutex_lock llock[NUMCPUS]; // array of local locks
			int threshold; // count to reach before transferring local to global
		}

		void init(counter_t *c, int threshold) {
			// init all locks, set all counters to 0, set desired threshold.
		}

		void update(counter_t *c, int threadID, int amt) {
			pthread_mutex_lock(&c->llock[threadID]); // Why lock local?
			c->local[threadID] += amt;
			if (c->local[threadID] >= c->threshold) {
				pthread_mutex_lock(&c->glock);
				c->global += c->local[threadID];
				pthread_mutex_unlock(&c->glock);
				c->local[threadID] = 0;
			}
			pthread_mutex_unlock(&c->llock[threadID]);
		}

		// return the global amount, which could inaccurate.
		int get(counter_t *c) {
			pthread_mutex_lock(&c->glock);
			int val = c->global;
			pthread_mutex_unlock(&c->glock);
			return val;
		}

If `S` is low in the above example, then accuracy of the global counter is good, but the performance is nearly as bad as the original concurrent counter.

If `S` is high, then the performance is excellent, but the global counter is less accurate at any given check.

## Data Structure 2: Concurrent Linked-List
 Examining the following concurrent linked-list insert:

		// Node
		typedef struct __node_t {
			int key;
			struct __node_t *next;
		} node_t;

		// List
		typedef struct __list_t {
			node_t *head;
			pthread_mutex_t lock;
		}

		int list_insert(list_t *L, int key) {
			pthread_mutex_lock(&L->lock);
			node_t *new = malloc(sizeof(node_t));
			if (new == NULL) {
				perror("malloc");
				pthread_mutex_unlock(&L->lock);
				return -1; // fail
			}

			new->key = key;
			new->next = L->head;
			L->head = new;
			pthread_mutex_unlock(&L->lock);
			return 0;
		}

Note that if the malloc for the new node fails, it is necessary to unlock the thread so that the structure doesn't hang forever.

While again this is a basic concurrent data structure, it does not scale well.

One way to **scale linked-lists** is the **hand-over-hand locking** or **lock-coupling** technique.

Instead of having a single lock for the entire list, add a lock per node of the list. When traversing the list, the code first grabs the next node's lock and then releases the current node's lock, ergo *hand-over-hand*.

This structure is in practice hard to make faster than the simple concurrent linked-list.

### What have we learnt?
More concurrency != Faster!!

By acquiring and releasing locks frequently, like in the simple concurrent counter example, it is actually potentially just as slow as a single-threaded technique.

Be wary of control flow changes that lead to function returns, exits or error conditions. They will need to make sure to unlock, deallocate memory, and undo all of the state changes which often leads to errors.

## Data Structure 3: Concurrent Queues
This version of a concurrent queue will use two locks, one for the head and the tail. This way we can have concurrent enqueuing and dequeuing:

// standard node defn ...

		typedef struct __queue_t {
			node_t *head;
			node_t *tail;
			pthread_mutex_t headLock;
			pthread_mutex_t tailLock;
		}

In order to separate dequeue and enqueue cross-over, you need a dummy node in the initialization:

		void Queue_init(queue_t *q) {
			node_t *tmp = malloc(sizeof(node_t));
			tmp->next = NULL;
			q->head = q->tail = tmp;
			pthread_mutex_init(&q->headLock, null);
			pthread_mutex_init(&q->tailLock, null);
		}

		void Queue_Enqueue(queue_t *q, int value) {
			node_t *tmp = malloc(sizeof(node_t));
			assert(tmp != NULL);
			tmp->value = value;
			pthread_mutex_lock(&q->tailLock);
			q->tail-> next = tmp;
			q->tail = tmp;
			pthread_mutex_unlock(&q->tailLock);
	 }

Dequeuing works in the same way and as you can see, each Enqueue and Dequeue don't touch eachother. The Enqueue does not reference the head and vice-versa.

The dummy node made in `init` allows us to not have to set the head in enqueuing which would require the `headLock`.

Similarly, dequeuing never removes the dummy, it returns immediately if the first node, the dummy, has no next link, which would be the first actual node. This way the tail is never changed in the dequeue.

When adding to the tail, the head is always the dummy and when removing from the head, it removes the one after the dummy.

## Data Structure 4: Hash Table
This concurrent hash table uses the concurrent list developed earlier. Its performance is excellent because instead of one lock, it uses a lock per hash bucket. This enables *many* concurrent operations since there are many buckets:

		typedef struct __hash_t {
			list_t lists[BUCKETS];
		} hash_t;

		void Hash_Init(hash_t *H) {
			int i;
			for (i = 0; i < BUCKETS; i++) {
				List_Init(&H->lists[i]);
			}
		}

		int Hash_Insert(hash_t *H, int key) {
			int bucket = key % BUCKETS;
			return List_Insert(&H->lists[bucket], key);
		}

Using the concurrent list build earlier, we will only hang when inserting into the same bucket as one that already has its lock acquired. With many buckets, this is seldom.

This is allowed because different buckets don't affect each other, at least when not having to grow/shrink the table which is not in the scope of this example.

### Knuth's Law
When building a concurrent data structure, start by building the most basic approach, which is to add a single big lock to provide synchronized access.

If that then suffers from performance issues, refine it. Don't make things unnecessarily fast at the risk of introducing bugs.

> Premature optimization is the root of all evil.






			


		

