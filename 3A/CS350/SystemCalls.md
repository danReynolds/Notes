# Mechanism: Limited Direction Execution
In order to virtualize the CPU, the OS needs to share the CPU among many jobs at the same time.

The basic idea is to run one process for a while, then switch to another, and so on in a technique called **time sharing.**

The challenges in building such virtualization begin with *performance.* How can virtualization be added without adding excessive overhead?

The second challenge is *control.* How can we run processes efficiently while retaining control over the CPU?

The OS cannot allow a process to run forever and take over the machine or access info that it should not.

## Basic Technique: Limited Direct Execution
To make a program run as fast as one expects, OS developers came up with a technique called **limited direct execution.**

The direct execution part of the idea is simple: just run the program directly on the CPU. When the OS wishes to start a program running, it creates a process entry for it in the process list, allocates some memory for the stack/heap, and loads the program code into memory from disk, locating its entry point and running the code:

		create entry for process list
		allocate memory for program
		load program into memory
		set up stack with argc/argv
		clear registers
		execute call main()
				run main()
				execute return from main()
		free memory of process
		remove from process list

This simple approach has issues:

1. if we just run the program, how can the OS make sure the program doesn't do anything we don't want it to do? While still running it efficiently?

2. when running a process, how does the OS stop it from running and switch to another process, thus implementing the **time sharing** required to virtualize the CPU?

## Problem 1: Restricted Operations
Direct execution has the advantage of being very fast, since the program runs natively on the hardware CPU and executes as quickly as one would expect.

But running on the hardware CPU has security concerns, what if the process wishes to perform some kind of restricted operation? Such as issuing an I/O request to disk or gaining access to more system resources such as CPU or memory?

The hardware assists the OS by providing **different modes of execution.** In **user mode**, applications do not have full access to hardware resources.

In **kernel mode**, the OS has access to the full resources of the machine. Special instructions to **trap** into the kernel and **return from trap** back to the user-mode programs are also provided, as well as instructions that allow the OS to tell the hardware where the **trap table** resides in memory.

One approach would be to let any process do whatever it wants in terms of I/O and other related operations.

But if we wanteed to build a file system that checks permissions before granting access to a disk, for example, we can't let any user process issue I/O to the disk, if we did, a process could simply read or write the entire disk and all protections would be lost.

The approach taken is to introduce a new processor mode, called **user mode.** Code that runs in user mode is restricted in what it can do.

For example, when running in user mode, a process can't issue I/O requests, doing so would result in the processor raising an exception and the OS would kill the process.

In **kernel mode**, code that runs can do what it likes, including privileged operations such as issuing I/O requests and executing all types of restricted instructions.

So what should a **user process** do when it wishes to perform some sort of privileged operation that only a **kernel process** can do?

To enable this, virtually all modern hardware provides the ability for user programs to perform a **system call.**

System calls allow the kernel to carefully expose certain key pieces of functionality to user programs, such as accessing the file system, creating and destroying processes, communicating with other processes and allocating more memory.

Most operating systems have hundreds of different system calls.

Steps of Executing a System Call:

1. To execute a system call, a program execute a special **trap** instruction. This instruction simultaneously jumps into the kernel and raises the privilege level to kernel mode.

2. Once in the kernel, the system can now perform whatever privileged operations are needed, **if allowed.**

3. When finished, the OS calls a special **return-from-trap** instruction which returns into the calling user program while also reducing the privilege level.

The hardware needs to be careful to save enough of the caller's registers to return correctly when the OS issues the `return-from-trap` instruction.

On x86, the processor pushes the program counter, flags, and a few other registers onto a per-process **kernel stack**, the `return-from-trap` will pop these values off of the stack and resume execution of the user-mode program.

## Why System Calls Look Like Procedure Calls
Why do system calls, such as `open()` or `read()` look just like C code? How does the system know this is a system call and not defined method?

It in fact **is** a procedure call in C, but hidden inside that procedure call is the famous **trap instruction.** The C library that provides `open()` uses a calling convention with the kernel to put arguments to `open` in registers or on the stack and executes the trap instruction.

It then returns control to the program that issued the system call, restoring return values and registers.

The parts of the C library that make system calls are hand-coded in assembly in order to ensure they process arguments and return values correctly.

When we run a trap instruction, the kernel needs to know where to go to execute the desired privileged operation. The kernel figures out where system calls route to using a **trap table** that is set up at boot time.

When the machine boots, it does so in privileged kernel mode and is therefore able to configure machine hardware as needed.

One of the first things the OS does is tell the hardware what code to run when certain exceptional events occur.

For example, what code should run when a hard-disk interrupt takes place, or when a keyboard interrupt occurs, or when programs make a system call.

The OS informs the hardware of the location of these **trap handlers**, usually with some kind of special instruction. Once the hardware is informed, it remembers the location of these handlers until the machine is next rebooted and therefore the hardware knows what to do and what code to jump to when system calls and other events take place.

The special instruction to inform the hardware of the location of the **trap tables** that map different trap instructions to **trap handlers** and run the desired code is a **privileged operation.**

If you try to execute this instruction in user mode, the hardware won't let you and the program will die.

There are **two phases** in the LDE Protocal:

1. At boot time, the kernel initializes the trap table and the CPU remembers the trap tables' location for subsequent use.
2. When running a process, the kernel first sets up a few things in kernel mode, allocating a spot on the process list, allocating memory, before using a return-from-trap instruction to start the execution of the process. The return-from-trap switches the CPU to user mode and begins running the process.

