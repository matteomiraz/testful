package testful.model;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import testful.model.MethodInformation.Kind;
import testful.model.MethodInformation.ParameterInformation;
import testful.model.xml.XmlMethod;
import testful.model.xml.XmlParameter;

/**
 * Methodz render methods of classes
 * 
 * @author matteo
 */
public class Methodz implements Serializable {

	private static final long serialVersionUID = -6044812551106557800L;

	private final Clazz clazz;
	private final String name;
	private final Clazz[] params;
	private final Clazz returnType;
	private final boolean isStatic;

	private final String fullMethodName;

	private transient Method method = null;

	private final MethodInformation info;

	Methodz(TestCluster cluster, Clazz clazz, Method m, XmlMethod xml) {
		this.clazz = clazz;
		name = m.getName();
		fullMethodName = m.toGenericString();
		params = cluster.getRegistry().convert(m.getParameterTypes());

		if(m.getReturnType() ==  Void.TYPE)
			returnType = null;
		else
			returnType = cluster.getRegistry().getClazz(m.getReturnType());

		method = m;
		isStatic = Modifier.isStatic(method.getModifiers());

		if(xml == null) {
			ParameterInformation[] params = new ParameterInformation[m.getParameterTypes().length];
			for(int i = 0; i < params.length; i++) {
				params[i] = new ParameterInformation(i);
				params[i].setCaptured(false);
				params[i].setCapturedByReturn(false);
				params[i].setMutated(false);
			}

			final Kind type = Modifier.isStatic(m.getModifiers()) ? Kind.STATIC : Kind.MUTATOR;
			final boolean returnsState = m.getReturnType() != null && m.getReturnType().isArray();

			info = new MethodInformation(type, returnsState, params);

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

			info = new MethodInformation(MethodInformation.Kind.convert(xml.getKind()), xml.isExposeState(), paramsInfo);
		}
	}

	public Clazz getClazz() {
		return clazz;
	}

	public MethodInformation getMethodInformation() {
		return info;
	}

	public Method toMethod() {
		if(method == null) try {
			method = clazz.toJavaClass().getMethod(name, Clazz.convert(params));
		} catch(Exception e) {
			return null; // never happens
		}
		return method;
	}

	/** Clear the cache: discard the Method reference */
	public void clearCache() {
		method = null;
	}

	public String getName() {
		return name;
	}

	public String getFullMethodName() {
		return fullMethodName;
	}

	public String getShortMethodName() {
		return toMethod().getName();
	}

	public Clazz[] getParameterTypes() {
		return params;
	}

	public Clazz getReturnType() {
		return returnType;
	}

	public boolean isStatic() {
		return isStatic;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(name).append("(");

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
		final int prime = 31;
		int result = 1;
		result = prime * result + clazz.hashCode();
		result = prime * result + name.hashCode();
		result = prime * result + Arrays.hashCode(params);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof Methodz)) return false;

		Methodz other = (Methodz) obj;
		return clazz.equals(other.clazz) && name.equals(other.name) && Arrays.equals(params, other.params);
	}

	/**
	 * Check if the method is an interesting one or not.<br>
	 * The method is skipped if
	 * <ul>
	 *  <li>it is not public</li>
	 * 	<li>it is a bridge or a synthetic method</li>
	 *  <li>it is implemented in a standard java class (e.g. equals is skipped if it is not
	 * 			overridden) </li>
	 * 	<li>it is related to JML contracts</li>
	 *  <li>it has invalid parameters (e.g., arrays)</li>
	 * </ul>
	 * 
	 * @param meth the method to check
	 * @return true if the method is not interesting, false otherwise
	 */
	public static boolean toSkip(Method meth) {
		// skip strange methods
		if(meth.isBridge() || meth.isSynthetic()) return true;

		// skip non-public methods
		if(!Modifier.isPublic(meth.getModifiers())) return true;

		// skip if one of the input or output sparameters is an array
		if(meth.getReturnType().isArray()) return true;
		for(Class<?> param : meth.getParameterTypes())
			if(param.isArray()) return true;

		Class<?> declaringClass = meth.getDeclaringClass();

		// skip java-related methods
		String packageName = declaringClass.getPackage() == null ? "" : declaringClass.getPackage().getName();
		if(packageName.startsWith("java.") || packageName.startsWith("javax.") || packageName.startsWith("sun.") || packageName.startsWith("org.jmlspecs.")) return true;

		// skip contract-related methods
		String methodName = meth.getName();
		if(withJmlContracts(declaringClass)
				&& (methodName.startsWith("checkHC$") || methodName.startsWith("checkInv$") || methodName.startsWith("checkPost$") || methodName.startsWith("checkPre$")
						|| methodName.startsWith("checkXPost$") || methodName.startsWith("evalOldExprInHC$") || methodName.startsWith("rac$"))) return true;

		return false;

	}

	private static boolean withJmlContracts(Class<?> declaringClass) {
		for(Class<?> i : declaringClass.getInterfaces())
			if("org.jmlspecs.jmlrac.runtime.JMLCheckable".equals(i.getCanonicalName())) return true;

		return false;
	}
}
