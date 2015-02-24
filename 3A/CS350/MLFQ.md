# Multi-Level Feedback Queue (MLFQ)
The MLFQ is one of the most well-known approaches to feedback scheduling.

The fundamental problem that the MLFQ tries to address is optimizing turnaround time, which can be done by running shorter jobs first, but the OS doesn't know how long a job will run for.

Additionally, MLFQ wants to make a system feel responsive to users, therefore minimizing response time as well.

While algorithms like Round-Robin are great for response time, they have terrible turnaround time. Similarly, STCF minimizing turnaround time, but has poor response time.

Given that we won't know how long a process will run, how can the scheduler learn as the system runs, and make the best scheduling decisions?

The MLFQ is a great example of a system that learns from the past to predict the future. Future predicting approaches are common in CS, and it is critical that they be accurate.

## MLFQ: Basic Rules
In our treatment, the MLFQ has a number of distinct queues, each assigned a **priority level.**

At any given time, a job that is ready to run is on a single queue. MLFQ uses priorities to decide which job should run at a given time, a job with higher priority is chosen to run.

For jobs on the same queue, we just use round-robin scheduling among those jobs.

The key to MLFQ scheduling lies in how the scheduler sets priorities. Rather than giving a fixed priority to each job, MLFQ varies the priority of job based on its **observed behaviour.**

If a job repeatedly relinquishes the CPU while waiting for input from the keyboard, MLFQ will keep it high priority, as this is how an interactive process might behave.

If instead a job uses the CPU intensively for long periods of time, MLFQ will reduce its priority. In this way, MLFQ will try to *learn* about processes as they run and use the *history* of a job to predict its future behaviour.

This gives us our first two rules of MLFQ:

1. **Rule 1: If Priority(A) > Priority(B), A runs.**
2. **Rule 2: If Priority(A) == Priority(B), A & B run in Round-Robin.**

This is clearly not good enough, however, as if A and B have highest priority and take a long time to stop, then C and D never get to run.

## Attempt #1: How to Change Priority
We need to decide how MLFQ is going to change the priority of a job over its lifetime.

Our workload consists of a mix of interactive jobs that are short-running (frequently relinquishing CPU) and some longer running CPU-heavy jobs.

Here are the rules for our priority adjustment algorithm:

3. **Rule 3:** When a job enters the system, it is placed at the highest priority (topmost queue).
4. **Rule 4a:** If a job uses up an entire time slice while running, its priority is reduced (moved down one queue).
5. **Rule 4b:** If a job gives up the CPU before time slice is up, it stays at the *same* priority level.

Now consider the case of a single long running job:

The job enters at the highest priority, then after the quantum expires, it's priority is reduced by one and is on the second highest queue. After running at that priority level for a full time-slice, it then is lowered to the third queue.

Then consider the short job:

If there are two jobs, A which is a long-running CPU intensive job, and B, which is a short-running interactive job, then assume A has been running for some time, and then B arrives.

B will start running in the highest queue, and then if it lasts longer than the quantum, be moved down a queue, but if it then finishes next time, A will resume on the lowest priority queue.

Because we don't know whether a job will be long or short, MLFQ first assumes that a job will be **short**, giving the job high priority.

If it actually is a short job, it will slowly move down the queue and soon prove itself to be a long-running batch process. In this manner, MLFQ approximates Shortest Job First (SJF).

## What About I/O?
As rule 4b states, if a process gives up the processor before using its time slice, we keep it at the same priority level. This makes sense, as an interactive job that does a lot of I/O shouldn't be moved to lower queues, since it is a very response-heavy job.

## Problems With the Current MLFQ
Our current MLFQ seems to do a fairly good job, sharing the CPU between long-running jobs and short I/O intensive interactive jobs. Unfortunately, the approach has **serious flaws.**

First is the problem of **starvation.** If there are too many interactive jobs in the system, then they will consume **all** of the CPU time running Round-Robin. The long-running jobs will never receive CPU time and will starve.

A second problem is the fact that we can currently **game the system.** A long running proces could issue an I/O request to some file it doesn't care about and then relinquish the CPU just before its time-slice expires.

This allows the long-running job to remain on the same high-priority queue.

The third issue is with jobs that start out as CPU intensive batch jobs, but then switch to requiring responsive I/O. This job has no way to *work its way up*, while our current MLFQ can bring jobs down, they cannot earn high priority once lost.

## Attempt #2: Priority Boost
Changing the rules to avoid the problem of starvation:

