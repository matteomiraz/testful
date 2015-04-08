# Introduction #

This guide presents the TestFul tool, focusing on its command-line version.

TestFul is also available as an eclipse plugin (The command-line tool might be updated more frequently than the eclipse plug-in.).
The internals of the command-line tool are the same of the eclipse plugin, but the latter provides some wizards to eases the usage of TestFul and provides eclipse users with a more integrated experience.

# The Quick Tutorial #

Provides the reader with a two-step tutorial for generating tests.

## The Bank Account ##

```
package bank;

public class BankAccount {
      
  private float balance = 0;

  public float getBalance() {
    return balance;
  }

  public void deposit(float amount) throws Exception {
    if(amount <= 0) 
      throw new Exception("Negative amount");

    balance += amount;
  }
      
  public boolean withdraw(float amount) throws Exception {
    if(amount <= 0) 
      throw new Exception("Negative amount");
          
    if(balance < amount) 
      return false;
          
    balance -= amount;
    return true;
  }
}
```

## Project Configuration ##

By default, TestFul uses an Eclipse-like project configuration, but
users can easily tailor TestFul to fit their project structure.

The _base_ directory is the root of the project, and it contains all the
resources related to the class the user wants to test. Depending on
your operating system and your personal settings, typical examples of
this directory are `/home/user/workspace/BankAccount` (UNIX) or
`c:\workspace\BankAccount` (Windows).

TestFul uses the current directory as base directory, but allows the
user to override it by using the `-dir` option.

Inside the `base` directory, the default project comprises the following sub-directories:
  * _source_ directory: contains the source files and the optional XML descriptions. TestFul uses the `src` directory by default, and allows the user to override it by specifying option `-dirSource`
  * _binary_ directory: contains the compiled classes. TestFul uses the `bin` directory as default, and users can use option `-dirCompiled`  to change it.
  * _instrumented_ directory. To generate tests, TestFul needs to monitor the execution of the classes being tested. For this reason, it instruments the bytecode of the classes and save the result in the "instrumented" directory. TestFul, by default, stores instrumented files in the `instrumented` sub-directory, and the user can change it by using option `-dirInstrumented`.
  * _output_ directory: here generated tests will be saved. By default, TestFul saves generated tests in the `genTests` directory, and allows users to change it using option `-dirTests`.

Notice that the _source_ and the _binary_ must be provided by the user, while TestFul creates _instrumented_ and _output_. For example, this is the structure of the BankAccount project:

