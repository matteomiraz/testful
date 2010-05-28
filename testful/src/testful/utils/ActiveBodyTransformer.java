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

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;

public class ActiveBodyTransformer extends BodyTransformer {

	public static BodyTransformer v(BodyTransformer delegate) {
		return new ActiveBodyTransformer(delegate);
	}

	private final BodyTransformer delegate;

	private ActiveBodyTransformer(BodyTransformer delegate) {
		this.delegate = delegate;
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected void internalTransform(Body body, String phaseName, Map options) {
		delegate.transform(body.getMethod().getActiveBody(), phaseName, options);
	}

}
