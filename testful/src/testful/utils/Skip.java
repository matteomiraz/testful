package testful.utils;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;


public class Skip implements Tag {

	public static final String NAME = "SKIP";
	public static final Skip s = new Skip();
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public byte[] getValue() throws AttributeValueException {
		throw new RuntimeException("Skip has no value for bytecode");
	}

}
