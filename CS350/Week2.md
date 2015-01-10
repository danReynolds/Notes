# Concurrency
Make it fast and accurate.

Programs can be multi-threaded, each thread is an independent agent in the program, doing things on the program's behalf.

But these threads access memory, and for them, each spot in memory is like a dish on the kitchen table. If two people reach for the same dish, one is going to be left unsatisfied. But if each person is allowed to grab their dishes one at a time, it is much slower.

There must be a way to ensure everyone gets the food they want quickly.

The OS must support multi-threaded applications with primitives like **locks** and **condition variables.**

## Threads
Each thread is much like a separate process, except that threads **share** the same address space and can access the same data.

The thread's are run inside of processes, but processes have their own virtual address spaces that allows a program to behave as if it has its own memory, whereas in reality the OS is multiplexing address spaces across physical memory/disk.

A Thread has a *context*:

* *Program Counter (PC)* to track where the program is fetching instructions from
* *Private virtualized set of registers* used for computations, therefore if there are two threads that are running on a single processor, when switching from thread T1 to another T2, a **context switch** must take place.

The register state of T1 must be saved and the register state of T2 must be restored. This two-step process is called dispatching T2.

The **saved state** of a thread is saved to a **Thread Control Block (TCB)** to store the state of each thread of a process.

A context switch does not change the address space.

In the simple model of the address space of a single-threaded process, there is a single stack at the bottom of the address space and growing upwards.

In a multi-threaded process, each thread runs independently and has its **own stack in the address space.** They share an address space dedicated for the process, but have their own stacks within that address space.

Therefore any stack-allocated variables, parameters and return values that are put on the stack must be placed in **thread-local storage**, the stack of the relevant thread.

There is no reason to assume that a thread that is created first will run first. If there is a threadA and threadB, threadA is created first and runs first, then threadB is created and runs second or both are created and then threadA finishes then threadB finishes or even threadB finishes then threadA finishes. The scheduler decides which thread runs first and it isn't order of creation.

## Sharing Data
Imagine that two threads want to update a shared global variable. 

If thread1 runs with the intention of incrementing the counter, but is preempted before updating the value of the counter at address blah, it saves its registers in its stack and then dispatches thread2.

Thread2 finishes, incrementing the counter and then saving, switching context back to thread1. Thread1 then updates the counter with the value it has saved from its register in its stack, which is the same as the current counter because thread2 has already made the counter larger!

While the counter should be 2 larger, it is only 1.

This is a **race condition.** The results depend on the timing execution of the code. This is a indeterminate result because running it multiple times will give unknown values.

# Atomically (As a Unit)
This problem would be prevented if all of the instructions from the thread were executed at once. If we could do `memory-add` to add to the value in memory, instead of first bringing it into one of the thread's registers, adding and then storing, we would either exit the thread having completed our single instruction or not, since an interruption cannot occur mid-instruction.

The hardware guarantees that the instruction has either been run, or not. This is an all or none approach that would guarantee our threads behaving as expected.

What we actually do instead of combining arbitrary instructions that are pertinent only to our specific needs, is to build a *general* set of **synchronization primitives.** 

These **hardware synchronization primitives** are used in combination with some help from the OS to build multi-threaded code that access critical sections in a synchronized, controlled manner. 

What is a critical section? Here are some concurrency definitions:

* **Critical Section:** a piece of code that accesses a **shared** resource, usually a variable or a data structure.

* **Race Condition:** arises if multiple threads enter the critical section at roughly the same time, both attempt to update the data structure, leading to an undesirable outcome.

* **Indeterminate Program:** a program with one or more race conditions. The outcome is not deterministic.

How do you build useful synchronization primitives with the hardware?

Additionally, there is the problem of one thread *needing* to wait for another before it continues.

When a thread performs a disk I/O and is put to sleep while the I/O does its thing, it must sleep and wake appropriately. 

We are learning about multi-thread processes because the OS was the **first** concurrent program and many techniques were created for use within the OS.

If two processes are both writing to a file, and both wish to append the data to the file, because an interrupt may occur at any time, the code that updates these shared structures is a **critical section**. It is accessing a **shared** resource, a shared data structure.

Therefore OS designers, from the very beginning of the introduction of the interrupt, had to worry about how to manage synchronization primitives. Every file system structure and virtually every kernel data structure has to be carefully accessed with the proper synchronization primitives.

**Atomic Operations:** a series of actions is *all or nothing.* Either all of the actions you group together occur, or none of them do. Sometimes the grouping of many actions into a single atomic action is called a **transaction.** 

Synchronization primitives turn short sequences of instructions into atomic blocks of execution.

File systems use techniques like journaling or copy-on-write in order to atomically transition their on-disk state, critical for operating correctly in the face of system failures.









