#ifndef _LISTING_LEAD_H_
#define _LISTING_LEAD_H_
#include <string>
#include "Lead.h"
#include "LeadDecorator.h"

class ListingLead : public LeadDecorator {
public:
  ListingLead(Lead*, std::string);         // constructor -- creates a new plan with a given plan owner name
  void open() const;                       // member function --opens the lead, sending out emails to relevant parties within the company
  void contact() const;                    // member function -- contacts the plan owner
  int memberBalance() const;               // member function -- returns the member's plan balance
  void memberLeads() const;                // member function -- returns the leads the member has on the site
private:
  std::string property_;                   // name of the property to be listed
  const static double DISCOUNT_ = 0.1;     // discount for members who travel and list properties
};
#endif
