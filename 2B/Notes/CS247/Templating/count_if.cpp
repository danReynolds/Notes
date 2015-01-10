#include <algorithm>
#include <vector>
#include <iostream>
int increment(int x) {
  return x+1;
}
int main() {
  std::vector<int> ints;
  ints.push_back(10);
  ints.push_back(100);
  ints.push_back(100);
  ints.push_back(100);
  ints.push_back(100);
  ints.push_back(100);

  std::cout << std::count_if(ints.begin(), ints.end(), std::bind2nd(std::greater<int>(),99));

  //similarly not using count if:
  std::vector<int> blah;
  std::transform(ints.begin(), ints.end(), back_inserter(blah),increment);
  std::vector<int>::iterator it = std::remove_if(ints.begin(), ints.end(), std::bind2nd(std::less_equal<int>(),99));
  std::cout << it - ints.begin();
}
