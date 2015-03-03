# Paging: Smaller Tables
The second problem that page tables produce is the fact that they take up so much memory.

A linear page table can get pretty big:

* 32-bit address space.
* Each page is 4KB, or $2^{12}$.
* Each page table entry is 4 bytes.

That means there are $2^{20}$ pages, with each PTE at 4 bytes that's $2^{20}$, or 1MB, times 4, so 4MB to hold the page table.

Noting that we usually have one page table per process, with 100 processes we are already getting to 400MB!

Since simple array-based page tables are too big, how can page tables be made smaller?

# Simple Solution: Bigger Pages
To reduce the size of the page table, bigger pages could be used. With 16KB sized pages, only $2^{18}$ pages would be made, with 4 bytes each that's 1MB per page table. Reducing the size by a factor of 4.

For every factor of 2 increase in page sizes, the memory required for page table allocation goes down by the same factor.

The big problem with large pages is that they leach to **waste within** each page, a problem called *internal fragmentation,* as the waste is internal to the unit of allocation.

Applications would allocate large pages but only using little bits and pieces of each and memory quickly fills up with these overly large pages.

# Multiple Page Sizes
Many architectures now support multiple page sizes. Usually a small 4KB or 8KB page size is used. If an application is smart, however, it can request a single large page, of even 4MB, placing a large data structure in this space while still only consuming one TLB entry.

Large page usage is common in database management systems.

The main reason for multiple page sizes isn't to reduce page table memory requirements, it is to reduce pressure on the TLB. Enabling a program to access more of its addres space without too many TLB misses.

Although using multiple page sizes makes the OS virtual memory manager more complex.

# Hybrid Approach: Paging and Segments
Assume that we have an address space in which the used portions of the heap and stack are small.

The single code page VPN 0 is mapped to the physical page 10, and the single heap page VPN 4 is mapped to the physical page 23, and the two stack pages at the other end of the address space VPNs 14 and 15 are mapped to physical pages 28 and 4 respectively.

Most of the page table is actually unused, full of invalid entries.

This is a waste. Besides VPN 0, 4, 14, 15, all the page table entries are invalid:

<table>
	<thead>
		<tr>
			<th>PFN</th>
			<th>Valid</th>
			<th>Protection</th>
			<th>Present</th>
			<th>Dirty</th>
		</tr>
	<tbody>
		<tr>
			<td>10</td>
			<td>1</td>
			<td>r-x</td>
			<td>1</td>
			<td>0</td>
		</tr>
		<tr>
			<td>-</td>
			<td>0</td>
			<td>---</td>
			<td>---</td>
			<td>---</td>
		</tr>
		<tr>
			<td>-</td>
			<td>0</td>
			<td>---</td>
			<td>---</td>
			<td>---</td>
		</tr>
		<tr>
			<td>-</td>
			<td>0</td>
			<td>---</td>
			<td>---</td>
			<td>---</td>
		</tr>
		<tr>
			<td>23</td>
			<td>1</td>
			<td>rw-</td>
			<td>1</td>
			<td>1</td>
		</tr>
</table>

...

The majority of the page table is full of empty entries. Instead of having a single page table for the entire address space of the process, try just having one per logical segment.

Then have three page tables, one for code, heap and stack parts of the address space. Segmentation uses a base and bounds register to determine the size of each segment.

In the hybrid, there are still those structures in the MMU, but instead of the **base register** pointing to the start of the segment itself, it points to the physical address of the **page table of that segment.**

The bounds register is used to indicate the end of the page table.

As an example:

There is a 32-bit virtual address space with 4KB pages, and an address space spit into four segments, only three of which are used: one for code, one for heap, one for stack.

To determine which segment an address refers to, use the top two bits of the address space. Assuming 00 is the unused segment, with 01 for code, 10 for heap and 11 for stack.

Then a virtual address space would have its top 2 bits for the Segment, its next 18 for the VPN and the last 12 for the page offset.

When a process is running, the base register for each of these segments contains the physical address of a linear page table for that segment, giving each process in the system 3 page tables.

On a context switch, these registers must be changed to reflect the location of the page tables of the newly running process.

On a TLB miss, assuming a hardware-managed TLB, where the hardware is responsible for handling TLB misses, the hardware uses the 2 segment bits to determine which base and bounds pair to use.

The hardware then takes the physical address there and combines it with the VPN to form the PTE:

		SN = (VirtualAddress & SEG_MASK) >> SN_SHIFT
		VPN = (VirtualAddress & VPN_MASK) >> VPN_SHIFT
		AddressOfPTE = Base[SN] + (VPN * sizeof(PTE)) // base reg pointing to start of page table

// Then the index of the page table is equal to the desired VPN

One critical difference in the hybrid scheme is the presence of the bounds register per segment. Each bounds register holds the value of the maximum **valid** page in the segment.

