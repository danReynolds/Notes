# Hard Disk Drives
These drives have been the main form of persistent storage in computer systems for decades and much of the development of file system technology is predicated on their behaviour.

## The Interface
The basic interface for all modern drives is straightforward. The drive consists of a large number of sectors (512-byte blocks), each of which can be read or written.

We can view the disk as an array of sectors which constitute the disk's **address space.**

Multi-sector operations are possible, many file systems will read or write 4KB at a time. However, when updating the disk, the only guarantee drive manufacturers make is that a single 512-byte write is **atomic.** Therefore if an untimely power loss occurs, only a portion of a larger write may complete.

There are some assumptions most clients of disk drivers make, but that are not specified directly in the interface. One can assume that accessing two blocks that are near one-another is faster than accessing two blocks that are far apart. One can also assume that accessing blocks in a contiguous chunk is the fastest access mode, usually much faster than a random access pattern.

## Basic Geometry
Let's start to understand some of the components of a modern disk.

* **Platter:** the platter is a circular hard surface on which data is stored persistently by inducing a magnetic field inside. A disk may have one or more platters, each platter has 2 sides, each of which is called a **surface.**

* These platters are usually made of some hard material like aluminium and then coated with a thin magnetic layer that allows the drive to store bits even when powered off.

* The platters are all bound together around the **spindle,** which is connected to a motor that spins the platters around at a constant fixed rate. The rate of rotation is measured in RPMs and typical values are from 7,200 to 15000 RPM.

Data is encoded on each surface in concentric circles of sectors, we call one such circle a **track.** A single surface contains many thousands of tracks, tightly packed together.

To read and write from the surface, we need a mechanism that allows us to either sense, read, the magnetic patterns on the disk or to induce a change in, write, them. This process of reading and writing is accomplished by the **disk head**, there is one such head per surface of the drive.

The disk head is attached to a single **disk arm**, which moves across the surface to position the head over the desired track.

## Single-Track Latency: The Rotational Delay
To understand how a request would be processed on our simple, one-track disk, imagine we now receive a request to read block 0. How should the disk service this request?

It must just wait for the desired sector to rotate under the disk head. This wait happens often enough in modern drives and is an important enough component of I/O service time that it has a special name: **rotational latency**. In the example, if the full rotational latency is R, the disk has to incur a rotational latency of about \frac{R}{2} to wait for sector 0 to come under the read/write head, assuming there are 12 sectors and we start at 0.

In the worst case, we have to wait until it comes all the way around, costing `R`.

## Multiple Tracks: Seek Time
So far our disk just has a single track, which is not realistic. With multiple tracks, now the head is positioned over a certain track.

To understand how the drive might access a given sector, we now trace what would happen on a request to a distant sector.

To service this read, the drive has to first move the disk arm to the correct track, such as the outermost one, in a process called a **seek.**

Seeks, along with rotations, are one of the most costly disk operations.

The seek, it should be noted, has many phases:

1. First in an *acceleration* phase the disk arm gets moving.
2. Then in the *coasting* phase the arm is moving at full speed.
3. Then in the *deceleration* phase it slows down.
4. Finally in the *settling* phase it stops and is positioned carefully over the correct track.

The **settling time** is often significant, as the drive must be certain to find the right track.

After the seek, the disk arm has positioned in the head over the right track. When the desired sector passes under the disk head, the read occurs.

This final read stage is called the **transfer**, where data is either read from or written to the surface.

We then get the clear 3 steps of I/O time:

1. First a seek.
2. Then waiting for rotational delay.
3. Finally a transfer.

## Some Other Details
There are some other details about how hard drives operate, such as **track skew,** to make sure that sequential reads can be properly serviced even when crossing track boundaries.

Sectors are often skewed like this because when switching from one track to another, the disk needs time to reposition the head, even to neighbouring tracks.

Without such skew, the head would be moved to the next track but the desired next block would have already rotated under the head, and thus the drive would have to wait almost the entire rotational delay to access the next block.

