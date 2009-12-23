package testful.mutation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import soot.IntType;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.IntConstant;
import soot.jimple.JasminClass;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.ReturnStmt;
import soot.util.JasminOutputStream;

/**
 * This class is used during the mutant-generation phase to store the mutator operators
 * applied and the number of mutation of each class. 
 * It also creates and manages the <code>testful.mutation.Config</code> class, that hosts:
 * <ul>
 * <li>a method with the name of <code>__max_mutation_full_mutated_class_Name__</code> that returns id of the
 * latest mutant of the class <code>full.mutated.class.Name</code></li>
 * <li>a field with the name of <code>__cur_mutation_full_mutated_class_Name__</code> indicating the active
 * mutation of the class <code>full.mutated.class.Name</code>:
 * <ul>
 * <li>(-inf,0] = vanilla class</li>
 * <li>[1,max] = mutants</li>
 * <li>(max, +inf) = vanilla class</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author matteo
 */
public class ConfigHandler {

	public static final ConfigHandler singleton = new ConfigHandler();

	private final SootClass config;
	
	private final Map<SootClass, Config> map;

	private ConfigHandler() {
		map = new HashMap<SootClass, Config>();

		SootClass tmpConfig = null;
		try {
			Scene.v().loadClassAndSupport(Utils.CONFIG_CLASS);
			tmpConfig = Scene.v().getSootClass(Utils.CONFIG_CLASS);
		} catch(RuntimeException e) {
		}
		if(tmpConfig == null) {
			tmpConfig = new SootClass(Utils.CONFIG_CLASS, Modifier.PUBLIC);

      Scene.v().loadClassAndSupport("java.lang.Object");
			tmpConfig.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
      Scene.v().addClass(tmpConfig);
		}

		config = tmpConfig;
	}

	public void done(String outputDir) throws IOException {
    File out = new File(outputDir + Utils.CONFIG_CLASS.replace('.', File.separatorChar) + ".class");
    
    out.getParentFile().mkdirs();
    
    System.out.println("Writing config to " + out.getAbsolutePath());
    
    PrintWriter writerOut = new PrintWriter(
    		new OutputStreamWriter(
    				new JasminOutputStream(
    						new FileOutputStream(out))));
    
    JasminClass jasminClass = new soot.jimple.JasminClass(config);
    jasminClass.print(writerOut);
    writerOut.flush();
    writerOut.close();
	}

	public int getMutationNumber(SootClass sClass, SootMethod meth, String type) {
		return map.get(sClass).getNextId(meth, type);
	}

	public void manage(String className) {
		Scene.v().loadClassAndSupport(className);
		SootClass sClass = Scene.v().getSootClass(className);

		if(map.containsKey(sClass)) {
			System.err.println("Error: duplicated class " + className);
			System.exit(1);
		}

		map.put(sClass, new Config(sClass));
	}

	public void writeStats(PrintStream out) {
		for(Entry<SootClass, Config> entry : map.entrySet())
			out.println("Class: " + entry.getKey().getName() + "\n" + entry.getValue().toString());
	}

	private class Config {

		private final AtomicInteger idGenerator;
		private final ReturnStmt retMax;
		private final Map<SootMethod, MethodMutants> map = new HashMap<SootMethod, MethodMutants>();

		public Config(SootClass sClass) {
			idGenerator = new AtomicInteger(0);

			config.addField(new SootField(Utils.getCurField(sClass.getName()), IntType.v(), Modifier.PUBLIC | Modifier.STATIC | Modifier.VOLATILE));

			if(testful.mutation.Launcher.singleton.isTrack()) {
				Scene.v().loadClassAndSupport(BitSet.class.getCanonicalName());
				SootClass bitSet = Scene.v().getSootClass(BitSet.class.getCanonicalName());
				sClass.addField(new SootField(Utils.EXECUTED_MUTANTS, bitSet.getType(), Modifier.PUBLIC | Modifier.STATIC));
			}

			SootMethod maxMut = new SootMethod(Utils.getMaxField(sClass.getName()), new ArrayList<Object>(), IntType.v(), Modifier.PUBLIC | Modifier.STATIC);
			config.addMethod(maxMut);
			JimpleBody body = Jimple.v().newBody(maxMut);
			retMax = Jimple.v().newReturnStmt(IntConstant.v(0));
			body.getUnits().add(retMax);
			maxMut.setActiveBody(body);
		}

		public int getNextId(SootMethod meth, String type) {
			final int id = idGenerator.incrementAndGet();
			retMax.setOp(IntConstant.v(id));

			MethodMutants methMutants = map.get(meth);
			if(methMutants == null) {
				methMutants = new MethodMutants();
				map.put(meth, methMutants);
			}
			methMutants.add(type, id);

			return id;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append("  total mutations: ").append(idGenerator.get()).append("\n");
			sb.append("  per method:\n");

			for(Entry<SootMethod, MethodMutants> e : map.entrySet()) {
				sb.append("  * ").append(e.getKey().getBytecodeSignature()).append("\n");
				e.getValue().print("    ", sb);
			}

			return sb.toString();
		}

		private class MethodMutants {

			private final Map<String, Set<Integer>> map = new HashMap<String, Set<Integer>>();

			public void add(String type, int id) {
				Set<Integer> set = map.get(type);
				if(set == null) {
					set = new HashSet<Integer>();
					map.put(type, set);
				}
				set.add(id);
			}

			public void print(String prefix, StringBuilder sb) {
				for(Entry<String, Set<Integer>> e : map.entrySet())
					sb.append(prefix).append(e.getKey()).append(" : ").append(e.getValue()).append("\n");
			}
		}
	}
}
