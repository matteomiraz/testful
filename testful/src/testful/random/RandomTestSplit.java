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

package testful.random;

import java.io.File;
import java.util.concurrent.Future;
import java.util.logging.Level;

import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.model.Operation;
import testful.model.ReferenceFactory;
import testful.model.TestCluster;
import testful.model.transformation.Splitter;
import testful.model.transformation.Splitter.Listener;
import testful.runner.DataFinder;
import testful.utils.ElementManager;
import testful.utils.SimpleEntry;

public class RandomTestSplit extends RandomTest {

	private final Splitter simplifier;

	public RandomTestSplit(File logDirectory, DataFinder finder, boolean reload, TestCluster cluster, ReferenceFactory refFactory, long seed, TrackerDatum ... data) {
		super(logDirectory, finder, reload, cluster, refFactory, seed, data);

		simplifier = new Splitter(true, cluster, refFactory);
	}

	@Override
	public void work(long duration) {
		start = System.currentTimeMillis();
		stop = start + duration;

		simplifier.register(new Listener() {

			@Override
			public void notify(TestCluster cluster, ReferenceFactory refFactory, Operation[] ops) {
				try {
					tests.put(new SimpleEntry<Operation[], Future<ElementManager<String, CoverageInformation>>>(ops, execute(ops)));
				} catch(InterruptedException e) {
					logger.log(Level.WARNING, "Interrupted: " + e.getMessage(), e);
				}
			}
		});

		while(System.currentTimeMillis() < stop) {
			Operation op = Operation.randomlyGenerate(cluster, refFactory, random);
			simplifier.analyze(op);
		}

		simplifier.flush();
	}
}
