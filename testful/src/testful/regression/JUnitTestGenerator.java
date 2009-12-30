package testful.regression;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import testful.ConfigProject;
import testful.ConfigRunner;
import testful.IConfigProject;
import testful.TestFul;
import testful.coverage.CoverageInformation;
import testful.model.AssignPrimitive;
import testful.model.Clazz;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.OperationPrimitiveResult;
import testful.model.OperationStatus;
import testful.model.PrimitiveClazz;
import testful.model.Reference;
import testful.model.Test;
import testful.model.TestCoverage;
import testful.model.TestReader;
import testful.model.TestSimplifier;
import testful.model.OperationStatus.Status;
import testful.runner.ClassFinderImpl;
import testful.runner.IRunner;
import testful.runner.RunnerPool;

public class JUnitTestGenerator extends TestReader {

	private static class Config extends ConfigProject implements IConfigProject.Args4j {

		@Option(required = false, name = "-execute", usage = "Should I re-execute the test?")
		private boolean execute;

		@Option(required = false, name = "-contracts", usage = "Should I use JML contracts?")
		private boolean contracts;

		@Option(required = false, name = "-dirTests", usage = "Specify the directory in which generated tests will be put")
		private File dirGeneratedTests;

		@Argument
		private List<String> tests = new ArrayList<String>();

		public boolean isExecute() {
			return execute;
		}

		public boolean isContracts() {
			return contracts;
		}

		public List<String> getTests() {
			return tests;
		}

		public File getDirGeneratedTests() {
			if(!dirGeneratedTests.isAbsolute()) dirGeneratedTests = new File(getDirBase(), dirGeneratedTests.getPath()).getAbsoluteFile();
			return dirGeneratedTests;
		}
	}

	private final boolean contracts;
	private final TestSimplifier simplifier;

	private final File destDir;
	private List<String> suite = new ArrayList<String>();


	public static void main(String[] args) {
		TestFul.printHeader("Regression Testing");

		Config config = new Config();
		TestFul.parseCommandLine(config, args, JUnitTestGenerator.class);

		IRunner executor = null;
		if(config.isExecute())
			executor = RunnerPool.createExecutor("JUnitGenerator", new ConfigRunner());

		JUnitTestGenerator gen = new JUnitTestGenerator(config, executor, config.isContracts(), config.getDirGeneratedTests());

		gen.read(config.getTests());
		gen.writeSuite("", "AllTests");

		System.exit(0);
	}

	public JUnitTestGenerator(IConfigProject config, IRunner executor, boolean contracts, File destDir) {
		this.contracts = contracts;
		this.destDir = destDir;

		if(executor != null) {
			simplifier = new TestSimplifier(executor, new ClassFinderImpl(config.getDirInstrumented(), config.getDirContracts(), config.getDirCompiled()));
		} else {
			simplifier = null;
		}

	}

	@Override
	public void read(String fileName, Test t) {
		PrintWriter out = null;
		try {
			if(simplifier != null) {
				t = simplifier.analyze(t);
				t.getCluster().clearCache();
			}

			final String className = t.getCluster().getCut().getClassName();

			String pkg = getPackageName(className);
			String testName = getTestName(fileName);

			File dir = new File(destDir, pkg.replace('.', File.separatorChar));
			dir.mkdirs();

			File testFile = new File(dir, testName + ".java");
			System.out.println("Converting test: " + fileName + " -> " + testFile.getAbsolutePath());
			out = new PrintWriter(testFile);

			if(!pkg.isEmpty()) {
				out.println("package " + pkg + ";");
				out.println();
			}
			writeClassHeader(out, fileName, t);

			out.println("public class " + testName + " extends junit.framework.TestCase {");
			out.println();
			out.println("\tprivate static final float EPSILON = 0.001f;");

			if(contracts) out.println("\tprivate boolean withContracts;");

			out.println();

			writeInit(out, t);

			Collection<Test> tests = t.split();

			int num = 0;
			for(Test test : tests)
				writeTest(out, num++, test);

			out.println("}");

			suite.add((pkg.isEmpty() ? "" : pkg + "." ) + testName);

		} catch(IOException e) {
			System.err.println("Cannot write the test: " + e);
		} finally {
			if(out != null) out.close();
		}
	}

