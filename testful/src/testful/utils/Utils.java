package testful.utils;


import java.util.LinkedList;
import java.util.List;

import testful.coverage.TrackerDatum;
import testful.coverage.whiteBox.AnalysisWhiteBox;
import testful.coverage.whiteBox.WhiteBoxData;

public class Utils {

	public static TrackerDatum[] readData(AnalysisWhiteBox whiteAnalysis) {
		List<TrackerDatum> ret = new LinkedList<TrackerDatum>();
		
		if(whiteAnalysis != null) {
			WhiteBoxData whiteData = whiteAnalysis.getData();
			if(whiteData  != null)
				ret.add(whiteData);
		}
		
		return ret.toArray(new TrackerDatum[ret.size()]);
	}

}
