#include <string>
#include <iostream>
#include "GeneralPlanLead.h"
#include "ComprehensivePlanLead.h"
#include "TripLead.h"
#include "ListingLead.h"

int main(void) {
  //Two families want to go on vacations together this year.
  //Karen's and Gene's families want to register with the online travel website Foreverscape.
  std::string member = "Karen's Family and Gene's Family";

  //They visit the site and pick the comprehensive plan. It has more fees, but offers better support
  //from Foreverscape staff, as well as access to information on travel options, and other bonuses.
  ComprehensivePlanLead planLead(member);
  //They review how much they owe.
  std::cout << "After setting up their Foreverscape plan: " << planLead.getMember() << " owe: " << planLead.memberBalance() << std::endl;

  //After picking their plan, they select the first place they want to go later that spring.
  TripLead firstVacation(&planLead, "Aruba");
  //They review how much they owe.
  std::cout << "After booking their first vacation: " << firstVacation.getMember() << " owe: " << firstVacation.memberBalance() << std::endl;

  //They all have time for one more trip, so after adding their first trip, they create another trip for the winter. 
  TripLead secondVacation(&firstVacation, "Jamaica");
  //They review how much they owe.
  std::cout << "After booking their second vacation: " << secondVacation.getMember() << " owe: " << secondVacation.memberBalance() << std::endl;

  //They realize this would cost them a lot, so they decide that they can rent out Karen's summer house in Cuba to
  //subsidize the total cost they will be paying.
  ListingLead cubaRental(&secondVacation, "Cuba");
  //They review how much they owe.
  std::cout << "After renting out Karen's Cuba home: " << cubaRental.getMember() << " owe: " << cubaRental.memberBalance() << std::endl;

  //Suddenly Gene realizes he has to move to Nunavut because of work, and cannot go on any vacations this year.
  cubaRental.changeMember("Karen's Family");
  std::cout << "After Gene's family backs out of the Foreverscape registration: " << cubaRental.getMember() << " owes: " << cubaRental.memberBalance() << std::endl;


  //Karen's family quickly reviews what they have done and submit:
  std::cout << "Karen's family has the following leads: ";  cubaRental.memberLeads();
  std::cout << std::endl;

  //Now that they are done on the site, they submit their application and await their upcoming trips!
  cubaRental.open();

  std::cout << "Have fun Karen's Family!" << std::endl;
  return 0;
}
