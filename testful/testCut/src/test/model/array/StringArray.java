package test.model.array;

import java.util.Arrays;

public class StringArray {
	private java.lang.String[] array = {};
	private int n = 0;

	public StringArray() { }
	public StringArray(StringArray a) {
		if(a == null) throw new InvalidException();
		array = a.toArray();
		n = array.length;
	}
	public StringArray(java.lang.String[] array) {
		if(array == null) throw new InvalidException();
		this.array = array;
		n = array.length;
	}

	public java.lang.String[] toArray() {
		return array;
	}

	public void addHead(java.lang.String o) {
		java.lang.String[] old = array;
		array = new java.lang.String[n+1];
		array[0] = o;
		for (int i = 0; i < n; i++)
			array[i+1] = old[i];

		n += 1;
	}

	public void addTail(java.lang.String o) {
		java.lang.String[] old = array;
		array = new java.lang.String[n + 1];
		for (int i = 0; i < n; i++)
			array[i] = old[i];

		array[n] = o;

		n += 1;
	}

	public java.lang.String getHead() {
		if(n <= 0) throw new InvalidException();

		return array[0];
	}

	public java.lang.String get(int n) {
		if(n < 0 || n >= this.n) throw new InvalidException();

		return array[n];
	}

	public java.lang.String getTail() {
		if(n <= 0) throw new InvalidException();

		return array[array.length - 1];
	}

	public void setHead(java.lang.String o) {
		if(n <= 0) throw new InvalidException();

		array[0] = o;
	}

	public void set(int n, java.lang.String o) {
		if(n < 0 || n >= this.n) throw new InvalidException();

		array[n] = o;
	}

	public void setTail(java.lang.String o) {
		if(n <= 0) throw new InvalidException();

		array[array.length-1] = o;
	}

	public void delHead() {
		if(n <= 0) throw new InvalidException();

		java.lang.String[] old = array;
		n -= 1;
		array = new java.lang.String[n];
		for (int i = 0; i < n; i++)
			array[i] = old[i+1];
	}

	public void delTail() {
		if(n <= 0) throw new InvalidException();

		java.lang.String[] old = array;
		n -= 1;
		array = new java.lang.String[n];
		for (int i = 0; i < n; i++)
			array[i] = old[i];
	}

	public String toString() {
		return "size: " + n + "  " + Arrays.toString(array);
	}
}

