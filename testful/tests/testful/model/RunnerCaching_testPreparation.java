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

import java.util.LinkedList;
import java.util.List;

/**
 * @author matteo
 *
 */
public class RunnerCaching_testPreparation extends AutoTestCase {

	@Override
	protected List<Test> perform(Test test) throws Exception {
		test = test.removeUselessDefs().simplify().getSSA();

		List<Test> ret = new LinkedList<Test>();
		List<Test> parts = TestSplitter.split(false, test);
		for(Test p: parts) {
			if(p.getTest().length > 100) {
				System.out.println("--- split ---");
				System.out.println(p);
				p = p.removeUselessDefs();
				System.out.println("--- removeUseless ---");
				System.out.println(p);
				p = p.simplify();
				System.out.println("--- simplify ---");
				System.out.println(p);
				p = p.getSSA();
				System.out.println("--- ssa ---");
				System.out.println(p);
				p = p.removeUselessDefs();
				System.out.println("--- removeuseless ---");
				System.out.println(p);
				p = p.reorganize();
				System.out.println("--- reorganize---");
				System.out.println(p);
				p = p.sortReferences();
				System.out.println("--- sort ---");
				System.out.println(p);
				System.out.println("--- end ---");
				System.exit(0);
			}
			
			p = p.removeUselessDefs();
			p = p.simplify();
			p = p.getSSA();
			p = p.removeUselessDefs();
			p = p.reorganize();
			p = p.sortReferences();

			ret.add(p);
		}
		
		return ret;
	}
}
