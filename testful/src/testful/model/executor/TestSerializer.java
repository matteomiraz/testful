/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011 Matteo Miraz
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

package testful.model.executor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;
import testful.model.AssignConstant;
import testful.model.AssignPrimitive;
import testful.model.Clazz;
import testful.model.Constructorz;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Methodz;
import testful.model.Operation;
import testful.model.OperationResult;
import testful.model.Reference;
import testful.model.ReferenceFactory;
import testful.model.ResetRepository;
import testful.model.StaticValue;
import testful.model.Test;
import testful.model.TestCluster;
import testful.runner.ObjectRegistry;
import testful.runner.ObjectType;

/**
 * Efficiently serializes and de-serializes Tests
 * @author matteo
 */
public class TestSerializer implements Externalizable {

	private static final Logger logger = Logger.getLogger("testful.runner");

	private transient Test   test;
	private transient byte[] serialized;

	private transient ObjectRegistry registry;

	@Deprecated
	public TestSerializer() { }

	public TestSerializer(Test test) {
		this.test = test;
	}

	/**
	 * Sets the ObjectRegistry to use during the (de)serialization.
	 * @param registry uses this ObjectRegistry during the (de)serialization
	 */
	public void setObjectRegistry(ObjectRegistry registry) {
		this.registry = registry;
	}

	/**
	 * Returns the test
	 * @return the test
	 */
	public Test getTest() {
		if(test == null) {
			test = deserialize(registry, serialized);
		}

		return test;
	}

	// ---------------------------------- serialize ----------------------------------

