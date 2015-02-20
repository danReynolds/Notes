# Beyond Physical Memory: Policies
When little memory is free, there is **memory-pressure** for the OS to start **paging out** pages to make room for actively-used pages. Deciding which page or pages to evict is encapsulated in the **replacement policy** of the OS.

## Cache Management
Since main memory holds a subset of all pages in the system, it can be viewed as a **cache** for virtual memory pages in the system.

The goal, therefore, in picking a replacement policy for this cache is to minimize **cache misses**. To minimize the number of times that we have to fetch a page from disk.

Knowing the number of cache hits and misses allows us to calculate the **average memory access time (AMAT)** for a program.

it is simply computed as the cost of a hit times the probability of a hit, plus the cost of a miss times the probability of a miss.

The **hit rate** is the percent of references found in memory.

## Optimal Replacement Policy
An optimal policy was developed by Belady many years ago, which leads to the fewest misses overall. The policy showed that a simple but difficult approach to implement would be to replace the page that will be accessed **furthest in the future**, resulting in the fewest-possible cache misses.

This makes sense, if you have to throw out some page, why not throw out the one that is needed furthest from now? You will refer to the other pages before you refer to this one, so why throw them out?

Examine the future for each page currently in the cache and see which page is accessed further in the future.

But the future is not generally known and you can't build the optimal policy for a general-purpose operating system. Therefore in developing real, deployable policies, the focus will be on approaches that find some other way to decide which page to evict.

## Simple Policy: FIFO
Many early systems avoided the complexity of trying to approach optimal and employed simple replacement policies.

Some systems used FIFO where pages were simply placed in a queue when they entered the system. This is a very simple implementation.

FIFO has a hit rate of 36% where it is defined as $\frac{hits}{hits + misses}$

## Simple Policy: Random
Another similar replacement policy is random, which simply picks a page to replace under memory pressure.

Over 40% of the time, random is as good as optimal, but of course this is random.

## Using History: LRU
Any policy as simple as FIFO or Random has a common problem: it might kick out a page that is about to be referenced again.

FIFO kicks out the page that was first brought in, if this happens to be a page that will be accessed soon, it performs poorly and is suboptimal.

It is important to use historical data to determine what pattern to evict pages.

One type of historical information a page-replacement policy uses is **frequency**. If a page has been accessed many times, it should probably not be replaced as it may well be used again soon.

Another property is **recency** of access. The more recently a page has been accessed, the more likely it will be accessed again.

This family of policies is based on the **principle of locality**., which says that programs tend to access certain code sequences like arrays quite frequently and we should thus try to use history to figure out which pages are important, keeping those in memory when it is time to evict a page.

A family of simple historically-based algorithms was born:

1. **Least Frequently Used (LFU):** this policy replaces the least frequently used page when an eviction must take place.
2. **Least Recently Used (LRU):** replaces the least recently used page.

They're essentially self-explanatory.

1. Spatial Locality: if a page P is accessed it is likely the pages around it will also be accessed.
2. Temporal Locality: pages that have been accessed recently are likely to be accessed again shortly.

## Approximating LRU
It can be expensive to implement a perfect LRU system, but it can be easily approximated.

The idea requires support from hardware, incorporating a **use bit** or a **reference bit**. Whenever a page is referenced, read or written, the use bit is set by hardware to 1.

The use bit can be incorporated in a number of algorithms, such as the **clock algorithm.** Imagine a clock hand points to a particular page to begin searching for a page to evict. If the clock hand is currently hovering over a page with a use bit of 1, then it sets it to zero and moves on.

Once it finds a page with a use bit set to 0, it evicts that page.

## Dirty Pages
One small modification to the clock algorithm that is commonly made is the additional consideration of whether a page has been **modified** while in memory. The reason for this is: if a page has been **modified** and is therefore **dirty,** it must be written back to disk to evict it, which is expensive.

If it has not been modified, and is therefore **clean** then eviction is free, the physical frame can simply be re-used for other purposes without additional I/O. Some VM systems prefer to evict clean pages over dirty pages.

To support this behaviour, the hardware includes a **dirty/modified bit** that is set anytime a page is written to.

## Other VM Policies
In addition to managing **page replacement**, the OS also has to decide *when* to bring a page into memory. This policy, called **page selection**, presents the OS with some different options:

For most pages, the OS uses **demand paging**, which means that the OS brings the page into memory when it is accessed or **on-demand.** Otherwise, the OS could bring a page in ahead of time if it anticipates it being used, which is a behaviour known as **pre-fetching.**

If P is accessed, it is likely P+1 will be accessed soon.

Another policy determines how the OS writes pages out to disk. Many systems collect a number of pending writes together in memory and write them to disk in one more efficient write. This behaviour is called **clustering** or **grouping** writes, and is effective because a disk drive performs a single large write more effectively than many small ones.

## Thrashing
What should the OS do when memory is just oversubscribed and the memory demands of the set of running processes simply exceeds the available physical memory? In this case, the system will constantly be paging, a condition referred to as **thrashing**.

Some earlier OS's had a complex mechanism to both detect and cope with thrashing. One solution was to use **admission control,** which told the system to not run a subset of processes in order to fit all **working sets**, the pages that are actually being used. This approach shows that it is sometimes better to do less work well than to try to do everything at once poorly.

Some current systems take a more draconian approach to memory overload. For example, some versions of Linux run an **out-of-memory-killer** when memory is oversubscribed, this daemon chooses a memory-intensive process and kills it, reducing memory in a none-too-subtle manner.

While successful at reducing memory pressure, this approach can have problems if, for example, it kills X server and renders and applications requiring the display unusable.

## Conclusion
The best solution to excessive paging is a simple and expensive one. Buy more memory.
