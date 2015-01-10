# Using Typename Instead of Class
Can now use *typename* instead of *class* in the template argument list.

	template<typename T> class X {};
	int main() { X<int> x;}

# Function Templates
Can create an infinite set of functions. Here is an example of a string sorter.

	template<typename T>
	T fromString(const string& s) {
		istringstream is(s)
		T t
		is >> t
		return t
	}

	template<typename T>
	string toString(T t) {
		ostringstream s
		s << t
		return s.str()
	}

This will change a string to whatever you want and then change whatever you want to a string.

# Re-use Vector with Templates
if you want vector to do more, such as sort, can use templates.

	template<typename T>
	class Sorted : public vector<T> {
		void sort();
	}

	template<class T>
	void Sorted<T>::sort() {
		// then just write sort using T
		T t next;
		T s prev;
		if t > s //...
	}

The *Sorted* template requires all templates to have a `>` operator. If there is a type that cannot use this operator, such as a pointer, you can use a **partial specialization**:

	template<typename T>
	class Sorted<T*> : public std::vector<T*> {
		void sort()
	}

	template<typename T>
	void Sorted<T*>::sort() {
		if *t > *s //now works for pointers
		...
	}

So the partial specialization uses T, but takes the pointer version of it. Alternatively, you could use a *Full Specialization*:

	template<>
	void Sorted<char*>::sort() {
		... //vastly different technique from the others
	}

if `char*` has a specific function only for it that must be used, then you cannot accept T or T* and do their thing but must use the specific one you make for `char*`

## Template Instantiation
Templates are not normally instantiated until they are needed. For class templates this means that only the member functions that are used are instantiated.

		class X {
			void f() {}
		}

		class Y {
			void g() {}
		}
	
		template <typename T> class Z {
			T t;
			void a() { t.f() }
			void b() { t.g() }
		}

		int main() {
			Z<X> zx;
			zx.a() // doesn't create b()
			Z<Y> zy;
			zy.b() // doesn't create a()
		}

Calling `zx.b()` would fail since `zx` is an `X` and has no `g` function. Similarly `zy.a()` would fail as 'zy' is a `Y` and has no `f` function. This compiles however since those other functions are never used, and this shows that the template could be either an `X` or a `Y`.

# Chapter 4 STL Containers & Iterators
Print ints to standard output:

		copy(a.begin(), a.end(), ostream_iterator<int>(cout, "\n"))
		
Copy just takes a range from a beginning to end and copies them to a third location. It can copy anywhere, just give it three iterators.

Most common STL line:

		for(iter i = blah.begin(); i != blah.end(); i++)

De-referencing an iterator returns the value in the container.

The basic iterator is only guaranteed to be able to perform == and != comparisons. Const iterators prevent changes to the value within.

Reversible containers have the `rbegin()` and `rend()` methods for working backwords. `rend` references one before the beginning of the container.

ex.

		std::vector<int> myvector (5);  // 5 default-constructed ints
  		std::vector<int>::reverse_iterator rit = myvector.rbegin();

  		int i=0;
  		for (rit = myvector.rbegin(); rit!= myvector.rend(); ++rit)
    		*rit = ++i;

  		std::cout << "myvector contains:";
  		for (std::vector<int>::iterator it = myvector.begin(); it != myvector.end(); ++it)
    		std::cout << ' ' << *it;
	 			std::cout << '\n';

This prints 5,4,3,2,1

ex.2 

		// istream_iterator example
		#include <iostream>     // std::cin, std::cout
		#include <iterator>     // std::istream_iterator

		int main () {
  		double value1, value2;
  		std::cout << "Please, insert two values: ";

  		std::istream_iterator<double> eos;              // end-of-stream iterator
  		std::istream_iterator<double> iit (std::cin);   // stdin iterator

  		if (iit!=eos) value1=*iit;

  		++iit;
  		if (iit!=eos) value2=*iit;

  		std::cout << value1 << "*" << value2 << "=" << (value1*value2) << '\n';
		
  		return 0;
		}

istream and ostream iterators can only have their value accessed once before they are erased, so put them in a variable.

## Useful Iterators:

1. `remove_if`: returns an iterator to the element that follows the last element **not** removed.

		#include <iostream>     // std::cout
		#include <algorithm>    // std::remove_if

		bool IsOdd (int i) { return ((i%2)==1); }

			int main () {
  			int myints[] = {1,2,3,4,5,6,7,8,9};            // 1 2 3 4 5 6 7 8 9

  			// bounds of range:
  			int* pbegin = myints;                          // ^
  			int* pend = myints+sizeof(myints)/sizeof(int); //                  ^

  			pend = std::remove_if (pbegin, pend, IsOdd);   // 2 4 6 8 ? ? ? ? ?
                                                 // ^       ^
  			std::cout << "the range contains:";
  			for (int* p=pbegin; p!=pend; ++p)
    		std::cout << ' ' << *p;
  			std::cout << '\n';

  			return 0;
		}

The range between *first* and the iterator returned by remove_if contains all the elements in the sequence for which the predicate did not return true.

It takes a first, last and predicate to do the comparison.

2. `find_if`:
Returns an iterator to the first element in the range for which *pred* does **not** return false. If pred is false for all elements, returns an iterator to `last()`

		// find_if example
		#include <iostream>     // std::cout
		#include <algorithm>    // std::find_if
		#include <vector>       // std::vector

		bool IsOdd (int i) {
  		return ((i%2)==1);
		}

		int main () {
  			std::vector<int> myvector;

  			myvector.push_back(10);
  			myvector.push_back(25);
  			myvector.push_back(40);
  			myvector.push_back(55);

  			std::vector<int>::iterator it = std::find_if (myvector.begin(), myvector.end(), IsOdd);
  			std::cout << "The first odd value is " << *it << '\n';

  			return 0;
		}

Takes an iterator to beginning, end and predicate.

3. `erase`:
Erases element(s) in specified range:

		// erasing from vector
		#include <iostream>
		#include <vector>

		int main () {
  			std::vector<int> myvector;

  			// set some values (from 1 to 10)
  			for (int i=1; i<=10; i++) myvector.push_back(i);

  			// erase the 6th element
  			myvector.erase (myvector.begin()+5);

  			// erase the first 3 elements:
  			myvector.erase (myvector.begin(),myvector.begin()+3);

  			std::cout << "myvector contains:";
  			for (unsigned i=0; i<myvector.size(); ++i)
    			std::cout << ' ' << myvector[i];
  			std::cout << '\n';

  			return
		}

Note: **can** erase all that satisfy a condition:

		  words.erase(std::remove_if(words.begin(), words.end(), isNotAlphabetic), words.end());

