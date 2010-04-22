package testful.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import testful.model.MethodInformation.ParameterInformation;

import ec.util.MersenneTwisterFast;

public class Invoke extends Operation {

	private static final long serialVersionUID = 3149066267226412341L;
	
	private final Reference _return;
	private final Reference _this;
	private final Methodz method;
	private final Reference[] params;

	public Invoke(Reference return1, Reference this1, Methodz method, Reference[] params) {
		super();
		_return = return1;
		_this = this1;
		this.method = method;
		this.params = params;
	}

	@Override
	public Operation adapt(TestCluster cluster, ReferenceFactory refFactory) {
		return new Invoke(refFactory.adapt(_return), refFactory.adapt(_this), cluster.adapt(method), refFactory.adapt(params));
	}

	public static Invoke generate(Clazz c, TestCluster cluster, ReferenceFactory refFactory, MersenneTwisterFast random) {

		// if there are no available methods
		Methodz[] usableMethods = c.getMethods();
		if(usableMethods.length <= 0) return null;

		Reference t = generateRef(c, cluster, refFactory, random);

		Methodz m = usableMethods[random.nextInt(usableMethods.length)];

		Clazz[] paramsType = m.getParameterTypes();
		Reference[] p = new Reference[paramsType.length];
		for(int i = 0; i < p.length; i++)
			p[i] = generateRef(paramsType[i], cluster, refFactory, random);

		Reference r = null;
		Clazz retClazz = m.getReturnType();
		if(retClazz != null && retClazz.getAssignableTo().length > 0) r = refFactory.getReference(retClazz.getAssignableTo()[random.nextInt(retClazz.getAssignableTo().length)], random);

		return new Invoke(r, t, m, p);
	}

	public Reference getTarget() {
		return _return;
	}

	public Reference getThis() {
		return _this;
	}

	public Methodz getMethod() {
		return method;
	}

	public Reference[] getParams() {
		return params;
	}

	@Override
	public String toString() {

		StringBuilder pars = null;
		for(Reference p : params) {
			if(pars == null) pars = new StringBuilder();
			else pars.append(", ");
			pars.append(p.toString());
		}

		String ret = "";
		if(_return != null) {
			ret = _return + " = ";
			if(_return.getClazz() instanceof PrimitiveClazz) ret += ((PrimitiveClazz) _return.getClazz()).getCast() + " ";
		}

		return ret + _this + "." + method.getName() + "(" + (pars != null ? pars.toString() : "") + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_return == null) ? 0 : _return.hashCode());
		result = prime * result + ((_this == null) ? 0 : _this.hashCode());
		result = prime * result + method.hashCode();
		result = prime * result + Arrays.hashCode(params);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof Invoke)) return false;

		Invoke other = (Invoke) obj;
		return ((_return == null) ? 
					other._return == null : 
					_return.equals(other._return)) && 
						((_this == null) ? 
								other._this == null : 
								_this.equals(other._this)) && method.equals(other.method) && Arrays.equals(params, other.params);
	}
	
	@Override
	protected Set<Reference> calculateDefs() {
		Set<Reference> defs = new HashSet<Reference>();

		if(getTarget() != null)
			defs.add(getTarget());
		
		MethodInformation info = getMethod().getMethodInformation();
		if(getThis() != null) 
			if(info.isMutator() || info.isReturnsState())
				defs.add(getThis());

		ParameterInformation[] pInfo = info.getParameters();
		for(int i = 0; i < pInfo.length; i++) {
			if(pInfo[i].isCaptured() || pInfo[i].isCapturedByReturn() || pInfo[i].isMutated())
				defs.add(getParams()[i]);
		}
		
		return defs;
	}
	
	@Override
	protected Set<Reference> calculateUses() {
		Set<Reference> uses = new HashSet<Reference>();

		if(getThis() != null)
			uses.add(getThis());
		
		for(Reference u : getParams())
			uses.add(u);
		
		return uses;
	}
}
