#include <iostream>
#include <iterator>
#include <list>

int main() {
  std::list<int> int_list;
  int_list.push_back(10);
  int_list.push_back(20);
  int_list.push_back(30);
  int_list.push_back(40);
  int_list.push_back(50);

  std::list<int>::iterator it = int_list.begin();
  std::list<int>::iterator it2 = int_list.end();
  int_list.erase(it, --it2); //now just have 50

  int_list.insert(it2, 2, 30); //insert places BEFORE where the iterator is at
  // here the iterator it2 was at 50, so put the 2 30's before 50.

  // int_list.insert(it, 30); would fail because where it was looking is now invalid
  it = int_list.begin();
  int_list.insert(it, 40); // now have 40 30 30 50

  int_list.unique(); //now have 40 30 50, removing contiguous identical elements
  int_list.sort(); // sort the elements
  int_list.reverse(); // reverse the elements

  //Now put in a range
  std::list<int> int_list2;
  int_list2.push_back(20);
  int_list2.push_back(10);

  int_list.insert(int_list.end(),int_list2.begin(), int_list2.end()); //int_list is 50 40 30, then past end put range [20,10]
  // gives 50 40 30 20 10

  std::ostream_iterator<int> iter(std::cout, "\n");
  std::copy(int_list.begin(), int_list.end(), iter);
  std::copy(int_list.rbegin(), int_list.rend(), iter); //and then from back to front

  return 0;
}
