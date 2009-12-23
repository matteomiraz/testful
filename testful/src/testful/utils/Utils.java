package testful.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import testful.coverage.TrackerDatum;
import testful.coverage.behavior.BehaviorTrackerData;
import testful.coverage.whiteBox.AnalysisWhiteBox;
import testful.coverage.whiteBox.WhiteBoxData;
import testful.model.xml.XmlClass;

public class Utils {

	public static TrackerDatum[] readData(Collection<XmlClass> xml, AnalysisWhiteBox whiteAnalysis) {
		List<TrackerDatum> ret = new LinkedList<TrackerDatum>();

		if(xml != null) {
			BehaviorTrackerData behData = new BehaviorTrackerData(xml);
			if(behData != null)
				ret.add(behData);
		}

		if(whiteAnalysis != null) {
			WhiteBoxData whiteData = whiteAnalysis.getData();
			if(whiteData  != null)
				ret.add(whiteData);
		}
		
		return ret.toArray(new TrackerDatum[ret.size()]);
	}

}
