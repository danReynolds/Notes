# C++ Templating
Suppose we want to create our own generic classes and functions. A function template describes a family of functions.

    template <typename T>
    int compare(const T &v1, const T &v2) {
      if (v1 < v2) return -1
      else return 0
    }
<hr>

    compare(1,3); //compare int
    compare(3.14,2.7) //compare double
    short s = 5;
    compare(1, s) //error
    compare(s,s)

<hr>

    template<typename T1, typename T2>
    int compare(const T1& a, const T2& b) {
      if (a<b) return -1
      else return 1
    }
    short i = 3
    int compare(double, double)
    int compare(string, string)
    int i = compare(2.0, 3.0) // double
    int i = compare(i, i) // will create a short version of the template

## Explicit Arguments
can explicitly state template parameter arguments types.

    template<typename T1, typename T2, typename T3>
    T1 sum( const T2& a, const T3& b) {
      return a + b
    }
    float f = sum<float>(10,3.14) // explicitly tell it what to do

<hr>

    template <typename T1, typename T2, typename T3>
    T3 sum(const T2& a, const T1& b) //wrong order
    float f = sum<float>(2.0,3) //compiler error at this point b/c only 1 specifier
    // need all 3
    float g = sum<int,double,double>(2.0,3) //fine, sum(int,double,double)

## Another Example
    template<class InputIter, class OutputIter, class Predicate>
    OutputIter copy_if(InputIter first, InputIter last, OutputIter result, Predicate pred)
    // to be continue

## Class Templates
Define a generic (parameterized) class.

    template <typename T> // T is element type
    class Stack {
    public:
      Stack()
      void push(const T&)
      T top() { return items_[top_] }
      T pop()
    private:
      T items_ [STACK_SIZE]
      int top_
    };
    
    template <typename T>
    void Stack<T>::push(const T &elem) {
      top_ += 1
      items_[top_] = elem
    }

It cannot infer the type to be used, must specify.

    stack<int> st1
    stack<double st2
    stack<BigObject> st3

## Non-Type Template Parameters
Can have non-type template parameters which we treat as compile-time constants.

1. Can provide a default  value

    template <typename T, int size = 100>
    class Stack {
      Stack()
      void push(const T&)
      T top()
      T pop()
      T items_ [size]
      int top_
    }

Client code provides a compile time value for size:

    Stack<int,99> mystack //stack of size 99
    Stack<int> //stack of size 100

Creates array at compile time, not on heap.

## Friends
There are three kinds of friend declarations that may appear in a class template. Each kind of declaration declares a friendship to one or more entities.

1. A friend declaration for an ordinary non-template class or function, which grants friendship to the specific named class or function.

2. A friend declaration for a class template or function template which grants access to all instances of the friend.

3. A friend declaration that grants access only to a specific instance of a class or function template.

<hr>

    template <typename T> class Foo {
      template <typename T> friend class Bar
      template <typename T> friend void func(const T3&)
      //any instantiations of Bar or the func function can see into Foo, type 2
    }

Can do this with the streaming operators:

    template<typename T> class Stack
    template <typename T> ostream& operator<<(ostream&, const Stack<T>)
    template <typename T>
    class Stack {
      //now declare streaming operator
      friend ostream& operator<< <T> (ostream&, const Stack<T>) // Note the <T>
      // gives access to a specific stream, so if stack is a double this is a stream
      // for double. Won't make one for ints, etc. Without the <T>
      // it would make the ostream work for all Stack<T>'s not just our double one.
      // this is a type 3
    }
    // friend
    template<typename T>
      ostream& operator<<(ostream sout, const Stack<T>& st)

## A Template's Implicit Interface
How the template definition uses variables of Type T will impose some requirements on allowable instantiations.

    template <typename T>
    T mumble(T val) {
      T newVal = val
      T *p = 0
      val.speak()
      cout << "val=" << val << endl
      if ( val < newVal )
        return "success"
    }

This code assumes our template has a lot, a speak function, an overloaded streaming operator, overloaded comparison operator, etc. It checks to see if there is a type it is aware of that will allow all of these operations.  

Otherwise get type errors if type it tries does not have a speak function, or a constructor. Don't overconstrain what you can pass in.