5. **Rule 5: After some time period S, move all jobs in the system to the topmost queue.**

This rule solves two problems at one. First, processes are guaranteed not to starve, since when they are moved to the top, they are guaranteed to run at least once more before being moved to a lower queue.

Also, if a CPU-heavy job has become interactive, it will not receive the necessary priority boost and remain on the high-priority queue.

The addition of some time period `S` begs the question, what should S be set to? This **voo-doo constant** has no obvious choice of value, and anything assigned would seem arbitrary. If `S` is set too high, long-running jobs could starve, but if it is too low, interactive jobs might not get a proper share of the CPU.

## Attempt 3: Better Accounting
 Another problem that must be addressed is how to prevent gaming of the scheduler. The real culprit are rules 4a and 4b.

The solution is to perform better **accounting** of the CPU time at each level of the MLFQ. Instead of forgetting how much of a time slice a process used at a given level, the scheduler should keep trap and demote it once it has used up its time slice.

This merges rules 4a and 4b:

4. **Rule 4:** One a job uses up its time allotment at a given level, regardless of how many times it has given up the CPU, its priority is reduced.

Now even if a job uses 99% of its time-slice, and then issues I/O, it will just move down a level on the queue.

Without protection against gaming, a process can issue an I/O just before a quantum ends and dominate the CPU by keeping its priority. Now with protection, regardless of the I/O behaviour of the process, it *slowly moves down the queues,* and cannot gain an unfair advantage of the CPU.

## Tuning MLFQ and Other Issues
A few other issue arise with MLFQ scheduling. One big question is how to **parameterize** such a scheduler. How many queues should there be? How big should the time slice be per queue? How often should priority be boosted in order to avoid starvation and account for changes in behaviour?

There is no easy answer and it requires experience and tuning to find the best solution.

Most MLFQ variants allow for varying time slices across different queues. The high-priority queues are usually give short time slices and are comprised of interactive jobs where quickly alternating between them makes sense. 

The low priority queues contain long-running jobs that are CPU-bound, therefore longer time-slices work well.

## Avoid Voo-Doo Constants
Avoiding voo-doo is a good idea whenever possible. One could try to make the system learn a good value, but that is hard. The result is usually a configuration file filled with default values that a seasoned administrator can tweak when something isn't working right. These are often left unmodified and we are left to hope that the defaults work well in the real-world.

This tip comes from OS professor John Ousterhout and is called Ousterhout's Law.

The Solaris MLFQ implementation is easy to configure, providing a set of tables that determine exactly how the priority of a process is altered throughout its lifetime, how long each time slice is, and how often to boost a priority of a job.

Other MLFQ schedulers don't use a table or the exact rules described here, but use mathematical formulae. The FreeBSD scheduler uses a formula to calculate the current priority of a job, basing it on how much CPU the process has used.

Usage is decayed over time, providing the desired priority boost.

Finally, many schedulers use a few other features, like reserving the highest priority levels for operating system work. Typical user jobs never obtain the highest levels of priority in the system.

Some systems also allow users to offer **advice** on priorities. For example, the command-line utility `nice` allows the user to increase or decrease the priority of the job.

## Use Advice Where Possible
As the OS rarely knows what is best for each process of the system, it is useful to provide interfaces to allow users or admins to provide some hints to the OS. These hints are called **advice**, as the OS need not necessarily pay attention to it, but might take the advice into account in order to make a better decision.

Such hints are useful in many parts of the OS such as the scheduler with `nice`.

## MLFQ: Conclusion
We can now see why it is called a *multi-level feedback queue.* Pay attention to how jobs behave over time and treat them accordingly.

MLFQ is interesting for one primary reason:

instead of demanding a priori knowledge of the nature of a job, it observes the execution of a job and prioritizes it accordingly.

In this way it manages to achieve the best of both worlds, delivering an excellent overall performance for short-running interactive jobs, while being fair and making progress for long-running CPU intensive jobs.

Many systems including BSD Unix derivatives, Solaris, Windows NT and others use a form of MLFQ as their base scheduler.

Recapping the MLFQ Rules:

1. **Rule 1:** If Priority(A) > Priority(B) run A.
2. **Rule 2:** If Priority(A) == Priority(B), run them in Round-Robin.
3. **Rule 3:** When a job enters the system, it is placed at the highest priority.
4. **Rule 4:** Once a job uses its time allotment at a given level, regardless of how many times it has given up CPU, it moves down a queue and its priority is reduced.
5. **Rule 5:** After some time period `S`, move all of the jobs in the system to the topmost queue.