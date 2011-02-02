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

package testful.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;
import testful.runner.DataFinder;
import testful.runner.IExecutor;
import testful.runner.ObjectRegistry;
import testful.runner.ObjectType;
import testful.runner.TestfulClassLoader;
import testful.utils.StopWatchNested;

/**
 * Efficiently serializes and de-serializes Executors
 * @author matteo
 */
public class ExecutorSerializer {

	private static final Logger logger = Logger.getLogger("testful.runner");

	// checks the System property of the current JVM
	private static final boolean DISCOVER_FAULTS = TestFul.getProperty(TestFul.PROPERTY_FAULT_DETECT, true);

	private static final boolean JAVA_SERIALIZATION = TestFul.getProperty(TestFul.PROPERTY_JAVA_SERIALIZATION, false);

	private static final StopWatchNested t_ser = StopWatchNested.Disabled.singleton;
	//private static final Timer2 t_ser = Timer2.getRootTimer("ser");
	private static final StopWatchNested t_ser_sInit = t_ser.getSubTimer("ser.aStreamInit");
	private static final StopWatchNested t_ser_sClose = t_ser.getSubTimer("ser.zStreamClose");
	private static final StopWatchNested t_ser_cluster = t_ser.getSubTimer("ser.cluster");
	private static final StopWatchNested t_ser_cluster_or = t_ser_cluster.getSubTimer("ser.cluster.objectRepository");
	private static final StopWatchNested t_ser_refs = t_ser.getSubTimer("ser.refs");
	private static final StopWatchNested t_ser_test = t_ser.getSubTimer("ser.test");
	private static final StopWatchNested t_ser_test_opInfo = t_ser_test.getSubTimer("ser.test.opInfo");
	private static final StopWatchNested t_ser_test_rst = t_ser_test.getSubTimer("ser.test.reset");
	private static final StopWatchNested t_ser_test_cr = t_ser_test.getSubTimer("ser.test.cr");
	private static final StopWatchNested t_ser_test_in = t_ser_test.getSubTimer("ser.test.in");
	private static final StopWatchNested t_ser_test_ap = t_ser_test.getSubTimer("ser.test.ap");
	private static final StopWatchNested t_ser_test_ac = t_ser_test.getSubTimer("ser.test.ac");

	private static final StopWatchNested t_dser = StopWatchNested.Disabled.singleton;
	//private static final Timer2 t_dser = Timer2.getRootTimer("dser");
	private static final StopWatchNested t_dser_sInit = t_dser.getSubTimer("dser.aStreamInit");
	private static final StopWatchNested t_dser_cluster = t_dser.getSubTimer("dser.cluster");
	private static final StopWatchNested t_dser_cluster_or = t_dser_cluster.getSubTimer("dser.cluster.objectRepository");
	private static final StopWatchNested t_dser_refs = t_dser.getSubTimer("dser.refs");
	private static final StopWatchNested t_dser_test = t_dser.getSubTimer("dser.test");
	private static final StopWatchNested t_dser_test_opInfo = t_dser_test.getSubTimer("dser.test.opInfo");
	private static final StopWatchNested t_dser_test_cr = t_dser_test.getSubTimer("dser.test.cr");
	private static final StopWatchNested t_dser_test_in = t_dser_test.getSubTimer("dser.test.in");
	private static final StopWatchNested t_dser_test_ap = t_dser_test.getSubTimer("dser.test.ap");
	private static final StopWatchNested t_dser_test_ac = t_dser_test.getSubTimer("dser.test.ac");
	private static final StopWatchNested t_dser_creation = t_dser.getSubTimer("dser.zExecCreation");


