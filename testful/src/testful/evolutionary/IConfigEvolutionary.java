package testful.evolutionary;

import testful.IConfigGeneration;
import testful.IConfigRunner;
import testful.evolutionary.jMetal.FitnessInheritance;

public interface IConfigEvolutionary extends IConfigGeneration, IConfigRunner, IConfigFitness {

	public int getLocalSearchPeriod();

	public int getLocalSearchElements();

	public int getPopSize();

	public FitnessInheritance getFitnessInheritance();

	public int getSmartInitialPopulation();

	public boolean isUseCpuTime();

}