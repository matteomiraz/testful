/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


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
