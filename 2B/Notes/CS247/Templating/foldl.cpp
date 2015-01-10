#include <list>
#include <functional>
#include <iostream>

struct customMult {
  int operator()(int x, int y) {
    return x + 3*y;
  }
} customM;

template <typename iter, typename T, typename proc>
T foldl(iter first, iter last, T init, proc f) {
  while (first != last) {
    init = f(init, *first);
    first++;
  }
  return init;
}

int main() {
  std::list<int> lister;
  lister.insert(lister.begin(), 20);
  lister.insert(lister.begin(), 30);
  lister.insert(lister.begin(), 40);

  std::list<int>::iterator it = lister.begin();

  int total = 0;

  total = foldl(it, lister.end(), total, std::plus<int>());

  std::cout << total << std::endl;

  total = foldl(it, lister.end(), total, customM);

  std::cout << total;

  return 0;
}
