#include <iostream>
#include <iterator>
#include <vector>
#include <algorithm>

int main() {
  std::vector<int> int_vector;
  int_vector.push_back(10);
  int_vector.push_back(20);
  int_vector.push_back(30);
  int_vector.push_back(40);
  int_vector.push_back(50);

  std::vector<int>::iterator it = int_vector.begin();
  std::vector<int>::iterator it2 = int_vector.end();
  int_vector.erase(it, --it2); //now just have 50

  it2 = int_vector.end();
  int_vector.insert(--it2, 2, 30);

  it = int_vector.begin();
  int_vector.insert(it, 40); // now have 40 30 30 50

  //sort and unique a different way because it doesn't have list's sort or unique
  std::vector<int> int_vector3; //make a new one because unique in the stl cannot change the size of the container, so fills it with next not same consecutive value
  std::sort(int_vector.begin(), int_vector.end()); // sort for unique copy to only take on of each number
  std::unique_copy(int_vector.begin(), int_vector.end(), inserter(int_vector3, int_vector3.begin())); //unique copy can copy over one of every run of the same number, so no consecutive duplicates in int_vector 3

  //Now put in a range
  std::vector<int> int_vector2;
  int_vector2.push_back(20);
  int_vector2.push_back(10);

  int_vector3.insert(int_vector3.end(),int_vector2.begin(), int_vector2.end()); //int_vector is 50 40 30, then past end put range [20,10]
  // gives 50 40 30 20 10

  std::ostream_iterator<int> iter(std::cout, "\n");
  std::copy(int_vector3.begin(), int_vector3.end(), iter);
  std::copy(int_vector3.rbegin(), int_vector3.rend(), iter);

  return 0;
}
