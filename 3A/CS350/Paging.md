# Paging
Segmentation helped to virtualize memory, but it had problems, especially managing free space as memory becomes fragmented.

## How to Virtualize Memory Without Segments
The idea of **paging** goes back to the earliest computer systems. Instead of splitting up the address space into logical segments, each of variable page size, we split up the address space into fixed-sized units called **pages**.

If we had 64 bytes, we could split it into 4 pages, pages 0-3 each 16 bytes.

With paging, physical memory is also split into some number of physical pages as well. Each page of physical memory is a **page frame.**

Paging offers a number of advantages:

1. **Flexibility:** with a fully-developed paging approach, the system will be able to support the abstraction of an address space effectively, regardless of how a process uses the address space. 

2. **Simplicity:** When the OS wishes to place a a 64-byte address space from above into our address space from the above example, it needs to find four free pages, the OS can keep a **free list** of all free pages for this and grabs the first four free ones off the list. They are not required to contiguous.

To record *where* each virtual page of the address space is placed in physical memory, the operating system keeps a *per-process* data structure called a **page table**. The major role of the page table is to store the **address translations** for each of the virtual pages of the address space, letting us know where in physical memory they live. The page table acts as the go between for the VPN's and the PFN's.

These could look like:

Virtual Page 0 -> Physical Frame 3
Virtual Page 1 -> Physical Frame 5

The page table is a per-process data structure, so each process has their own. 

Now if we have a process with an address space of 64 bytes:

		mov <virtual address>, %eax

To translate the virtual address that the process generated, we have to first split it into two components:

1. The **Virtual Page Number** to get the right page table entry (PTE), and consequently the right page frame number (PFN) which is the actual physical memory for the page we want.
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

There also might be a need for **protection bits**, indicating whether the page can be read from, written to, or executed from.

There could also be a **present bit**, which indicates whether a page is in physical memory or on disk, this is useful for address spaces that are larger than physical memory.

A **dirty bit** indicates whether the page has been modified since it was brought into memory.

A **reference bit** is used to track whether the page has been accessed and is useful in determining which pages are popular and should be kept in memory.

## Paging: Also Too Slow
In addition to potentially being too big for physical memory, paging tables can also slow down the system:

		mov 21, %eax

With this instruction, the system must first **translate** the virtual address 21 into the correct physical address 117 (for example).

Therefore before issuing the load from address 117, the system must first fetch the proper page table entry from the process' page table, perform the translation, and finally get the data from physical memory.

To do this, the hardware must know where the page table is for the currently-running process.

If there is a single **page-table base register** that contains the starting location of the page table, then to find the desired PTE, the hardware will need to perform:

		VPN = (VirtualAddress & VPN_MASK) >> SHIFT
		PTEAddr = PageTableBaseRegister + (VPN * sizeof(PTE))

where `VPN_MASK` would be set to `110000` and we shift right to get the two-bit virtual page number.

We then use this virtual page number, `VPN`, to index into the array of page table entry addresses.

Once the physical address is know, the hardware fetches the PTE from physical memory, which has data like the bits listed above as well as the Physical Frame Number (PFN) which is concatenated with the offset from the virtual address to from the desired physical address referenced in the program:

		offset = VirtualAddress & OFFSET_MASK
		PhysAddr = (PFN << SHIFT) | offset

Get the offset, then take the PFN, shift it to the upperbits, and add `OR` the offset into the lower bits.

So VPN to PTE to PFN, PFN replaces VPN, offset stays the same.

The whole process looks like:

		// Extract the VPN from the virtual address
		VPN = (VirtualAddress & VPN_MASK) >> SHIFT
	
		// Form the address of the page-table entry (PTE)
		PTEAddr = PTBR + (VPN * sizeof(PTE))
		
		// Fetch the PTE
		PTE = AccessMemory(PTEAddr)

		// Check data on the PTE
		if (PTE.valid == False)
			RaiseException(SEGMENTATION_FAULT)
		else if (CanAccess(PTE.ProtectBits) == FALSE)
			RaiseException(PROTECTION_FAULT)
		else
			// Access is OK: form physical address and fetch it
			offset = VirtualAddress & OFFSET_MASK
			PhysAddr = PTE.PFN << PFN_SHIFT | offset
			Register = AccessMemory(PhysAddr)

The hardware can then fetch the desired data from memory and put it in `eax`. So Exciting!!

We now have a working understanding of what happens on each memory reference. Paging requires us to perform one extra memory reference in order to first fetch the translation from the page table.

## The Page Table
The page table is one of the most important in the Memory Management System of an OS.

A page table stores **virtual-to-physical address translations**, letting the system know where each page of an address space resides in physical memory.

Because each address space requires such translations, there is generally one page table per process in the system.

Page tables need to be designed to run quickly, and not take up to much memory.

## Memory Trace
Here is an example of simple memory accesses:

		int array[1000];
		...
		for (i = 0; i < 1000; i++)
			array[i] = 0;

To understand what memory accesses this code will make, it is essential to **disassemble** the code and look at the resultant assembly.

		0x1024 mov $0x0, %edi, %eax, 4
		0x1028 incl %eax
		0x102c cmpl $0x03e8, %eax
		0x1030 jne 0x1024

`edi` holds the base address, `eax` the index, and eax is compared with `03e8`, otherwise known as 1000 in decimal. If the comparison is not equal, it jumps back to the top of the loop.

Each code instruction could be in PageTable[VPN] where VPN = 1, since the code is usually the first thing placed in memory. Then the accesses for the `mov` command could go off and reference PageTable[39] for example, which translates to some other PFN in physical memory.

## Conclusion
Paging has many advantages over segmentation, and other approaches, as it does not lead to external fragmentation, since it divides memory into fixed-size units. It is also flexible, enabling the sparse use of virtual addresses.

Implementing paging without care leads to slower machines due to extra memory accesses to the page table, as well as memory waste, memory filled with page tables instead of useful application data.







