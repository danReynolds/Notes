# Segmentation
The base and bounds method of allocation an address space for a process leaves big chunks of free space in the middle between the stack and the heap.

How do we support a potentially large address space that will eventually need a lot of memory?

## Segmentation: Generalized Base/Bounds
Instead of having just one base and bounds pair in the MMU, have a base and bounds pair per **logical segment** of the address space.

A segment is just a contiguous portion of the address space of some length.

Segmentation allows the OS to place each of the segments in different parts of physical memory and avoid filling physical memory with unused virtual address space.

We have three logical segments: code, stack and heap.

Segmentation allows the OS to place each one of those segments in different parts of physical memory.

The only hardware structure in the MMU required to support segmentation is a set of three base and bounds register pairs.

It could look like:

<table>
	<thead>
		<tr>
			<th>Segment</th>
			<th>Base</th>
			<th>Size</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>Code</td>
			<td>32K</td>
			<td>2K</td>
		</tr>
		<tr>
			<td>Heap</td>
			<td>34K</td>
			<td>2K</td>
		</tr>
		<tr>
			<td>Stack</td>
			<td>28K</td>
			<td>2K</td>
		</tr>
	</tbody>
</table>

The code segment is placed at address 32K and has a size of 2K, with its head right next to it, but its stack in a different location all together.

If you now take must do an address translation, say from virtual address 100, in the code segment, the hardware will add the base value to the offset, 100, and arrive at 100 + 32K = 32868.

It will then check if 100 is less than 2K, find that it is within the bounds, and issue the reference to physical memory address 32868.

If you then look at virtual address 4200 in the heap, it adds 4200 to the the base address for the heap, 34KB, and we get 39016. Which is **wrong.**

The process thinks the heap starts at 4K, so a reference to virtual address 4200 is intended to go to `address start of heap` + 200.

We must extract the byte in the **segment** that it is trying to access, then add that byte to the base register of the physical address where the heap was loaded.

This way, we then get base (34KB) + (4200 - 4096) = 34920.

Steps:

1. Take the virtual address and subtract where the process thinks it is loaded to get the right offset for that segment.
2. Add that offset to the physical address where that segment was actually loaded using the base register for the segment stored in the MMU.
3. Access the memory referenced at the location `base register + offset` if permitted.

If there was ever a reference to an illegal address, the hardware detects that the address is out of bounds, trapping into the OS and issuing a segmentation fault.

## The Segmentation Fault
A segmentation fault occurs from a memory access one a segmented machine to an illegal address.

## Which Segment
How does the hardware know which segment is being referenced by the virtual address?

### Explicit Approach
Chop up the address space into segments based on the top few bits of the virtual address. In the above example with the code, heap and stack, there are three segments, so 2-bits are needed to decide which segment it is.

If the top two bits are used to select the segment, the hardware knows that if the top two bits are 00, then the virtual address is in the **code segment** and uses the **code** base and bounds pair to relocate the address to the correct physical location.

If the top two bits are 01, the hardware knows the address is in the heap, and uses the heap base and bounds.

Redoing the previous example, if translating the virtual address in the heap of 4200, it would be:

		01 000001101000 // rest is 104, the offset, in binary

The first two bits, 01 are the segment and the rest is the offset.

The next 12 bits are then taken as the offset into the segment. Adding this offset to the base register, the hardware finds the correct physical address.

For the bounds check, just see if the offset is less than the bound, if not, the address is illegal.

If all base and bounds for each segment were stored in arrays `Base` and `Bounds`, this code would look like:

		// get top 2-bits
		Segment = (VirtualAddress & SEG_MASK) >> SEG_SHIFT

		// get offset
		Offset = VirtualAddress & OFFSET_MASK
		if (Offset >= Bounds[Segment])
			RaiseException(PROTECTION_FAULT)
		else
			PhysAddr = Base[Segment] + Offset
			Register = AccessMemory(PhysAddr)

`SEG_MASK` would be 0x3000, if working with 14-bits, (11 0000 0000 0000) so that we get the first two bits, shifting them down 12 to get the number `11` or `10`, etc to indicate the segment.

`OFFSET_MASK` would be 0xFFF, again if working with 14-bits, (00 1111 1111 1111) so that we can get the offset.

Then just check the offset to make sure it is within the bounds.

Notice that if we're using two bits for picking one of our three segments, one segment of the address space is unused (if 00 is code, 01 is heap, 10 is stack, 11 is unused).

Some systems prefer to put the code in the heap, so that only one bit is needed to pick the segment and no segment goes unused.

## Implicit Approach
In this approach, the hardware determines the segment by noticing how the address was formed. If the segment was generated from the PC, it was fetching an instruction and came from the code section.

If the address is based off of the stack or base pointer, it must be in the stack segment, any other address must be in the heap.

## What About the Stack?
The critical issue with the stack is that rather than checking the offset forwards, the stack grows backwards.

If it is loaded in physical memory 28KB it goes **backwards** to 26KB.

For the hardware translation, the stack requires more than just a base and bounds registers, it requires a bit that indicates which way the **segment grows**.

<table>
	<thead>
		<tr>
			<th>Segment</th>
			<th>Base</th>
			<th>Size</th>
			<th>Growth Direction</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>Code</td>
			<td>32K</td>
			<td>2K</td>
			<td>1</td>
		</tr>
		<tr>
			<td>Heap</td>
			<td>34K</td>
			<td>2K</td>
			<td>1</td>
		</tr>
		<tr>
			<td>Stack</td>
			<td>28K</td>
			<td>2K</td>
			<td>0</td>
		</tr>
	</tbody>
</table>

