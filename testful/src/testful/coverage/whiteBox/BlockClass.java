/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
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

package testful.coverage.whiteBox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import testful.TestFul;

public class BlockClass extends Block implements Iterable<Block> {

	private static final long serialVersionUID = 6039856116509927723L;

	private static final Logger logger = Logger.getLogger("testful.coverage.whiteBox");

	private static final String FILE_SUFFIX = ".wgz";

	public static BlockClass read(URL classURL) throws IOException, ClassNotFoundException {
		ObjectInput oi = null;

		final String str = classURL.toString();
		int i = str.lastIndexOf('/');
		String pre = str.substring(0, i+1);
		String classFileName = str.substring(i+1);

		if(TestFul.DEBUG && !classFileName.endsWith(".class")) TestFul.debug("The class file does not ends with .class " + classURL);

		final String url = pre + classFileName.substring(0, classFileName.length()-6) + FILE_SUFFIX;
		try {
			oi = new ObjectInputStream(new GZIPInputStream(new URL(url).openStream()));
			return (BlockClass) oi.readObject();
		} finally {
			if(oi != null) {
				try {
					oi.close();
				} catch(IOException e) {
				}
			}
		}
	}

	public void write(File baseDir) {
		ObjectOutput oo = null;
		try {
			final File file = new File(baseDir, name.replace('.', File.separatorChar) + FILE_SUFFIX);
			oo = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
			oo.writeObject(this);
		} catch(IOException e) {
			Logger.getLogger("testful.coverage.whitebox").log(Level.WARNING, "Cannot write the white box data file: " + e.getMessage(), e);
		} finally {
			if(oo != null)
				try {
					oo.close();
				} catch(IOException e) {
				}
		}
	}


	private static final boolean printLocalData = false;

	private final String name;
	private final Set<Data> fields;

	private final Set<BlockFunctionEntry> methods;

	public BlockClass(String name, Set<Data> fields) {
		this.name = name;
		this.fields = fields;
		methods = new HashSet<BlockFunctionEntry>();
	}

	public String getName() {
		return name;
	}

	public Set<Data> getFields() {
		return new HashSet<Data>(fields);
	}

	public Set<BlockFunctionEntry> getMethods() {
		return methods;
	}

	void addMethod(BlockFunctionEntry m) {
		methods.add(m);
	}

	@Override
	public boolean updateData() {
		BitSet oldIn = in;
		out = in = new BitSet();
		for(Edge e : pre)
			in.or(e.getFrom().out);

		oldIn.xor(in);
		return !oldIn.isEmpty();
	}

