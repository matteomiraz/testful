package testful.random;

import java.util.Set;

import testful.IConfigGeneration;
import testful.IConfigRunner;

public interface IConfigRandom extends IConfigGeneration, IConfigRunner {

	public static enum RandomType {
		SIMPLE, SPLIT
	}

	public float getpGenNewObj();

	public boolean isNoStats();

	public boolean isVerbose();

	public RandomType getRandomType();

	public Set<String> getSettings();

}