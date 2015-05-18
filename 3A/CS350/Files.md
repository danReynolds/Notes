# Files and Directories
There is one final critical piece to the virtualization puzzle: **persistent storage.**

A persistent storage device, such as a HDD or a SSD store information permanently. Unlike memory, whose contents are lost when there is a power loss.

## Files and D's
A file is simply a linear array of bytes, each of which can be read or written to.

Each file has some kind of low-level name, usually a number of some kind, often the user is not aware of this name.

For historical reasons, the low-level name of a file is often referred to as its **inode number.** The OS does not know much about the structure of a file, whether it is an image, or a text file, C code, etc, rather it is simply the responsibility of the file system to store data on disk.

The second abstraction is that of a **directory.** A directory, like a file, also has a low-level name, an inode number.

Its contents, however, are quite specific:

It contains a list of user-readable name, low-level name pairs (names and inumbers).

For example, there could be a file with the low-level name "10" and the user-readable name "foo".

Each entry in a directory refers to either files or other directories.

By placing directories within directories, users are able to build an arbitrary **directory tree.**

The directory tree starts at a **root directory**, in UNIX-based systems, this is `/`, and uses some kind of separator to name subsequent sub-directories.

## Creating Files
Uses the `open()` system call, passing it the `O_CREAT` flag:

		int fd = open("foo", O_CREAT | O_WRONGLY | O_TRUNC);

The open routine takes a number of flags. In this example, the program creates the file, specifying that it can only write tot he file, and if the file already exists, first truncate it to a size of zero bytes and remove existing content.

The return value of `open()` is a **file descriptor.** A file descriptor is just an integer that is local to the process, and is used in UNIX systems to access files. Therefore once a file is opened, you use the file descriptor to read or write to the file, assuming you have permission.

In this way, a file descriptor can be thought of as a pointer to an object of type file. Once you have such an object, you can call other methods to access the file, like read and write.

## Reading and Writing Files
Suppose you used `cat` to print out a file. If you want to know what happens, you can use a tool called **strace** to trace the system calls made by a program.

Strace traces every system call made by a program while it runs and dumps the trace to the screen for you to see.

When you first open a file, the file descriptor return will almost certainly be equal to or above 3.

This is because each process already has three files open, one for standard input, which can be read to receive input, standard output, which the process can write to in order to dump info to the screen, and standard error, which the process can write error messages to.

## Reading and Writing Non-Sequentially
To read or write a file at a certain offset, use the `lseek` system call.

The first argument is a file descriptor, the second is the file offset, and the third determines exactly how the seek is performed.

For each file a process opens, the OS tracks a *current* offset, which determines where the next read or write will begin reading from or writing to in the file.

