# Beyond Physical Memory: Mechanisms
So far, we've used address spaces that are unrealistically small and fit completely in physical memory. In fact, we've been assuming that every address space of every running process combined fit into physical memory. No more.

This realism requires an additional level in the **memory hierarchy**. To support large address spaces, the OS needs to stash away portions of the address space that currently aren't in large demand.

This place should have more capacity than memory, and is generally slower. This role is usually served by a **hard-disk drive.** Big and slow drives sit at the bottom of the hierarchy, with physical memory just above.

## How to Go Beyond Physical Memory
How can the OS make use of a larger, slower device to transparently provide the illusion of a large virtual address space?

It is first important to consider why we want a large address space. This is mostly because of convenience and ease of use.

With a large address space, you don't have to worry about if there is room enough in memory for a program's data structures, instead you just write the program naturally, allocating memory as needed.

It is a powerful illusion that the OS provides.

Beyond just a single process, the addition of a swap space allows the OS to support the illusion of a large virtual memory for multiple concurrently running processes.

The invention of multiprogramming almost demanded the ability to swap out some pages, as early machines could not hold all the pages needed by all processes at once.

## Swap Space
Some space on disk needs to be reserved for swapping pages back and forth between disk and memory.

The area on disk reserved for this is called the **swap space**. The OS can then read and write to and from swap space in page-sized units. To do so, the OS will need to remember the **disk address** of a given page.

The size of the swap space is important, for now it is assumed to be very large.

The swap space is not the only on-disk location for swapping traffic. The code pages for a program like `ls` or `df` are found on disk and when executed, are loaded into memory, either all at once or one page at a time as needed.

If the system needs to make room in physical memory for other things, it knows it can get rid of the code loaded for `ls`, for example, since it has the code on disk and it can be swapped back in anytime.

## Present Bit
On a memory reference, the process generates virtual memory references for instruction fetches or data accesses and the hardware translates them into physical addresses before fetching the desired data from memory.

The hardware first extracts the VPN from the virtual address, checks the TLB for a match, and if it produces the resulting PFN, fetching it from memory.

If not found in the TLB, the hardware locates the page table in memory and loads the PTE, checks the bits to see if the page table entry is valid and present and if so, loads it into the TLB, retrying the instruction.

If memory is allowed to be swapped to disk, then when the hardware looks in the PTE, it may find that the page is *not present* in physical memory.

The way the hardware or the OS checks this is through the present bit. If the present bit is one, it means the page is present in physical memory and everything proceeds as normal.

If the present bit is zero then it is not in memory and must be on disk somewhere. This generates a **page fault** and the OS is invoked to handle the exception. The **Page Fault Handler** runs and services the page fault.

## The Page Fault
With TLBs, there are hardware and software managed translation. In either system, if a page is not present the OS is put in charge to address the issue. Virtually all systems handle page faults in software, even with a hardware-managed TLB.

This is because swapping the page from disk to memory is slow, so even if the OS takes time to execute the fault, the disk operation itself is traditionally where the overhead lies. Additionally, to be able to handle a page fault, the hardware would have to understand swap spaces, how to issue I/Os to disk and other details it doesn't have baked in.

The OS will need to swap the page into memory and must know how to find the desired page.

The OS could use the bits in the PTE normally used for data such as the PFN of the page for a disk address instead.

When the OS receives the page fault for a page, it looks in the PTE to find the address on disk and issues a request to fetch the page into memory.

When the disk I/O completes, the OS will then update the page table to mark the page as present, update the PFN field of the PTE to record the in-memory location of the newly-fetched page, and retry the instruction.

This next attempt could easily generate a TLB miss, which would the be updated into the TLB and another restart would find the translation in the TLB, proceeding to fetch the desired data or instruction from memory.

While I/O is happening to get the page from disk, the process will be blocked. The OS is free to run other ready processes while the page fault is being serviced. Because I/O is expensive, this overlap of the I/O (page fault) of one process and the execution of another is another way a multiprogrammed system makes effective use of hardware.

## What if Memory is Full?
In the process described above, it was assumed that there is free memory in which to put the **page in** a page from **swap space.** This may not be the case and the OS might have to first **page out**. One or more pages to make room for the new page that is coming in.

The process of picking a page to kick out or replace is the **page-replacement policy**. Kicking out the wrong page at the wrong time can have a terrible effect on performance, so this is critical.

## Page Fault Control Flow
There are now three important cases to consider when a TLB miss occurs:

1. The page must be both **present**, so in physical memory, and **valid**, in which case, it can just grab the PFN from the PTE and put it in the TLB.
2. If valid but not present, the page fault handler must be run to bring it into physical memory, using the PTE's PFN as the address on disk to get the swap the page in from.
3. It could also be an invalid page, perhaps even an address outside of the address space of the process due for example to a bug in the program. In this case, the hardware just traps into the kernel and the process is killed before even checking the present bit.

When the OS swaps in the page, it must then make sure there is space in physical memory:

		PFN = FindFreePhysicalPage()
		if (PFN == -1) // if no free page in physical memory
			PFN = EvictPage() // run replacement algorithm
		Diskread(PTE.DiskAddr, pfn) // read the page at saved PTE's PFN
		PTE.present = True // change bit in PTE now that its in phys mem.
		PTE.PFN = PFN // update PTE to phys mem, not disk anymore.
		RetryInstruction() // retry the instruction, page fault handled.

## When Replacement Really Occurs
The OS usually doesn't way until memory is full before swapping to disk, rather to keep a small amount of memory free, most operating systems have a kind of **high watermark** (HW) and **low watermark** (LW) to help decide when to start evicting pages from memory.

When the OS notices that there are fewer than the LW pages available, a background thread that is responsible for freeing memory runs.

The thread evicts pages until there are HW pages available. The background thread, sometimes called the **swap or page daemon** then goes to sleep.

By performing a number of replacements at once, new performance optimizations are possible. Many systems will cluster or group a number of pages and write them out at once to the swap partition, increasing the efficiency of the disk.

## Conclusion
We now use the **present bit**, which when false, issues a page fault that traps into the kernel and is responded to by the page fault handler, which arranges for the transfer of the desired page from disk to memory, first replacing some pages in memory to make room for the one coming in.

These actions all take place **transparently** to the process. As far as the process is concerned, it is just accessing its own, private contiguous virtual memory.

Behind the scenes, the process' pages are placed in arbitrary and **non-contiguous** locations in physical memory and sometimes they are not even present in memory, requiring a swap in from disk.

