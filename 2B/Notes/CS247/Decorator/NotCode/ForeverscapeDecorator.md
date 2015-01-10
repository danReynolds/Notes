# Design Pattern:
*Foreverscape* travel site member registration is implemented using the *Decorator Pattern*. 

## Problem
When new members register on the travel site, they can pick several different plan types, and then proceed to create as many trip leads as they want. They can also make listing leads if they want to use the site to list their own travel spots.

The *decorator pattern* allows us to stack as many trips and property listings the new member could want dynamically on top of each other when they sign up with a plan.

Instead of having to maintain concrete combinations of each number of trips and listings for a certain plan, decorators can be added on top of each other to produce the desired result. This allows new users of the site to customize their experience to get exactly what they want and puts the design of their unique Foreverscape experience into their own hands.

































