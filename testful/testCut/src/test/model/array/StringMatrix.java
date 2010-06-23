package test.model.array;

public class StringMatrix {

	public static StringArray testful_crea(int p0)  { return new StringArray (crea(p0)); }
	public static String[] crea(int n) {
		if(n < 0) n = 0;
		if(n > 10) n = 10;

		return new String[n];
	}

	public static int testful_conta(StringArrayArray p0)  { return conta(p0.toArray() ); }
	public static int conta(String[][] m) {

		int n = 0;
		for (int i = 0; i < m.length; i++)
			n += m[i].length;

		return n;
	}
}
