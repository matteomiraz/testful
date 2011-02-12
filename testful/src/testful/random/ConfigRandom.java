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
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

import testful.ConfigGeneration;
import testful.IConfigCut;
import testful.IConfigGeneration;
import testful.IConfigProject;
import testful.TestfulException;

public class ConfigRandom implements IConfigRandom, IConfigGeneration.Args4j, IConfigCut.Args4j, IConfigProject.Args4j {

	private final IConfigGeneration.Args4j configGenerator = new ConfigGeneration();

	@Option(required = false, name = "-pGenNewObj", usage = "Probability to create new objects")
	private float pGenNewObj = 0.35f;

	@Option(required = false, name = "-type", usage = "Choose the falvor of random testing")
	private RandomType randomType = RandomType.SIMPLE;

	@Option(required = false, name = "-seed", usage = "Set the seed of the random number generator")
	private long seed = System.currentTimeMillis();

	/* (non-Javadoc)
	 * @see testful.random.IConfigRandom#getpGenNewObj()
	 */
	@Override
	public float getpGenNewObj() {
		return pGenNewObj;
	}

	public void setpGenNewObj(float pGenNewObj) {
		this.pGenNewObj = pGenNewObj;
	}

	/* (non-Javadoc)
	 * @see testful.random.IConfigRandom#getSimple()
	 */
	@Override
	public RandomType getRandomType() {
		return randomType;
	}

	public void setRandomType(RandomType simple) {
		randomType = simple;
	}

	/**
	 * Set the seed to use in the random number generator
	 * @param seed the seed to use in the random number generator
	 */
	public void setSeed(long seed) {
		this.seed = seed;
	}

	/**
	 * Returns the seed to use in the random number generator
	 * @return the seed to use in the random number generator
	 */
	@Override
	public long getSeed() {
		return seed;
	}

	@Override
	public File getDirBase() {
		return configGenerator.getDirBase();
	}

	@Override
	public String getCut() {
		return configGenerator.getCut();
	}

	@Override
	public boolean isReloadClasses() {
		return configGenerator.isReloadClasses();
	}

	@Override
	public File getDirSource() {
		return configGenerator.getDirSource();
	}

	@Override
	public File getDirCompiled() {
		return configGenerator.getDirCompiled();
	}

	@Override
	public List<File> getLibraries() {
		return configGenerator.getLibraries();
	}

	@Override
	public File getDirGeneratedTests() {
		return configGenerator.getDirGeneratedTests();
	}

	@Override
	public int getTime() {
		return configGenerator.getTime();
	}

	@Override
	public File getDirInstrumented() {
		return configGenerator.getDirInstrumented();
	}

	@Override
	public int getNumVar() {
		return configGenerator.getNumVar();
	}

	@Override
	public int getNumVarCut() {
		return configGenerator.getNumVarCut();
	}

	@Override
	public void setDirBase(File dirBase) {
		configGenerator.setDirBase(dirBase);
	}

	@Override
	public int getMaxTestLen() {
		return configGenerator.getMaxTestLen();
	}

	@Override
	public void setDirSource(File dirSource) {
		configGenerator.setDirSource(dirSource);
	}

	@Override
	public void setCut(String cut) throws TestfulException {
		configGenerator.setCut(cut);
	}

	@Override
	public void setReloadClasses(boolean reloadClasses) {
		configGenerator.setReloadClasses(reloadClasses);
	}

	@Override
	public void setDirCompiled(File dirCompiled) {
		configGenerator.setDirCompiled(dirCompiled);
	}

	@Override
	public void addLibrary(File library) {
		configGenerator.addLibrary(library);
	}

	@Override
	public void setDirGeneratedTests(File dirGeneratedTests) {
		configGenerator.setDirGeneratedTests(dirGeneratedTests);
	}

	@Override
	public void setTime(int time) {
		configGenerator.setTime(time);
	}

	@Override
	public void setDirInstrumented(File dirInstrumented) {
		configGenerator.setDirInstrumented(dirInstrumented);
	}

	@Override
	public void setNumVar(int numVar) {
		configGenerator.setNumVar(numVar);
	}

	@Override
	public void setNumVarCut(int numVarCut) {
		configGenerator.setNumVarCut(numVarCut);
	}

	@Override
	public void setMaxTestLen(int maxTestLen) {
		configGenerator.setMaxTestLen(maxTestLen);
	}

	@Override
	public boolean isQuiet() {
		return configGenerator.isQuiet();
	}

	@Override
	public void setQuiet(boolean quiet) {
		configGenerator.setQuiet(quiet);
	}

	@Override
	public File getLog() {
		return configGenerator.getLog();
	}

	@Override
	public void setLog(File log) {
		configGenerator.setLog(log);
	}

	@Override
	public LogLevel getLogLevel() {
		return configGenerator.getLogLevel();
	}

	@Override
	public void setLogLevel(LogLevel logLevel) {
		configGenerator.setLogLevel(logLevel);
	}

	/* (non-Javadoc)
	 * @see testful.IConfig#validate()
	 */
	@Override
	public void validate() throws CmdLineException {
		// everything is ok!
	}
}
