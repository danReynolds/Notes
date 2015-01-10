# Design Philosophy of STL

1. A collection of useful, efficient, type-safe, generic containers.
    * A container has almost no understanding of the element type.
    * Each container should define its own iterators
    * Container methods use static dispatch, are not declared *virtual*
2. A collection of useful, efficient and generic algorithms that operate on iterators
    * *generic* algorithms known nothing about the structure it's operating on, apart from the fact that it can be traversed by an iterator and knows almost nothing about the elements in the structure

    * Define container methods only when the generic algorithm are unsuitable or much less efficient

**Type-safe** because the language will check whether you put in an object that is not of the object the container is expecting.

# Some facts about the STL
* The STL assumes little about contained elements.
    * The element type must allow copying/assignment
    * This is legal: `vector<vector<string>> v`
* For *ordered* collections, the element type must support `operator<` or you can provide a special functor (function-object) of your own
* The STL assumes value semantics for its contained elements; objects are copied to and from containers more often than you might realize. e.g. When a vector doubles in size, if you have big objects you might want pointers instead

# Why is there no Inheritance in STL?
* Stepanov thinks OOP is wrong, his religion is *generic programming*. Templates are used to achieve a more flexible kind of polymorphism without inheritance.
    * Uses *Duck Typing*, if "it looks like a Duck, it's a Duck". vs if "it inherits from Duck, it's a Duck"
* The algorithms are designed so that almost any algorithm can be used with an STL container, or any other data structure that supports the idea of iterators.
* The containers are just different enough that code reuse isn't really practical. No container methods are virtual in the interests of efficiency.
    * So no container destructors are declared as virtual either! Leak danger!

# Review: Polymorphic Containers
* Suppose we want to model a graphical *scene* that has an ordered list of *figures* (*Rectangles*, *Circles*, etc).
    * Recall *Figure* is an abstract base class
    * The *Scene* will have a textual *Caption*, plus the list of *Figures* which we can implement using a *vector*
* To draw the scene, we print *Caption* somehow then we draw the figures in order. What should list look like:

1. `vector<Figure>` won't work, this would be a vector of figure objects and no other kind, but figure is an ABC and cannot be instantiated. If figure were not an ABC we would only be able to store figures, not polymorphic.
2. `vector<Figure&>` This won't work, a reference is not new in itself, not a real value, just implemented using pointer but not a pointer, no storage associated with it.
3. `vector<Figure*>` This does work, stores a list of pointers and can point to subclasses, objects are on the heap.

# Container of objects or ptrs?
Elements are copied to/from containers more often than you'd think:

    Circle c("red");
    Vector<Figure> figList;
    figList.push_back(c);

vs

    Circle c("red");
    vector <Figure*> figList;
    figList.push_back(&c);

## Objects:
1. Copy operations could be expensive
2. two red circles
3. changes to one do not affect the other
4. when figlist dies it will destroy its copy of circle
5. risk of static slicing

## Pointers:
1. Allows for polymorphic containers
2. When figlist dies only the pointer is destroyed
3. Client code must clean up objects after deletion

<hr>

# The C++ Standard Library
* What's in the official C++ Standard Library is defined by various standards: C++98, 11, 14
* We are going to concentrate on the C++03 standard
    * the STL parts were actually defined in C++98 and were not changed for C++03
    * compilers were slow to provide good implementations of the STL
* We will mention a few things of particular interest that are new in C++11

# The C++ STL
1. Generic *containers* that take the element type as a param: vector, list etc.

Three main kinds:

1. Sequence containers: vector, deque, list
2. Container Adapters: stack, queue, priority_queue
3. Ordered associative containers: [multi]set, [multi]map

C++11 adds:

1. Sequence containers: array, forward_list
2. Unordered associative containers: unordered_[multi]set, unordered_[multi]map

## Main STL Containers:
1. Deque
2. List
3. Set
4. Map

# Sequence Containers
* There is a total ordering of contiguous values, no gaps, on elements based on the order in which you added them into the container. Ordering is not based on an intrinsic key/value

### Vector

* expandable array that supports access
* must be stored contiguously, so ptr arithmetic will work. O(1) access guaranteed.

### Deque
* allows fast insertion at beginning/end
* random access that is fast, but cannot guarantee elements are stored contiguously. Can use the ++ on an iterator, but not pointer arithmetic.
* operator[] and `at` must be overloaded to work correctly

### List
* It is a plain linked-list
* Supports only sequential access, via iterators, no random access via operator[] or `at`

### std::array
Functionality, it's a (compile-time) fixed-size vector:

    array<string,12> monthName = {"Jan", ... "Dec"}

Why not just use a C-style array?

* it's just as efficient as a C-style array, but none of the drawbacks, no pointer trickery
* like vector, has a `at()`, and `size()`
* there is no good reason to use a C-style array over std::array

Why not use a vector?

* strong typing, if you know the size should be fixed, enforce it
* because arrays are sometimes faster and more space efficient
* the actual storage for vector elements is always on the heap

### std::forward_list

basically, it's a singly-linked list. Compared to `std::list` you:

1. lose immediate access to end
2. you lose ability to iterate backwards
3. gain a little space efficiency as you save the cost of one ptr per list element
4. there is no `size()` method as keeping a size counter would make the linked list operations slightly slower

# Container Adapters
* usually a trivial wrapping of another sequence container data structure by a narrower interface with a specialized idea of how to add/remove elements
* the implementations use the ideas of composition and delegation
* You can specify in the constructor call which container you want to be in the underlying implementation

Use an adapter to make a stack from a vector:

    template <typename T>
    void Stack<T>::push() {
      v.push_back(); //just use the vector's but user sees push()
   }


