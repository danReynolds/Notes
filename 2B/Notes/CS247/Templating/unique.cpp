#include<vector>
#include<iostream>
#include<iterator>
#include<algorithm>
#include<list>

template<typename U, typename T>
U unique(T first, T last) {
  U result;
  while(first != last) {
    if (std::find(result.begin(), result.end(), *first) == result.end())
      result.insert(result.end(), *first);
    ++first;
  }
  return result;
}

template<typename T>
T uniqueInPlace(T cont) {
  typename T::iterator iter = cont.begin();
  while(iter != cont.end()) {
    typename T::iterator occurrence = std::find_end(iter, cont.end(), iter, iter);
    if (occurrence != iter)
      std::cout << *occurrence;
    ++iter;
  }
  return cont;
}

int main() {
  std::list<int> ints;
  ints.push_back(1);
  ints.push_back(10);
  ints.push_back(100);
  ints.insert(ints.end(), 10, 50);

  std::list<int> ints2 = uniqueInPlace<std::list<int> >(ints);

  std::ostream_iterator<int> a(std::cout, "\n");
  std::copy(ints2.begin(), ints2.end(), a);

  return 0;
}
