# Scheduling
While context switching should now be clear, the high level **policies** that the OS scheduler employs have not yet been explained.

There are various **scheduling policies**, also called disciplines, that have been developed. The origins of scheduling predate the computer and this should be no surprise.

# Workload Assumptions
Determining the workload is a critical part of building policies and the more you know about the workload, the more fine-tuned your policy can be. 

Our goal is to start with a simple policy and move to a **fully-operational scheduling discipline.**

We will make the following assumptions about the processes, sometimes called jobs that are running in the system:

1. Each job runs for the same amount of time
2. All jobs arrive at the same time
3. Once started, each job runs to completion
4. All jobs only use the CPU, they do not do I/O
5. The run-time of each job is unknown

## Scheduling Metrics
Beyond these assumptions, it is also necessary to have a **scheduling metric** to determine the proficiency of a scheduling solution.

For now, the only metric is a **turnaround time**. The turnaround time of a job is defined as:

$T_{turnaround} = T_{completion} - T_{arrival}$

Where our arrival time is currently 0, since we assume all arrive at the same time.

Both performance and fairness are important metrics for scheduling. They are often at odds with each-other. Great performance can come at the cost of fairness to certain processes.

## FIFO
The most basic algorithm for scheduling is the FIFO approach.

FIFO has several positive properties: it is simple and easy to implement. One issue, however, is that every job is run based simply on arrival in the queue, leading to issues in turnaround time:

If three jobs `A, B, C` all arrive at the same time and are placed in our FIFO queue, each taking 10 seconds, then the turnaround time is simply:

$T_{turnaround} = \frac{10 + 20 + 30}{3} = 20$

Which is fine, but if we don't assume that all jobs run the same amount of time, crazy I know, then it can get out of hand:

If job A now runs for 100 seconds, then $T_{turnaround} = \frac{110 + 120 + 130}{3} = 110$

B and C have to wait a long time, every time in or order to run. This problem is known as the **convoy effect.**

In the **convoy effect**, a number of relatively-short potential consumers of a resource get queued behind a heavyweight resource consumer.

## Shortest Job First (SJF)
A very simple solution to this problem is to run the shortest job first, and so on.

Now B and C run within the first 20 seconds, and job A runs slightly later, but at no expense to other processes.

The new turnaround time becomes: $T_{turnaround} = \frac{10 + 20 + 120}{3} = 50$, a considerable improvement.

We run into another problem, however, if a job that won't take much time is added to the queue after a job that takes a long time.

Now if A arrives first, and runs 100 seconds while B and C arrive at time 10, and wait, the turnaround time becomes: $T_{turnaround} = \frac{100 + (110 - 10) + (120 - 10)}{3} = 103.3$

## Shortest Time-To-Completion First (SCTF)
To fix this problem, relax the constraint that jobs must run to completion.

The scheduler can **preempt** job A when job B and C arrives. SJF is so far a **non-preemptive** scheduler, which is basically unheard of these days.

The **Shortest Time-To-Completion First (SCTF)** or **Preemptive Shortest Job First (PSJF)** scheduler determines which of a newly arrived jobs and its existing jobs will take the least time and then schedules that one.

Then in our example, STCF would preempt A and run B and C to completion. Only when they are finished would A's remaining time be scheduled.

This greatly improves turnaround: $T_{turnaround} = \frac{(120 - 0) + (20 - 10) + (30 - 10)}{3} = 50$.

## A New Metric: Response Time
If turnaround time was the only metric we cared about, then SCTF would be a great scheduling policy.

When a user is sitting at a terminal they demand interactive performance from the system. The metric of **response-time** addresses the issue of how long it will be before a process is scheduled to run:

$T_{response} = T_{firstrun} - T_{arrival}$

If A arrives at 0 and B and C at 10, the response time of each job is 0 for job A, 0 for job B and 10 for C, average: 3.33.

If three jobs arrive at the same time, the third job has to wait for the previous two to run in their entirety.

While good for turnaround time, this approach is bad for response time and interactivity.

How can we build a scheduler that is sensitive to response time?

## Round Robin
Round Robin scheduling helps to alleviate the issue of high response time. Instead of running jobs to completion, Round Robin runs a job for a **time slice**, sometimes called a **scheduling quantum**, and then switches to the next job in the queue.

Assume that now jobs A, B, C arrive at the same time in the system, and that they each run for 5 seconds. A round robin scheduler with a quantum of 1 second would cycle through the jobs quickly, the average response time of RR is $\frac{0 + 1 + 2}{3} = 1$, since it takes only 1 second before every job is first run.

The length of each quantum is critical for round robin. The shorter it is, the better the performance of round robin under response-time metrics.

Making the quantum too short, however, is also problematic. The cost of context switching cannot be allowed to dominate overall performance. It has to amortize the cost of switching without making it so long that the system isn't responsive.

The cost of context switching is not just from the OS saving and restoring of a few registers. When programs run, they build up a great deal of state in the CPU caches, TLBs, and other on-chip hardware.

Round-Robin is an excellent scheduler if response time is the only metric considered. But what about turnaround time? Now if A, B, C are each 5 seconds, it takes until 13 for A, 14 for B and 15 for C, since we are switching every quantum of 1 second.

That gives a turnaround time of: $T_{turnaround} = \frac{13 + 14 + 15}{3} = 14$. Not good. This is expected, since Round-Robin is stretching out jobs, switching constantly. Turnaround time only cares about when things finish, so stretching them out like this has abysmal performance.

Typically any policy like Round-Robin that is **fair**, with high response time, will perform poorly on metrics like turnaround time. This is a **trade-off** of fairness versus performance.

## Incorporating I/O
A scheduler has a decision to make when a job initiates I/O requests, because the currently-running job won't be using the CPU during I/O, it is blocked waiting for I/O completion.

The scheduler should probably schedule another job on the CPU at that time.

When the I/O completes, the scheduler moves the original thread back into ready state, but it could also run it and has to make a decision.

Assume we have jobs A and B, each requiring 50ms of CPU time. A runs for 10ms and then issues an I/O request for another 10ms while B just uses CPU and does not perform I/O.

A can be treated as 5 10-ms sub jobs, while B is a single 50ms job.

If we treat each 10ms job of A as a separate job, then when the system starts, it has to choose between the first 10ms A, (consider the other 4 as not having arrived) or a 50ms B. It chooses the shorter, A. Then when the first sub job of A has completed, since B is the only one left (the other 4 sub-jobs are still considered to not yet have arrived), it runs. Then when the next sub-job of A does arrive, it preempts B and runs for 10 ms before issuing its 10ms I/O.

Doing this allows for **overlap**, with the CPU being used by one process while waiting for the I/O of another process to complete.

By treating each CPU burst as a job, the scheduler makes sure that processes that are interactive are run frequently. While those interactive jobs are performing I/O, either outputting to a file, the screen, or inputting, the processor runs CPU intensive jobs.

## No More Oracle
The final assumption left to remove is the assumption that the scheduler knows the length of each job. In a general purpose OS, the OS usually knows very little about the length of each job.

## Conclusion
We have developed two families of approach:

1. The first runs the shortest job remaining and optimizes turnaround time **Shortest Time-To-Completion First (SCTF)**.
2. The second alternates between all jobs and optimizes response time **Round-Robin (RR)**.

We have still not yet solved the problem of the fundamental inability of the OS to see into the future. Shortly we will see how to overcome this problem by building a scheduler that uses the recent past to predict the future.

This scheduler is called a **multi-level feedback queue**.




