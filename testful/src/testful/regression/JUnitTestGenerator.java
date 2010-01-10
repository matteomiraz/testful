package testful.regression;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import testful.ConfigProject;
import testful.IConfigProject;
import testful.TestFul;
import testful.coverage.CoverageExecutionManager;
import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.coverage.whiteBox.AnalysisWhiteBox;
import testful.model.AssignPrimitive;
import testful.model.Clazz;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.OperationPrimitiveResult;
import testful.model.OperationStatus;
import testful.model.OptimalTestCreator;
import testful.model.PrimitiveClazz;
import testful.model.Reference;
import testful.model.Test;
import testful.model.TestCoverage;
import testful.model.TestExecutionManager;
import testful.model.TestReader;
import testful.model.TestSplitter;
import testful.runner.ClassFinder;
import testful.runner.ClassFinderCaching;
import testful.runner.ClassFinderImpl;
import testful.runner.Context;
import testful.runner.RunnerPool;
import testful.utils.ElementManager;
import testful.utils.ElementWithKey;
import testful.utils.Utils;

public class JUnitTestGenerator extends TestReader {
	private static final Logger logger = Logger.getLogger("testful.regression");

	/** maximum number of operation per jUnit test */
	private static final int MAX_TEST_LEN = 1000;

	private static class Config extends ConfigProject implements IConfigProject.Args4j {

		@Option(required = false, name = "-reload", usage = "Reload classes before each run (reinitialize static fields)")
		private boolean reloadClasses;

		@Option(required = false, name = "-noMinimize", usage = "Do not try to split and minimize tests.")
		private boolean noMinimize;

		@Option(required = false, name = "-dirTests", usage = "Specify the directory in which generated tests will be put")
		private File dirGeneratedTests;

		@Argument
		private List<String> tests = new ArrayList<String>();

		public List<String> getTests() {
			return tests;
		}

		public File getDirGeneratedTests() {
			if(!dirGeneratedTests.isAbsolute()) dirGeneratedTests = new File(getDirBase(), dirGeneratedTests.getPath()).getAbsoluteFile();
			return dirGeneratedTests;
		}

		public boolean isReloadClasses() {
			return reloadClasses;
		}

		public boolean isMinimize() {
			return !noMinimize;
		}
	}

	private final File destDir;
	private final IConfigProject config;
	private final boolean minimize;

	private final boolean reloadClasses;
	private final ClassFinder finder;
	private final RunnerPool runner;
	private final TestSuite suite = new TestSuite();


	public static void main(String[] args) {

		Config config = new Config();
		TestFul.parseCommandLine(config, args, JUnitTestGenerator.class, "JUnit test generator");

		if(!config.isQuiet())
			TestFul.printHeader("JUnit test generator");

		TestFul.setupLogging(config);

		RunnerPool.getRunnerPool().startLocalWorkers();

		JUnitTestGenerator gen = new JUnitTestGenerator(config, config.getDirGeneratedTests(), config.isReloadClasses(), config.isMinimize());

		gen.read(config.getTests());

		gen.writeSuite();

		System.exit(0);
	}

	public JUnitTestGenerator(IConfigProject config, File destDir, boolean reloadClasses, boolean minimize) {
		this.config = config;
		this.destDir = destDir;
		this.minimize = minimize;

		runner = RunnerPool.getRunnerPool();
		this.reloadClasses = reloadClasses;
		ClassFinder tmp = null;
		try {
			tmp = new ClassFinderCaching(new ClassFinderImpl(config.getDirInstrumented(), config.getDirContracts(), config.getDirCompiled()));
		} catch (RemoteException e) {
			// ignore
		}
		finder = tmp;
	}

	@Override
	public void read(String fileName, Test test) {
		TestCase testCase = suite.get(test.getCluster().getCut().getClassName());

		if(minimize) {
			// simplify
			test = test.removeUselessDefs().simplify().getSSA();

			// split
			List<Test> parts = TestSplitter.split(false, test);

			for (Test part : parts) {
				//re-simplify
				part = part.removeUselessDefs().simplify().getSSA().removeUselessDefs().reorganize().sortReferences();
				testCase.add(part);
			}
		} else {
			testCase.add(test);
		}
	}

