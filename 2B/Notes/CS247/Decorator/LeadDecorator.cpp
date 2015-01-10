#include <string>
#include <iostream>
#include "LeadDecorator.h"
#include "Lead.h"

// constructor
LeadDecorator::LeadDecorator(Lead* lead) : lead_(lead) {}

void LeadDecorator::open() const {
  lead_->open();
}

// member function -- contacts the plan owner
void LeadDecorator::contact() const {
  lead_->contact();
}

// member function -- returns the owner of the plan as a string
std::string LeadDecorator::getMember() const {
  return lead_->getMember();
}

// member function -- changes the plan owner name to a provided string
void LeadDecorator::changeMember(const std::string member) {
  lead_->changeMember(member);
}

// member function -- returns the member's plan balance
int LeadDecorator::memberBalance() const {
  return lead_->memberBalance();
}

// member function -- returns the leads the member has on the site
void LeadDecorator::memberLeads() const {
  return lead_->memberLeads();
}

