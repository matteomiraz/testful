# How Testful Works? #

**Testful** is an innovative approach for the (semi) automatic
generation of unit tests for Java classes based on search-based
approaches. The generation of tests is seen as a
search problem, and thus it is tackled through search-based algorithms
(e.g., random, hill climbing, evolutionary computing). Initially these
approaches were developed for procedural software, and only recently
they have been adapted to cope with object-oriented
systems.

Object-oriented systems are particularly tedious when
we want to test their features. One must put the objects in proper
states and provide the correct values for the input parameters of the
functions under test. For this reason, tests for object-oriented
systems are conceptually composed of two parts: the first creates the
desired state of the object/system, while the second exercises the
actual behavior.

Traditional search-based approaches do not take in account the
internal states of objects, even if they explicitly target stateful
systems. Reaching a proper configuration of the objects' state can be
expensive, but we must consider that we do not need to create a
dedicated state for each feature we want to exercise. The same state
(configuration) can enable different features, and new interesting
states can be reached from known ones. This means that a smart test
generation solution must reuse known states for as many features as
possible, and it must also exploit the newly obtained configurations
to exercise new features.

These are the underpinnings of **Testful**. It exploits the
internal state of objects to drive the exploration of the search space
(i.e., of all the possible tests). **Testful** targets the utmost
statement and branch coverage on the class under test (CUT) and
combines an evolutionary algorithm with a hill climbing to work
respectively at class and method level. The former puts objects in
useful states, used by the latter to exercise the uncovered parts of
the class.


## Key principles ##

At the beginning, analyzes the class under test to figure
out all the classes that might be involved in the test (the
_test cluster_). This is done by considering the CUT and by
transitively including the type of all parameters of all public
methods (and constructors). Since abstract classes and interfaces can
be used as formal parameters, **Testful** considers also the additional
classes specified by the user.

For each type contained in the test cluster, **Testful** creates a set
of variables. To enable the polymorphic behaviors, it stores an object
of type **A** either in a variable with the same type or in a
variable whose type is an ancestor of **A** (i.e., **A**'s
super-classes or **A**'s implemented interfaces). Conversely,
when an object is selected from a variable with type **A**, it
may be an instance of **A** or of one of its subclasses.

**Testful** renders any test for the CUT using a sequence of operations
working on those variables. Each test starts in a clean environment,
in which all variables are not initialized; each operation can both
use the variables as actual parameter and store the result in a
variable. A test is rendered by using three kinds of operations::

  * **assignment** assigns a primitive value -i.e., boolean, byte, integer, long, float, double- to a variable.
  * **object creation** creates an object by calling one of the constructors -using variables as actual parameters- and stores the created object in a variable.
  * **method invocation** invokes a method, using variables as receiving object and actual parameters. If the method returns a non-void value, it may be stored in a variable. Note that if the method mutates the state of some objects, the change affects subsequent operations.

Through this structure, **Testful** randomly generates some tests, which
will be evolved using search-based techniques towards the optimal
test. For this purpose, **Testful** works both at the class and method
level, using respectively an evolutionary algorithm and a hill climb.

The former focuses on the whole classes, seeking for a test able to
put objects in interesting states. To recognize these states, we use
as heuristic the level of structural coverage (both statement and
branch coverage) achieved by the test. The higher it is, the better
the test is able to exercise involved classes, and to put objects in
interesting states. The evolutionary algorithm uses this information
as guidance to drive the recombination of tests (i.e., their sequences
of operations).

The latter drills down through the conditions contained in the
different methods. Its goal is to reach a branch never executed
previously, which may represent a feature not exercised yet. To save
on effort, **Testful** only considers branches belonging to a condition
being evaluated, but the execution flow of the current test always
takes a different branch with respect to the one selected.

As starting point, **Testful** uses a test created by the class-level
evolutionary algorithm, which executes the condition controlling the
chosen branch. That test may contain operations to exercise other
features, but they do not affect the chosen branch; to converge
earlier, we use an algorithm to prune out these
operations.

For this purpose, **Testful** can exploit the information regarding the
effects of each method and each constructor. **Testful** can speed-up
the test-generation process by applying some optimizations on the
tests being evaluated. This is achieved by using information regarding
the _type_ of each method, its violations of the
_information hiding_ principle, and its side-effects.

As for types, a method can be: a _mutator_, when it may change the
object's state (this is the default value), a _pure_, when it does not
change the state, but it should not be used to check the correctness
of the behavior (e.g., for the `hashCode` method), an _observer_, when
it is pure and it can be used to check the state of the object (used
in jUnit assertions), or a _static_, when it does not belong to any
object.

A method can violate the _information hiding_ principle by exposing
parts of its internal state, or it can have _side-effects_ on its
parameters. Consider for example the following class:

```
class NoHiding {
  private List state;
  public void setState(List list) {
    this.state = list;
  }
  public List getState() {
    return this.state;
  }
  public static copy(List from, List to) {
    to.addAll(from);
  }
}
```

The first two methods expose the internal representation of the
object. Method `getState` does it directly, by returning the internal
state to the user, while the method `setState` does it indirectly: its
parameter becomes part of the state.

The third method has _side-effects_ on its parameters: it mutates
their state and exchange part of their internal states.

**Testful** allows the user to specify the violation to good software
engineering principles for these three methods.

Note that this information is not used directly in the test generation
process; moreover, if that information is missing or incorrect, the
quality of the result is preserved, but **Testful** may take longer to
generate good tests.

The search process randomly changes the test, executes it, monitors
the condition that controls the selected branch, and calculates the
distance between the values currently used and the one to enter the
branch.

If the search process is successful, the generated test has an
improved structural coverage. The evolutionary algorithm that works at
class level recognizes this and will use it as new starting point to
reach even higher structural coverage.

### Implementation ###

**Testful** is fully implemented in Java. Its internal architecture is
modular, to allow one to easily integrate new coverage criteria, and
organized around three modules:

  * The **instrumenter** exploits _SOOT_ to statically analyze the class under test and insert the tracking code to measure statement coverage, branch coverage, and the distance between the actual and ideal conditions to reach a branch.
  * The **test generator** implements the **Testful** approach. It is based on _jMetal_'s evolutionary algorithm. It uses the Java class-loading facility to load the instrumented classes and ensure isolation among concurrent evaluation of tests. It is able to produce jUnit-compliant tests.
  * **Eclipse GUI** integrates **Testful** with the Eclipse Java Development Tool to provide users with a standard development environment, and improve their user experience.