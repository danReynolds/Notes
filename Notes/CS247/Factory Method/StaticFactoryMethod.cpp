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
  friend class ForcesFactorySingleton;

private:
  static std::map<std::string, AbstractForcesFactory*> forceFactories_;
};

std::map<std::string, AbstractForcesFactory*> AbstractForcesFactory::forceFactories_;

class ForcesFactorySingleton {
public:
  static ForcesFactorySingleton* getInstance();
  static Force* createForce(std::string);

protected:
  ForcesFactorySingleton() {}

private:
  static ForcesFactorySingleton* instance_;
};


class ArmyFactory : public AbstractForcesFactory {
public:
  Force* create();
};

class NavyFactory : public AbstractForcesFactory {
public:
  Force* create();
};

ForcesFactorySingleton* ForcesFactorySingleton::getInstance() {
  if (instance_ == 0) {
    instance_ = new ForcesFactorySingleton();
    AbstractForcesFactory::forceFactories_["Army"] = new ArmyFactory();
    AbstractForcesFactory::forceFactories_["Navy"] = new NavyFactory();
  }
  return instance_;
}

ForcesFactorySingleton* ForcesFactorySingleton::instance_ = 0;

Force* ForcesFactorySingleton::createForce(std::string forceType) {
  if (AbstractForcesFactory::forceFactories_.find(forceType) != AbstractForcesFactory::forceFactories_.end())
    return AbstractForcesFactory::forceFactories_[forceType]->create();
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
  ForcesFactorySingleton::getInstance()->createForce("Army")->message();
  ForcesFactorySingleton::getInstance()->createForce("Navy")->message();
  return 0;
}
