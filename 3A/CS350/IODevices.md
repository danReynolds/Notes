# I/O Devices
A system typically will have a CPU attached to main memory via some kind of a **memory bus**. Some devices are connected to the system via a general **I/O bus**, which in many modern system is a **PCI**.

Even lower down are one or more of what we call a called **peripheral bus**, such as a **SCSI, SATA, or USB**. These connect the slowest devices to the system including disks, mice, and similar components.

We have multiple buses for speed and cost reasons. The shorter the bus,t he faster, but the less room it has for devices.

Engineering a bus for high performance is costly. Designers have created a system where components that require high performance, like the graphics card, are nearer to the CPU.

The advantage of placing slower devices on a peripheral bus is that you can place many of them.

## Canonical Device
A device has two important components. The first is the hardware interface it presents to the rest of the system. The hardware must also present some kind of interface that allows the system software to control its operation.

The second part of any device is its internal structure. This part is implementation specific and is responsible for implementing abstraction the device presents to the system.

Very simple hardware devices will have one or a few hardware chips to implement their functionality. More complex devices will include a simple CPU, some general purpose memory, and other device-specific chips.

Modern RAID controllers might consist of hundreds of thousands of lines of **firmware**, software within a hardware device, to implement functionality.

## Canonical Protocol
The simplified device interface presented by the devices is comprised of **three registers.**

1. A **status** register that can be read to see the current status of a device.
2. A **command** register to tell the device to perform a certain task.
3. A **data** register to pass data to the device or get data back.

By reading and writing to these registers, the OS can control the device behaviour.

A typical OS interaction with the device might look like:

		While (STATUS == BUSY)
			; // wait until device is not busy
		Write data to DATA register
		Write command to COMMAND register
		While (STATUS == BUSY)
			; // wait until the device is done with the request

The protocol has four steps:

1. The OS waits until the device is ready to receive a command by repeatedly reading the status register. This is basic polling.
2. The OS sends some data to the data register, when the main CPU is involved with the data movement, we refer to it as **programmed I/O**.
3. The OS writes a command to the command register, doing so implicitly lets the device know that both the data is present and that it should begin working on the command.
4. The OS waits for the device to finish by again polling. It may get an error code to indicate success or failure.

The first problem with this approach is that polling is inefficient. It wastes CPU time waiting for potentially slow devices to complete its activity instead of switching to another ready process and better utilizing the CPU.

## Lowering CPU Overhead with Interrupts
Instead of polling the device repeatedly, the OS can issue a request, put the calling process to sleep, and context switch to another task.

When the device is finally finished with the operation, it will raise a hardware interrupt, causing the CPU to jump into the OS at a pre-determined interrupt service routine (ISR) or more simply the **interrupt handler.**

The handler will finish the request, reading error codes, returned data, and wake the process waiting for the I/O, which can then proceed as desired.

Now the CPU can run process 1 until it issues I/O, at which time process 1 waits, and the CPU runs process 2. When the hardware is finished its I/O, it issues a hardware interrupt, trapping the CPU into the kernel, where it cleans up whatever the hardware was doing and wakes process 1.

Using interrupts is powerful, but not always the best solution. If a device performs its task very quickly, then using an interrupt will actually slow down the system, since the cost of context switching is high, and all that happens is that you immediately switch back.

If a device is fast, it may be best to poll, otherwise use interrupts.

If the speed of the device is not known, or sometimes fast and sometimes slow, a **hybrid** approach that polls for a little while and then switches to use interrupts could be an effective solution. This is a **two-phased** approach.

Another reason not to use interrupts arises in networks. When a huge stream of incoming packets each generate an interrupt, it is possible for the OS to **livelock**, find itself only processing interrupts and never allowing a user-level process to run and actually service the requests.

A web server that suddenly experiences a high load should occasionally use polling to control what is happening in the system and allow the web server to service some requests before going back to the device to check for more packet arrivals.

Another interrupt-based optimization is **coalescing.** A device which needs to raise an interrupt first waits for a bit before delivering the interrupt to the CPU. While waiting, other requests may complete, and multiple interrupts can be coalesced into a single delivery, lowering the overhead of interrupt processing.

## More Efficient Data Movement with DMA
When using programmed I/O to transfer a large chunk of data to a device, the CPU is once again over-burdened with a trivial task and is wasting time and effort. It must transfer data from main memory to the device. The device then transfers this data to the disk.

