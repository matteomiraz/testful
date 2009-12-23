package testful.model;

import java.io.Serializable;

import testful.utils.ElementWithKey;

public abstract class OperationInformation implements ElementWithKey<String>, Serializable {

	private static final long serialVersionUID = -62965317854987267L;
	
	private final String key;

	public OperationInformation(String key) {
		this.key = key;
	}

	@Override
	public String getKey() {
		return key;
	}
	
	@Override
	public OperationInformation clone() throws CloneNotSupportedException {
		return (OperationInformation) super.clone();
	}
}