When the process wishes to use a system call, it traps back into the OS, which handles it and once again returns control via a return-from-trap to the process.

When the program is finished, it returns from main and the `exit()` system call is invoked, which traps in the OS and performs cleanup.

## Problem 2: Switching Between Processes
The next problem with Limited Direct Execution is achieving a switch between processes. Switching between processes should be simple, right?

The OS should just decide to stop one process and start another, but if a process is running on the CPU this by definition means that the OS is **not running.** If the OS is not running, how can it do anything at all? It can't!

## Regaining Control: A Cooperative Approach
One approach that some systems have taken in the past is called the **Cooperative Approach.**

In this approach, the OS *trusts* the processes of the system to behave responsibly.

Processes that run for too long are assumed to periodically give up the CPU so that the OS can decide to run some other task.

A friendly process gives up the CPU when it makes a **system call**, since opening a file or sending a message or creating a process switches from user mode to kernel mode, pausing the execution of the program and allowing the CPU to execute its trap handler.

Systems like this often include a **yield** system call which does nothing except transfer control to the OS so it can run other processes. It is a system call designed to give the OS control.

Applications also transfer control to the OS when they do something illegal. If an application divides by zero, or it tries to access memory it cannot access, it will generate a **trap** instruction to the OS.

The OS will then have control of the CPU again and will likely terminate the offending process.

Therefore in a cooperative scheduling system, the OS regains control of the CPU by waiting for a system call or an illegal operation of some kind to take place.

Is this passive approach ideal? What happens if a process ends up in an infinite loop, never making a system call?

## Taking Control: A Non-Cooperative Approach
Without help from the hardware, the OS cannot do much when a process refuses to make system calls and never returns control to the OS.

The only solution to an infinite loop in a program with a Cooperative Scheduler is to reboot!

The solution for the OS to re-take control is to use **timer interrupts**. A timer device can be programmed to raise an interrupt every few milliseconds.

A pre-configured **interrupt handler** in the OS runs, at this point the OS has regained control of the CPU and can do what it wants, either stop the process, and start a different one, or let it resume.

The OS must, like before, inform the hardware of which code to run when the timer interrupt occurs and this is set at boot time like the setting up and location instructions for the trap table.

Also during the boot sequence, the OS must start the timer, which is a privileged operation. Once the timer is started, the OS can feel safe in that control will eventually be returned to it and it is free to run user programs.

When an interrupt occurs, the hardware must save enough of the state of the program that was running when the interrupt occurred so that a return-from-trap instruction will be able to resume the program successfully.

This is similar to when there is an explicit system call trap into the kernel with various registers being stored onto a kernel stack and being restored from the return-to-trap instruction.

## Saving and Restoring Context
Once the OS regains control via a system call, or takes control via a timer interrupt, it has to decide to keep running the previous process or switch to a different one.

This decision is made by the **scheduler**. If the scheduler decides to switch the process, the OS executes a low level piece of code called a **context switch.**

A context switch saves a few registers for the currently executing process onto its kernel stack and restores the registers for the chosen process that is to resume execution.

By doing so, the OS ensures that when the return-from-trap instruction is executed instead of returning to the process that was running, the system resumes execution of another process.

To save the context, the OS executes assembly code to save registers, the PC, and the stack pointer of the process.

By switching stacks, the kernel enters the call in the context of one process, and returns in the context of another.

When the OS finally executes a return-from-trap instruction, the soon-to-be-executing process becomes the currently-running process and the switch completes.

Both the interrupt and the switch save registers. The interrupt saves registers on the kernel stack for that process so that when it returns, it can resume the program, either the one it interrupted or a different one it chose to switch to. The switch saves registers into the process structure of A, in OS161 this is the `switchframe`. Which now holds the stack pointer at no offset, and the registers relative to the sp.

It restores the registers of process B from process structure B's switchframe, specifically by loading B's stack pointer on its switchframe into the current sp.

Finally, the OS returns-from-trap which restores B's registers and starts running it.

It is important to note that there are **two types of register saves/restores during this protocol:**

1. The first is when the timer interrupt occurs, in this case the **user registers** of the running process are implicitly saved by the **hardware** using the kernel stack (in the address space) of that process.

2. When the OS decides to switch from process A to process B, the **kernel registers** are explicitly saved by the **software** into the memory in the process structure, the switchframe, of the process.

## Worried About Concurrency?
The OS needs to be concerned about what happens if during an interrupt or trap handling, another interrupts occurs.

One simple thing an OS could do is **disable interrupts** during interrupt processing, doing so ensures that when one interrupt is being handled, no other one will be delivered to the CPU.

The OS has to be careful when doing this, disabling interrupts too long could lead to lost interrupts.

Operating systems also have a number of locking schemes to protect internal data structures.

## Summary
These mechanisms such as trap instruction and context switches are all low-level ways to implement CPU virtualization and are collectively referred to as **limited direct execution.**

The idea is straightforward:

run the program you want to on the CPU, but first make sure to set up the hardware so as to limit what the process can do without OS assistance.

Users don't know much about OS's on average. Baby proof your OS by locking the dangerous stuff and only letting them get at it through you, the kernel.

The baby-proofing comes through boot time setup of trap handlers and starting an interrupt timer, and then by only running processes in a restricted **user mode**.

Now the kernel can be quite certain that processes can run efficiently, only requiring OS intervention to perform privileged operations or when they have monopolized the CPU for too long and need to be switched out.



















