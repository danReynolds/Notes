OBJECTS = Lead.o GeneralPlanLead.o ComprehensivePlanLead.o main.o LeadDecorator.o TripLead.o ListingLead.o
CXX = g++
EXEC = exec

all: $(OBJECTS)
	$(CXX) $^ $(CXXFLAGS) -o $(EXEC)

clean:
	rm -rf *.o

Lead.o: Lead.h Lead.cpp
GeneralPlanLead.o: GeneralPlanLead.h GeneralPlanLead.cpp
ComprehensivePlanLead.o: ComprehensivePlanLead.h ComprehensivePlanLead.cpp
main.o: main.cpp
LeadDecorator.o: LeadDecorator.cpp LeadDecorator.h
TripLead.o: TripLead.cpp TripLead.h
ListingLead.o: ListingLead.cpp ListingLead.h
