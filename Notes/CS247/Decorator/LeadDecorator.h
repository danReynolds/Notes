#ifndef _LEAD_DECORATOR_H_
#define _LEAD_DECORATOR_H_
#include <string>
#include "Lead.h"

class LeadDecorator : public Lead {
public:
  virtual void open() const;                          // member function -- opens the lead, sending out emails to relevant parties within the company   
  virtual void contact() const;                       // member function -- contacts the plan owner
  std::string getMember() const;                      // member function -- returns the owner of the plan as a string
  void changeMember(const std::string);               // member function -- changes the plan owner name to a provided string
  virtual int memberBalance() const;                  // member function -- returns the member's plan balance
  void memberLeads() const;                           // member function -- returns the leads the member has on the site
protected:
  LeadDecorator(Lead*);                               // constructor -- protected to ensure the class is abstract
private:
  Lead* lead_;                                        // component that is to be accessed
};
#endif
