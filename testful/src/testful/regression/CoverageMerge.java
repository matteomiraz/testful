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

package testful.regression;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

import testful.IConfig;
import testful.TestFul;
import testful.coverage.CoverageInformation;
import testful.utils.ElementManager;

/**
 * Reads binary coverage reports and merge them
 * @author matteo
 */
public class CoverageMerge {

	private static final Logger logger = Logger.getLogger("testful.regression");

	private static class Config implements IConfig {

		@Option(required = true, name = "-out", usage = "Write the combined binary coverage in this file")
		private File out;

		@Option(required = false, name = "-print", usage = "Print the combined coverage")
		private boolean print = false;

		@Argument
		private List<File> arguments = new ArrayList<File>();

		@Override
		public void validate() throws CmdLineException { }
	}

	public static void main(String[] args) {
		Config config = new Config();
		TestFul.parseCommandLine(config, args, CoverageMerge.class, "Coverage merger");


		ElementManager<String, CoverageInformation> combined = new ElementManager<String, CoverageInformation>();
		for (File file : config.arguments) {
			try {
				ElementManager<String, CoverageInformation> read = ElementManager.read(file);
				for (CoverageInformation rCov : read) {
					CoverageInformation cCov = combined.get(rCov.getKey());
					if(cCov == null) {
						cCov = rCov.createEmpty();
						combined.put(cCov);
					}

					cCov.merge(rCov);
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}

		if(config.out != null) {
			try {
				combined.write(config.out);
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}

		if(config.print) {
			StringBuilder sb = new StringBuilder();
			for (CoverageInformation cov : combined)
				sb.append(cov.getKey()).append("=").append(cov.getQuality()).append(";");
			System.err.println("combined coverage: " + sb.toString());

		}
	}


}
