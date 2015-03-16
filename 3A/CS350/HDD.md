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