With the hardware understanding that segments can grow in the negative direction, the virtual addresses must be translated slightly differently:

if virtual address 15KB should map to physical address 27KB, the virtual address in binary form should look like:

		11 1100 0000 0000

The top two bits indicate the segment, leaving an offset of 3KB.

If the max size of the segment is 4KB, to get the right spot, just take 3KB (offset) - 4KB (max size) and get -1KB, the growing up location for the stack.

An offset of 0 would be at the max spot for the stack, -4KB and an offset of 4KB would be a the beginning of the stack.

To check the bounds, just see if the absolute value of the offset is less than the bounds register.

## Support for Sharing
As support for segmentation grew, system designers realized that they could create new types of efficiencies with a little more hardware support.

For example, to save memory, sometimes it is useful to **share** certain memory segments between address spaces.

**Code sharing**, sharing of code segments, is common and still in use in systems today.

to support sharing, there needs to be extra hardware support in the form of **protection bits.**

Basic support adds a few bits per segment, indicating whether or not a program can read or write a segment, or execute code that lies within the segment.

By setting a code segment to read-only, the same code can be shared across multiple processes without worry of harming isolation, while each process still thinks it is accessing its own private memory.

Now the table for address segments looks like:

<table>
	<thead>
		<tr>
			<th>Segment</th>
			<th>Base</th>
			<th>Size</th>
			<th>Growth Direction</th>
			<th>Protection</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>Code</td>
			<td>32K</td>
			<td>2K</td>
			<td>1</td>
			<td>Read-Execute</td>
		</tr>
		<tr>
			<td>Heap</td>
			<td>34K</td>
			<td>2K</td>
			<td>1</td>
			<td>Read-Write</td>
		</tr>
		<tr>
			<td>Stack</td>
			<td>28K</td>
			<td>2K</td>
			<td>0</td>
			<td>Read-Write</td>
		</tr>
	</tbody>
</table>

The code segment in the table can be read and executed by multiple processes, mapped into multiple virtual address spaces.

With protection bits, the hardware algorithm described earlier would also change. In addition to checking whether a virtual address is within bounds, it must also check whether a particular access is allowed.

If a user tries to write to a read-only page, or execute from a non-executable page, the hardware should raise an exception.

## Fine-Grained vs Coarse-Grained Segmentation
Most of the systems so far have only had a few segments, code, stack heap, and this can be considered **coarse-grained** segmentation as it keeps the address space in relatively large chunks.

However, some early systems were more flexible and allowed for the address spaces to consist of a large number of smaller segments, referred to as **fine-grained** segmentation.

Supporting many segments requires even more hardware support, with a **segmentation table** stored in memory.

Such segmentation tables usually support the creation of large numbers of segments and enable systems to use segments in more flexible ways. The reasoning was that by having fine-grained segments, the OS could better learn about which segments are in use and which are not, allowing it to utilize main memory more effectively.

## OS Support
Summarizing segmentation, pieces of the address space are relocated into physical memory as the system runs, and allows for huge savings of physical memory compared to the naive base and bounds approach.

Segmentation does raise new issues. For example, what should the OS do at a context switch?

The segment registers must be saved and restored. Each process has its own virtual address space and the OS must make sure to set up these registers correctly before letting the process run again.

The second more important issue is managing free space in physical memory. When a new address is created, the OS has to be able to find space in physical memory for its segments.

Previously, it was assumed that each address space was the same size, and physical memory could be thought of as a bunch of slots where processes would fit. Now we have a number of segments per process, each possibly a different size and in a different spot.

The general problem that arises is that physical memory quickly becomes full of little holes of free space, making it difficult to allocate new segments or to grow existing ones.

This is the problem of **external fragmentation.**

If a process wishes to allocate a 20KB segment, and there is 24KB free, but not in one contiguous segment, the OS cannot satisfy the 20KB request.

One solution to this problem would be to **compact** physical memory be rearranging the existing segments.

The OS could stop whichever processes are running, copy data to one contiguous region of memory, change their segment register values to point to the new physical locations, and free up larger regions of memory.

Compaction is expensive, as copying segments is memory-intensive and generally requires a considerable amount of processor time.

A simpler approach is to use a free-list management algorithm that tries to keep large extents of memory available for allocation.

There are hundreds of approaches that people have taken, including:

1. **Best-Fit Algorithm:** keeps a list of free spaces and returns the one closest in size that satisfies the desired allocation to the requested.

2. **Worst-Fit**
3. **First-Fit**
4. **Buddy Algorithm**

No matter how good the algorithm, external fragmentation still exists, and a good algorithm only attempts to minimize it.

The fact that so many different algorithms exist to try to minimize external fragmentation shows that there is no real **best** way to solve the problem.

The only real solution is to avoid the problem and never allocate memory in variable-sized chunks.

# Conclusion
Segmentation solves many problems and builds a more effective virtualization of memory.

Beyond just dynamic relocation (base and bounds), segmentation can better support sparse address spaces and avoids the potential waste of memory between logical segments.

It is fast, as doing the arithmetic segmentation requires is easy and well-suited for hardware.

It also allows code sharing, if code is placed within a separate segment, such a segment could potentially be shared across multiple running programs if given the correct permissions.

It also poses potential problems, the issue of external fragmentation and the chopping up of free memory into odd-sized pieces can make satisfying a memory-allocation request difficult.

Segmentation is also not flexible enough to support the fully generalized, sparse address space. If there is a large but sparsely-used heap all in one logical segment, the entire heap must reside in memory in order to be accessed.

If the model of how the address space is being used doesn't match how the segmentation has been designed, then the segmentation doesn't work very well. On to more solutions.











