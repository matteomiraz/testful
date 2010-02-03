package testful.random;

import java.io.File;
import java.util.List;

import org.kohsuke.args4j.Option;

import testful.ConfigGeneration;
import testful.ConfigRunner;
import testful.IConfigCut;
import testful.IConfigGeneration;
import testful.IConfigProject;
import testful.IConfigRunner;
import testful.TestfulException;

public class ConfigRandom
implements IConfigRandom, IConfigRunner.Args4j, IConfigGeneration.Args4j, IConfigCut.Args4j, IConfigProject.Args4j {

	private final IConfigGeneration.Args4j configGenerator = new ConfigGeneration();
	private final IConfigRunner.Args4j configRunner = new ConfigRunner();

	@Option(required = false, name = "-pGenNewObj", usage = "Probability to create new objects")
	private float pGenNewObj = 0.35f;

	@Option(required = false, name = "-type", usage = "Choose the falvor of random testing")
	private RandomType randomType = RandomType.SIMPLE;

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

	@Override
	public File getDirBase() {
		return configGenerator.getDirBase();
	}

	@Override
	public String getCut() {
		return configGenerator.getCut();
	}

	@Override
	public boolean isReload() {
		return configGenerator.isReload();
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
	public File getDirGeneratedTests() {
		return configGenerator.getDirGeneratedTests();
	}

	@Override
	public File getDirContracts() {
		return configGenerator.getDirContracts();
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
	public boolean isCache() {
		return configGenerator.isCache();
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
	public void setReload(boolean reload) {
		configGenerator.setReload(reload);
	}

	@Override
	public void setDirCompiled(File dirCompiled) {
		configGenerator.setDirCompiled(dirCompiled);
	}

	@Override
	public void setDirGeneratedTests(File dirGeneratedTests) {
		configGenerator.setDirGeneratedTests(dirGeneratedTests);
	}

	@Override
	public void setDirContracts(File dirContracts) {
		configGenerator.setDirContracts(dirContracts);
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
	public void setCache(boolean cache) {
		configGenerator.setCache(cache);
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
	public List<String> getRemote() {
		return configRunner.getRemote();
	}

	@Override
	public boolean isLocalEvaluation() {
		return configRunner.isLocalEvaluation();
	}

	@Override
	public void addRemote(String remote) {
		configRunner.addRemote(remote);
	}

	@Override
	public void disableLocalEvaluation(boolean disableLocalEvaluation) {
		configRunner.disableLocalEvaluation(disableLocalEvaluation);
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
}
