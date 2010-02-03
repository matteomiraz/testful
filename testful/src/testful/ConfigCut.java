package testful;

public class ConfigCut extends ConfigProject implements IConfigCut.Args4j, IConfigProject.Args4j {

	/** the class under test */
	private String cut;

	public ConfigCut() {
		super();
	}

	public ConfigCut(IConfigProject config) {
		super(config);
	}

	public ConfigCut(IConfigCut config) {
		super(config);
		cut = config.getCut();
	}

	/* (non-Javadoc)
	 * @see testful.IConfigCug#getCut()
	 */
	@Override
	public String getCut() {
		return cut;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.IConfigCut.Args4j#setCut(java.lang.String)
	 */
	@Override
	public void setCut(String cut) throws TestfulException {
		if(cut == null || cut.trim().isEmpty()) throw new TestfulException("You must specify the class name");
		this.cut = cut.trim();
	}

}