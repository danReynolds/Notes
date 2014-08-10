#include <iostream>
#include <sstream>

using namespace std;


////////////// Function Templates

template<typename T>
T fromString(const string& s) {
  stringstream is(s);
  T t;
  is >> t;
  return t;
}

template <typename T>
string toString(T t) {
  stringstream s;
  s << t;
  return s.str();
}

////////////// partial specialization
// allows it to work for pointers, as before it would take the address
template <typename T>
string toString(T* t) {
  stringstream s;
  s << *t;
  return s.str();
}

//full specialization, overrides the default template version
template <>
string toString(char* t) {
  return "there is a house in new orleans";
}

// param same as return type example
template<typename T>
T returnType(const T &v1) {
  return v1;
}

// compare example
template<typename T>
bool compare(const T &v1, const T &v2) {
  if (v1 < v2) {
    return true;
  }
  else {
    return false;
  }
}

template<typename T1, typename T2>
bool compare(const T1 &v1, const T2 &v2) {
  if (v1 < v2) {
    return true;
  }
  else {
    return false;
  }
}

template<typename T1, typename T2, typename T3>
T1 sum(const T2& a, const T3&b) {
  return a + b;
}

template<typename T1, typename T2, typename T3>
T3 sumBad(const T2&a, const T1& b) {
  return a + b;
}


////////////// Class Templates

template <typename T>
class Stack {
public:
  void push(const T&);
  template <typename U>
  friend Stack<U> foo(Stack<U>);
  friend Stack<T> fooRestricted(Stack<T>);
  T items_ [20];
  int top_;
};

template<typename T>
void Stack<T>::push(const T& elem) {
  top_ = top_ + 1;
  items_[top_] = elem;
}

template<typename U>
Stack<U> foo(Stack<U> blah) {
  return blah;
}

template<typename U>
Stack<U> fooRestricted(Stack<U> blah) {
  return blah;
}

////////////// main

int main() {
  int number = 100;
  char b = 'c';
  int* p;
  char* a = &b;
  p = &number;

  string word = toString(p);
  std::cout << word;

  string house = toString(a);
  std::cout << house;

  // comparisons
  std::cout << std::endl;
  std::cout << compare(1,3);
  std::cout << std::endl;
  std::cout << compare(3.14,2.7);
  std::cout << std::endl;
  double s = 5;
  compare<double> (1,s); //tells compiler to make them both doubles
  //just cant do compare(1,s) if there isnt a template that takes 2 different types

  compare(1,45.0); //now that there is a 2 type template for compare, will work
  compare<int>(1,45.0); // can tell it to have the first an int, second whatever, just type converts

  float f = sum<float>(2.0,3); //REQUIRES a return type
  float m = sum<double,float>(2.0,3);
  // float g = sumBad<float>(2.0,3); //this fails because of the order
  //it is going to make T1, the second param a float, then T2 and T3 it doesnt know
  // T3 needs a return type
  float g = sumBad<float,float,int>(2.0,3.9); //specifying now allows T3 to have a return type
  std::cout << g;

  //IN CONCLUSION ALWAYS REQUIRES its RETURN TYPE UNLESS

  returnType(10); // if the return type is the same as all params, then it can infer what should happen

  // Class Templates
  Stack<int> st1;
  Stack<double> st2;
  int test = 0;
  st2.push(test);
  foo(st2);


  return 0;
}
