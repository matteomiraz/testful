/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011  Matteo Miraz
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

package testful.coverage.behavior;

import java.util.HashMap;
import java.util.Map;

import testful.TestFul;
import testful.coverage.CoverageInformation;
import testful.coverage.Tracker;
import testful.runner.ObjectRegistry;
import testful.runner.RemoteClassLoader;
import testful.utils.ElementManager;

public class BehaviorTracker extends Tracker {

	private static final BehaviorTracker tracker = new BehaviorTracker();

	public static BehaviorTracker getTracker() {
		return tracker;
	}

	private final ElementManager<String, CoverageInformation> elemManager = new ElementManager<String, CoverageInformation>();
	private BehaviorCoverage coverage;

	/** the AbstractorRegistry or null if the behavioral coverage is disabled */
	private final AbstractorRegistry data;

	private BehaviorTracker() {

		if(TestFul.DEBUG && !(BehaviorTracker.class.getClassLoader() instanceof RemoteClassLoader))
			TestFul.debug(new ClassCastException(BehaviorTracker.class.getName() + " must be loaded by the RemoteClassLoader"));

		data = (AbstractorRegistry) ObjectRegistry.singleton.getObject(AbstractorRegistry.ISERIALIZABLE_ID);

		if(TestFul.DEBUG && data == null)
			TestFul.debug(new Exception(BehaviorTracker.class.getName() + " cannot find the abstractor factory. Is Behavioral Coverage disabled?"));

		reset();
	}

	@Override
	public void reset() {
		if(data == null) return;

		coverage = BehaviorCoverage.getEmpty();
		elemManager.putAndReplace(coverage);
	}

	@Override
	public ElementManager<String, CoverageInformation> getCoverage() {
		return elemManager;
	}

	public void add(Abstraction pre, AbstractionMethod partition, Abstraction post) {
		if(data == null) return;

		coverage.add(pre, partition, post);
	}

	public Abstraction abstractState(Object _this) {
		if(data == null) return null;

		if(_this == null) return new AbstractionObjectReference("", true);

		AbstractorObjectState abs = data.getAbstractorClass(_this.getClass().getName());

		if(abs == null) return new AbstractionObjectReference("", false);

		return abs.get(_this);
	}

	public AbstractionMethod abstractMethod(String className, Object _this, String methodName, Object[] params) {
		if(data == null) return null;

		String name = className + "." + methodName.replace('/', '.');

		// get the method abstractor
		AbstractorMethod abs = data.getAbstractorMethod(name);
		if(abs == null) {
			if(TestFul.DEBUG) System.out.println("NO abstractions for method " + name);
			return null;
		}

		// create the context of the invocation (set this and parameters)
		Map<String, Object> ctx = new HashMap<String, Object>();
		ctx.put("this", _this);
		for(int i = 0; i < params.length; i++)
			ctx.put("p" + i, params[i]);

		return abs.get(ctx);
	}
}
