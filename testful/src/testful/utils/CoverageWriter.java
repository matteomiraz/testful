package testful.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import testful.coverage.CoverageInformation;

public class CoverageWriter {

	private final Logger log;

	public CoverageWriter(String name) {
		log = Logger.getLogger(name);
	}

	public void write(long gen, long len, ElementManager<String, CoverageInformation> infos) {
		if(!log.isLoggable(Level.FINE)) return;

		StringBuilder out = new StringBuilder("coverage ");

		out.append("gen=").append(gen);
		out.append(";").append("len=").append(len);

		for(CoverageInformation info : infos)
			out.append(";").append(info.getKey()).append("=").append(info.getQuality());

		log.fine(out.toString());
	}
}
