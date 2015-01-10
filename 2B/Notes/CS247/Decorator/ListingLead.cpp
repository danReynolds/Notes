#include <string>
#include <iostream>
#include "ListingLead.h"

// constructor -- creates a new plan with a given plan owner name
ListingLead::ListingLead(Lead* lead, std::string property) : LeadDecorator(lead), property_(property) {}

// member function --opens the lead, sending out emails to relevant parties within the company
void ListingLead::open() const {
  LeadDecorator::open();
  std::cout << "Sending email to the listing team since " << LeadDecorator::getMember() << " would like to rent out their property: " <<  property_ << std::endl;
}

// member function -- contacts the plan owner
void ListingLead::contact() const {
  std::cout << "contacting member " << " to discuss and help plan listing their property" << std::endl;
}

// member function -- returns the member's plan balance
int ListingLead::memberBalance() const {
  return LeadDecorator::memberBalance() - LeadDecorator::memberBalance() * DISCOUNT_;
}

// member function -- returns the leads the member has on the site
void ListingLead::memberLeads() const {
  LeadDecorator::memberLeads();
  std::cout << "ListingLead ";
}