The outer tracks also tend to have more sectors than the inner tracks, because of geometry. These tracks are often referred to as **multi-zoned** disk drives, where the disk is organized into multiple zones, where a zone is a consecutive set of tracks on a surface. Each zone has the same number of sectors per track, and outer zones have more sectors than inner zones.

An important part of any modern disk drive is its **cache**, which for historical reasons is called a **track buffer.** This cache is just a small amount of memory usually around 8 or 16MB which the drive can use to hold data read from or written to disk.

For example, when reading a sector from the disk, the drive might decide to read in all of the sectors on the track and cache them in its memory. Doing so allows the drive to quickly respond to any subsequent requests to the same track.

On writes, the drive has a choice: should it acknowledge the write has actually completed when it has put the data in its memory, or after the write has actually been written to disk?

The former is called **write-back** caching, and the latter **write-through.** Write back caching generally makes the drive appear faster, but can be dangerous. If the file system or applications require that data be written to a disk in a certain order for correctness then a write-back caching approach can lead to problems.

## Dimensional Analysis
Using units to cancel things out. 

## I/O Time: Doing the Math
I/O time can now be represented as the sum of three major components:

T$_{I/O} = T_{seek} + T_{rotation} + T_{transfer}$

The rate of I/O is easily computed from the time. Simply divide the size of the transfer by the time it took:

$R_{I/O} = \frac{Size(Transfer)}{T_{I/O}}$

Assume there are two types of workloads that we are interested in:

1. **Random Workload:** common in many important applications, including database management systems.

2. **Sequential Workload:** reads a large number of sectors consecutively from disk, without jumping around. Sequential access patterns are very common and are known well.

Consider two different drives from Seagate, the

Cheetah:
* Capacity 300GB
* RPM 15K
* Average Seek 4ms
* Max Transfer 125 MB/s
* Platters 4
* Cache 16MB
* Connects via SCSI

Barracuda:
* Capacity 1TB
* RPM 7.2K
* Average Seek 9ms
* Max Transfer 105MB/s
* Platters 4
* Cache 16/32MB
* Connects via SATA

Calculating the *random-workload* on the Cheetah:

$T_{seek} = 4ms, T_{rotation} = 2ms, T_{transfer} = \frac{4KB}{125 MB/s} =$ 30 micro seconds.

The average seek time of 4ms is just taken as the average time by the manufacturer, a full seek time from one edge of the surface to another would probably take longer.

The average rotational latency is calculated from the RPM directly, 15K RPM is 250 RPS, therefore each rotation takes 4ms.

On average, the disk will encounter a half rotation and thus take 2ms as the average.

Then the total time for I/O is the addition of these three values, totalling: 6.001ms.

The rate of I/O is then the size of the transfer over this value: $\frac{4KB}{6.001ms} = 0.66MB/s$

The same calculation for the Barracuda yields a $T_{I/O}$ of 9ms + 4.16ms + 38 micro seconds = 13.6ms and a rate of 0.31MB/s.

Now for sequential I/O of the Cheetah, assume a transfer size of 100MB. We then get 2ms + 4ms + 100MB / 125MB/s = 806ms. 100MB / 806ms gives near the peak transfer rate.

And of the Barracuda, 9ms + 4.16ms + 100MB / 105MB/s = 950ms, also near its peak transfer rate.

The Average Seek Time is estimated by books and papers to be approximately $\frac{1}{3}$ of the full seek time, as a result of a simple integration related to the average seek distance, not average seek time.

There are N tracks, and we need to seek from track x to y. The average distance between x and y can be calculated by exploring all possible distances x and y:

$\sum{x=0}{n}\sum{y=0}{n} (x - y)$

And dividing by $n^{2}$, all the possible locations of x and y. Solving this as an integral yields $\frac{1}{3}n$, where n is the full distance.

## Disk Scheduling
Because of the high cost of I/O, the OS usually plays a major role in deciding the order of I/Os issued to the disk. Given a set of I/O requests, the disk scheduler examines the requests and decides which one to schedule next.