	private void writeTest(PrintWriter out, int testNum, Test test) {
		out.println("\tpublic void testFul" + testNum + "() throws Exception {");
		out.println();

		int i = 0;
		for(Operation op : test.getTest()) {
			OperationStatus status = (OperationStatus) op.getInfo(OperationStatus.KEY);
			if(status == null || (status.getStatus() != Status.NOT_EXECUTED && status.getStatus() != Status.PRECONDITION_ERROR)) {

				if(i > 0 && i % 500 == 0) {
					out.println("\t\tt" + testNum + "_part" + (i / 500) + "();");
					out.println("\t}");
					out.println();
					out.println("\tprivate void t" + testNum + "_part" + (i / 500) + "() {");
				}

				i++;

				if(status == null) {
					if(op instanceof Invoke || op instanceof CreateObject) {
						out.println("\t\ttry {");
						out.println("\t\t\t" + op + "; // " + i);
						out.println("\t\t} catch(Throwable e) {");


						if(contracts) {
							out.println("\t\t\tif(!withContracts) System.err.println(e);");
							out.println("\t\t\telse if(e instanceof org.jmlspecs.jmlrac.runtime.JMLAssertionError && !(e instanceof org.jmlspecs.jmlrac.runtime.JMLPreconditionError)) fail(e.getMessage());");
						} else {
							out.println("\t\t\tSystem.err.println(\"" + i + ") \" + e);");
						}


						out.println("\t\t}");
					} else out.println("\t\t" + op + "; // " + i);

				} else if(status.getStatus() == Status.SUCCESSFUL) {
					out.println("\t\t" + op + ";");

					Reference target = null;
					if(op instanceof Invoke) target = ((Invoke) op).getTarget();
					else if(op instanceof CreateObject) target = ((CreateObject) op).getTarget();

					OperationPrimitiveResult result = (OperationPrimitiveResult) op.getInfo(OperationPrimitiveResult.KEY);
					if(target != null && result != null && result.isSet()) {
						Clazz type = target.getClazz();
						if(type instanceof PrimitiveClazz) switch(((PrimitiveClazz) type).getType()) {
						case BooleanClass:
						case BooleanType:
							out.println("\t\tassertEquals((boolean)" + AssignPrimitive.getValueString(result.getValue()) + ", (boolean)" + target.toString() + ");");
							break;
						case ByteClass:
						case ByteType:
							out.println("\t\tassertEquals((byte)" + AssignPrimitive.getValueString(result.getValue()) + ", (byte)" + target.toString() + ");");
							break;
						case CharacterClass:
						case CharacterType:
							out.println("\t\tassertEquals((char)" + AssignPrimitive.getValueString(result.getValue()) + ", (char)" + target.toString() + ");");
							break;
						case ShortClass:
						case ShortType:
							out.println("\t\tassertEquals((short)" + AssignPrimitive.getValueString(result.getValue()) + ", (short)" + target.toString() + ");");
							break;
						case IntegerClass:
						case IntegerType:
							out.println("\t\tassertEquals((int)" + AssignPrimitive.getValueString(result.getValue()) + ", (int)" + target.toString() + ");");
							break;
						case LongClass:
						case LongType:
							out.println("\t\tassertEquals((long)" + AssignPrimitive.getValueString(result.getValue()) + ", (long)" + target.toString() + ");");
							break;
						case FloatClass:
						case FloatType:
							out.println("\t\tassertEquals((float)" + AssignPrimitive.getValueString(result.getValue()) + ", (float)" + target.toString() + ", EPSILON);");
							break;
						case DoubleClass:
						case DoubleType:
							out.println("\t\tassertEquals((double)" + AssignPrimitive.getValueString(result.getValue()) + ", (double)" + target.toString() + ", EPSILON);");
							break;
						}
						else if(type.getClassName().equals(String.class.getCanonicalName())) out.println("\t\tassertEquals(" + AssignPrimitive.getValueString(result.getValue()) + "," + target.toString() + ");");
					}

				} else if(status.getStatus() == Status.EXCEPTIONAL) {
					out.println("\t\ttry {");
					out.println("\t\t\t" + op + ";");
					out.println("\t\t\tfail(\"Expecting a " + status.getException() + "\");");
					out.println("\t\t} catch(" + status.getException().getClass().getCanonicalName() + " e) {");
					out.println("\t\t\tassertEquals(\"" + status.getException().getMessage() + "\", e.getMessage());");
					out.println("\t\t}");
				} else out.println("\t\t" + op + "; //TODO: this was a faulty invocation!");
				out.println();
			}
		}
		out.println("\t}");
	}