Will remove all from the beginning of the iterator returned by remove_if to the end of words, which will do **all** of the ones returned by remove_if [remove-erase paradigm](http://en.wikipedia.org/wiki/Erase-remove_idiom)

4. `remove_copy_if`:
Copies the elements in the range first to last to the range beginning at the result for all elements **not** satisfying the predicate, because those that don't satisfy the predicate are *removed*.

		// remove_copy_if example
		#include <iostream>     // std::cout
		#include <algorithm>    // std::remove_copy_if
		#include <vector>       // std::vector

		bool IsOdd (int i) { return ((i%2)==1); }

		int main () {
  			int myints[] = {1,2,3,4,5,6,7,8,9};
  			std::vector<int> myvector (9);

  			std::remove_copy_if (myints,myints+9,myvector.begin(),IsOdd);

  			std::cout << "myvector contains:";
  			for (std::vector<int>::iterator it=myvector.begin(); it!=myvector.end(); ++it)
    			std::cout << ' ' << *it;
  			std::cout << '\n';

  			return 0;
		}

This returns the even numbers in `myvector`.

However, since the result iterator was a `myvector.begin()`, it copied but would not *grow* the size of myvector, if for example we had said `std::vector<int myvector(0)` vs 9.

If you need to dynamically `insert` elements, growing the size of the vector, use `std::back_inserter` or `std::forward_inserter` depending on what you need. `std::back_inserter` translates to `push_back` calls for a vector.

5. `sort`:
Sorts the elements in the range. Sorts on itself, so nothing returned, in place sorts.

		// sort algorithm example
		#include <iostream>     // std::cout
		#include <algorithm>    // std::sort
		#include <vector>       // std::vector

		bool myfunction (int i,int j) { return (i<j); }

		struct myclass { //functor
  			bool operator() (int i,int j) { return (i<j);}
		} myobject;

		int main () {
  			int myints[] = {32,71,12,45,26,80,53,33};
  			std::vector<int> myvector (myints, myints+8);               // 32 71 12 45 26 80 53 33

  			// using default comparison (operator <):
  			std::sort (myvector.begin(), myvector.begin()+4);           //(12 32 45 71)26 80 53 33

  			// using function as comp
  			std::sort (myvector.begin()+4, myvector.end(), myfunction); // 12 32 45 71(26 33 53 80)

  			// using object as comp
  			std::sort (myvector.begin(), myvector.end(), myobject);     //(12 26 32 33 45 53 71 80)

  			// print out content:
  			std::cout << "myvector contains:";
  			for (std::vector<int>::iterator it=myvector.begin(); it!=myvector.end(); ++it)
    			std::cout << ' ' << *it;
  			std::cout << '\n';

  			return 0;
		}

6. `transform`:
Applies an operation sequentially to the elements in the range, storing it in the result.

2 variations:

1. takes the beginning and end of a range, then applies the predicate, putting the result of applying the operation in a third location.

2. takes the beginning and end of a range, then the beginning of another range, and then the beginning of where to put the results, applying a binary function or functor like `std::plus<int>()` to add the elements going from the beginning of the first range to its end combined with the successive elements beginning with the start of the second range.


		// transform algorithm example
		#include <iostream>     // std::cout
		#include <algorithm>    // std::transform
		#include <vector>       // std::vector
		#include <functional>   // std::plus

		int op_increase (int i) { return ++i; }

		int main () {
  			std::vector<int> foo;
  			std::vector<int> bar;

  			// set some values:
  			for (int i=1; i<6; i++)
    			foo.push_back (i*10);                         // foo: 10 20 30 40 50

  			bar.resize(foo.size());                         // allocate space

  			std::transform (foo.begin(), foo.end(), bar.begin(), op_increase);
                                                  // bar: 11 21 31 41 51

  			// std::plus adds together its two arguments:
  			std::transform (foo.begin(), foo.end(), bar.begin(), foo.begin(), std::plus<int>());
                                                  // foo: 21 41 61 81 101

  			std::cout << "foo contains:";
  			for (std::vector<int>::iterator it=foo.begin(); it!=foo.end(); ++it)
    			std::cout << ' ' << *it;
  			std::cout << '\n';

  			return 0;
		}

7. `copy`:
As you would expect, copies the range into the destination.

		// copy algorithm example
		#include <iostream>     // std::cout
		#include <algorithm>    // std::copy
		#include <vector>       // std::vector

		int main () {
  			int myints[]={10,20,30,40,50,60,70};
  			std::vector<int> myvector (7);

  			std::copy ( myints, myints+7, myvector.begin() );

  			std::cout << "myvector contains:";
  			for (std::vector<int>::iterator it = myvector.begin(); it!=myvector.end(); ++it)
    			std::cout << ' ' << *it;

  			std::cout << '\n';

  			return 0;
		}

Funny thing, there is **no** `copy_if`, so if you want to copy on a condition, you need to use `remove_copy_if`, which copies over the ones **not** satisfying the predicate.

8. `replace_if`:
Assigns a new value to all the elements in the range for which the predicate returns true.

		// replace_if example
		#include <iostream>     // std::cout
		#include <algorithm>    // std::replace_if
		#include <vector>       // std::vector

		bool IsOdd (int i) { return ((i%2)==1); }

		int main () {
  			std::vector<int> myvector;

  			// set some values:
  			for (int i=1; i<10; i++) myvector.push_back(i);               // 1 2 3 4 5 6 7 8 9

  			std::replace_if (myvector.begin(), myvector.end(), IsOdd, 0); // 0 2 0 4 0 6 0 8 0

  			std::cout << "myvector contains:";
  			for (std::vector<int>::iterator it=myvector.begin(); it!=myvector.end(); ++it)
    			std::cout << ' ' << *it;
  			std::cout << '\n';

  			return 0;
		}






		

