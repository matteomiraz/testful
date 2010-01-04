package testful.model;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import testful.model.MethodInformation.ParameterInformation;
import testful.model.xml.XmlConstructor;
import testful.model.xml.XmlParameter;

public class Constructorz implements Serializable {

	private static final long serialVersionUID = -6107876679557652859L;

	private final Clazz clazz;
	private final Clazz[] params;
	private final MethodInformation info;
	private final String fullConstructorName;

	private transient Constructor<?> constructor = null;

	Constructorz(TestCluster cluster, Constructor<?> c, XmlConstructor xml) {
		clazz = cluster.getRegistry().getClazz(c.getDeclaringClass());
		params = cluster.getRegistry().convert(c.getParameterTypes());

		constructor = c;
		fullConstructorName = c.toGenericString();

		if(xml == null) {
			ParameterInformation[] params = new ParameterInformation[c.getParameterTypes().length];
			for(int i = 0; i < params.length; i++) {
				params[i] = new ParameterInformation(i);
				params[i].setCaptured(false);
				params[i].setCapturedByReturn(false);
				params[i].setMutated(false);
			}
			info = new MethodInformation(true, false, params);
		} else {
			List<XmlParameter> paramsXml = xml.getParameter();
			ParameterInformation[] paramsInfo = new ParameterInformation[paramsXml.size()];
			for(int i = 0; i < paramsXml.size(); i++) {
				XmlParameter p = paramsXml.get(i);
				paramsInfo[i] = new ParameterInformation(i);
				paramsInfo[i].setMutated(p.isMutated());
				paramsInfo[i].setCaptured(p.isCaptured());
				paramsInfo[i].setCapturedByReturn(p.isExposedByReturn());
			}

			for(int i = 0; i < paramsXml.size(); i++)
				for(int exch : paramsXml.get(i).getExchangeState())
					paramsInfo[i].addCaptureStateOf(paramsInfo[exch]);

			info = new MethodInformation(true, true, paramsInfo);
		}
	}

	public Clazz getClazz() {
		return clazz;
	}

	public MethodInformation getMethodInformation() {
		return info;
	}

	public Constructor<?> toConstructor() {
		if(constructor == null) {
			try {
				constructor = clazz.toJavaClass().getConstructor(Clazz.convert(params));
			} catch(Exception e) {
				e.printStackTrace();
				return null; // never happens
			}
		}
		return constructor;
	}

	/** Clear the cache: discard the Constructor reference */
	public void clearCache() {
		constructor = null;
	}

	public String getFullConstructorName() {
		return fullConstructorName;
	}

	public String getShortConstructorName() {
		return "new " + clazz.getClassName();
	}

	public Clazz[] getParameterTypes() {
		return params;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(clazz.getClassName()).append("(");

		boolean first = true;
		for(Clazz p : params) {
			if(first) first = false;
			else sb.append(", ");

			sb.append(p.getClassName());
		}

		return sb.append(")").toString();
	}

	@Override
	public int hashCode() {
		return (31 + clazz.hashCode()) * 31 + Arrays.hashCode(params);
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof Constructorz)) return false;

		Constructorz other = (Constructorz) obj;
		return clazz.equals(other.clazz) && Arrays.equals(params, other.params);
	}
}
