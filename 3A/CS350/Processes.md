# The Process
A process is a running program. The program sits on disk a instructions, waiting to spring into action.

It is the operating system that takes these bytes and gets them running, transforming the program into something useful.

A single CPU can only *actually* run one program at a time, but this is not what the user wants. In reality we need music players, text editors, and all sorts of software to run simultaneously. The OS needs to create the illusion of many CPUs.

The OS creates this illusion by **virtualizing** the CPU. By running one process and stopping it to run another, very quickly and very often, it creates the illusion of concurrency.

The switching of processes on the fly is called **time-sharing** the CPU. By allowing a resource to be used for a little while by one entity, and then a little while by another, the resource, either a CPU or a network link, etc, can be shared by many. The **space-sharing** equivalent is to divide disk space for files or processes so that it is like you have many virtual hard disks.

To implement virtualization of the CPU, the OS will need low-level machinery, and high-level intelligence. The low-level machinery are called **mechanisms**, and they implement a needed piece of functionality.

A **context switch** gives the OS the ability to stop running one program and start another. This is used by all modern OS's.

On top of these mechanisms is some of the intelligence of the OS, called **policies.** Policies are algorithms for making some kind of decision within the OS.

Given a number of possible programs to run on a CPU, which should the OS run? A **scheduling policy** in the OS makes this decision.

It is important to separate **policies** and **mechanisms.** While mechanisms explain the *how*, such as how a program performs a context switch, a policy describes the *which*, for which process the operating system should be running.

The policies are the decision makers, and the mechanisms are the doers who implement what the policies want.

## The Abstraction of the Process
The process is the abstraction provided by the OS of a running program.

To understand what constitutes a process, we need to understand its **machine state**: what a program can read or update when it is running.

At any given time, what parts of the machine are important to the execution of the program?

### Parts of a Process

1. One component of a machine state that comprises a process is its *memory.*

Instructions and the data that the program reads and writes are in memory. The memory that the process can address, called its **address space**, is part of the process.

2. Another part of the process' machine state are *registers*. Many instructions explicitly read or update registers and they are important to the execution of the process.

There are some particularly important registers that form part of the machine state, such as the **program counter (PC)**, also called the **instruction pointer, or IP.** The program counter holds the location of the next instruction to be executed.

A **stack pointer** and associated **frame pointer** are used to manage the stack for function parameters, local variables and return addresses.

3. Programs often access persistent storage devices. I/O information like this might include a list of the files the process currently has open.

# Process API
This is a brief look at the process API that explains what basic things must be included in any interface of an operating system. These APIs in some form are available on any modern OS.

* **Create**: An operating system must include some method to create new processes. When you type a command into the shell or double click an app, the OS is invoked to create a new process to run the program indicated.

* **Destroy**: While many processes will run and just exit by themselves when complete, when they don't, the user may want to kill them and an interface to halt a runaway process is required.

* **Wait**: Sometimes it is useful to wait for a process to stop running and some kind of waiting interface should be provided.

* **Miscellaneous Control:** Other than killing or waiting for a process, there are other controls that are possible. Most operating systems provide methods for suspending a process and resuming it.

* **Status**: There are usually interfaces to get some status info about a process as well, such as how long it has been running or what state it is in.

## Process Creation
How does the OS turn a program into a process and get it up and running? The first thing that the OS must do to run a program is to **load** its code and data into memory in the address space of the process.

Programs initially reside on **disk** in executable format. In simple OS's, the moving of the program's code and data from disk to memory was done **eagerly**, all at once before running the program.

Modern OS's do this **lazily,** loading the pieces of code or data only as they are needed during program execution.

This is discussed more in **paging** and **swapping**, topics of virtualizing memory.

For now it is enough to say that the OS must move the program code and data from disk to memory.

Once this is done, the OS must allocate some memory for the program's **run-time stack.** C programs use the stack for local variables, function parameters, and return addresses. The OS allocates the memory for these things and gives it to the process.

The OS may then allocate some memory for the program's **heap**.

The heap is used for explicitly requested dynamically-allocated data. Programs request this space by running `malloc` and release it by calling `free`.

As more `malloc` calls are made, the OS may get involved and allocate more memory to the process to help satisfy these calls.

In terms of persistence, each process has three open **file descriptors**, for standard input, output and error. These descriptors let programs easily read input from the terminal as well as print output to the screen.

By

1. loading the code and data of the program into memory from disk
2. initializing a stack and heap
3. doing work related to file descriptor I/O setup

The OS has set the stage for program execution. It then just has to start the C program at the `main` entrypoint.

By jumping to the `main` routine, the OS transfers control of the CPU to the newly-created process and the program begins its execution.

# Process States
Now that we have some idea of what a process is, it is important to define the possible **states** of a process.

A process can be in one of three states:

1. **Running:** In the running state, a process is running on a processor. It is currently executing instructions.

2. **Ready:** In the ready state, a process is ready to run but for some reason the OS has chosen not to run it at the given moment.

3. **Blocked:** In the blocked state, a process has performed some kind of operation that makes it not ready to run until another event of some kind takes place. When a process initiates an I/O request to disk, so writing to a file for example, it becomes blocked since it doesn't need the processor for this and another thread can use the processor.

Moving a process from `ready` to `running` is called **scheduling** the process, and the opposite transition of moving from `running` to `ready` is **descheduling**. 

The decision to pick which process should run on the CPU at any time is made by the OS **scheduler**.

## Data Structures
The OS is a program and like an other, it has some key data structures that track relevant pieces of information.

For example, to track the state of each process, the OS will likely keep some kind of process list for all processes that are ready, as well as information to track which process is currently running.

It must also track blocked processes, when I/O events complete, etc.

When a process is blocked, the **register context** will hold the contents of its registers. Its registers will be saved to this memory location, and by restoring these registers (placing them back from the saved location into actual physical registers), the OS can resume running the process.

This is a **context switch.**

Operating Systems are replete with many important data structures such as the process list. The process list is the first such structure. It is one of the simpler ones but any OS that has the ability to run multiple programs at once will have something like this structure in order to keep track of running programs in the system.

Sometimes people refer to the individual structure that stores information about a process as a **Process Control Block (PCB)**, a fancy way of describing a C structure that contains info about each process.

A process is a running program. Mechanisms and policies allow the OS to manage processes as they transition from running, to ready, or blocked and ultimately finish.



