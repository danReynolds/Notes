> # Principles
> [Course Notes](https://www.student.cs.uwaterloo.ca/~cs247/current/Lectures/12OO_Principles.pdf)  

1. Open-close Principle
2. Liskov Substitutability Principle
3. Favour Composition over Inheritance, broken for Patron, correct for Borrowable
4. Dependency Inversion Principle
5. Single Responsibility Principle, broken by Library Admin
6. Law of Demeter

# What is Wrong with this Design
	LibraryAdmin::checkout() {
		borrowable->checkout();
	}

The Loan collection is only accessible through the collection of borrowables. Give patron a link to all the items it has out on loan, so that you can navigate from the patron to the collection of borrowables.

## Changing the Borrowable
	//Add fine to the Patron:           
	Patron:
		fine()
		finels()
	//Add fine to Loan:
	Loan:
		ComputeFine()

Make it possible to compute a fine when they return a book. have a compute fine and a check for outstanding fine. 

	//ReserveStatus:
		return()
		checkout()
	// public ReserveStatus OnReserve:
		return()
		checkout()
	// public ReserveStatus OffReserve:
		return()
		checkout()
	
	//Borrowable:
		ReserveStatus status
		putOnReserve()
		putOffReserve()

# Announcements
1. Midterm office hours from: Wed 3:30-4:30 and Thurs 10:00-12:30
2. No lecture thursday, no tutorial friday
3. The project part 2 probably won't go out before the long weekend. But begin migration to MVC. Download and install gtk 


