package testful.coverage.whiteBox;

import java.io.Serializable;
import java.util.Arrays;

public class DataAccess implements Serializable {
	private static final long serialVersionUID = -7933747383773429832L;

	private final int id;
	private final Integer[] context;

	public DataAccess(int id, Integer[] context) {
		this.id = id;
		this.context = context;
	}
	
	public int getId() {
		return id;
	}
	
	public Integer[] getContext() {
		return context;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + Arrays.hashCode(context);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		
		if(!(obj instanceof DataAccess)) return false;
		DataAccess other = (DataAccess) obj;
		
		if(id != other.id) return false;
		if(!Arrays.equals(context, other.context)) return false;
		
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(id);
		
		if(context != null) {
			sb.append("(");
			for(Integer i : context)
				sb.append(" ").append(i).append(" ");
			
			sb.append(")");
		}
		
		return sb.toString();
	}
}

