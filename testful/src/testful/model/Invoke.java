/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package testful.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import testful.model.MethodInformation.Kind;
import testful.model.MethodInformation.ParameterInformation;
import ec.util.MersenneTwisterFast;

public class Invoke extends Operation {

	private static final long serialVersionUID = 3149066267226412341L;

	private final Reference _return;
	private final Reference _this;
	private final Methodz method;
	private final Reference[] params;

	public Invoke(Reference _return, Reference _this, Methodz method, Reference[] params) {
		super(37 * 37 * 37 * ((_return == null) ? 0 : _return.hashCode()) +
				37 * 37 * ((_this == null) ? 0 : _this.hashCode()) +
				37 * method.hashCode() +
				Arrays.hashCode(params));

		this._return = _return;
		this._this = _this;
		this.method = method;
		this.params = params;
	}

	@Override
	public Operation adapt(TestCluster cluster, ReferenceFactory refFactory) {
		final Invoke ret = new Invoke(refFactory.adapt(_return), refFactory.adapt(_this), cluster.adapt(method), refFactory.adapt(params));
		ret.addInfo(this);
		return ret;
	}

	public static Invoke generate(Clazz c, TestCluster cluster, ReferenceFactory refFactory, MersenneTwisterFast random) {

		// if there are no available methods
		Methodz[] usableMethods = c.getMethods();
		if(usableMethods.length <= 0) return null;

		Methodz m = usableMethods[random.nextInt(usableMethods.length)];

		Reference t = m.isStatic() ? null : generateRef(c, cluster, refFactory, random);

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

	/**
	 * Returns the reference of the object accepting the method call. Null if the method is static.
	 * @return the reference of the object accepting the method call. Null if the method is static.
	 */
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

		String ret = "";
		if(_return != null) {
			ret = _return + " = ";
			if(_return.getClazz() instanceof PrimitiveClazz) ret += ((PrimitiveClazz) _return.getClazz()).getCast() + " ";
		}

		StringBuilder pars = null;
		for(Reference p : params) {
			if(pars == null) pars = new StringBuilder();
			else pars.append(", ");
			pars.append(p.toString());
		}


		return ret + (_this == null ? method.getClazz().getClassName() : _this.toString())+ "." + method.getName() + "(" + (pars != null ? pars.toString() : "") + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof Invoke)) return false;

		Invoke other = (Invoke) obj;

		if(_return == null ) {
			if(other._return != null) return false;
		} else {
			if(!_return.equals(other._return)) return false;
		}

		if(_this == null) {
			if(other._this != null) return false;
		} else {
			if(!_this.equals(other._this)) return false;
		}

		return method.equals(other.method) && Arrays.equals(params, other.params);
	}

	@Override
	protected Set<Reference> calculateDefs() {
		Set<Reference> defs = new HashSet<Reference>();

		if(getTarget() != null)
			defs.add(getTarget());

		MethodInformation info = getMethod().getMethodInformation();
		if(getThis() != null)
			if(info.getType() == Kind.MUTATOR || info.isReturnsState())
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

	@Override
	public Operation clone() {
		final Invoke clone = new Invoke(_return, _this, method, params);
		clone.addInfo(this);
		return clone;
	}
}
