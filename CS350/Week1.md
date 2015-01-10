# Introduction to Operating Systems
*Virtualization* is taking a physical resource like the processor, memory, or disk and transforming it into a more general, powerful, and easy-to-use virtual form of itself. The OS is a *virtual machine.*

The OS provides a *standard library* of system calls that are available to applications. 

Each of the CPU, memory, and disk are **resources** of the system. The OS acts as the resource manager.

The OS makes it seem like the system has a very large number of virtual CPUs.

If I run multiple programs, one printing A repeatedly, another B and another C:

		./print "A" &; ./print "B" &; ./print "C" &;

It doesn't print AAAAAABBBBBCCCCCC but ABCABCACB etc.

Turning a single CPU into a seemingly infinite number of CPUs allows many programs to run at once and is called **virtualizing the CPU**.

**Policies** are used throughout the OS to answer questions like, if two programs both want to run now, which *should?*

We can see how the OS is the resource manager.

## Virtualizing Memory
Each running process has a **PID**, process identifier, of the running program.

If you run the same program multiple times, it runs as separate processes. If both programs update the value at the same memory address, you don't get the value from the one process in the other process.

It is as if each running program has its own private memory, instead of sharing the same physical memory with other programs.

The OS is **virtualizing memory.** Each process has its own private *virtual address space*, which the OS maps onto the physical memory of the machine.

A memory reference within one running program **does note affect the address space of other processes or the OS itself.** It is as if the process has the physical memory all to itself.

In reality, the physical memory is a shared resource and the OS manages it.

## Concurrency
We need to manage multiple threads properly. The multiple threads run in the same memory space as other functions, meaning that the values at memory can be changed by other threads and values are volatile.

## Persistence
When the computer loses power, any data in memory is lost. Hardware and software needs to store data persistently, using such devices as hard-drives.

Unlike the abstractions provided by the CPU and memory, applications share information on disc. There is not a private virtualized disk for each app, so that apps can share and manipulate common files.