	/* (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {

		// serialized.length {serialized}

		if(serialized == null) {
			if(TestFul.DEBUG) {
				if(test == null) TestFul.debug("The test to serialize is missing");
				if(registry == null) TestFul.debug("The object registry has not been set");
			}

			serialized = serialize(registry, test);
		}

		out.writeInt(serialized.length);
		out.write(serialized);

	}

	public static byte[] serialize(ObjectRegistry registry, Test test) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(baos);

			//			// checks the System property of the current JVM
			//			private static final boolean DISCOVER_FAULTS = TestFul.getProperty(TestFul.PROPERTY_FAULT_DETECT, true);
			//			oo.writeUTF(executor.getName());
			//			oo.writeBoolean(DISCOVER_FAULTS);

			String clusterID = ObjectType.contains(registry, test.getCluster());

			// perform the standard serialization
			if(clusterID == null) {
				oo.writeBoolean(false);
				oo.writeObject(test.getCluster());
				oo.writeObject(test.getReferenceFactory().getReferences());
				oo.writeObject(test.getTest());

			} else {
				oo.writeBoolean(true);
				oo.writeUTF(clusterID);

				// write the references: num.type.refs { ref.class.id, num.refs }
				Map<Clazz, Integer> refNum = test.getReferenceFactory().getRefNum();
				oo.writeInt(refNum.size());
				for (Entry<Clazz, Integer> ref : refNum.entrySet()) {
					oo.writeInt(ref.getKey().getId());
					oo.writeInt(ref.getValue());
				}

				// write the test: test.length { op.type [op-specific data] }
				oo.writeInt(test.getTest().length);
				for (Operation op : test.getTest()) {

					if (op instanceof ResetRepository) {
						// op.type=0 no_extra_data
						oo.writeByte(0);

					} else if(op instanceof AssignConstant) {
						// op.type=1 target.id staticValue.id {info ~ null}
						oo.writeByte(1);
						AssignConstant ac = (AssignConstant)op;
						oo.writeInt(ac.getTarget() == null ? -1 : ac.getTarget().getId());
						oo.writeInt(ac.getValue() == null ? -1 : ac.getValue().getId());

					} else if(op instanceof AssignPrimitive) {
						// op.type=2 target.id value {info ~ null}
						oo.writeByte(2);
						AssignPrimitive ap = (AssignPrimitive)op;
						oo.writeInt(ap.getTarget() == null ? -1 : ap.getTarget().getId());
						writePrimitive(oo, ap.getValue());

					} else if(op instanceof CreateObject) {
						// op.type=3 target.id cosntructor.id params.len {param.id} {info ~ null}
						oo.writeByte(3);
						CreateObject co = (CreateObject)op;
						oo.writeInt(co.getTarget() == null ? -1 : co.getTarget().getId());
						oo.writeInt(co.getConstructor().getId());
						oo.writeInt(co.getParams().length);
						for (Reference param : co.getParams())
							oo.writeInt(param.getId());

						writeOpInfo(oo, op);

					} else if(op instanceof Invoke) {
						// op.type=3 target.id this.id method.id params.len {param.id} {info ~ null}
						oo.writeByte(4);
						Invoke in = (Invoke)op;
						oo.writeInt(in.getTarget() == null ? -1 : in.getTarget().getId());
						oo.writeInt(in.getThis() == null ? -1 : in.getThis().getId());
						oo.writeInt(in.getMethod().getId());
						oo.writeInt(in.getParams().length);
						for (Reference param : in.getParams())
							oo.writeInt(param.getId());

						writeOpInfo(oo, op);

					} else
						logger.warning("Unknown operation: " + op.getClass().getName() + " - " + op);
				}
			}

			oo.close();
			return baos.toByteArray();

		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			return new byte[0];
		}
	}
	private static void writePrimitive(ObjectOutputStream oo, Serializable value) throws IOException {

		// isNull [ type value ]

		if(value == null) oo.writeBoolean(true);
		else {
			oo.writeBoolean(false);

			if(value instanceof Boolean)        { oo.writeShort(0); oo.writeBoolean((Boolean) value); }
			else if(value instanceof Byte)      { oo.writeShort(1); oo.writeByte((Byte) value); }
			else if(value instanceof Character) { oo.writeShort(2); oo.writeChar((Character) value); }
			else if(value instanceof Double)    { oo.writeShort(3); oo.writeDouble((Double) value); }
			else if(value instanceof Float)     { oo.writeShort(4); oo.writeFloat((Float) value); }
			else if(value instanceof Integer)   { oo.writeShort(5); oo.writeInt((Integer) value); }
			else if(value instanceof Long)      { oo.writeShort(6); oo.writeLong((Long) value); }
			else if(value instanceof Short)     { oo.writeShort(7); oo.writeShort((Short) value); }
			else if(value instanceof String)    { oo.writeShort(8); oo.writeUTF((String) value); }
			else { oo.writeShort(-1); logger.warning("Unexpected primitive: " + value); }
		}
	}
	private static void writeOpInfo(ObjectOutputStream oo, Operation op) throws IOException {

		OperationResult or = (OperationResult) op.getInfo(OperationResult.KEY);

		// isOpResult [ isVerifier [ verifier ] ]
		if(or == null) oo.writeBoolean(false);
		else {
			oo.writeBoolean(true);
			if(or instanceof OperationResult.Verifier) {
				oo.writeBoolean(true);
				oo.writeObject(or);
			} else {
				oo.writeBoolean(false);
			}
		}
	}

	// --------------------------------- deserialize ---------------------------------

	/* (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

		int len = in.readInt();
		serialized = new byte[len];

		in.readFully(serialized);
	}

	public static Test deserialize(ObjectRegistry registry, byte[] serialized) {

		ObjectInputStream oi = null;
		try {

			ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
			oi = new ObjectInputStream(bais);

			//			String execClassName = oi.readUTF();
			//			boolean discoverFaults = oi.readBoolean();

			final TestCluster testCluster;
			final ReferenceFactory testRefFactory;
			final Operation[] testOps;

			boolean optimized = oi.readBoolean();
			if(!optimized) {

				// perform the standard de-serialization
				testCluster = (TestCluster) oi.readObject();
				testRefFactory = (ReferenceFactory) oi.readObject();
				testOps = (Operation[]) oi.readObject();

			} else {

				String clusterID = oi.readUTF();

				// use the ObjectRegistry and the advanced serialization
				testCluster = (TestCluster) registry.getObject(clusterID);


				// read the references: num.type.refs { ref.class.id, num.refs }
				int refLen = oi.readInt();

				Map<Clazz, Integer> refMap = new TreeMap<Clazz, Integer>();
				for(int i = 0; i < refLen; i++) {
					Clazz clazz = testCluster.getClazzById(oi.readInt());
					int num = oi.readInt();
					refMap.put(clazz, num);
				}
				testRefFactory = new ReferenceFactory(refMap);
				final Reference[] testRefs = testRefFactory.getReferences();

				// read the operations
				int testLen = oi.readInt();
				testOps = new Operation[testLen];
				for (int i = 0; i < testLen; i++) {

					byte operationType = oi.readByte();
					switch(operationType) {
					case 0: { // ResetRepository
						testOps[i] = ResetRepository.singleton;
						break;
					}

					case 1: { // AssignConstant
						int targetId = oi.readInt();
						Reference ref = (targetId < 0 ? null : testRefs[targetId]);

						int valueId = oi.readInt();
						StaticValue staticValue = valueId < 0 ? null : testCluster.getStaticValueById(valueId);

						testOps[i] = new AssignConstant(ref, staticValue);
						break;
					}

					case 2: { // AssignPrimitive
						int targetId = oi.readInt();
						Reference ref = (targetId < 0 ? null : testRefs[targetId]);

						Serializable value = readPrimitive(oi);

						testOps[i] = new AssignPrimitive(ref, value);
						break;
					}

					case 3: { // CreateObject
						int targetId = oi.readInt();
						Reference target = (targetId < 0 ? null : testRefs[targetId]);

						int constructorId = oi.readInt();
						Constructorz constructor = testCluster.getConstructorById(constructorId);

						int paramLen = oi.readInt();
						Reference[] params = new Reference[paramLen];
						for (int j = 0; j < paramLen; j++) {
							int paramId = oi.readInt();
							params[j] = testRefs[paramId];
						}

						testOps[i] = new CreateObject(target, constructor, params);
						readOpInfo(oi, testOps[i]);
						break;
					}

					case 4: { // Invoke
						int targetId = oi.readInt();
						Reference target = (targetId < 0 ? null : testRefs[targetId]);

						int thisId = oi.readInt();
						Reference _this = (thisId < 0 ? null : testRefs[thisId]);

						int methodId = oi.readInt();
						Methodz method = testCluster.getMethodById(methodId);

						int paramLen = oi.readInt();
						Reference[] params = new Reference[paramLen];
						for (int j = 0; j < paramLen; j++) {
							int paramId = oi.readInt();
							params[j] = testRefs[paramId];
						}

						testOps[i] = new Invoke(target, _this, method, params);
						readOpInfo(oi, testOps[i]);
						break;
					}

					default:
						logger.warning("Unknown operation serialized type " + operationType);
					}
				}
			}

			return new Test(testCluster, testRefFactory, testOps);

			//			@SuppressWarnings("unchecked")
			//			Class<? extends IExecutor> execClass = (Class<? extends IExecutor>) Class.forName(execClassName);
			//			Constructor<? extends IExecutor> execCns = execClass.getConstructor(TestCluster.class, Reference[].class, Operation[].class, Boolean.TYPE);
			//			return execCns.newInstance(testCluster, testRefs, testOps, discoverFaults);

		} catch(Exception exc) {
			logger.log(Level.WARNING, exc.getMessage(), exc);

		} finally {
			if(oi != null) {
				try {
					oi.close();
				} catch(IOException e) {
					logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		return null;
	}

	private static Serializable readPrimitive(ObjectInputStream oi) throws IOException {

		// isNull [ type value ]

		boolean isNull = oi.readBoolean();
		if(isNull) return null;

		final Serializable ret;
		short type = oi.readShort();
		switch(type) {
		case 0: ret = oi.readBoolean(); break;
		case 1: ret = oi.readByte(); break;
		case 2: ret = oi.readChar(); break;
		case 3: ret = oi.readDouble(); break;
		case 4: ret = oi.readFloat(); break;
		case 5: ret = oi.readInt(); break;
		case 6: ret = oi.readLong(); break;
		case 7: ret = oi.readShort(); break;
		case 8: ret = oi.readUTF(); break;
		default: logger.warning("Unexpected serialized primitive type: " + type); ret = null;
		}

		return ret;
	}

	private static void readOpInfo(ObjectInput oi, Operation op) throws ClassNotFoundException, IOException {

		boolean isOpResult = oi.readBoolean();
		if(isOpResult) {

			boolean isVerifier = oi.readBoolean();
			if(isVerifier)
				op.addInfo((OperationResult.Verifier) oi.readObject());
			else
				op.addInfo(new OperationResult());
		}
	}

}