	@Override
	public Iterator<Block> iterator() {
		return new Iterator<Block>() {

			private final Set<Block> considered;
			private final Set<Block> toConsider;

			// constructor
			{
				considered = new LinkedHashSet<Block>();
				toConsider = new LinkedHashSet<Block>();

				toConsider.add(BlockClass.this);
			}

			@Override
			public boolean hasNext() {
				return !toConsider.isEmpty();
			}

			@Override
			public Block next() {
				if(!hasNext()) throw new NoSuchElementException();

				Block ret = toConsider.iterator().next();
				toConsider.remove(ret);
				considered.add(ret);

				for(Edge e : ret.getPost()) {
					Block to = e.getTo();

					if(to != null && !considered.contains(to)) toConsider.add(to);
				}

				if(ret instanceof BlockClass)
					for(BlockFunctionEntry m : ((BlockClass) ret).getMethods())
						if(!considered.contains(m)) toConsider.add(m);

				return ret;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public void performDataFlowAnalysis() {
		int iter = 0;
		boolean run = true;

		long start = System.nanoTime();
		while(run) {
			iter++;
			run = false;
			for(Block b : this)
				run |= b.updateData();
		}
		long end = System.nanoTime();
		logger.info("class " + name + ": " + iter + " data-flow iterations (" + (end - start)/1000000.0 + " ms)");
	}

	public String getDot() {
		StringBuilder sb = new StringBuilder();

		sb.append("digraph clazz {\n");

		for(Block b : this) {
			if(b instanceof BlockClass) {
				BlockClass c = (BlockClass) b;
				sb.append("  ").append(c.getId()).append(" [shape=box,label=\"").append(c.name).append("\\nFields:");
				for(Data d : c.fields)
					sb.append(" ").append(d);
				if(printLocalData) printLocalDataAnalysis(b, sb);
				sb.append("\"];\n");

				for(Data field : c.getFields()) {
					sb.append("  f").append(field.getId()).append(" -> ").append(c.getId()).append(" [style=dashed,color=green];\n");
					sb.append("  f").append(field.getId()).append(" [shape=note,color=green,label=\"").append(field.getFieldName()).append(" (").append(field).append(")");
					sb.append("\\ndefs:");
					for(DataDef ddef : field.getDefs())
						sb.append(" ").append(ddef);

					sb.append("\\nuses: ").append(field.getUses().size());
					for(DataUse use : field.getUses())
						sb.append("\\nuse ").append(use).append(": defs(").append(use.getDefUseNum()).append("):").append(use.getDefUseString());

					sb.append("\"];\n");
				}

			} else if(b instanceof BlockFunctionEntry) {
				BlockFunctionEntry c = (BlockFunctionEntry) b;
				sb.append("  ").append(c.getId()).append(" [label=\"(").append(c.getId()).append(") ").append(c.getFullQualifiedName()).append("::Start");
				if(printLocalData) printLocalDataAnalysis(b, sb);
				sb.append("\"];\n");
			} else if(b instanceof BlockFunctionExit) {
				BlockFunctionExit c = (BlockFunctionExit) b;
				sb.append("  ").append(c.getId()).append(" [label=\"").append(c.getFullQualifiedName()).append("::End");
				if(printLocalData) printLocalDataAnalysis(b, sb);
				sb.append("\"];\n");
			} else if(b instanceof BlockFunctionCall) {
				BlockFunctionCall c = (BlockFunctionCall) b;
				sb.append("  ").append(c.getId()).append(" [label=\"").append(c.getId()).append("\\ncall:").append((c.isStatic() ? "static " : "")).append(c.getMethodName());
				if(printLocalData) printLocalDataAnalysis(b, sb);
				sb.append("\",color=red];\n");
			} else if(b instanceof BlockBasic) {
				BlockBasic c = (BlockBasic) b;
				sb.append("  ").append(c.getId()).append(" [label=\"").append(c.getId());
				if(printLocalData) printLocalDataAnalysis(b, sb);
				sb.append("\"];\n");
			}

			for(Edge e : b.getPost())
				if(e.getTo() != null) {
					sb.append("  ").append(b.getId()).append(" -> ").append(e.getTo().getId());
					if(e instanceof EdgeConditional) {
						sb.append("[style=dashed,color=blue");

						EdgeConditional c = (EdgeConditional) e;
						Condition condition = c.getCondition();
						if(condition instanceof ConditionIf) {
							ConditionIf cIf = (ConditionIf) condition;
							if(c == cIf.getTrueBranch()) sb.append(",label=\"").append(cIf.getCondition()).append(":True id=#").append(c.getId()).append("#\"");
							else if(c == cIf.getFalseBranch()) sb.append(",label=\"").append(cIf.getCondition()).append(":False id=#").append(c.getId()).append("#\"");
						} else if(condition instanceof ConditionSwitch) {
							ConditionSwitch cSwi = (ConditionSwitch) condition;
							if(c == cSwi.getDefaultCase()) sb.append(",label=\"default id=#").append(c.getId()).append("#\"");
							else for(Entry<Integer, EdgeConditional> entry : cSwi.getCases().entrySet()) {
								if(entry.getValue() == c) {
									sb.append(",label=\"case " + entry.getKey() + " id=#").append(c.getId()).append("#\"");
									break;
								}
							}
						}


						sb.append("]");

					} else if(e instanceof EdgeExceptional) {
						EdgeExceptional c = (EdgeExceptional) e;
						sb.append("[style=dashed,color=red,label=\"").append(c.getExceptionClass()).append("\"]");
					}
					sb.append(";\n");
				}
		}

		sb.append("}\n");

		return sb.toString();
	}

	private void printLocalDataAnalysis(Block b, StringBuilder sb) {
		if(b instanceof BlockBasic) {

			boolean intro = false;
			for(DataDef def : ((BlockBasic) b).getDefs()) {
				if(fields.contains(def.getData())) {
					if(!intro) {
						sb.append("\\ndefs:");
						intro = true;
					}
					sb.append(" ").append(def);
				}
			}

			intro = false;
			for(DataUse use : ((BlockBasic) b).getUses()) {
				if(fields.contains(use.getData())) {
					if(!intro) {
						sb.append("\\nuses:");
						intro = true;
					}
					sb.append(" ").append(use).append("(").append(use.getDefUseNum()).append(")");
				}
			}
		}
	}
}
