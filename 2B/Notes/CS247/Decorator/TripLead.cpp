#include <string>
#include <iostream>
#include "TripLead.h"

// constructor -- creates a new plan with a given plan owner name
TripLead::TripLead(Lead* lead, std::string destination) : LeadDecorator(lead), location_(destination) {}

// member function --opens the lead, sending out emails to relevant parties within the company
void TripLead::open() const {
  LeadDecorator::open();
  std::cout << "Sending email to travel team since " << LeadDecorator::getMember() << " would like to plan a trip to " <<  location_ << std::endl;
}

// member function -- contacts the plan owner
void TripLead::contact() const {
  std::cout << "contacting member " << " to discuss and help plan current trip" << std::endl;
}

// member function -- returns the member's plan balance
int TripLead::memberBalance() const {
  return LeadDecorator::memberBalance() + TRIPCOST_;
}

// member function -- returns the leads the member has on the site
void TripLead::memberLeads() const {
  LeadDecorator::memberLeads();
  std::cout << "TripLead ";
}
