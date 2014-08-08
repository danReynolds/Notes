#include <string>
#include <iostream>
#include "GeneralPlanLead.h"

// constructor -- creates a new plan with a given plan owner name
GeneralPlanLead::GeneralPlanLead(std::string member) : member_(member), balance_(0) {}

// destructor
GeneralPlanLead::~GeneralPlanLead() {}

// member function -- contacts the plan owner
void GeneralPlanLead::contact() const {
  std::cout << "Contacting member " << member_ << " regarding their member information." << std::endl;
}

// member function --opens the lead, sending out emails to relevant parties within the company
void GeneralPlanLead::open() const {
  std::cout << "Sending email to member management team to send a pamphlet to " << member_ << ", but no gift basket, they're only a general member." << std::endl;
}

// member function -- returns the owner of the plan as a string
std::string GeneralPlanLead::getMember() const {
  return member_;
}

// member function -- changes the plan owner name to a provided string
void GeneralPlanLead::changeMember(const std::string newMember) {
  member_ = newMember;
}

// member function -- returns the member's plan balance
int GeneralPlanLead::memberBalance() const {
  return balance_;
}

// member function -- returns the leads the member has on the site
void GeneralPlanLead::memberLeads() const {
  std::cout << "GeneralPlanLead ";
}

