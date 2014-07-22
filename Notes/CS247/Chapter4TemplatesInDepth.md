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
  		or (std::vector<int>::iterator it = myvector.begin(); it != myvector.end(); ++it)
    std::cout << ' ' << *it;
	 std::cout << '\n';




		

