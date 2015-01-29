# Process API
UNIX presents one of the most interesting ways to create a new process with a pair of system calls: `fork` and `exec`.

A third routine, `wait`, can be used by a process wishing to wait for a process it has created to complete.

## Forking
The `fork` system call is used to create a new process. The fork code on a UNIX system can look like:

		int main(int argc, char *argv[]) {
			printf("hello world (pid:%d)\n", (int) getpid());
			int rc = fork();
			if (rc < 0) {
				fprintf(stderr, "fork failed");
				exit(1);
			else if (rc == 0) {
				printf("hello, i am child (pid:%d)\n", (int) getpid());
			}
			else {
				printf("hello, I parent of %d (pid:%d)\n", rc (int) getpid))
			}
			return 0;		
		}

When the process first starts running, it prints out a `hello world`. Included in this message is its **Process Identifier (PID)**. The PID is useful if someone wants to manipulate a process, such as stopping it.

The process calls `fork`, but the odd part is that the process that is created is almost an *exact copy of the calling process.* To the OS, it now looks like there are two copies of `p1` running and both are about to return from the `fork` call.

The child process does not start running at `main` after it is created, rather it starts running after the `fork` call, as if **it** had called the fork itself.

It is not an exact copy, while it has its own copy of the address space (its own private memory), its own registers including a PC, the values it returns to the caller of `fork` are **different.**

While the parent receives the PID of the newly created child, the child simply returned a 0.

So if the return value is 0, you know you have just created and are now running the child.

It then becomes simple to differentiate between the two.

The outcome of `fork` in its current state is **not deterministic.** The child process could run first and print out that it is a child, and then the parent prints out that it is a parent of the child, or vice-versa.

The CPU scheduler decides which process runs at any given moment in time and we cannot assume whether it would run the forked `child` or the `parent` first.

This non-determinism can lead to problems in multi-threaded programs.

### Adding wait() System Call
It is often useful for a parent to wait for a child process to finish what it is doing. This is accomplished using the `wait` system call.

Now for the parent we do:

		int wc = wait(NULL)
		printf("hello, I am parent of %d (wc:%d) (pid:%d)\n", wc, rc)

When the child is done, `wait` will return, guaranteeing that the child will finish first and print that it is the child before the parent prints.

### Adding the exec() System Call
`exec()` is a final important piece of the process creation API. This system call is useful when you want to run a program that is different from the calling program. 

Here we tell the child to `execvp` and run the program `wc`:

		int main() {
			printf("hello world (pid: %d)\n", (int) getpid());
			int rc = fork();
			if (rc < 0) {
				fprintf(stderr, "fork failed")
			else if (rc == 0) {
				printf("hello I am child ...")
				char *myargs[3];
				myargs[0] = strdup("wc"); // program `wc`, word count
				myargs[1] = strdup("p3.c"); // file to run program on
				myargs[2] = NULL; // marks end of array
				execvp(myargs[0], myargs); // runs word count program
				printf("this SHOULD NOT PRINT OUT");
			else {
				int wc = wait(NULL);
				printf("hello I am parent of...")
			}
		}

Given the name of an executable and some arguments like the file to run it on, `exec` loads code and data from that executable and overwrites its current code segment and data with it, the heap and stack and other parts of the memory space of the program are re-initialized.

Then the OS runs that program, passing in arguments as the `argv` of the process.

The parent runs `fork`, and the child runs `execvp`.

It does not create a new process, rather it transforms the currently running child program into a different program (wc). `exec` does not return, so the child essentially no longer exists as a program and has been replaced with the one it called. The parent also never finishes because it is waiting for a child that has been transformed into something else.

### Why? Motivating the API
Why create such an odd interface for creating a new process, or running a different one?

Separating `fork` and `exec` allows the shell to run code after the call to `fork` but before the call to `exec`. This code can alter the environment of the ready to be run program and enables interesting features.

Now in the shell, when you type an executable, the shell figures out where in the file system the executable resides, calls `fork` to create a new child process, calls some variant of `exec` to run the command and then waits for the command to complete by calling `wait`.

When the child completes, the shell returns from `wait` and prints out a prompt again, ready for the next command.

The separation of `fork` and `exec` allow for many useful things:

		prompt> wc p3.c > newfile.txt

In this example, the output of the program `wc` is **redirected** into the output fil `newfile.txt`. The way this is done is quite simple:

When the child is created, so after `fork()` but before calling `exec()`, this shell closes standard output and opens the file `newfile.txt`. By doing so, any output from the soon-to-be-running program `wc` are sent to the file instead of the screen.

The **fork** and **exec** combo allow for changes to be made to the system before our program executes!!

UNIX systems start looking for free file descriptors at zero. In this case, STDOUT_FILENO will be the first available one and thus gets assigned when `open` is called. Subsequent writes by the child process to the standard output file descriptor by `printf` for example, will then be routed transparently to the newly-opened file instead of the screen.

		// this is P4
		int main(int argc, char *argv[]) {
			int rc = fork();
			if (rc < 0) {
				fprintf("error");
			}
			else if (rc == 0) {
				close(STDOUT_FILENO);
				open("./p4.output", O_CREAT|O_WRONGLY|O_TRUNC, S_IRWXU);
				
				// now exec "wc"
				char *myargs[3];
				myargs[0] = strdup("wc"); // program wc
				myargs[1] = strdup("p4.c") // argument file to count
				myargs[2] = NULL // mark end
				execvp(myargs[0], myargs);
			}
			else {
				int wc = wait(NULL);
			}
		}

Now when `p4` is run, the shell prints the prompt, as if it were immediately ready for your next command and nothing had run. What it has done though, in fact, is that the program `p4` called `fork` to create a new child and then ran the `wc` program via a call to `execvp`.

You don't see any output to the screen because the child has redirected output to the `p4.output` file before calling `execvp`.

UNIX pipes are implemented in a similar way, but with the `pipe` system call.

With a pipe, the output of one process is connected to an in-kernel **pipe** (queue) and the input of another process is connected to that same pipe, thus the output of one process is seamlessly used as the input to the next.

This allows long chains of commands to be strung together like:

		grep foo file | wc -l

### Other Parts of the API
Beyond `fork`, `exec`, and `wait`, there are many other interfaces for interacting with processes in UNIX systems.

the `kill` call is used to send signals to a process, including directives to go to sleep, die and other useful imperatives.

The `ps` command allows you to see which processes are running. Similarly, the command `top` displays the processes of the system and how much CPU and other resources they are eating up.



		

