# C++ Standard Template Library
1. A collection of useful, typesafe and generic containers that:
    * know almost nothing about elements
    * focus mostly on membership
    * know nothing about algorithms
    * can define own iterators

2. A collection of useful and efficient algorithms that
    * operate on structures sequentially via iterators
    * know almost nothing about elements in structures
    * know nothing about data structures they operate on

## Overview
Most STL algorithms process a sequence of data elements.

1. Traverse a sequence of elements bounded by two iterators
2. Access elements through the iterators
3. Operate on each element during traversal

		template<class InputIterator, class T>
		InputIterator find (inputIterator first, inputIterator last, const T& val)
		// first points to first element in range, last points past last element in range

## Non-Modifying Algorithms
A number of the algorithms read but never write to the elements in their input range.

	while(first!=last)
		if (*first==val) return first
		++first
		return last

ex.1

	#include<algorithm>
	vector<int> vec
	... //put stuff in
	vector<int>::iterator result = find(vec.begin(), vec.end(),42)
	
Result either equals some iterator whose value, `*result` = 42, or it is equal to `vec.end()`
	
## Algorithms Over Two Sequences
Algorithms that operate over two sequences of data specify the full range over the first sequence and only the start of the second sequence.

	template<class InputIterator1, class inputIterator2>
	bool equal (InputIterator1 first1, InputIterator1 last1, InputIterator2 first2) {
		while (first1!=last1) {
			if (!(*first1 == *first2))
				return false
			++first1; ++first2;
		}
		return true
	}

### Potential Issues

1. Requires that both sequences be the same length, if first sequence is shorter or longer then it could break if first is longer and could return the wrong result if first is shorter.

2. the iterators could not be holding the same type, equality on their values could not be implemented.

3. first, last don't point to same container. There are no checks for whether you're comparing first and last from the sasme container. Could provide a `last()` that is less than a `first()` from another container, boundary errors, infinite loops.

4. If the class does not have equal defined even if they are the same type then it won't work

ex. 2

	vector<int>myvector (myints, myints+5)
	if (equal(myvector.begin(), myvector.end(), myints))
		cout << "The contents of both sequences are equal"

## Modifying Algorithms
Some algorithms overwrite element values in existing container. We must take care to ensure that the destination sequence is large enough for the number of elements being written

	template<class InputIterator, class OutputIterator>
		OutputIterator copy(InputIterator first, InputIterator last, OutputIterator result) {
			while (first!=last) {
				*result = *first
				++result; ++first;
			}
			return result;
	}

## Overwriting vs Inserting
The default behaviour is to write to a destination sequence, overwriting existing elements.
	
	istream_iterator<string> is (inFile)
	istream_iterator<string> eof
	vector<string> text
	copy(is, eof, back_inserter(text))

Will go from beginning to end of file and use an insert instead of default overwrite used in copy to add strings to *text* vector. A back-insert iterator is a special type of output iterator designed to allow algorithms that usually overwrite elements (such as copy) to instead insert new elements automatically at the end of the container. The container must have a `push_back` method such as `vector`.

	ofstream outFile("output.txt")
	ostream_iterator<string> os(outFile, "\n")
	copy(text.begin(), text.end(), os);

This one sends the strings in the text vector to the output file delimiting by a new line.

## Removing Elements

	template <class ForwardIterator, class T>
	ForwardIterator remove(FowardIterator first, ForwardIterator last, const T& val)
	
Algorithms never directly change the size of containers, need to use container operators to add/remove elements. Instead algorithms rearrange elements, sometimes placing undesirable elements at the end of the container and returning an iterator past the last valid element.

	vector<int>::iterator end = remove(vec.begin(), vec.end(), 42)
	vec.erase(end, vec.end()) //to remove all 42's

## Algorithms that Apply Operations
A number of algorithms apply operations to the elements in the input range, such as `sort()`, `transform()`, etc.

Some STL algorithms accept a predicate:
1.  applied to all elements in iteration
2. used to restrict set of data elements that are operated on

	bool gt20(int x) { return 20 < x; }
	bool gt10(int x) { return 10 < x; }
	int a[] = { 20, 25, 10 }
	int b[10]

	remove_copy_if(a, a+3, b, gt20); //b[] =25
	cout << count_if(a, a+3, gt10) //prints 2

