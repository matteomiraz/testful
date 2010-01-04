package testful.model;

import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeMap;


/**
 * This class stores tests, and is able to give back the best/worst test
 * stored based on its declared rating (during insertion).
 * This container is ideal for giving the best population to the evolutionary algorithm.
 * @author Tudor
 */
public class TestsCollection {

	private TreeMap<Float,Queue<Operation[]>> christmasTree = new TreeMap<Float,Queue<Operation[]>>();

	public void insertTest(float inRating,Operation[] inTest){
		Float rating = new Float(inRating);
		if (!christmasTree.containsKey(rating)){
			//It means that there is no Test with the given rating
			//I must make a new category for this Test:
			christmasTree.put(rating, new LinkedList<Operation[]>());
		}
		//Insert another Test for a given Rating
		christmasTree.get(rating).add(inTest);
	}

	public Operation[] giveBestTest(){
		//No Categories => No Tests => Null
		if (christmasTree.isEmpty()) return null;
		//Give the list for the best rated tests
		Queue<Operation[]> testList = christmasTree.get(christmasTree.lastKey());
		//pick a test from there
		Operation[] ret = testList.poll();
		//if there is no other test with the same rating, eliminate the rating
		if (testList.isEmpty()){christmasTree.remove(christmasTree.lastKey());}

		return ret; //and.. o yeah... return the Test :)
	}
}