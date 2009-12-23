package testful.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.WeakHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Cloner<E extends Serializable> {

	private static final WeakHashMap<Serializable, byte[]> prevNorm = new WeakHashMap<Serializable, byte[]>();
	private static final WeakHashMap<Serializable, byte[]> prevGzip = new WeakHashMap<Serializable, byte[]>();

	private final boolean compressed;
	private final byte[] buff;

	public Cloner(E obj, boolean compress) {
		this.compressed = compress;
		this.buff = serialize(obj, compressed);
	}

	@SuppressWarnings("unchecked")
	public E get() {
		return (E) deserialize(buff, compressed);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T copy(T obj) {
		byte[] buff = serialize(obj, false);
		return (T) deserialize(buff, false);
	}

	public static byte[] serialize(Serializable obj, boolean compress) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutput oo = new ObjectOutputStream(compress ? new GZIPOutputStream(baos) : baos);
			oo.writeObject(obj);
			oo.close();
			return baos.toByteArray();
		} catch(IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public static byte[] serializeWithCache(Serializable obj, boolean compress) {
		WeakHashMap<Serializable, byte[]> prev = compress ? prevGzip : prevNorm;
		byte[] ret = prev.get(obj);
		if(ret != null) return ret;

		ret = serialize(obj, compress);
		prev.put(obj, ret);
		return ret;
	}

	public static Serializable deserialize(byte[] buff, boolean compressed) {
		ObjectInput oi = null;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(buff);
			oi = new ObjectInputStream(compressed ? new GZIPInputStream(bais) : bais);
			return (Serializable) oi.readObject();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(oi != null) try {
				oi.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(buff) + (compressed ? 19 : 37);
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof Cloner<?>)) return false;

		return compressed == ((Cloner<?>) obj).compressed && Arrays.equals(buff, ((Cloner<?>) obj).buff);
	}
}
