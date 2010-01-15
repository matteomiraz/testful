package testful.coverage.whiteBox;

import java.io.Serializable;

public class DataAccess implements Serializable {
	private static final long serialVersionUID = -7933747383773429832L;

	private final int id;
	private final Stack context;
	private final int hashCode;

	public DataAccess(int id, Stack context) {
		this.id = id;
		this.context = context;

		hashCode = (31 + id) * 31 + (context != null ? context.hashCode() : 0 );
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
		if(context == null) {
			if(other.context != null) return false;
		} else {
			if(other.context == null) return false;
			if(!context.equals(other.context)) return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(id);

		if(context != null) {
			sb.append("(");
			for(Integer i : context.stack)
				sb.append(" ").append(i).append(" ");

			sb.append(")");
		}

		return sb.toString();
	}
}

