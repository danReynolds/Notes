#include <list>
#include <functional>
#include <iostream>

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

  std::cout << total;
  return 0;
}
