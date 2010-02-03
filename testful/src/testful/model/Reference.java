package testful.model;

import java.io.Serializable;

public class Reference implements Serializable {

	private static final long serialVersionUID = 2679593062737380911L;
	
	/** The type of the reference */
	private final Clazz clazz;
	/** The id of the reference, within the same type (e.g., the 1st Integer) */
	private final int pos;
	/** The id of the reference */
	private final int id;
	
	private final int hashCode;

	Reference(Clazz clazz, int pos, int id) {
		this.clazz = clazz;
		this.pos = pos;
		this.id = id;
		
		int result = 1;
		result = 31 * result + clazz.hashCode();
		result = 31 * result + id;
		result = 31 * result + pos;
		hashCode = result;
	}

	public Clazz getClazz() {
		return clazz;
	}

	public int getPos() {
		return pos;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return clazz.getClassName().replace('.', '_') + "_" + pos;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof Reference)) return false;

		Reference other = (Reference) obj;
		return id == other.id && pos == other.pos && clazz.equals(other.clazz);
	}
}
