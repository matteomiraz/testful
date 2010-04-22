package testful.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import testful.utils.JavaUtils;
import ec.util.MersenneTwisterFast;

public class ReferenceFactory implements Serializable {

	private static final long serialVersionUID = -3020356606264676381L;
	
	private final Map<Clazz, Reference[]> refMap;
	private transient Reference[] all;

	public ReferenceFactory(TestCluster cluster, int cutSize, int auxSize) {
		Set<Clazz> refClasses = new HashSet<Clazz>();
		
		for(Clazz c : cluster.getCluster())
			refClasses.add(c.getReferenceClazz());

		int idGenerator = 0;
		refMap = new HashMap<Clazz, Reference[]>();
		for(Clazz c : refClasses) {
			Reference[] refs = new Reference[c == cluster.getCut() ? cutSize : auxSize];

			for(int i = 0; i < refs.length; i++)
				refs[i] = new Reference(c, i, idGenerator++);

			refMap.put(c, refs);
		}
	}

	/**
	 * Create a reference factory with map.get(clazz) entries for each clazz used as key
	 * @param map indicates how many references create for each clazz 
	 */
	public ReferenceFactory(Map<Clazz, Integer> map) {
		int idGenerator = 0;
		refMap = new HashMap<Clazz, Reference[]>();
		for(Entry<Clazz, Integer> e : map.entrySet()) {
			Reference[] refs = new Reference[e.getValue()];

			for(int i = 0; i < refs.length; i++)
				refs[i] = new Reference(e.getKey(), i, idGenerator++);

			refMap.put(e.getKey(), refs);
		}
	}

	/**
	 * Returns the set of references (not ordered)
	 * @return an unordered set of references
	 */
	public Reference[] getReferences() {
		if(all == null) {
			int num = 0;
			for(Reference[] refs : refMap.values())
				num += refs.length;
			
			all = new Reference[num];
			
			int i = 0;
			for(Reference[] refs : refMap.values())
				for(Reference ref : refs)
					all[i++] = ref;
		}

		return all;
	}

	public Reference[] getReferences(Clazz c) {
		return refMap.get(c.getReferenceClazz());
	}

	public Reference getReference(Clazz c, MersenneTwisterFast random) {
		Reference refs[] = refMap.get(c.getReferenceClazz());

		if(refs == null) return null;

		return refs[random.nextInt(refs.length)];
	}

	@Override
	public int hashCode() {
		return 31 + Arrays.hashCode(getReferences());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof ReferenceFactory)) return false;
		ReferenceFactory other = (ReferenceFactory) obj;

		return JavaUtils.similar(getReferences(), other.getReferences());
	}

	/**
	 * Converts a reference of another instance of a ReferenceFatory into a
	 * reference of this instance of the ReferenceFactory
	 * 
	 * @param ref a reference
	 * @return a reference of this instance of the ReferenceFactory
	 */
	public Reference adapt(Reference ref) {
		if(ref == null) return null;
		return refMap.get(ref.getClazz())[ref.getPos()];
	}

	/**
	 * Converts an array of references of another instance of a ReferenceFatory
	 * into an array of references of this instance of the ReferenceFactory
	 * 
	 * @param array an array of references
	 * @return an array of references of this instance of the ReferenceFactory
	 */
	public Reference[] adapt(Reference[] array) {
		Reference[] ret = new Reference[array.length];

		for(int i = 0; i < array.length; i++)
			ret[i] = adapt(array[i]);

		return ret;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < all.length; i++)
			sb.append(" ").append(i).append(": ").append(all[i].toString()).append("\n");
		
		return sb.toString();
	}
}
