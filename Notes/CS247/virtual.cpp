#include <iostream>

class Base {
public:
  void aMethod();
  virtual void bMethod();
  virtual void cMethod();
};
class Derived : public Base {
public:
  void aMethod();
  virtual void bMethod();
  virtual void dMethod();
private:
  virtual void cMethod();
};

void Base::aMethod() {
  std::cout << "BASE" << std::endl;
}

void Base::bMethod() {
  std::cout << "BASE" << std::endl;
}

void Base::cMethod() {
  std::cout << "BASE" << std::endl;
}

void Derived::aMethod() {
  std::cout << "DERIVED" << std::endl;
}

void Derived::bMethod() {
  std::cout << "DERIVED" << std::endl;
}

void Derived::cMethod() {
  std::cout << "DERIVED C because at runtime it doesnt check private, just goes to vtable and picks derived as normal" << std::endl;
}

void Derived::dMethod() {
  std::cout << "DERIVED" << std::endl;
}

int main (void) {
Base *b = new Derived();
b->aMethod(); // (a)
b->bMethod(); // (b)
b->cMethod(); // (c)
// b->dMethod(); // (d) // fails because the base has no d method
Derived d;
d.aMethod(); // (e)
// d.cMethod(); // (f) //fails because d has no c method
d.dMethod(); // (g)
Base b1 = d;
b1.aMethod(); // (h)
b1.bMethod(); // (i)
b1.cMethod(); // (j)
}
