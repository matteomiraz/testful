package test.model.generic;

public class Generic<T> {

	public Generic(   ) {              }
	public Generic(T p) {              }

	public void m1(T p) {              }
	public   T  m2(   ) { return null; }
	public   T  m3(T p) { return null; }
}