With programmed I/O, the CPU must move data to and from devices by hand. The solution is to use **Direct Memory Access (DMA)**, a DMA engine is essentially a very specific device within a system that can orchestrate transfers between devices and main memory without much CPU intervention.

The OS would program the DMA engine by telling it where the data lives in memory, how much data to copy, and which device to send it to.

The OS is then done with the transfer and can proceed with other work while the DMA goes off and performs the transfer.

When the DMA is complete, the DMA controller raises an interrupt, and the OS knows the transfer is complete.

# Methods of Device Interaction
It is necessary to establish how the OS actually communicates with the device.

Over time, two primary methods of device interaction were developed. The first and oldest method is to have explicit **I/O instructions**. These instructions specify a way for the OS to send data to specific device registers and allow the construction of the protocols described above.

On x86, the *in* and *out* instructions can be used to communicate with devices.

To send data to a device, the caller specifies a register with the data in it and a specific port which names the device.

These instructions are privileged. The OS controls devices and therefore the OS is the only entity allowed to directly communicate with them. If any program could read or write the disk, then any program could gain complete access to the machine.

The second method to interact with devices is **memory-mapped I/O.** With this approach, the hardware makes device registers available as if they were memory locations.

To access a particular register, the OS issues a load or store to the address, the hardware then routes the load/store to the device instead of main memory.

Memory-mapped I/O is nice in that no new instructions are needed to support it, but both approaches are valid.

## Fitting Into the OS: The Device Driver
One final problem is how to fit devices, each of which have a very specific interface, into the OS which we would like to keep as general as possible.

A file system, for example, should work on top of SCSI disks, IDE disks, USB keychain drives, and so on. How can we keep most of the OS device neutral, hiding the details of device interactions from major OS subsystems?

The problem is solved through abstraction, at the lowest level, a piece of software in the OS must know in detail how a device works. We call this piece of software a **device driver**, and any specifics of device interaction are encapsulated within.

In Linux, a file system is completely oblivious to the specifics of which disk class it is using, it simply issues block read and write requests to the generic block layer, which routes them to the appropriate device driver, which handles the details of issuing the specific request.

This encapsulation can have negative effects, for example, if there is a device that has many capabilities, but has to present a generic interface to the rest of the kernel, then those special capabilities will go unused.

In Linux, with SCSI devices, which have a very rich error reporting, other block devices have much simpler error reporting, all that higher levels of software ever receive is a generic EIO (generic IO error) error code, any extra details that SCSI may have provided is lost to the file system.

Because device drivers are needed for any device you plug into the system, over time they have come to represent a huge percentage of kernel code.

Studies of the Linux kernel reveal that over 70% of OS code is found in device drivers. When people tell you that the kernel has millions of lines of code, what they are really saying is that the OS has millions of lines of device driver code.

Because device drivers are often written by amateurs, not full-time kernel developers, they tend to have many more bugs and are contributors to kernel crashes.

## Case Study: Simple IDE Disk Driver
An IDE disk presents a simple interface to the system, consisting of four types of register: control, command block, status, error.

These registers are available by reading or writing to specific I/O addresses using the *in/out* instructions.

The basic protocol to interact with the device is:

1. **Wait for drive to be ready.** Read the Status Register until the drive is not busy and READY.
2. **Write parameters to command registers.** Write the sector count, and logical block address (LBA) of the sectors to be accessed, and drive number, either master or slave, as IDE permits just two drives, to command registers.
3. **Start the I/O**. By issuing read/write commands to the command register.
4. **Data transfer: (For writes).** Wait until drive status is READY and DRQ (drive request for data), write data to data port.
5. **Handle Interrupts.** In the simplest case, handle an interrupt for each sector transferred, more complex approaches allow batch and thus one final interrupt when the entire transfer is complete.
6. **Error Handling.** After each operation, read the status register. If the error bit is on, read the error register for details.

## Conclusion
Two techniques, the interrupt and DMA have been introduced to help with device efficiency and two approaches to accessing device registers, explicit I/O instructions and memory-mapped I/O, have been described.

Finally, the notion of a device driver has been presented, showing how the OS itself can encapsulate low-level details and make it easier to build the rest of the OS in a device-neutral fashion.




