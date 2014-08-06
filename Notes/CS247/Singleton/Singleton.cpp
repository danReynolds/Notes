#include <iostream>

class Singleton {
public:
  static Singleton* Instance(int i);
protected:
  Singleton(int i) { calc_ = i; }
private:
  friend std::ostream& operator<<(std::ostream&, const Singleton&);
  static Singleton* instance_;
  int calc_;
};

Singleton* Singleton::instance_ = 0;

Singleton* Singleton::Instance(int i) {
  if (instance_ == 0) {
    instance_ = new Singleton(i);
  }
  return instance_;
}

std::ostream& operator<<(std::ostream& os, const Singleton& instance) {
  std::cout << instance.calc_;
}

int main() {
  std::cout << *Singleton::Instance(12);
  return 0;
}
