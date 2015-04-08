# (NON) warranty #
!Testful is released without any warranty. It is just a prototype, whose only purpose is to test the validity of a holistic evolutionary testing approach.

# Known Limitations #

  * TestFul only generate tests for single-threaded classes (Programs that uses Threads are not supported).
  * TestFul performs better if the class does not have any static state, which might be evolved during a sequence of operations. To correctly test classes with a static state, TestFul must be launched with `-reload` option.
  * TestFul does not sets public fields that classes might expose. We consider this fact a bad design, and we encourage to do NOT expose publicly any field that constitutes the state of the class.

# What's Next #

We keep improving TestFul in every release... here we report some of our ideas, grouped as fixes for some current limits and new features we want to provide. (feel free to contribute!)

## Current Limitations ##

  * TestFul is not able to deal with arrays and enumerations when used as input parameters or return value in methods. (We already have some idea on how to manage arrays... but we need some time for the implementation!)
  * For _generic_ classes, TestFul only consider the erased version (e.g., it cannot test `ArrayList<Integer>`, but only `ArrayList<Object>`).

## Prominent features (the cool stuff) ##

  * _Ant Tasks_ and _Maven Plugins_ to automatically generate tests
  * Use Maven for managing the building process