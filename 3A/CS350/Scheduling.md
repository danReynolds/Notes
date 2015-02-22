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

$T_{turnaround}$