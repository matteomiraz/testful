package testful.model;

import java.util.HashSet;
import java.util.Set;

import ec.util.MersenneTwisterFast;

public class AssignConstant extends Operation {

	private static final long serialVersionUID = -3040251916711818748L;
	
	private final Reference ref;
	private final StaticValue staticValue;

	public AssignConstant(Reference ref, StaticValue staticValue) {
		super();
		this.ref = ref;
		this.staticValue = staticValue;
	}

	@Override
	public Operation adapt(TestCluster cluster, ReferenceFactory refFactory) {
		return new AssignConstant(refFactory.adapt(ref), cluster.adapt(staticValue));
	}

	public static AssignConstant generate(Clazz c, TestCluster cluster, ReferenceFactory refFactory, MersenneTwisterFast random) {
		StaticValue[] constants = c.getConstants();
		if(constants.length > 0 && !random.nextBoolean(Operation.SET_TO_NULL)) {
			final Reference ref;
			final Clazz[] assignableTo = c.getAssignableTo();
			if(assignableTo.length > 0) ref = refFactory.getReference(assignableTo[random.nextInt(assignableTo.length)], random);
			else ref = refFactory.getReference(c, random);
			
			return new AssignConstant(ref, constants[random.nextInt(constants.length)]);
		}

		return new AssignConstant(refFactory.getReference(c, random), null);
	}

	public Reference getTarget() {
		return ref;
	}

	public StaticValue getValue() {
		return staticValue;
	}

	@Override
	public String toString() {
		if(staticValue == null) return ref + " = null";
		else if(ref.getClazz() instanceof PrimitiveClazz) return ref + " = " + ((PrimitiveClazz) ref.getClazz()).getCast() + staticValue;
		else return ref + " = " + staticValue;
	}

	@Override
	public int hashCode() {
		return (ref != null ? 31 * ref.hashCode() : 0) + (staticValue == null ? 0 : staticValue.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof AssignConstant)) return false;

		AssignConstant other = (AssignConstant) obj;
		return (ref == null ? other.ref == null : ref.equals(other.ref)) && 
						(staticValue == null ? other.staticValue == null : staticValue.equals(other.staticValue));
	}
	
	@Override
	protected Set<Reference> calculateDefs() {
		Set<Reference> defs = new HashSet<Reference>();
		if(getTarget() != null)
			defs.add(getTarget());

		return defs;
	}
	
	@Override
	protected Set<Reference> calculateUses() {
		return emptyRefsSet;
	}
}
