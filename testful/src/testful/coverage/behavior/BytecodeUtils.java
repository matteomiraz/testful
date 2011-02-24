package testful.coverage.behavior;

import java.util.List;

import testful.model.xml.XmlConstructor;
import testful.model.xml.XmlMethod;
import testful.model.xml.XmlParameter;

public class BytecodeUtils {

	//result: <init>(DDII)
	public static String getBytecodeName(XmlConstructor cns) {
		return "<init>" + BytecodeUtils.getBytecodeName(cns.getParameter());
	}

	//result: MethodName(DDII)
	public static String getBytecodeName(final XmlMethod method) {
		return method.getName() + getBytecodeName(method.getParameter());
	}

	//result: (DDII)
	public static String getBytecodeName(List<XmlParameter> params) {
		StringBuilder ret = new StringBuilder();

		ret.append('(');

		for(XmlParameter p : params)
			ret.append(getBytecodeName(p));

		ret.append(')');

		return ret.toString();
	}

	public static String getBytecodeName(final XmlParameter type) {
		if(type.getType().equals("void"))	return "V";
		if(type.getType().equals("boolean")) return "Z";
		if(type.getType().equals("char")) return "C";
		if(type.getType().equals("byte")) return "B";
		if(type.getType().equals("short")) return "S";
		if(type.getType().equals("int")) return "I";
		if(type.getType().equals("float")) return "F";
		if(type.getType().equals("long")) return "J";
		if(type.getType().equals("double")) return "D";

		return "L" + type.getType() + ";";
	}

	//	// (DDII)V
	//	public static Class[] getArgumentTypes(final String methodDescriptor) {
	//    char[] buf = methodDescriptor.toCharArray();
	//    int off = 1;
	//    int size = 0;
	//    while (true) {
	//        char car = buf[off++];
	//        if (car == ')') {
	//            break;
	//        } else if (car == 'L') {
	//            while (buf[off++] != ';') {
	//            }
	//            ++size;
	//        } else if (car != '[') {
	//            ++size;
	//        }
	//    }
	//    Class[] args = new Class[size];
	//    off = 1;
	//    size = 0;
	//    while (buf[off] != ')') {
	//        args[size] = getType(buf, off);
	//        off += args[size].len + (args[size].sort == OBJECT ? 2 : 0);
	//        size += 1;
	//    }
	//    return args;
	//}
	//
	//  private static Class<?> getType(final char[] buf, final int off) {
	//    int len;
	//    switch (buf[off]) {
	//        case 'V':
	//            return Void.class;
	//        case 'Z':
	//            return Boolean.TYPE;
	//        case 'C':
	//            return Character.TYPE;
	//        case 'B':
	//            return Byte.TYPE;
	//        case 'S':
	//            return Short.TYPE;
	//        case 'I':
	//            return Integer.TYPE;
	//        case 'F':
	//            return Float.TYPE;
	//        case 'J':
	//            return Long.TYPE;
	//        case 'D':
	//            return Double.TYPE;
	//        case '[':
	//            len = 1;
	//            while (buf[off + len] == '[') {
	//                ++len;
	//            }
	//            if (buf[off + len] == 'L') {
	//                ++len;
	//                while (buf[off + len] != ';') {
	//                    ++len;
	//                }
	//            }
	//            return new Type(ARRAY, buf, off, len + 1);
	//            // case 'L':
	//        default:
	//            len = 1;
	//            while (buf[off + len] != ';') {
	//                ++len;
	//            }
	//            return new Type(OBJECT, buf, off + 1, len - 1);
	//    }
	//}
	//

}
