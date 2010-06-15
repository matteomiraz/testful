/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010 Matteo Miraz
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

package testful.testCut;

import junit.framework.Assert;
import testful.ConfigCut;
import testful.GenericTestCase;
import testful.model.Clazz;
import testful.model.Methodz;
import testful.model.PrimitiveClazz;
import testful.model.Reference;
import testful.model.ReferenceFactory;
import testful.model.TestCluster;
import testful.runner.TestfulClassLoader;

/**
 * TestCase tailored to a particular testCut class.
 * @author matteo
 */
public abstract class SingleTestCUT {

	/** The test cluster */
	public final TestCluster cluster;

	/** The reference factory*/
	public final ReferenceFactory refFactory;

	/** The class under test */
	public final Clazz cut;

	/** Contains references to the class under test */
	public final Reference[] cuts;

	protected SingleTestCUT(String cutName) throws Exception {
		try {
			ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
			config.setCut(cutName);
			cluster = new TestCluster(new TestfulClassLoader(GenericTestCase.getFinder()), config);
			refFactory = new ReferenceFactory(cluster, 4, 4);

			cut = cluster.getCut();
			cuts = refFactory.getReferences(cut);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
			throw e;
		}
	}

	protected boolean checkMethod(Methodz m, String name, Clazz ... aParams) {
		if(!name.equals(m.getName())) return false;
		return checkParameters(m.getParameterTypes(), aParams);
	}

	protected boolean checkParameters(final Clazz[] p1, Clazz... p2) {
		if(p1.length != p2.length) return false;

		for (int i = 0; i < p2.length; i++) {
			if(p1[i] instanceof PrimitiveClazz) {
				if(p2[i] instanceof PrimitiveClazz) {
					if(((PrimitiveClazz)p1[i]).getReferenceClazz().equals(((PrimitiveClazz)p2[i]).getReferenceClazz()))
						return true;
				} else {
					return false;
				}
			}
			if(!p1[i].equals(p2[i]))
				return false;
		}

		return true;
	}
}
