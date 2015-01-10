#include <vector>
#include <iostream>

class Component {
public:
  virtual void Draw() = 0;
  virtual void addComponent(Component*) = 0; // or define a default implementation for this
  virtual void removeComponent() = 0; // or define a default implementation for this
};

class Composite : public Component {
public:
  void Draw();
  void addComponent(Component*);
  void removeComponent();
private:
  std::vector<Component*> components_;
};

class Primitive : public Component {
public:
  void Draw();
  void addComponent(Component*);
  void removeComponent();
};

void Primitive::Draw() {
  std::cout << "Drawing me!" << std::endl;
}

void Primitive::addComponent(Component* comp) {
  std::cout << "No-op" << std::endl;
}

void Primitive::removeComponent() {
  std::cout << "No-op" << std::endl;
}

void Composite::Draw() {
  for (std::vector<Component*>::iterator it = components_.begin(); it != components_.end(); ++it) {
    (*it)->Draw();
  }
}

void Composite::addComponent(Component* comp) {
  components_.push_back(comp);
}

void Composite::removeComponent() {
  components_.pop_back();
}

int main() {

  Component* a = new Composite();
  Component* b = new Primitive();
  Component* c = new Composite();

  a->addComponent(b);
  a->addComponent(c);
  b->addComponent(c);
  c->addComponent(b);
  a->Draw();

  return 0;
}