	public static <T extends IExecutor> byte[] serialize(DataFinder finder, Class<T> executor, Test test) {
		try {
			t_ser.start();

			t_ser_sInit.start();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(baos);
			t_ser_sInit.stop();

			writeString(oo, executor.getName());

			oo.writeBoolean(DISCOVER_FAULTS);

			t_ser_cluster.start();
			String clusterID = null;
			if(!JAVA_SERIALIZATION) {
				t_ser_cluster_or.start();
				clusterID = ObjectType.contains(finder, test.getCluster());
				t_ser_cluster_or.stop();
			}

			// perform the standard serialization
			if(clusterID == null) {
				oo.writeBoolean(false);
				oo.writeObject(test.getCluster());
				t_ser_cluster.stop();

				t_ser_refs.start();
				oo.writeObject(test.getReferenceFactory().getReferences());
				t_ser_refs.stop();

				t_ser_test.start();
				oo.writeObject(test.getTest());
				t_ser_test.start();

			} else {
				oo.writeBoolean(true);
				writeString(oo, clusterID);
				t_ser_cluster.stop();

				t_ser_refs.start();
				// write the references: refs.length { ref.class.id, ref.pos }
				Reference[] refs = test.getReferenceFactory().getReferences();
				oo.writeInt(refs.length);
				for (int i = 0; i < refs.length; i++) {
					Reference ref = refs[i];

					oo.writeInt(ref.getClazz().getId());
					oo.writeInt(ref.getPos());

					if(TestFul.DEBUG && ref.getId() != i)
						TestFul.debug(new Exception("Reference id is not its ordinal position in the 'referenceFactory.getReferences()' array"));
				}

				t_ser_refs.stop();

				t_ser_test.start();

				// write the test: test.length { op.type [op-specific data] }
				oo.writeInt(test.getTest().length);
				for (Operation op : test.getTest()) {

					if (op instanceof ResetRepository) {
						t_ser_test_rst.start();
						// op.type=0 no_extra_data
						oo.writeByte(0);
						t_ser_test_rst.stop();

					} else if(op instanceof AssignConstant) {
						t_ser_test_ac.start();
						// op.type=1 target.id staticValue.id {info ~ null}
						oo.writeByte(1);
						AssignConstant ac = (AssignConstant)op;
						oo.writeInt(ac.getTarget() == null ? -1 : ac.getTarget().getId());
						oo.writeInt(ac.getValue() == null ? -1 : ac.getValue().getId());
						t_ser_test_ac.stop();

					} else if(op instanceof AssignPrimitive) {
						t_ser_test_ap.start();
						// op.type=2 target.id value {info ~ null}
						oo.writeByte(2);
						AssignPrimitive ap = (AssignPrimitive)op;
						oo.writeInt(ap.getTarget() == null ? -1 : ap.getTarget().getId());
						writePrimitive(oo, ap.getValue());

						t_ser_test_ap.stop();

					} else if(op instanceof CreateObject) {
						t_ser_test_cr.start();
						// op.type=3 target.id cosntructor.id params.len {param.id} {info ~ null}
						oo.writeByte(3);
						CreateObject co = (CreateObject)op;
						oo.writeInt(co.getTarget() == null ? -1 : co.getTarget().getId());
						oo.writeInt(co.getConstructor().getId());
						oo.writeInt(co.getParams().length);
						for (Reference param : co.getParams())
							oo.writeInt(param.getId());

						t_ser_test_cr.stop();

						writeOpInfo(oo, op);

					} else if(op instanceof Invoke) {
						t_ser_test_in.start();
						// op.type=3 target.id this.id method.id params.len {param.id} {info ~ null}
						oo.writeByte(4);
						Invoke in = (Invoke)op;
						oo.writeInt(in.getTarget() == null ? -1 : in.getTarget().getId());
						oo.writeInt(in.getThis() == null ? -1 : in.getThis().getId());
						oo.writeInt(in.getMethod().getId());
						oo.writeInt(in.getParams().length);
						for (Reference param : in.getParams())
							oo.writeInt(param.getId());
						t_ser_test_in.stop();

						writeOpInfo(oo, op);

					} else
						logger.warning("Unknown operation: " + op.getClass().getName() + " - " + op);
				}
				t_ser_test.stop();
			}

			t_ser_sClose.start();
			oo.close();
			byte[] ret = baos.toByteArray();
			t_ser_sClose.stop();

			t_ser.stop();

			return ret;

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
			else if(value instanceof String)    { oo.writeShort(8); writeString(oo, (String) value); }
			else { oo.writeShort(-1); logger.warning("Unexpected primitive: " + value); }
		}
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
		case 8: ret = readString(oi); break;
		default: logger.warning("Unexpected serialized primitive type: " + type); ret = null;
		}

