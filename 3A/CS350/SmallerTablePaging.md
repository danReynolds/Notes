# Paging: Smaller Tables
The second problem that page tables produce is the fact that they take up so much memory.

A linear page table can get pretty big:

* 32-bit address space.
* Each page is 4KB, or $2^12$.
* Each page table entry is 4 bytes.

That means there are $2^20$ pages, with each PTE at 4 bytes that's $2^20$, or 1MB, times 4, so 4MB to hold the page table.

Noting that we usually have one page table per process, with 100 processes we are already getting to 400MB!

Since simple array-based page tables are too big, how can page tables be made smaller?

# Simple Solution: Bigger Pages
To reduce the size of the page table, bigger pages could be used. With 16KB sized pages, only $2^18$ pages would be made, with 4 bytes each that's 1MB per page table. Reducing the size by a factor of 4.

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

To track whether a page of the page table is valid, and if valid, where it is in memory, use a new structure called a **page directory.**

The page directory can be used to tell you where a page of the page table is, or that the entire page of the page table contains no valid pages.

This strategy just makes entire parts of the linear page table disappear, freeing those physical frames for other uses.

The page directory, in a simple two-level table contains one entry per page of the page table. It consists of **page directory entries (PDE)**. A PDE minimally has a valid bit and a page frame number, similar to a PTE.

