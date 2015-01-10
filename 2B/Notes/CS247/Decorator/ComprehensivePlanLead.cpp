#include <string>
#include <iostream>
#include "ComprehensivePlanLead.h"

// constructor -- creates a new plan with a given plan owner name
ComprehensivePlanLead::ComprehensivePlanLead(std::string member) : member_(member), balance_(500) {}

// destructor
ComprehensivePlanLead::~ComprehensivePlanLead() {}

// member function -- contacts the plan owner
void ComprehensivePlanLead::contact() const {
  std::cout << "Contacting member " << member_ << " regarding their member information." << std::endl;
}

// member function --opens the lead, sending out emails to relevant parties within the company
// emails are not sent when the lead is constructed in case the member backs out of the process.
void ComprehensivePlanLead::open() const {
  std::cout << "Sending email to member management team to give " << member_ << " a gift basket and pamphlets because of their high membership status." << std::endl;
}

// member function -- returns the owner of the plan as a string
std::string ComprehensivePlanLead::getMember() const {
  return member_;
}

// member function -- changes the plan owner name to a provided string
void ComprehensivePlanLead::changeMember(std::string newMember) {
  member_ = newMember;
}

// member function -- returns the member's plan balance
int ComprehensivePlanLead::memberBalance() const {
  return balance_;
}

// member function -- returns the leads the member has on the site
void ComprehensivePlanLead::memberLeads() const {
  std::cout << "ComprehensivePlanLead ";
}

