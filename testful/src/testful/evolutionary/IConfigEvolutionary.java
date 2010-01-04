package testful.evolutionary;

import testful.IConfigGeneration;
import testful.IConfigRunner;
import testful.evolutionary.jMetal.FitnessInheritance;

public interface IConfigEvolutionary extends IConfigGeneration, IConfigRunner, IConfigFitness {

	public int getLocalSearchPeriod();

	public int getPopSize();

	public FitnessInheritance getFitnessInheritance();

	public boolean isSmartInitialPopulation();

	public boolean isQuiet();

	public boolean isVerbose();
}