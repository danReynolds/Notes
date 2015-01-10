# [Namespaces](http://example.com/ "Title")
## Namespace Pollution
The point is to provide protection from names being the same within each library. With *std* we see headers included in the standard libraries.

Think of a hierarchy of telephone numbers, it uses country code and area code to distinguish between the same numbers.

## Span Multiple Files
When you have same namespace in multiple files, will append one to the other

	namespace myLib {
		void f()
	}
	
	namespace myLib2 {
		void f();
		void g();
		void h() { my Lib::f() } //non-local name
	}
	
	namespace myLib {
		void g()
	}

## Rational Example
### Best Practice: 
put related types, classes, functions together in same namespace
	
	namespace RatADT {
		class Rational {
			public:
				Rational()
		}
	}

	RatADT::Rational::Rational()

should be done in the main of the cpp file, not within the namespace.

## Global Namespaces
implicitly declared in every program and includes all names defined at the global scope. Where all global variables reside. Each file that defines entities at the global scope add those names to the global namespace.

	::member_name // refers to member of global namespace

## Unnamed Namespace
An unnamed namespace is used to declare a local namespace for file-scoped names:

	namespace {
		void f();
	}

Used to create and define functions in a file that you don't want to add to the global namespace. Prevents cluttering of the global namespace. Makes them visible where you are but not anywhere else.

## Referencing Namespace Members
There are constructs for allowing non-local namespace members to be referenced as if local

1. Using declaration:
makes one name on par with local names

    *using std::cout;*

2. Using direction:
Makes all of the names visible. Allows multiple occurrences of a name. Superseded by local declarations of the same name.

	*using namespace std;*

## Namespace Etiquette
* Never place a using directive in a **header file**. Don't know where header is included
* Never include before an *\#include* directive. Affects the names visible in the subsequent header.




