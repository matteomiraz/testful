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

import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class BlockClass extends Block implements Iterable<Block> {

	private static final long serialVersionUID = 6039856116509927723L;

	private static final boolean printLocalData = true;

	private final String name;
	private final Set<Data> fields;

	private final Set<BlockFunctionEntry> methods;

	final BitSet blocksCode, blocksContract;
	final BitSet conditionsCode, conditionsContract;

	public BlockClass(String name, Set<Data> fields) {
		this.name = name;
		this.fields = fields;
		methods = new HashSet<BlockFunctionEntry>();
		blocksCode = new BitSet();
		blocksContract = new BitSet();
		conditionsCode = new BitSet();
		conditionsContract = new BitSet();
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

	public BitSet getBlocksCode() {
		return blocksCode;
	}

	public BitSet getBlocksContract() {
		return blocksContract;
	}

	public BitSet getConditionsContract() {
		return conditionsContract;
	}

	public BitSet getConditionsCode() {
		return conditionsCode;
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

		final Logger logger = Logger.getLogger("testful.coverage.instrumenter.whitebox");

		int iter = 0;
		boolean run = true;

		while(run) {
			long start = System.currentTimeMillis();
			run = false;
			for(Block b : this)
				run |= b.updateData();

			long end = System.currentTimeMillis();
			logger.info("DataFlow iteration: " + iter++ + " " + (end - start) + " ms");
		}
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
				printDataAnalysis(b, sb);
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
				printDataAnalysis(b, sb);
				sb.append("\"];\n");
			} else if(b instanceof BlockFunctionExit) {
				BlockFunctionExit c = (BlockFunctionExit) b;
				sb.append("  ").append(c.getId()).append(" [label=\"").append(c.getFullQualifiedName()).append("::End");
				printDataAnalysis(b, sb);
				sb.append("\"];\n");
			} else if(b instanceof BlockFunctionCall) {
				BlockFunctionCall c = (BlockFunctionCall) b;
				sb.append("  ").append(c.getId()).append(" [label=\"").append(c.getId()).append("\\ncall:").append((c.isStatic() ? "static " : "")).append(c.getMethodName());
				printDataAnalysis(b, sb);
				sb.append("\",color=red];\n");
			} else if(b instanceof BlockBasic) {
				BlockBasic c = (BlockBasic) b;
				sb.append("  ").append(c.getId()).append(" [label=\"").append(c.getId());
				//printDataAnalysis(b, sb);
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
							if(c == cSwi.getDefaultBranch()) sb.append(",label=\"default id=#").append(c.getId()).append("#\"");
							else for(Entry<Integer, EdgeConditional> entry : cSwi.getBranches().entrySet()) {
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

	@SuppressWarnings("unused")
	private void printDataAnalysis(Block b, StringBuilder sb) {
		//				sb.append("\\ninDefs:");
		//				BitSet bs = b.getIn();
		//				for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
		//					sb.append(" ").append(i);
		//				}

		//		if(b instanceof BlockClass) {
		//			sb.append("\\nmask:");
		//			bs = ((BlockClass)b).getMask();
		//			for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
		//				sb.append(" ").append(i);
		//			}
		//		} else
		if(b instanceof BlockBasic) {
			boolean intro = false;
			for(DataDef def : ((BlockBasic) b).getDefs())
				if(printLocalData || fields.contains(def.getData())) {
					if(!intro) {
						sb.append("\\ndefs:");
						intro = true;
					}
					sb.append(" ").append(def);
				}

			intro = false;
			for(DataUse use : ((BlockBasic) b).getUses())
				if(printLocalData || fields.contains(use.getData())) {
					if(!intro) {
						sb.append("\\nuses:");
						intro = true;
					}
					sb.append(" ").append(use).append("(").append(use.getDefUseNum()).append(")");
				}

			//						sb.append("\\ngenDefs:");
			//						bs = ((BlockBasic) b).getGens();
			//						for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
			//							sb.append(" ").append(i);
			//						}
			//
			//						sb.append("\\nkillDefs:");
			//						bs = ((BlockBasic) b).getKills();
			//						for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
			//							sb.append(" ").append(i);
			//						}

		}

		//				sb.append("\\noutDefs:");
		//				bs = b.getOut();
		//				for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
		//					sb.append(" ").append(i);
		//				}

	}
}
