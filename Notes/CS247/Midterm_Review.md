# Review Session
Inheritance lets us re-use code with an "is a" relationship between objects.  
<hr>
# Inheritance in Constructors
How are base class parts of derived objects created?  
Base class constructor default will be called unless your provide your own in the initialization list.
<hr>
# Vtables  

Dynamic dispatch is what happens when you have virtual functions with pointers to the base or derived class. Since they're a virtual function, it looks them up in the pointer to the *vtable*.  
If it is not virtual, and you call it on base class pointer, it will just call the base class method, not the derived class.
<hr>
# Make
Make is a tool for generating executables that allows us to compile large
software projects easily and save time on compilation by compiling
incrementally.  

	CXX = g++  

	CXXFLAGS = -Wall -MMD  

	OBJECTS =  

	DEPENDS = ${OBJECTS:.o=.d}  

	EXEC =  

	${EXEC} : \${OBJECTS}  

	${CXX} ${CXXFLAGS} ${OBJECTS} -o ${EXEC}  

	clean :  

	rm -rf ${DEPENDS} ${OBJECTS} ${EXEC}  

	-include ${DEPENDS}

List the .o files in the OBJECTS variable. It infers whether it needs to compile .o files again. If any of the files that something depends on changes, it will know. 
<hr>

# Passing by Constant Reference

When programming in C++ we often pass objects to functions. When we
pass by value the object’s copy constructor is called, usually copying the
entire object. This can be quite ineﬃcient so we try to avoid it by passing
a pointer which will only require 32 bits (or 64) to be copied. However,
when we pass a pointer we have to use dereferencing syntax to actually
get to the object so we instead pass a reference which allows us to use the
object as though it were the actual object. Now that we are using a
reference, if the object changed in the function the change will propagate
back to the original object since it is a reference to the original object. To
avoid this, as well as to allow passing literals, we make the reference
constant.

<hr>
# Entity Based
1. Prohibit assignment in the copy constructor
2. Prohibit Type Conversion
3. Avoid Equality
4. Are Mutable

# Value-Based
1. Implement equality and other comparison operator
2. Include a copy constructor and assignment operator
3. Are immutable (instead of changing the value a new value is created)

# Design Patterns  
## Singleton Pattern
Restricts the instantiation of a class to one object. We can enforce this by keeping there to one.  

## Template Method
have a function on a base class that works regardless of whether it's dealing with a specific derived class. They all follow the method's structure.  

## Adapter Pattern
takes stuff from one interface, and makes the two incompatible interfaces able to work together.
## Strategy Pattern
Way of making algorithms interchangeable at run-time. Have humanplayer and computerplay both just sets of methods that the overall player can switch between. Then just delete the human player interface and make a computerplayer interface instead.  
Overall player would just delegate the methods to use.

## Observer Pattern
One class subject has a vector and can subscribe observers to and from the list of subscribed observers.

# MVC
Technically not a design pattern but a software architectural pattern
(broader scope than a design pattern). MVC is used when designing user
interfaces, separating the application into three parts: model, view and
controller. The model takes care of the logic of the application, updating
the view when it changes. The controller takes user input and sends
commands to the model. The view uses the observer pattern to interact
with the model, recieving a notiﬁcation each time the model is updated
then requesting information from the model that it uses to generate a
representation for the user.  

## Divides into 3 Parts: 
1. Model
2. View
3. Controller

Whenever model changes, notifies the view by calling update on them. They all query the model for what they need and update.

![Alt text](/home/vingilot/coding/school/2B/Notes/CS247/strategy.png)

# Composition over Inheritance
## When Inheritance
When you need to be able to upcasting when passing as params, or if you are using all the functionality of the parent class.  

## When Composition  
We choose composition over inheritance because it is possible to modify
the component at run-time while we are able to use it in a similar way to
inheritance by delegation of methods. We still need inheritance when we
need a type hierarchy and it is still useful when using the entire interface
of an existing class.
# Single Responsibility Principle
Each changeable design decision should be encapsulated in separate module.  

# Dependency Inversion Principle
Make the client module and the low level module both depend on an abstraction layer that acts as an interface between them. Similar to the adapter pattern.

# Liskov Substitutability Principle
Derived class must:
1. Accept the same messages, so same functions, with matching signatures.
2. Require no more and promise no less than the base class. Weaker or same precondition, stronger or equal postcondition. Derived class function will change stuff in such a way it fulfils all the conditions the base would have done.  
3. Match the behaviour of the base class, not use more memory or time, any invariants of the base class it should also follow.

# Law of Demeter
* You can play with yourself
* You can play with your own toys, but not take them apart.
* You can play with toys that were given to you
* You can play with toys you've made yourself

Also, you can do it to globals. Only one depth.

ex. Provide the C++ definition of the assignment operator= for the blah Class.  

* Check for self assignment
* Delete what was in the object before unless it was self assignment
* Copy everything into the object from the other object
* Return the dereference of this


