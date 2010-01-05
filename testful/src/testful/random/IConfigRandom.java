package testful.random;

import testful.IConfigGeneration;
import testful.IConfigRunner;

public interface IConfigRandom extends IConfigGeneration, IConfigRunner {

	public static enum RandomType {
		SIMPLE, SPLIT
	}

	public float getpGenNewObj();

	public RandomType getRandomType();
}