		return ret;
	}

	private static <T> void writeString(ObjectOutputStream oo, String string) throws IOException {
		oo.writeShort(string.length());
		oo.writeChars(string);
	}

	public static IExecutor deserialize(byte[] serialized) {

		ObjectInputStream oi = null;
		try {

			if(TestFul.DEBUG && !(ExecutorSerializer.class.getClassLoader() instanceof TestfulClassLoader))
				throw new ClassCastException("ExecutorSerializer must be loaded by the Testful Class Loader, when executing method deserialize");

			t_dser.start();

			t_dser_sInit.start();
			ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
			oi = new ObjectInputStream(bais);
			t_dser_sInit.stop();

			String execClassName = readString(oi);

			boolean discoverFaults = oi.readBoolean();

			final TestCluster testCluster;
			final Reference[] testRefs;
			final Operation[] testOps;

			t_dser_cluster.start();
			boolean optimized = oi.readBoolean();
			if(!optimized) {

				// perform the standard de-serialization
				testCluster = (TestCluster) oi.readObject();
				t_dser_cluster.stop();

				t_dser_refs.start();
				testRefs = (Reference[]) oi.readObject();
				t_dser_refs.stop();

				t_dser_test.start();
				testOps = (Operation[]) oi.readObject();
				t_dser_test.stop();

			} else {

				String clusterID = readString(oi);

				// use the ObjectRegistry and the advanced serialization
				t_dser_cluster_or.start();
				testCluster = (TestCluster) ObjectRegistry.singleton.getObject(clusterID);
				t_dser_cluster_or.stop();

				t_dser_cluster.stop();

				t_dser_refs.start();
				// read the references: refs.length { ref.class.id, ref.pos, ref.id }
				int refLen = oi.readInt();
				testRefs = new Reference[refLen];
				for(int i = 0; i < refLen; i++) {
					Clazz clazz = testCluster.getClazzById(oi.readInt());
					int pos = oi.readInt();
					testRefs[i] = new Reference(clazz, pos, i);
				}
				t_dser_refs.stop();

				t_dser_test.start();
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
						t_dser_test_ac.start();
						int targetId = oi.readInt();
						Reference ref = (targetId < 0 ? null : testRefs[targetId]);

						int valueId = oi.readInt();
						StaticValue staticValue = valueId < 0 ? null : testCluster.getStaticValueById(valueId);

						testOps[i] = new AssignConstant(ref, staticValue);
						t_dser_test_ac.stop();

						break;
					}

					case 2: { // AssignPrimitive
						t_dser_test_ap.start();

						int targetId = oi.readInt();
						Reference ref = (targetId < 0 ? null : testRefs[targetId]);

						Serializable value = readPrimitive(oi);

						testOps[i] = new AssignPrimitive(ref, value);
						t_dser_test_ap.stop();

						break;
					}

					case 3: { // CreateObject
						t_dser_test_cr.start();

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
						t_dser_test_cr.stop();

						readOpInfo(oi, testOps[i]);

						break;
					}

					case 4: { // Invoke
						t_dser_test_in.start();

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
						t_dser_test_in.stop();

						readOpInfo(oi, testOps[i]);

						break;
					}

					default:
						logger.warning("Unknown operation serialized type " + operationType);
					}

				}
				t_dser_test.stop();

			}
			t_dser_creation.start();

			@SuppressWarnings("unchecked")
			Class<? extends IExecutor> execClass = (Class<? extends IExecutor>) Class.forName(execClassName);
			Constructor<? extends IExecutor> execCns = execClass.getConstructor(TestCluster.class, Reference[].class, Operation[].class, Boolean.TYPE);

			IExecutor exec = execCns.newInstance(testCluster, testRefs, testOps, discoverFaults);

			t_dser_creation.stop();

			t_dser.stop();

			return exec;
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

	private static String readString(ObjectInput oi) throws IOException {

		short execCharArrayLen = oi.readShort();
		char[] execCharArray = new char[execCharArrayLen];
		for (int i = 0; i < execCharArray.length; i++)
			execCharArray[i] = oi.readChar();
		return new String(execCharArray);
	}

	private static void writeOpInfo(ObjectOutputStream oo, Operation op) throws IOException {

		t_ser_test_opInfo.start();

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

		t_ser_test_opInfo.stop();

	}

	private static void readOpInfo(ObjectInput oi, Operation op) throws ClassNotFoundException, IOException {

		t_dser_test_opInfo.start();

		boolean isOpResult = oi.readBoolean();
		if(isOpResult) {

			boolean isVerifier = oi.readBoolean();
			if(isVerifier)
				op.addInfo((OperationResult.Verifier) oi.readObject());
			else
				op.addInfo(new OperationResult());
		}

		t_dser_test_opInfo.stop();
	}
}
