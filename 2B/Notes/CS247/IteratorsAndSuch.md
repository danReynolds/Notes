## Duck-Typing vs. Inheritance

* STL doesn't use inheritance or define any methods as virtual.
    * Encourages reuse of *adaptation* where we instantiate and wrap rather than inherit
    * C++ templates allow for ad hoc polymorphism, thus "duck typing"

## Design Philosophy

* A container class should define the API that is natural for it
* No inheritance, no implied subtyping
* Your de facto shape is what defines you, *Do you walk like a duck?*, not who you inherit from

## Adapter Design Pattern

* Often you find you have some library data structure that kind of implements what you want, but its API does not resemble what you naturally desire.
    * For example, *vector* can be used as a *stack*, but has more operations/is bigger
    * Often it's very close, *pop_back* vs *pop*

* An adapter is sometimes called a *wrapper* class
    * It's built on top of an adaptee class, in this case *vector*



    template <typename T>
    void Stack<T>::push(T val) {
     v.push_back(val) //where v is a vector
    }
  
## Adapters and STL

* STL defines its own adapter classes for stack, queue, and priority_queue

## Duck typing redux

* Suppose we would like to write a card game implementation, and we want to model a pile of playing cards, `Card*`

* We want it to support the natural *CardPile* ideas of *addCard*, *discard*, *shuffle*, *merge*, etc.

* We also want clients to be able to treat *CardPile* like a sequential polymorphic container, so iterate etc.
    * Should it be random access? If so should we use a *vector*?
    * Or can we use *list* which could be faster for merging piles?

* The tempting thing to do is inherit from *vector*
    * The we get all of the vector operations for free unlike when we created *Stack*, we actually want most of the functionality of *vector*

* All vector methods are non-virtual, and the destructor is also non-virtual. Better idea is to create the interface you want with the members you need and ignore the rest.

* Duck typing is heavily used in some languages, such as Python.
    * It's more flexible, you get exactly what you want
    * It's less regular, meaning you have to remember more details and exceptions to apparent conventions
    * it indulges the programmer at the expense of the client

## But there is another way

### Private Inheritance

* We don't really want *private* inheritance with a circle that inherits from a shape, but if we *did*then this is what would happen:
    * Inside the class def of Circle, we would have the direct access to the non-private methods of Figure.
    * Outside of Circle, clients would not be able to treat a Circle as if it were polymorphically a Figure.

* That is, all of the inherited public or protected member of the parent are private in the child, and you break external polymorphism when people try and use it elsewhere.

* We can selectively make some of the methods of the parent available to clients using *using*, as in `using Figure::getColor()`

* We get all versions of any overloaded methods we use, so we get both of these versions of begin()

    using vector<Card*>::iterator;
    using vector<Card*>::begin

* This approach is safe because it breaks polymorphism.
    * Can't instantiate a *CardPile* to a *vector<Card*>* so there's no risk of a call to a destructor causing a mem leak, etc.

* Also clients won't accidentally call wrong version of inherited non-virtual functions

## Associative Containers

* The ordering of the elements is based on a *key* value (a piece of the element, etc an employee's records sorted by SIN, not sorted by order of insertion

* Implemented using a kind of BST, so $O(\log n)$ lookup.

* Can't iterate through container elements in order.

* *unordered* associative containers C++11
    * No ordering assumed among elements
    * Implemented using hash tables, look up $O(1)$

* Of the ordered ones, `map` is the most useful, also called an associative array

* We don't usually think of sets as being sorted or sortable but they must be to use these library classes

* We must ensure we have an ordering defined on the element type via `operator<()` even if there isn't a natural one.

* We can provide our own functor definition via `operator<()`

## Map

* maps a key to a unique value

* `map<T1, T2> m;

* T1 is the key field type, it must support `operator<` which must in turn be a strict weak ordering
    * antireflexive, antisymmetric, transitive

    * it is common to use strings or numbers as keys, can use ptrs.

### Same-keys

* The compiler will use the following test for equality even if you have your own definition of `operator==`  


     `if !(a<b) && !(b<a) //this is always used`

## Iterators

* iterator is a fundamental design pattern of OOP

* it represents an abstract way of walking through some interesting data structure, call it v, eg using a for loop

* You start at the beginning and advance one element at a time until you reach the end

* In its simplest form, you are given:
    * A ptr to the first element of the collection
    * A ptr to just beyond the last element, reaching this value is the stopping criterion for the iteration
    * A way of advancing to the next element, usually `operator++', bidirectional.

* Usual usage pattern: f (iter $_1 $, iter $_2$)

* The implementor of f will assume that:
    * if I set p=iter then p++ should advance to the next element
    * *p should get me the current element

## STL Containers provide iterators

* If c is a *vector*, etc. then
    * `c.begin()` and `c.end()` will always return a ptr to the first and one past last elements, `operator++` will always work. So you say:

		    vector<string>::const_iterator
		    map<int,string>::iterator
    		list<Figure*>::reverse_iterator

## Why are Iterators Awesome?

* They provide a simple, natural interface for accessing container elements. They are implemented for you be each STL container

* Each STL container class defines at least one iterator type, plus can point you to first and last element for each

* *vector* provides a forward and backword bi-directional.

## Why Awesome++

* All containers support `insert` and `erase`.

* `v.insert(iter1, iter2, iter3)`
    * insert into v at position iter1 a range of external elements who begin and end are iter2 and iter3

* `v.erase(iter1, iter2)`
    * erase from range iter1 to iter2, not including any element iter2 might point to

## Iterator Categories

Iterator categories are hierarchical, with lower levels adding more constraints.

## Insert Iterators

Iterator that inserts elements into the container. 

* **back inserter**: uses container's methods `push_back()`

* **inserter**: uses container's `insert()`

		#include <algorithm>
		#include <iterator>
		...

		istream_iterator<string> is (cin);
		istream_iterator<string> eof; //end sentinel, just a variable name, means i'm done
		vector<string> text;
		copy(is, eof, back_inserter(text))

## Algorithms and Iterators

* STL algorithms perform an abstract operation on a set of data, ex `sort, random_shuffle, find`
    * `find()` using naiive iterators is O(n) whereas `set::find()` is O(1)
    * always use the container's version if it has one, the generic will be slower





















  



