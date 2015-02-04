# Paging
Segmentation helped to virtualize memory, but it had problems, especially managing free space as memory becomes fragmented.

## How to Virtualize Memory Without Segments
The idea of **paging** goes back to the earliest computer systems. Instead of splitting up the address space into logical segments, each of variable page size, we split up the address space into fixed-sized units called **pages**.

If we had 64 bytes, we could split it into 4 pages, pages 0-3 each 16 bytes.

With paging, physical memory is also split into some number of physical pages as well. Each page of physical memory is a **page frame.**

Paging offers a number of advantages:

1. **Flexibility:** with a fully-developed paging approach, the system will be able to support the abstraction of an address space effectively, regardless of how a process uses the address space. 

2. **Simplicity:** When the OS wishes to place a a 64-byte address space from above into our address space from the above example, it needs to find four free pages, the OS can keep a **free list** of all free pages for this and grabs the first four free ones off the list. They are not required to contiguous.

To record *where* each virtual page of the address space is placed in physical memory, the operating system keeps a *per-process* data structure called a **page table**. The major role of the page table is to store the **address translations** for each of the virtual pages of the address space, letting us know where in physical memory they live.

These could look like:

Virtual Page 0 -> Physical Frame 3
Virtual Page 1 -> Physical Frame 5

The page table is a per-process data structure, so each process has their own. 

Now if we have a process with an address space of 64 bytes:

		mov <virtual address>, %eax

To translate the virtual address that the process generated, we have to first split it into two components:

1. The **Virtual Page Number** to get the right physical page frame.
2. The **Offset** that tells us where in the page frame to look.

Because the virtual address space of the process is 64 bytes, we need 6 bits total for the address. Two bits to select the correct page (VPN), and 4 bits to select the correct byte in the 16-byte page (offset).

So page 1 offset 0 would be : `01 0000`
Byte 64 would be: `11 1111`
`mov 21, %eax` would be: `01 0101`

With our VPN we can now index the page table and find the physical page for virtual page 1. The VPN is replaced with its **PFN (physical frame number)** equivalent and then issue the load to physical memory, since the offset does not change.

## Where are Page Tables Stored?
Page tables can easily get very large, with 32-bit instructions and 4KB page size, the offset requires 12-bits, $2^{12}$ = 4096, and the VPN would be the remaining 20-bits.

A 20-bit VPN implies that there are $2^{20}$ page frames and translations that the OS would have to manage for each process, assuming we need 4 bytes **per table entry (PTE)** to hold the physical translation.

If we need 4 bytes per table entry (so to map to the physical address, plus an other info), that's 4 bytes * $2^{20}$, a megabyte is a million bytes so this is 4MB of memory required for our page table.

Now if there are 100 processes running, the OS would need 400MB of memory just for all those translations, since each process has its own page table.

Because page tables are so big, we don't keep any special on-chip hardware in the MMU to store the page table of the currently-running process. Instead, the page table for each process is stored in **memory** somewhere.

		Page Table:
			3 7 5 2
		
Would have page 0 of the address space at physical page frame 3, bytes 48-64, page 1 of the address space at page frame 7, bytes 112-128 of the address space, etc.

## What's Actually in the Page Table?
The page table is just a data structure that is used to map virtual page numbers to physical page frame numbers, so any data structure could work.

The simplest form is a **linear page table**, which is just an array. The OS indexes the array by the VPN and looks up the page-table entry (PTE) at the index in order to find the desired PFN. This is the simple form of data structure is what we will use for now.

The contents of the Page Table Entry include a **valid bit**, which is used to indicate whether the particular translation is valid, for example if when a process begins, it will have the code and heap at one end of its address space and the stack at the other. All the unused space in-between will be marked invalid and the process will throw an exception if it tries to access that space. The valid bit is crucial for supporting a **sparse address space.** By simply marking all the **unused pages** in the address space **invalid**, it removes the need to allocate physical frames for those pages and saves physical memory.

There also might be a need for **protection bits**, 




