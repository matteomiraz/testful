package testful.coverage.behavior;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import testful.TestFul;
import testful.coverage.CoverageInformation;
import testful.coverage.Tracker;
import testful.utils.ElementManager;

public class BehaviorTracker extends Tracker {

	private static final boolean DEBUG = false;
	
	private static final BehaviorTracker tracker = new BehaviorTracker();

	public static BehaviorTracker getTracker() {
		return tracker;
	}

	private final ElementManager<String, CoverageInformation> elemManager;
	private BehaviorCoverage coverage;

	private BehaviorTrackerData data;

	private BehaviorTracker() {
		coverage = BehaviorCoverage.getEmpty();
		elemManager = new ElementManager<String, CoverageInformation>(coverage);
	}

	@Override
	public void reset() {
		coverage = BehaviorCoverage.getEmpty();

		elemManager.putAndReplace(coverage);
	}

	@Override
	public ElementManager<String, CoverageInformation> getCoverage() {
		return elemManager;
	}

	public void add(Abstraction pre, AbstractionMethod partition, Abstraction post) {
		if(DEBUG) System.out.println("bt:add " + pre + "," + partition + "," + post);
		coverage.add(pre, partition, post);
	}

	public Abstraction abstractState(Object _this) {
		if(DEBUG) System.out.println("bt:as " + _this);
		
		if(_this == null) return new AbstractionObjectReference("", true);

		if(data == null) data = (BehaviorTrackerData) Tracker.getDatum(BehaviorTrackerData.KEY);

		AbstractorObjectState abs = data.getAbstractorClass(_this.getClass().getCanonicalName());

		if(abs == null) return new AbstractionObjectReference("", false);

		return abs.get(_this);
	}

	public AbstractionMethod abstractMethod(String className, Object _this, String methodName, Object[] params) {
		String name = className + "." + methodName.replace('/', '.');

		if(DEBUG) System.out.println("bt:am " + name + "," + _this + "," + methodName + "," + Arrays.toString(params));
		
		if(data == null) data = (BehaviorTrackerData) Tracker.getDatum(BehaviorTrackerData.KEY);

		// get the method abstractor
		AbstractorMethod abs = data.getAbstractorMethod(name);
		if(abs == null) {
			if(TestFul.DEBUG) System.out.println("NO abstractions for method " + name);
			return null;
		}

		// create the context of the invocation (set this and parameters)
		Map<String, Object> ctx = new HashMap<String, Object>();
		ctx.put("this", _this);
		for(int i = 0; i < params.length; i++)
			ctx.put("p" + i, params[i]);

		return abs.get(ctx);
	}
}
