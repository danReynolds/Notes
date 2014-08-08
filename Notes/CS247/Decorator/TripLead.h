#ifndef _TRIP_LEAD_H_
#define _TRIP_LEAD_H_
#include <string>
#include "Lead.h"
#include "LeadDecorator.h"

class TripLead : public LeadDecorator {
public:
  TripLead(Lead*, std::string);            // constructor -- creates a new plan with a given plan owner name
  void open() const;                       // member function --opens the lead, sending out emails to relevant parties within the company
  void contact() const;                    // member function -- contacts the plan owner
  int memberBalance() const;               // member function -- returns the member's plan balance
  void memberLeads() const;                // member function -- returns the leads the member has on the site
private:
  std::string location_;                   // location of the trip as a string
  const static int TRIPCOST_ = 1000;       // cost of the trip
};
#endif