If the code segment, for example, is only using its first three pages, then the code segment page table will only have three entries allocated to it and the bounds register will be set to 3.

Memory accesses **beyond** the end of the segment will generate an exception. In this manner, the hybrid approach realizes significant memory savings compared to the linear page table. Unallocated pages between the stack and the heap no longer take up space in the page table, everything is compact.

This approach does have problems:

* It still requires segmentation, which is not as flexible as it assumes a certain usage pattern of the address space. If we have a large but sparsely used heap, we can still end up with a lot of page table waste.

* This hybrid causes external fragmentation again. While most of the memory is managed in page-sized units, page tables can now be an arbitrary size in multiples of PTEs. Finding free space necessarily becomes more complicated.

## Multi-level Page Tables
A different approach doesn't rely on segmentation but attacks the same problem:

how to get rid of all the invalid regions in the page table instead of keeping them in memory?

This approach is a **multi-level page table**, as it turns the linear page table into a tree structure.

This approach is **very effective** and many modern systems use it.

The idea is to chop up the page table into page-sized units, then, if an entire page of page-table entries (PTE's) is invalid, don't allocate that page of the page table at all.

To track whether a **page of the page table** is valid, and if valid, where it is in memory, use a new structure called a **page directory.**

The page directory can be used to tell you where a page of the page table is, or that the entire page of the page table contains no valid pages.

This strategy just makes entire parts of the linear page table disappear, freeing those physical frames for other uses.

The page directory, in a simple two-level table contains one entry per page of the page table. It consists of **page directory entries (PDE)**. A PDE minimally has a **valid bit** and a **page frame number**, similar to a PTE.

If the PDE entry is valid, it means that in the page of the page table pointed to by that PDE, one of the PTEs is valid.

Again, *in at least one PTE on that page pointed to by the PDE, the valid bit in that PTE is set to one.*

Multi-level page tables only allocate page-table space in proportion to the amount of address space being used and is generally compact and supports sparse address spaces.

Secondly, each portion of the page table now fits within a page, making it easier to manage memory.

The OS can simply **grab the next free page** when it needs to allocate or grow a page table.

This is much more efficient than the simple linear page table, which is just an array of PTEs indexed by VPN, forced to reside contiguously and atomically in memory.

With a **multi-level** structure, we add a **layer of indirection**, through use of the page directory that points to page sized pieces of the page table.

That indirection allows us to place page-table pages wherever we would like in physical memory.

It is important to note that there is a cost to multi-level tables. On a TLB miss, two loads from memory will be required to get the right **translation** information from the page table. One for the proper page directory, and one for that page table directory's PTE.

This is an example of a **time-space trade-off:**
We wanted smaller tables but not for free, although we require less memory now, a TLB miss suffers from a higher cost (an extra memory load) with this smaller page table.

Another negative is the *complexity* of the new solution. Whether it is the hardware or the OS handling the page-table lookup on a TLB miss, doing so is more difficult than with a simple *walk the page table* lookup, before the introduction of directories.

Often we are willing to increase the complexity in order to improve performance or reduce overheads.

## Detailed Multi-Level Example
Imagine a small address space of size 16KB, with 64-byte pages. That's $2^{14}$ bits in the address and 6-bits for the page offset, leaving 8-bits for VPN.

A linear page table would have $2^{8}$ or 256 entries, even if only a small portion of the address space is in use.

In this example, virtual pages 0 and 1 are for code, virtual pages 4 and 5 are for heap and virtual pages 254 and 255 are for the stack. The rest of the pages are unused.

To build a two-level page table for this address space, start with the full linear page table and break it into page-sized units. The full page table had 256 entries, and since page size is 64-bytes, assuming a PTE size of 4-bytes, each page can hold 16 PTEs.

Now to actually retrieve an entry we want, start with the page directory:

Our page table is small, 256 entries across 16 pages. The page directory needs one entry per page of the page table, so it has 16 entries. We then need 4-bits of the VPN to index the directory, we pick the top 4-bits of the 16-bit address.

Once we extract the **page-directory index, ** or PDIndex, we can use it to find the **PDE** with the simple calculation:

		PDEAddr = PageDirbase + PDindex * sizeof(PDE)

After loading in the PDE entry at this address, if it is marked invalid, raise an exception. Otherwise, if the PDE is valid, now fetch the PTE from the page of the page table pointed to by this PDE. The page of the page table has 16 entries, which means we require another 4-bits in the VPN to indicate the PTE we want from the page table page.

This **page-table index** (PTIndex) can then be used to index into the page table itself, giving the address of the PTE:

		PTEAddr = (PDE.PFN << SHIFT) //location of page table page + (PTIndex * sizeof(PTE)) //offset needed to get correct PTE in page table page

We now have the desired PTE address, which we dereference and get the PTE from. The PTE gives us the PFN for the physical frame we need, and we use the last 8-bits of the VPN to get the offset, dereference the result and get the answer.

In this example, VPN 0, 1, 4 5 all fit on one page of the broken up page table and VPN 254 and 255 on another. Then instead of having the full 16 pages for the page table pages, we only allocate 2, since the rest is marked invalid in the page directory.

Finally, we can see from: `11 1111 1000 0000` that this would be:

		`11 11` = the index in the page directory, so this is the last one.
		`11 10` = the index in the page of the page table, so we get the second last PTE (254 in the stack).
		`00 0000` = this is the 6-bit offset in the PFN pointed to by PTE 254.

Then:

		PhysAddr = (PTE.PFN << SHIFT) + offset

We now know how a two-level page table works.

## More Than Two Levels
In some cases, a deeper tree is needed for the page directory system.

The goal in constructing a multi-level page table is to make each piece of the page table fit within a single page. The page directory itself can get too big if we are split the page table into too many pages:

If we have a 30-bit virtual address space and a 512 byte page size, then that's $2^{9}$ for the page offset, and 21-bits for the VPN. That's a lot of PTEs, so if we break that up, we get $2^{7}$, 128 PTEs per page, assuming each PTE is 4 bytes. Then when we index into a page of the page table, we need 7-bits of the VPN to determine which PTE we want, Leaving 21-7 = 14 bits left for the page directory. If the page directory has $2^{14}$ entries, each PDE being 4-bytes, that's 64KB required, and each page is 512 bytes, so that's 128 pages required for the page directory.

Now our goal of making every piece of the multi-level page table fit into a page has vanished.

To fix this problem, build a further level of the tree by splitting the page directory into multiple pages and adding a new page directory on top of that, to point to the pages of the page directory.

## The Translation Process: Remember the TLB
To summarize the entire process of address translation using a two-level page table, we can examine the following:

		VPN = (VirtualAddress & VPN_MASK) >> SHIFT
		(Success, TLBEntry) = TLB_Lookup(VPN)
		if (Success == True) // TLB Hit
			if (CanAccess(TLBEntry.ProtectBits) == True)
				Offset = VirtualAddress & OFFSET_MASK
				PhysAddr = (TLBEntry.PFN << SHIFT) | Offset
				Register = AccessMemory(PhysAddr)
			else
				RaiseException(ProtectionFault)
		else //TLB Miss
			// first, get the page directory entry
			PDIndex = (VPN & PD_MASK) >> PD_SHIFT
			PDEAddr = PDBR // page directory base register + (PDIndex * sizeof(PDE))
			PDE = AccessMemory(PDEAddr)
			if (PDE.valid == False) // nothing in this PDE
				RaiseException(SegmentationFault)
			else
				// PDE is valid: now fetch PTE from page of page table
				PTIndex = (VPN & PT_MASK) >> PT_SHIFT //index of PTE we want
				PTEAddr = (PDE.PFN << SHIFT) + (PTIndex * sizeof(PTE))
				PTE = AccessMemory(PTEAddr)
				if (PTE.valid == False)
					RaiseException(SegmentationFault)
				else if (canAccess(PTE.ProtectBits) == False)
					RaiseException(ProtectionFault)
				else
					TLB_Insert(VPN, PTE.PFN, PTE.ProtectBits)
					RetryInstruction()

If in the TLB, its one memory access. If not in the TLB it is 3, one to get the PDE entry, another to get the PTE entry, and then a final time to get the PFN from the TLB after we retry the instruction.

## Inverted Page Tables
An even more extreme space savings in the world of page tables is found with **inverted page tables.** Instead of having many page tables, one per process of the system, keep a single page table that has an entry for each *physical page* of the system.

The entry tells us which process is using this page and which virtual page of that process maps to this physical page.

Finding the correct entry is now a matter of searching through this data structure.

A hash table is often built for speedy lookups.

Inverted page tables illustrate what we've said from the beginning, page tables are just data structures. You can do lots of crazy things with data structures to make them bigger, smaller, slower or faster.

## Swapping page Tables to Disk
So far, we have assumed that page tables reside in kernel-owned physical memory. Even with the many tricks used to reduce the size of the page table, it is still possible they they may be too big to fit into memory all at once.

Some system space such page tables in **kernel virtual memory**, allowing the system to **swap** some of these page tables to disk when memory pressure is high.

These are the **swap partitions**.

## Conclusion
Real page tables are not just built as linear arrays but also as much more complex data structures. The trade-offs include bigger tables, faster TLB misses, vs more memory, worse TLB costs on a miss.

In a memory-constrained system, a small structure makes sense, in a system with large amounts of memory, a bigger table that speeds up the TLB might be the right choice.

With software managed TLBs, the entire space of data structures opens up to the delight of the operating system innovator. We are now the operating-system developers. Yay.