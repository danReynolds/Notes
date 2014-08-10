#include <iostream>

template <typename T>
class Exception {
public:
  Exception(T error);
  T what();
private:
  T error_;
};

template <typename T>
Exception<T>::Exception(T error) : error_(error) {}

template <typename T>
T Exception<T>::what() {
  std::cout << error_;
}

int main() {
  try {
    throw(Exception<int>(10));
  } catch(Exception<int>& e) {
    e.what();
  }
  return 0;
}
