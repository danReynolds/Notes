# Address Spaces
Initially, the OS was a set of routines that sat in physical memory (RAM) and there would be one running program, a process, that sat in physical memory and used the rest of memory.

Eventually, **multiprogramming** became prevalent, allowing multiple processes to run at a given time, switching between them such as in the instance when one would do I/O.

Doing this increased the utilization of the CPU. These efficiency increases were critical when hardware was costly.

This led to **time sharing,** the running of once process for a short time, giving it full access to all memory then stopping it, saving its state to disk (we don't do this now, not to disk like that), loading some other processor's state, running it for a while.

But as memory grew, saving it all to disk when processes switched was **very slow.** Saving and restoring register-level state, like the PC, was very quick, but saving and restoring the entire contents of memory to and from disk was terrible.

It is now accepted that a process should  be **left in memory** while switching between the, allowing the OS to implement time sharing efficiently.

As time-sharing became more popular, there were new demands on the OS. Allowing multiple programs to reside concurrently in physical memory makes **protection** an important issue. A process should not be able to read or tamper with some other process' memory.

## The Address Space
The OS must create an **easy to use** abstraction of physical memory. This is called the **address space.** It is the running program's view of memory in the system.

The address space contains all of the memory state of the running program:

1. The code of the program must reside in memory somewhere and are kept in the address space.
2. The program uses a stack to keep track of where it is in the function call chain (stackframe), as well as to allocate local variables and pass parameters and return values to and from routines.
3. The heap is used for **dynamically allocated, user managed memory.** 

Since the code is static and easy to place in memory, it goes at the top of the address space and know it won't need any more space the as the program runs.

Next, there are two regions of the address space that may grow or shrink as the program runs.

They go at opposite ends with the **heap growing downwards from the top and the stack growing upwards from the bottom.** This is convention.

This is changed a little when we have multiple threads in the same address space, each with their own stack.

## Virtualizing Memory
How can the OS build this abstraction of a private, potentially large address space for multiple running processes, all sharing memory, on top of a single, physical memory?

When the OS does this, we say it is **virtualizing memory.** The running program thinks it is being loaded into memory at a particular address, say 0, and has a potentially very large address space, when this is not the case. 

All the processes think this. Ignorance is bliss.

When a process A tries to perform a load at address 0, a **virtual address,** the OS must work with hardware support to make sure the load doesn't actually go to physical address 0, but rather to physical address 320KB or wherever process A is actually loaded.

## Principle of Isolation
If two entities are properly isolated from one another, this implies that one can fail without affecting the other.

Operating systems isolate processes in this way to prevent them from harming each other. By using memory isolation, the OS further ensures that running programs cannot affect the operation of the underlying OS.

Some modern OS's take isolation even further by walling off pieces of the OS from other pieces of the OS.

These walled regions are called **microkernels** and they provide greater reliability than typical monolithic kernel designs.

# Goals of Virtual memory
One of the jobs of the OS is to virtualize memory, and virtualize it with style. One major goal of a virtual memory system is **transparency.** The illusion provided by the OS should not be visible to applications. A transparent system is one that is hard to notice. Consider the work that the OS does behind the scenes as the cloud, but the fact that the applications don't see this cloud, and see transparently through it, is the illusion.

The program is not aware that memory is virtualized, rather the program behaves as if it has its own private physical memory. Behind the scenes, the OS and hardware does all the work to multiplex memory among many different jobs and creates the illusion.

Another goal of virtual memory is **efficiency.** The OS should make virtualization as efficient as possible. It should not make processes run more slowly, or take up too much space for the structures needed to support virtualization.

A third goal of virtual memory is **protection.** The OS should make sure to **protect** processes from one another and to protect itself.

When a process performs a load, or store or any instruction fetch, it should not be able to access or affect in any way the memory not in its address space.

Protection enables isolation of processes, each in their own sandboxes.

## Every Address is Virtual
When you print out a pointer in `C`, the large hex value returned is a **virtual address.**

Any address that the programmer sees at the user-program level is virtual. It's only the OS through virtualization of memory that knows where in the physical memory of the machine the instructions and data values lie.

If you print out an address in a program, it's a virtual one. An illusion of how things are laid out in memory. Only the OS knows the real locations.

When printing the code, heap, and stack locations in a `C` program on my linux box, the code and heap were at lower addresses, close together, and the stack was at a very high address.

This makes sense since the code comes first, then the heap grows down and the stack grows up.

All of these addresses are again, virtual, and will be translated by the OS and hardware in order to get values from their true physical locations.

## Conclusion
The Virtual Memory system is responsible for providing the illusion of a large, sparse, private address space to programs, which hold all of their instructions and data therein.

The OS, with some help from hardware, takes each of these virtual memory references and translates them into physical addresses in physical memory.



