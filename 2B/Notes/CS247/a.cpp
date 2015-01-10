class A {
public:
  friend void foo();
};

void foo() {
  return;
}

int main() {
  A a;
  foo();
  return 0;
}