Unlike job scheduling, it is possible to reasonably estimate the length of time the disk request will take. By estimating the seek and possible rotational latency of a request, the disk scheduler can know how long each request will take, and greedily pick the one that will take the least time to service first. The disk scheduler tries to follow the principle of **SJF**.

## SSTF Shortest Seek Time First
One early disk scheduling approach is **shortest-seek-time-first (SSTF)**, which orders the queue of I/O requests by track, picking requests on the nearest track to complete first. For example, assuming the current position of the head is over the inner track, and we have requests for sectors 21 on the the middle track and 2 on the outer track, we would issue the request to 21 first, since it is closer, and wait for that to be completed before issuing the request to sector 2.

Unfortunately, the drive geometry is not available to the OS, it only knows about an array of blocks, and cannot determine which ones to go to first.

This problem is easily fixed using a different approach called **nearest-block-first (NBF)** which schedules the request with the nearest block address next.

The next problem is more fundamental: **starvation.** If there was a steady stream of requests to the inner track, the requests to the outer track would never be serviced by a pure SSTF approach.

## Elevator a.k.a SCAN Algorithm
The answer to the issue of starvation is this algorithm, originally called SCAN, which simply moves across the disk servicing requests in order across the tracks.

Call a single pass across the disk a sweep. If a request comes for a block on a track that has already been serviced on this sweep of the disk, then it is not handled immediately, rather it is queued until the next sweep.

This algorithm is sometimes called the **elevator** algorithm, because it behaves like an elevator which is either going up or down and picks up requests in the direction it is going.

## Shortest Positioning Time First (SPTF)
While seek time is important, our scheduler also needs to incorporate rotational latency.

Before discussing **SPTF**, it is important to grasp the problem fully:

The head could be positioned over sector 30 on the inner track, and the scheduler needs to decide, should it schedule sector 16 on the middle track or sector 8 on the outer track for its next request?

It depends!

What it depends on here is the relative seek time as compared to the rotational latency.

If seek time is much higher than rotational latency, then SSTF variants are fine. We always should stay on our track since it is more expensive to seek than to wait for the necessary sector.

But if seek is faster than rotation, it would make more sense to seek further to service request 8 on the outer track than to perform the shorter seek to the middle track to service 16, since we will get request 8 much sooner than 16 and it is inexpensive to seek.

On modern drives, both seek and rotation are roughly equivalent and therefore SPTF is useful and improves performance.

It is difficult to implement on an OS, however, since the OS does not have a good idea where track boundaries are or where the disk head is in terms of the rotation of the section of the platter.

## Other Scheduling Issues
Where is disk scheduling performed? On older systems, the OS did all the disk scheduling, after looking through the set of pending requests, the OS would pick the best one and issue it to the disk. When that request completes, the next one would be chosen.

In modern systems, disks can accommodate multiple outstanding requests, and have sophisticated internal schedulers that can implement SPTF accurately.

The OS scheduler usually picks what it thinks the best few requests are and issues them to disk, then disk uses its internal knowledge of head position and detailed track layout info to service the requests in the best-possible, SPTF order.

Another important related task performed by disk schedulers is I/O **merging.**

Imagine a series of requests to read blocks 33, 8, 34, etc. The scheduler should **merge** the requests for blocks 33 and 34 into a single two-block request, any re-ordering that the scheduler does is performed on the merged requests.

Merging is particularly important at the OS level, as it reduces the number of requests sent to the disk and lowers overhead.

One final problem that modern schedulers address is:

How long should the system wait before issuing an I/O to disk? One might naively think that the disk, once it has even a single I/O, should immediately issue the request to the drive, this approach is called **work-conserving**, as the disk will never be idle if there are requests to serve.

Research on **anticipatory disk scheduling** has shown that sometimes it is better to wait for a bit, in what is called a **non-work-conserving** approach. By waiting, a new *better* request may arrive at the disk, and the overall efficiency will be increased. Deciding when to wait and for how long is inherently difficult.

## Conclusion
We have presented a summary of how disks work.