	public void writeSuite() {
		suite.write();
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	private class TestSuite extends ElementManager<String, TestCase>{
		private static final long serialVersionUID = -209417855781416430L;

		@Override
		public TestCase get(String className) {
			TestCase test = super.get(className);
			if(test == null) {
				test = new TestCase(className);
				put(test);
			}
			return test;
		}

		public void write() {
			for (TestCase test : this) {
				List<String> tests = test.write();

				// if there is only 1 test, skip the creation of the suite!
				if(tests.size() > 1) {
					try {
						String suiteName = "AllTests_" + test.getClassName();

						File testFile = new File(destDir, test.getPackageName().replace('.', File.separatorChar) + File.separatorChar + suiteName + ".java");
						PrintWriter wr = new PrintWriter(testFile);

						if(!test.getPackageName().isEmpty()) {
							wr.println("package " + test.getPackageName() + ";");
							wr.println();
						}

						wr.println("import junit.framework.*;");
						wr.println("import junit.textui.*;");
						wr.println();
						wr.println("public class " + suiteName + " {");
						wr.println();
						wr.println("\tpublic static void main(String[] args) {");
						wr.println("\t\tTestRunner runner = new TestRunner();");
						wr.println("\t\tTestResult result = runner.doRun(suite(), false);");
						wr.println();
						wr.println("\t\tif (! result.wasSuccessful())");
						wr.println("\t\t\tSystem.exit(1);");
						wr.println("\t}");

						wr.println("\tpublic static junit.framework.Test suite() {");
						wr.println("\t\tjunit.framework.TestSuite suite = new junit.framework.TestSuite(\"Test generated by testFul\");");
						wr.println();

						for(String testName : tests)
							wr.println("\t\tsuite.addTestSuite(" + testName + ".class);");

						wr.println();
						wr.println("\t\treturn suite;");
						wr.println("\t}");
						wr.println("}");
						wr.close();

						logger.info("Test suite " + suiteName + " saved");

					} catch(IOException e) {
						logger.log(Level.WARNING, "Cannot write the test suite: " + e.getMessage(), e);
					}
				}
			}
		}
	}

	private class TestCase implements ElementWithKey<String> {

		/** full qualified class name */
		private final String fullQualifiedClassName;

		/** the package of the class */
		private final String packageName;

		/** the (simple) class name, without package */
		private final String className;

		private OptimalTestCreator tests = new OptimalTestCreator();

		private final TrackerDatum[] data;

		public TestCase(String className) {
			fullQualifiedClassName = className;

			{
				StringBuilder pkgBuilder =  new StringBuilder();
				String[] parts = className.split("\\.");

				this.className = parts[parts.length - 1];

				for(int i = 0; i < parts.length-1; i++) {
					if(i > 0) pkgBuilder.append(".");
					pkgBuilder.append(parts[i]);
				}

				packageName = pkgBuilder.toString();
			}

			AnalysisWhiteBox whiteAnalysis = AnalysisWhiteBox.read(config.getDirInstrumented(), className);
			data = Utils.readData(whiteAnalysis);
		}

		public String getPackageName() {
			return packageName;
		}

		public String getClassName() {
			return className;
		}

		private String getTestName(Integer idx) {
			String testName = className.replace('-', '_');
			testName = testName.replace(' ', '_');
			if(idx != null) testName = testName + "_" + idx;
			return testName + "_TestCase";
		}

		public void add(Test t) {
			try {
				// calculate the coverage for the test
				Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager> ctx = CoverageExecutionManager.getContext(finder, t, data);
				ctx.setRecycleClassLoader(!reloadClasses);
				Future<ElementManager<String, CoverageInformation>> f = runner.execute(ctx);

				tests.update(new TestCoverage(t, f.get()));
			} catch (Exception e) {
				logger.log(Level.WARNING, "Failed to evaluate a test: " + e.getMessage(), e);
			}
		}

		@Override
		public String getKey() {
			return fullQualifiedClassName;
		}

		public List<String> write() {

			final List<TestCoverage> optimalTests = tests.get();
			final List<String> fileNames = new ArrayList<String>();

			final boolean singleTest = isSingleTest(optimalTests);

			/** the progressive number of a jUnit TestCase */
			int currentJUnitTest = 0;

			/** progressive number of each test method, within a single jUnit TestCase */
			int currentTestMethod = 0;

			int testLength = 0;
			PrintWriter writer = null;
			for (TestCoverage test : optimalTests) {

				try {
					testLength += test.getTest().length;
					if(writer == null || testLength > MAX_TEST_LEN) {

						if(writer != null) writeFooterAndClose(writer);

						String testName = getTestName(singleTest?null:currentJUnitTest++);

						File dir = new File(destDir, packageName.replace('.', File.separatorChar));
						dir.mkdirs();

						File testFile = new File(dir, testName + ".java");
						writer = new PrintWriter(testFile);

						logger.info("Creating test " + testName);
						writeHeader(writer, testName);
						fileNames.add((packageName.isEmpty()? "" : packageName + ".") + testName);
						currentTestMethod = 0;
						testLength = test.getTest().length;
					}

					writeTest(test, writer, ++currentTestMethod);

				} catch (FileNotFoundException e) {
					logger.log(Level.WARNING, "Cannot open the file: " + e.getMessage(), e);
				}
			}

			if(writer != null) writeFooterAndClose(writer);

			return fileNames;
		}


		private void writeTest(Test test, PrintWriter out, int testNumber) {
			final Operation[] operationStatus;
			try {
				OperationPrimitiveResult.insert(test.getTest());
				OperationStatus.insert(test.getTest());
				Context<Operation[], TestExecutionManager> ctx = TestExecutionManager.getContext(finder, test, data);
				ctx.setStopOnBug(false);
				Future<Operation[]> result = runner.execute(ctx);
				operationStatus = result.get();
			} catch(Exception e) {
				logger.warning(e.getMessage());
				return;
			}

			out.println();
			out.println("\tpublic void testFul" + testNumber + "() throws Exception {");
			out.println();

			// create variables
			out.println("\t\tObject tmp = null;");

			{	// group references by type
				Map<String, List<String>> map = new HashMap<String, List<String>>();
				for(Reference ref : test.getReferenceFactory().getReferences()) {
					List<String> vars = map.get(ref.getClazz().getClassName());
					if(vars == null) {
						vars = new ArrayList<String>();
						map.put(ref.getClazz().getClassName(), vars);
					}
					vars.add(ref.toString());
				}
				// create variable lines: "Type var1 = null, var2 = null;"
				for (String type : map.keySet()) {
					out.print("\t\t" + type);

					boolean first = true;
					for (String var : map.get(type)) {
						if(first) first = false;
						else out.print(",");

						out.print(" " + var + " = null");
					}
					out.println(";");
				}
			}

			out.println();
			for(Operation op : operationStatus) {
				OperationStatus status = (OperationStatus) op.getInfo(OperationStatus.KEY);

				if(status == null) {
					if(op instanceof Invoke || op instanceof CreateObject) {
						out.println("\t\ttry {");
						out.println("\t\t\t" + op + ";");
						out.println("\t\t} catch(Throwable e) {");
						out.println("\t\t\te.printStackTrace();");
						out.println("\t\t}");
					} else {
						out.println("\t\t" + op + ";");
					}

				} else {
					switch(status.getStatus()) {
					case SUCCESSFUL:
						if(op instanceof Invoke) {

							OperationPrimitiveResult result = (OperationPrimitiveResult) op.getInfo(OperationPrimitiveResult.KEY);
							if(result != null && result.isSet()) {
								Reference target = ((Invoke) op).getTarget();;
								Clazz retType = ((Invoke)op).getMethod().getReturnType();

								Invoke inv = new Invoke(null, ((Invoke) op).getThis(), ((Invoke) op).getMethod(), ((Invoke) op).getParams());
								out.println("\t\ttmp = " + inv + ";");

								// Put the value also in the original target
								if(target != null) {
									final String cast;
									if(target.getClazz() instanceof PrimitiveClazz) {
										cast = ((PrimitiveClazz) target.getClazz()).getCast() + " ";
									} else {
										cast = "(" + target.getClazz().getClassName() + ")";
									}

									out.println("\t\t" + target.toString() + " = " + cast + " tmp");
								}

								// check the result (tmp's value)
								if(retType instanceof PrimitiveClazz) {
									switch(((PrimitiveClazz) retType).getType()) {
									case BooleanClass:
									case BooleanType:
										out.println("\t\tassertEquals((boolean)" + AssignPrimitive.getValueString(result.getValue()) + ", (boolean) tmp);");
										break;
									case ByteClass:
									case ByteType:
										out.println("\t\tassertEquals((byte)" + AssignPrimitive.getValueString(result.getValue()) + ", (byte) tmp);");
										break;
									case CharacterClass:
									case CharacterType:
										out.println("\t\tassertEquals((char)" + AssignPrimitive.getValueString(result.getValue()) + ", (char) tmp);");
										break;
									case ShortClass:
									case ShortType:
										out.println("\t\tassertEquals((short)" + AssignPrimitive.getValueString(result.getValue()) + ", (short) tmp);");
										break;
									case IntegerClass:
									case IntegerType:
										out.println("\t\tassertEquals((int)" + AssignPrimitive.getValueString(result.getValue()) + ", (int) tmp);");
										break;
									case LongClass:
									case LongType:
										out.println("\t\tassertEquals((long)" + AssignPrimitive.getValueString(result.getValue()) + ", (long) tmp);");
										break;
									case FloatClass:
									case FloatType:
										out.println("\t\tassertEquals((float)" + AssignPrimitive.getValueString(result.getValue()) + ", (float) tmp, 0.001f);");
										break;
									case DoubleClass:
									case DoubleType:
										out.println("\t\tassertEquals((double)" + AssignPrimitive.getValueString(result.getValue()) + ", (double) tmp, 0.001f);");
										break;
									}
								} else if(retType.getClassName().equals(String.class.getCanonicalName())) {
									out.println("\t\tassertEquals(" + AssignPrimitive.getValueString(result.getValue()) + "," + target.toString() + ");");
								} else {
									System.err.println("Unusable return status");
								}

								out.println();

								break;
							}
						}

						out.println("\t\t" + op + ";");
						break;

					case EXCEPTIONAL:
						out.println("\t\ttry {");
						out.println("\t\t\t" + op + ";");
						out.println("\t\t\tfail(\"Expecting a " + status.getException() + "\");");
						out.println("\t\t} catch(" + status.getException().getClass().getCanonicalName() + " e) {");
						out.println("\t\t\tassertEquals(\"" + status.getException().getMessage() + "\", e.getMessage());");
						out.println("\t\t}");
						break;

					case POSTCONDITION_ERROR:
						out.println("\t\t" + op + "; //TODO: this was a faulty invocation!");
						break;

					case NOT_EXECUTED:
						//skip: the operation has not been executed!
						break;
					case PRECONDITION_ERROR:
						//skip: the operation has not been executed!
						break;
					}
				}
			}

			out.println("\t}");
		}

		private void writeHeader(PrintWriter out, String testName) {
			if(!packageName.isEmpty()) {
				out.println("package " + packageName + ";");
				out.println();
			}

			out.println("/** Test Generated by TestFul */");
			out.println("public class " + testName + " extends junit.framework.TestCase {");
		}

		private void writeFooterAndClose(PrintWriter writer) {
			writer.println("}");
			writer.println();
			writer.close();
		}

		/**
		 * Checks if the optimal tests fits in a single test
		 * @param optimalTests the optimal tests
		 * @return true if the sum of all the operations are less than the maximum length of a test
		 */
		private boolean isSingleTest(List<TestCoverage> optimalTests) {
			int tot = 0;
			for (TestCoverage t : optimalTests) {
				tot += t.getTest().length;
				if(tot > MAX_TEST_LEN)
					return false;
			}

			return true;
		}

		@Override
		public TestCase clone() throws CloneNotSupportedException {
			throw new CloneNotSupportedException("It is impossible to clone test cases");
		}
	}
}
