# Version 2.0 (alpha - work in progress) #
  * Added advanced coverage criteria
    * Data-Flow Graph
    * Behavioral Coverage
  * Boundary Value Coverage
  * Support for mutation testing
  * Improved performances (60x)
    * avoid useless local serializations
    * improved serialization for remote workers
    * limited the use of Tracker Data
    * Improved jMetal's NSGA-II Ranking algorithm
  * Better modularization
    * generic remote job executor
  * Improved the stability
    * tons of bug fixes

# Version 1.2 (internal release) #
  * Improved Fault Coverage tracking
  * Test Stopper
    * Each method and constructor has a maximum execution time.
    * If a method / constructor requires more time, it is stopped (and a failure is reported).
  * new XML schema
    * **Automagic** configuration
    * New instrumentation policy
  * Improved performances
    * Removed useless data from runtime
  * Improved project setup:
    * Support for libraries
  * Improved QA process
    * Created automated building and verification process
    * More jUnit tests
    * Possibility to provide the random SEED to use and make Testful deterministic

# Version 1.1.3 #
  * Workaround for the instrumentation problem
    * SOOT upgraded to 2.4.0
    * The Tracker.getSingleton() is invoked every time a tracker is needed (workaround, no caching)
  * Tracking of the test's execution statistics
    * Logging the number of:
      * operations (total)
      * invalid operations (not executed due to a preconditions problems)
      * erroneous operations (executed, but with postconditions errors)
      * successful operations (executed, without any error)

# Version 1.1.2 #
  * New XML schema
    * The XSD is online at http://testful.sourceforge.net/schema/1.1/testful.xsd
    * The array property has been removed

# Version 1.1.1 #
  * Fixed a naive jUnit bug, modifying our jUnit test conversion (the **ICSE 2010 bug**!).

# Version 1.1.0 #
  * Eclipse GUI support
  * Used in the formal research demonstration at [ICSE 2010](http://matteo.miraz.it/research/papers/ICSE10)

# Version 1.0.0 #
  * Initial version
    * The evolutionary algorithm is guided by the coverage of the control-flow graph (both basic blocks and branches)
    * Usage of a hybrid approach to efficiently reach high structural coverages
  * Used for the paper presented at [ICST 2010](http://matteo.miraz.it/research/papers/ICST10)
  * Contains the efficiency enhancement techniques presented at [CEC 2010](http://matteo.miraz.it/research/papers/CEC10)