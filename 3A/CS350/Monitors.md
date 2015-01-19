# Monitors
Here is a pretend monitor written in C notation:

		monitor class account {
		private:
			int balance = 0;
		public:
			void deposit(int amount) {
				balance = balance + amount;
			}
	
			void withdrew(int amount) {
				balance = balance - amount;
			}
		}

C does not actually support monitors and the keyword `monitor` is used as it would be in other languages that do support it.

Java supports monitors, with what are called *synchronized methods.* 

In the above code, there are **critical sections:**

* The balance cannot be accesses/changed while another thread is concurrently access/changing it as well.

In a **monitor** class, this problem is eliminated. Monitors ensure that **only one thread can be active within the monitor at a time.**

Multiple threads know that deposit and withdraw require mutual exclusion to be preserved.

THe monitor accomplishes this with a simple lock. Whenever a thread tries to call a monitor routine, it implicitly tries to acquire the monitor lock.

If it succeeds, it will run the method code, otherwise the thread is blocked.

You can imagine the `deposit` and `withdraw` methods above to be surrounded by `mutex_lock` and `mutex_unlock`.

## Why Use Monitors
At the time, OOP was just becoming popular. The idea was to gracefully blend some of the key concepts of concurrent programming with some of the basic approaches of OOP.

## What More Does It Provide?
Monitors support **condition variables:**

		monitor class BoundedBuffer {
		private:
			int buffer[MAX];
			int fill, use;
			int fullEntries = 0;
			cond_t empty;
			cond_t full;

		public:
			void produce(int element) {
				if (fullEntries == MAX)
					wait(&empty);
				buffer[fill] = element;
				fill = (fill + 1) % MAX;
				fullEntries++;
				signal(&full);
			}

			int consume() {
				if (fullEntries == 0)
					wait(&full);
				int tmp = buffer[use];
				use = (use + 1) % MAX;
				fullEntries--;
				signal(&empty);
				return tmp;
		}

The **state variable** is `fullEntries`. This solution is similar to the semaphores implementation.

The code above uses **Hoare Semantics**, `signal` immediately wakes a thread and runs it, so context is **immediately** transferred to the thread that was just woken.

In Hoare Semantics, after a signal, the caller does not continue to run, context switches to the callee, the one who is woken by the `signal`.

> In theory, there is no difference between theory and practice, but in practice, there is.

Hoare Semantics were difficult to implement, and were replaced with **Mesa Semantics** in practice. The critical difference is that in **Mesa Semantics,** the `signal` wakes the thread on the condition variable wait channel, but that thread still needs to re-acquire the lock it was run under.

It does **not** immediately run, rather it has been **hinted** that its state variable could now be satisfied.

When it is woken, according to Mesa semantics, it must *recheck* its state variable and either sleep again, or continue if it is satisfied.

If you remember nothing else from this class, **always recheck the condition after being woken.** Use **while loops, not if loops**.