![http://wiki.testful.googlecode.com/hg/img/userGuide-before.png](http://wiki.testful.googlecode.com/hg/img/userGuide-before.png)

## Test Generation ##

The user must create an _instrumented_ version of the class he
wants to test, and then he can leverage TestFul to generate tests.
The overall process, with the artifacts required and created by each phase, is the following:

![http://wiki.testful.googlecode.com/hg/img/userGuide-tools.png](http://wiki.testful.googlecode.com/hg/img/userGuide-tools.png)

The process starts with the user that provides the classes he wants to test, and their optional descriptions (in XML files, as we will explain later). These classes are instrumented by using the `instrumenter` tool. As outcome, this tool creates both an _instrumented_ version of the original classes and some _static analysis_ files. These two artifacts, with the (optional) XML class description are used by TestFul to generate the desired tests.

### Instrumentation ###

The instrumentation is performed by the tool `instrumenter.jar`.
Beside the aforementioned parameters to pinpoint the structure of the project, the tool accepts as arguments the classes we want instrument.

We run the instrumenter tool from the simple BankAccount's directory,
by writing:
```
~/workspace/BankAccount$ java -jar instrumenter.jar bank.BankAccount

Testful v. 1.2.0 (dbdcfd283d1e) - Instrumenter
Copyright (c) 2010 Matteo Miraz - http://code.google.com/p/testful
This program comes with ABSOLUTELY NO WARRANTY.
This is free software, and you are welcome to redistribute it under certain conditions.
For more information, read http://www.gnu.org/licenses/gpl-3.0.txt

15:50:55 Instrumenting: [bank.BankAccount]
Soot started on Thu Mar 17 15:50:57 CET 2011
Transforming bank.BankAccount... 
15:50:57 Instrumenting bank.BankAccount.<init>
15:50:57 Instrumenting bank.BankAccount.getBalance
15:50:58 Instrumenting bank.BankAccount.deposit
15:50:58 Instrumenting bank.BankAccount.withdraw
Writing to /home/matteo/workspace/BankAccount/./instrumented/bank/BankAccount.class
Soot finished on Thu Mar 17 15:50:58 CET 2011
Soot has run for 0 min. 0 sec.
15:50:58 class bank.BankAccount: 1 data-flow iterations (1.543991 ms)
```

Once the instrumentation is done, some artifacts are created inside the instrumentation directory (the default is `instrumented`):
  * the instrumented version of the class (file `instrumented/bank/BankAccount.class`)
  * the static analysis of the class (file `instrumented/bank/BankAccount.wgz`). The result of the static analysis is also present in the file `instrumented/bank/BankAccount.dot`. This can be processed with [Graphviz](http://www.graphviz.org/) to obtain the image of the Program Dependence Graph.

### TestFul ###

Once the classes have been instrumented, it is possible to run TestFul with the following command:

` java -jar testful.jar -cut bank.BankAccount `

In this case, we only specify the class under test (option `-cut`), and we use the default value for all the TestFul's options.

Given the simplicity of the class being tested, we can reduce the running time to 20 seconds by specifying:

  * **-smartAncestors 5** for using only 5 seconds of random testing, instead of 60 seconds).
  * **-time 15** for running the evolutionary algorithm of TestFul for only 15 seconds, instead of 10 minutes (600 seconds).

```
~/workspace/BankAccount$ java -jar testful.jar -smartAncestors 5 -time 10 -cut bank.BankAccount 

Testful v. 1.2.0 (dbdcfd283d1e) - Evolutionary test generator
Copyright (c) 2010 Matteo Miraz - http://code.google.com/p/testful
This program comes with ABSOLUTELY NO WARRANTY.
This is free software, and you are welcome to redistribute it under certain conditions.
For more information, read http://www.gnu.org/licenses/gpl-3.0.txt

17:04:55 WARNING Cannot parse XML descriptor of class bank.BankAccount: java.io.FileNotFoundException: 
/home/matteo/workspace/BankAccount/./src/bank/BankAccount.xml (No such file or directory)
17:04:55 Generating smart population
17:04:55 Detected 2 cpus (or cores): starting one thread per cpus.
17:04:55 Started 2 workers
17:04:55 Added testful-1300377895540
17:04:56 20.24% 0:03 to go Running 805 jobs (4374 done)
Coverage:
  Def-Exposition: 0.0
  Def-Use Pairs: 0.0
  P-Uses: 0.0
  Basic Block Coverage: 15.0
  Branch Coverage: 6.0
  fault discovered: 0.0

[ ... ]

17:05:00 100.34% 0:00 to go Running 274 jobs (91365 done)
Coverage:
  Def-Exposition: 0.0
  Def-Use Pairs: 0.0
  P-Uses: 0.0
  Basic Block Coverage: 15.0
  Branch Coverage: 6.0
  fault discovered: 0.0

17:05:01 (60.14%) Creating initial population - 0:03 to go
17:05:01 (60.22%) Generation 0 (initial population) - 0:03 to go
17:05:01 (60.54%) Generation 1 - 0:03 to go
17:05:02 (62.01%) Generation 2 - 0:03 to go
17:05:02 (62.96%) Generation 3 - 0:03 to go
17:05:02 (64.15%) Generation 4 - 0:03 to go
17:05:02 (64.88%) Generation 5 - 0:03 to go
17:05:02 Local search on fronteer (1)

[ ... ]

17:05:05 Local search on fronteer (21)
17:05:05 (98.02%) Generation 56 - 0:00 to go
17:05:05 (98.57%) Generation 57 - 0:00 to go
17:05:05 (98.85%) Generation 58 - 0:00 to go
17:05:05 (99.19%) Generation 59 - 0:00 to go
17:05:05 (99.57%) Generation 60 - 0:00 to go
17:05:05 Local search on fronteer (35)
17:05:05 Selected target: 4 (score: 160.0 length: 7)
17:05:05 Target 4 missed 1 times
17:05:05 WARNING The behavioral model is missing
17:05:05 Creating test BankAccount_TestCase
```

It is safe to ignore the two warnings.
  * The first one indicates that TestFul was not able to locate any XML description of the class (it is optional, and in this first run we omitted it).
  * The second warning indicates that TestFul did not derived any behavioral model of the class under test, which is in line with our requests (it must be enabled explicitly).

The last line shows that tests has been created, and they are put in the
_genTests_ directory (unless the `-dirTests` option has been used). There it is possible to find both the binary version (extension .ser.gz) and the jUnit version of generated tests. The user can inspect the latter and run it using the standard jUnit 3.x. An example of a generated test is the following (TestFul is a randomized approach, hence it is likely that it generates different tests every time it is used):

```
package bank;

/** Test Generated by TestFul */
public class BankAccount_TestCase extends junit.framework.TestCase {

  // Binary test: /home/matteo/workspace/BankAccount/./genTests/bank/BankAccount_1.ser.gz
  public void testFul1() throws Exception {

    java.lang.Float java_lang_Float_0 = null, java_lang_Float_1 = null, java_lang_Float_2 = null, java_lang_Float_3 = null;
    bank.BankAccount bank_BankAccount_0 = null, bank_BankAccount_1 = null;

    java_lang_Float_0 = (float)-3.4028235E38;
    java_lang_Float_1 = (float)2.7860668E38;
    bank_BankAccount_0 = new bank.BankAccount();
    assertEquals(0.0, (float)bank_BankAccount_0.getBalance(), 0.001);

    bank_BankAccount_1 = new bank.BankAccount();
    assertEquals(0.0, (float)bank_BankAccount_1.getBalance(), 0.001);

    bank_BankAccount_0.deposit(java_lang_Float_1);
    assertEquals(2.7860668E38, (float)bank_BankAccount_0.getBalance(), 0.001);

    try {
      bank_BankAccount_1.withdraw(java_lang_Float_0);
      fail("Expecting a java.lang.Exception");
    } catch(java.lang.Exception e) {
      assertEquals("Negative amount", e.getMessage());
      assertEquals(0.0, (float)bank_BankAccount_1.getBalance(), 0.001);
    }

    float tmp0 = bank_BankAccount_0.getBalance();
    java_lang_Float_2 = (float) tmp0;
    assertEquals(2.7860668E38, tmp0, 0.001);
    assertEquals(2.7860668E38, (float)bank_BankAccount_0.getBalance(), 0.001);

    boolean tmp1 = bank_BankAccount_1.withdraw(java_lang_Float_1);
    assertEquals(false, tmp1);
    assertEquals(0.0, (float)bank_BankAccount_1.getBalance(), 0.001);

    float tmp2 = bank_BankAccount_1.getBalance();
    java_lang_Float_3 = (float) tmp2;
    assertEquals(0.0, tmp2, 0.001);
    assertEquals(0.0, (float)bank_BankAccount_1.getBalance(), 0.001);

    boolean tmp3 = bank_BankAccount_0.withdraw(java_lang_Float_2);
    assertEquals(true, tmp3);
    assertEquals(0.0, (float)bank_BankAccount_0.getBalance(), 0.001);

    try {
      bank_BankAccount_0.deposit(java_lang_Float_3);
      fail("Expecting a java.lang.Exception");
    } catch(java.lang.Exception e) {
      assertEquals("Negative amount", e.getMessage());
      assertEquals(0.0, (float)bank_BankAccount_0.getBalance(), 0.001);
    }

  }
}
```