	private void writeInit(PrintWriter out, Test test) {
		for(Reference ref : test.getReferenceFactory().getReferences())
			out.println("\tprivate " + ref.getClazz().getClassName() + " " + ref.toString() + ";");
		out.println();

		out.println("\tprivate void init() {");

		if(contracts)
			out.println("\t\tthis.withContracts = org.jmlspecs.jmlrac.runtime.JMLCheckable.class.isAssignableFrom(" + test.getCluster().getCut().getClassName() + ".class);");

		for(Reference ref : test.getReferenceFactory().getReferences())
			out.println("\t\t" + ref.toString() + " =  null;");
		out.println("\t}");
		out.println();

		out.println("\t@Override");
		out.println("\tprotected void setUp() {");
		out.println("\t\tinit();");
		out.println("\t}");
		out.println();
		out.println("\t@Override");
		out.println("\tprotected void tearDown() {");
		out.println("\t\tinit();");
		out.println("\t}");
		out.println();
	}

	private static void writeClassHeader(PrintWriter out, String fileName, Test test) {
		out.println("/**");
		out.println("  * Test generated automatically from \"" + fileName + "\" by TestFul test generator<br/>");
		if(test instanceof TestCoverage) {
			out.println("  * Coverage: <ul>");
			for(CoverageInformation info : ((TestCoverage) test).getCoverage())
				out.println("  *   <li>" + info.getName() + ": " + info.getQuality() + "</li>");
			out.println("  * </ul>");
		}
		out.println("  **/");
	}

	private static String getPackageName(final String className) {
		StringBuilder pkgBuilder =  new StringBuilder();
		String[] parts = className.split("\\.");

		for(int i = 0; i < parts.length-1; i++) {
			if(i > 0) pkgBuilder.append(".");
			pkgBuilder.append(parts[i]);
		}

		return pkgBuilder.toString();
	}

	private static String getTestName(String fileName) {
		String testName = fileName.substring(fileName.lastIndexOf(File.separator) + 1, fileName.length()).trim();
		testName = testName.replace('-', '_');
		testName = testName.replace(' ', '_');
		if(testName.length() == 1) testName = testName.toUpperCase();
		else testName = testName.substring(0, 1).toUpperCase() + testName.substring(1);
		testName = "Test" + testName;
		return testName;
	}

	public void writeSuite(String packageName, String className) {
		if(packageName == null) packageName = "";

		try {
			File testFile = new File(destDir, packageName.replace('.', File.separatorChar) + File.separatorChar + className + ".java");
			PrintWriter wr = new PrintWriter(testFile);

			if(!packageName.isEmpty()) {
				wr.println("package " + packageName + ";");
				wr.println();
			}

			wr.println("import junit.framework.*;");
			wr.println("import junit.textui.*;");
			wr.println();
			wr.println("public class " + className + " {");
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

			for(String test : suite)
				wr.println("\t\tsuite.addTestSuite(" + test + ".class);");

			wr.println();
			wr.println("\t\treturn suite;");
			wr.println("\t}");
			wr.println("}");
			wr.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
