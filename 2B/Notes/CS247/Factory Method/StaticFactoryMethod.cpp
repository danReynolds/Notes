#include <string>
#include <iostream>
#include <map>

class Force {
public:
  virtual void message() = 0;
};

class Army : public Force {
public:
  void message();
  friend class ArmyFactory;
private:
  Army() {}
  static std::string report_;
};

std::string Army::report_ = "All's well on the ground.\n";

class Navy : public Force {
public:
  void message();
  friend class NavyFactory;
private:
  Navy() {}
  static std::string report_;
};

std::string Navy::report_ = "The seas remain ever our companion.\n";

class AbstractForcesFactory {
public:
  virtual Force* create() = 0;
  friend class ForcesFactoryInitializer;
  static Force* createForce(std::string);

private:
  static std::map<std::string, AbstractForcesFactory*> forceFactories_;
};

std::map<std::string, AbstractForcesFactory*> AbstractForcesFactory::forceFactories_;

class ForcesFactoryInitializer {
protected:
  ForcesFactoryInitializer();

private:
  static ForcesFactoryInitializer instance_;
};



class ArmyFactory : public AbstractForcesFactory {
public:
  Force* create();
};

class NavyFactory : public AbstractForcesFactory {
public:
  Force* create();
};

ForcesFactoryInitializer::ForcesFactoryInitializer() {
  AbstractForcesFactory::forceFactories_["Army"] = new ArmyFactory();
  AbstractForcesFactory::forceFactories_["Navy"] = new NavyFactory();
}

ForcesFactoryInitializer ForcesFactoryInitializer::instance_;

Force* AbstractForcesFactory::createForce(std::string forceType) {
  if (forceFactories_.find(forceType) != forceFactories_.end())
    return forceFactories_[forceType]->create();
  else
    return 0;
}

Force* ArmyFactory::create() {
  return new Army();
}

Force* NavyFactory::create() {
  return new Navy();
}

void Army::message() {
  std::cout << report_;
}

void Navy::message() {
  std::cout << report_;
}

int main() {
  AbstractForcesFactory::createForce("Army")->message();
  return 0;
}
