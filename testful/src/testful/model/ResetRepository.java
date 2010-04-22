package testful.model;

import java.util.Set;

/**
 * Reset the repository (i.e. put all references to null)
 * 
 * @author matteo
 */
public final class ResetRepository extends Operation {

	private static final long serialVersionUID = -8362944709273921763L;
	
	public static final ResetRepository singleton = new ResetRepository();

	private ResetRepository() {
		super();
	}

	@Override
	public int hashCode() {
		return 31;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || obj instanceof ResetRepository;
	}

	@Override
	public String toString() {
		return "init()";
	}

	@Override
	public Operation adapt(TestCluster cluster, ReferenceFactory refFactory) {
		return this;
	}
	
	@Override
	protected Set<Reference> calculateDefs() {
		throw new NullPointerException("Cannot calculate defs for ResetRepository!");
	}
	
	@Override
	protected Set<Reference> calculateUses() {
		return emptyRefsSet;
	}
}
