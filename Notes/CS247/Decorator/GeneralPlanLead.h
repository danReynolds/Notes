#ifndef _GENERAL_PLAN_LEAD_H_
#define _GENERAL_PLAN_LEAD_H_
#include "Lead.h"
#include <string>

class GeneralPlanLead : public Lead {
public:
  GeneralPlanLead(std::string);                       // constructor -- creates a new plan with a given plan owner name
  ~GeneralPlanLead();                                 // destructor
  void contact() const;                               // member function -- contacts the plan owner
  void open() const;                                  // member function --opens the lead, sending out emails to relevant parties within the company
  void changeMember(const std::string newMember);     // member function -- changes the plan owner name to a provided string
  std::string getMember() const;                      // member function -- returns the owner of the plan as a string
  int memberBalance() const;                          // member function -- returns the member's plan balance
  void memberLeads() const;                           // member function -- returns the leads the member has on the site
private:
  std::string member_;                                // name of the plan owner
  int balance_;                                       // amount owed to the site
};

#endif

