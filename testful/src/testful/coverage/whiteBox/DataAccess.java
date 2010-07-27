package testful.coverage.whiteBox;

import java.io.Serializable;

import testful.TestFul;

public class DataAccess implements Serializable {
	private static final long serialVersionUID = -7933747383773429832L;

	private final int id;
	private final Stack context;
	private final int hashCode;

	public DataAccess(int id, Stack context) {
		if(context == null) {
			NullPointerException exc = new NullPointerException("The context must not be null");
			TestFul.debug(exc);
			throw exc;
		}

		this.id = id;
		this.context = context;

		hashCode = (31 + id) * 31 + context.hashCode();
	}

	public int getId() {
		return id;
	}

	public Stack getContext() {
		return context;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;

		if(!(obj instanceof DataAccess)) return false;
		DataAccess other = (DataAccess) obj;

		if(id != other.id) return false;
		if(!context.equals(other.context)) return false;

		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(id);
		sb.append(context.toString());

		return sb.toString();
	}
}

