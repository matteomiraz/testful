package test.model.cluster.testEnum;

public class Normal {

	public Normal(Types t) { }
	public Normal(Types t, int i) { }

	public static void foo(Types t) { }
	public static void foo(Types t, int i) { }

	public void bar(Types t) { }
	public void bar(Types t, int i) { }


	private static test.model.cluster.testEnum.Types testful_convert_Types(short s) {switch(s%3) { default: return test.model.cluster.testEnum.Types.A; case 1: return test.model.cluster.testEnum.Types.B; case 2: return test.model.cluster.testEnum.Types.C;  } }

	public Normal(short p0)  { this( testful_convert_Types(p0) ); }
	public Normal(short p0, int p1)  { this( testful_convert_Types(p0) , p1); }
	public static void testful_foo(short p0)  { foo( testful_convert_Types(p0) ); }
	public static void testful_foo(short p0, int p1)  { foo( testful_convert_Types(p0) , p1); }
	public void testful_bar(short p0)  { bar( testful_convert_Types(p0) ); }
	public void testful_bar(short p0, int p1)  { bar( testful_convert_Types(p0) , p1); }

}