These are conditional algorithms, performing an operation if the data satisfies a condition. Remove_copy_if makes no changes to a and copies to b if it passes the condition.

## Function Objects
If we need a function that refers to data other than the iterated elements, we need to define a function object or *functor*.

	class gt_n {
		int value_;
	public:
		gt_n(int val) : value_(val) {}
		bool operator()(int n) { return n > value_; }
	};

Class overloads operator (), the function call operator. Operator () allows an object to be used with function call syntax.

	gt_n gt4(4);
	cout << gt4(3) //prints 0 for false
	cout << gt4(5) //prints 1 for true

ex.

	class inc {
		inc(int amt) : increment_(amt) {}
		int operator()(int x) { return x + increment_; }
		int increment_;
	}

transform(V.begin(), V.end(), V.begin(), inc(100));

Here we use the class as a function to increment by 100. Operand 1 and 2 specify where and for what range to get the values, and operand 3 specifies where to begin putting the +100 values.

By putting them at the beginning we are essentially increasing every element in the container by 100

## Predefined Function Objects
Header `<functional` defines a number of useful generic function objects:

* plus<T>, minus<T>, times<T>, divides<T>, modulus<T>, etc.
* greater<T>, less<T>, equal<T>

Can be used to customize STL algorithms. For example, sort by default uses `operator<`. To instead sort in descending order, could use the function object `greater`.

		sort(v.begin(), v.end(), greater<string>)())

Now sort for strings uses greater instead of less.

ex.

		int op_increase(int i) { return ++i; }
		int main() {
			std::vector<int> foo, bar
			for (int i = 1; i < 6; i++)
				foo.push_back(i*11)
			
			bar.resize(foo.size()) //allocate space
			std::transform(foo.begin(), foo.end(), bar.begin(), op_increase)
			// now bar has 12,23,34,45,56
			
			for (std::vector<int>::iterator it = foo.begin(); it != foo.end(); ++i)
				std::cout << *it

Now add together elements from foo and bar:

		std::transform(foo.begin(), foo.end(), bar.begin(), foo.begin(), std::plus<int>()) //23,45,66,87...

This says to go from foo's beginning to its end, summing bar and foo using `std::plus` for ints and put the result in foo. If we don't want to overwrite because we don't know the lengths of foo and bar, could use a `forward_inserter` or `back_inserter`

## Function Object Adaptors
`<functional>` also defines useful generic adaptors to modify the interface of an object.

1. *bind1st* convert a binary function object to a unary function object by fixing the value of the first operand

		bind1st(greater<int>(), 100) // now does 100 > x instead of y > x, converts greater to a unary function using 100 as the *first* operand.

If we had said `bind2nd` it would have used y > 100

2. *mem_fun* converts a member function into a function object, only when member function is called on pointers to objects.

		vector<string> strings
		vector<Shape*> shapes
		transform(strings.begin(), strings.end(), dest, mem_fun(&:Shape::size))

# Object Composition
A compound object represents a composition of heterogeneous, possibly recursive, component objects.

Law of Demeter: client code interacts with compound object.

## Composite Design Pattern
Takes a different approach, gives the client access to all member types in a compound object via a *uniform interface*.

### Problem:
composite object consists of several heterogeneous parts. Client code is complicated by knowledge of object structure. Client code must change if data structure changes.

### Solution:
Create a uniform interface for the object's components. Interface advertises all operations that components offer. Client deals only with the new uniform interface. Uniform interface is the union of the component's services.

### When to Use:

1. client usually ignores the differences in element types
2. mostly transverses the entire composition
3. there exist reasonable default implementation operations
4. there exist reachable default implementations of operations
5. all calls to inappropriate operations are caught

1. *Uniformity*: - preserves the illusion that components can be treated the same way
2. *Safety*: - avoid cases where client tries to do something meaningless, add components to a leaf, etc

## Iterator Pattern
1. Goal: to encapsulate the strategy for iterating through a composite so that it can be changed at run-time.

2. Goal: to allow the client to iterate through a composite without exposing the composite's representation.

## Decorator Pattern
Useful if you have an object with lots of different *optional* features. An example could be a window, with lots of different features, but some navigation buttons/quitting buttons etc, scroll bars that are sometimes there.

Ideally, each of these would be separate objects, the border itself would be separate from the vertical scroll bar, which is separate from the horizontal scroll bar in order to maintain a **separation of concerns**.

Most features are additive, some must change others. 














