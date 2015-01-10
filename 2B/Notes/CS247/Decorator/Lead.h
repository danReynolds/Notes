#ifndef _LEAD_H_
#define _LEAD_H_
#include <string>

class Lead {
public:
  Lead();                                               // constructor
  ~Lead();                                              // destructor
  virtual std::string getMember() const = 0;            // member function -- returns the owner of the plan as a string
  virtual void changeMember(const std::string) = 0;     // member function -- changes the plan owner name to a provided string
  virtual void contact() const = 0;                     // member function -- contacts the plan owner
  virtual void open() const = 0;                        // member function -- opens the lead, sending out emails to relevant parties within the company
  virtual int memberBalance() const = 0;                // member function -- returns the member's plan balance
  virtual void memberLeads() const = 0;                 // member function -- returns the leads the member has on the site
};

#endif

