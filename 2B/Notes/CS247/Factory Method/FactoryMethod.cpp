#include <string>
#include <iostream>

class Force {
public:
  virtual void message() = 0;
};

class Army : public Force {
public:
  void message();
private:
  static std::string report_;
};

std::string Army::report_ = "All's well on the ground.\n";

class Navy : public Force {
public:
  void message();
private:
  static std::string report_;
};

std::string Navy::report_ = "The seas remain ever our companion.\n";

class AbstractForcesFactory {
public:
  void report();
  virtual Force* create() = 0;
};

class ArmyFactory : public AbstractForcesFactory {
public:
  Force* create();
};

class NavyFactory : public AbstractForcesFactory {
public:
  Force* create();
};

void AbstractForcesFactory::report() {
  Force* force = create();
  force->message();
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
  AbstractForcesFactory* groundUnits = new ArmyFactory();
  groundUnits->report();
  AbstractForcesFactory* seaBrigade = new NavyFactory();
  seaBrigade->report();
  
  delete groundUnits;
  delete seaBrigade;

  return 0;
}
