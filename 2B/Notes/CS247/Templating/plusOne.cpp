#include <list>
#include <functional>
#include <iostream>

template <typename pred>
struct f {
private:
  pred pred_;
public:
  f(pred pre) : pred_(pre) {}

  template <typename T>
  int operator() (int x, T val) {
    if (pred_(val)) {
      return x+1;
    }
    else {
      return x;
    }
  }
};

int main() {
  f< std::binder1st<std::equal_to<double> > > a(std::bind1st(std::equal_to<double>(),4.0));
  std::cout << a(4, 4.0) << std::endl;

  bool test = false;
  // f< std::logical_not<bool> > b(std::logical_not<bool>());
  // std::cout << b(4, test) << std::endl;


  return 0;
}
