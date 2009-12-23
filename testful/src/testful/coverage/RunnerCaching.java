package testful.coverage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import testful.model.Test;
import testful.model.TestSplitter;
import testful.runner.ClassFinder;
import testful.runner.Context;
import testful.runner.IRunner;
import testful.utils.Cloner;
import testful.utils.ElementManager;
import testful.utils.TestfulLogger;

public class RunnerCaching {

	private boolean enabled;

	private final static int RAM_SIZE = 10000;
	private final static int SCORE = 8;

	private final static boolean DISK_ENABLED = false;
	
	private final File dir; 

	private long origJobs = 0;

	private long cacheHitRam = 0;
	private long cacheHitDisk = 0;
	private long cacheMiss = 0;

	private final IRunner runner;

	/** tests being evaluated */
	private final Map<TestWithData, Future<ElementManager<String,CoverageInformation>>> evaluating;

	/** evaluated tests */
	private final Cache<TestWithData, ElementManager<String,CoverageInformation>> cache;

	/** key: hex(hash)-len  value: uniqueId */
	private final Map<String, Set<String>> diskEntries;

	public RunnerCaching(IRunner runner, boolean enableCache) {
		this.runner = runner;
		this.enabled = enableCache;

		this.evaluating = new HashMap<TestWithData, Future<ElementManager<String,CoverageInformation>>>();
		this.cache = new Cache<TestWithData, ElementManager<String,CoverageInformation>>(RAM_SIZE, SCORE);

		if(DISK_ENABLED) {
			this.diskEntries = new LinkedHashMap<String, Set<String>>();
			this.dir = new File(TestfulLogger.singleton.getBaseDir(), "cache");
			this.dir.mkdir();
		} else {
			this.diskEntries = null;
			this.dir = null;
		}
	}

	private long timeSplit = 0; 
	private long timeReorganize = 0;
	private long timeReferenceSort = 0;
	private long timePrepare = 0;
	private long timePostProcess = 0;

	/**
	 * Split the test into smaller parts, and execute each of them independelty.
	 * Uses a caching mechanism to ensure that each part is executed only once: subsequent evaluations reuses previous result.
	 */
	public Future<ElementManager<String, CoverageInformation>> executeParts(ClassFinder finder, boolean reloadClasses, Test test, TrackerDatum[] data) {
		if(!enabled) {
			Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager> ctx = CoverageExecutionManager.getContext(finder, test, data);
			ctx.setRecycleClassLoader(!reloadClasses);
			return runner.execute(ctx);
		}

		origJobs++;

		long start = System.nanoTime();
		test = test.removeUselessDefs().simplify().getSSA();
		long stop = System.nanoTime();
		timePrepare += (stop-start);

		start = System.nanoTime();
		List<Test> parts = TestSplitter.split(false, test);
		stop = System.nanoTime();
		timeSplit += (stop-start);


		CachingFuture ret = new CachingFuture();
		for(Test part: parts) {
			execute(finder, reloadClasses, part, data, ret);
		}

		return ret;
	}

	public Future<ElementManager<String, CoverageInformation>> execute(ClassFinder finder, boolean reloadClasses, Test test, TrackerDatum[] data) {
		if(!enabled) {
			Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager> ctx = CoverageExecutionManager.getContext(finder, test, data);
			ctx.setRecycleClassLoader(!reloadClasses);
			return runner.execute(ctx);
		}

		origJobs++;
		CachingFuture ret = new CachingFuture();

		execute(finder, reloadClasses, test, data, ret);

		return ret;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void enable() {
		enabled = true;
	}

	public void disable() {
		enabled = false;
	}

	private long timeTot = 0; 
	private long timeDisk = 0; 
	private long timeMiss = 0; 

	private void execute(ClassFinder finder, boolean reloadClasses, Test test, TrackerDatum[] data, CachingFuture ret) {
		long start = System.nanoTime();
		test = test.removeUselessDefs().simplify().getSSA().removeUselessDefs();
		long stop = System.nanoTime();
		timePostProcess += (stop-start);

		start = System.nanoTime();
		test = test.reorganize();
		stop = System.nanoTime();
		timeReorganize += (stop-start);

		start = System.nanoTime();
		test = test.sortReferences();
		stop = System.nanoTime();
		timeReferenceSort += (stop-start);

		start = System.nanoTime();

		TestWithData testWithData = new TestWithData(test, data);
		
		ElementManager<String, CoverageInformation> cov;
		if((cov = cache.get(testWithData)) == null) {
			synchronized(evaluating) {
				if((cov = cache.get(testWithData)) == null) {
					Future<ElementManager<String, CoverageInformation>> fut = evaluating.get(testWithData);
					if(fut != null) {
						cacheHitRam++;
						ret.add(testWithData, fut);
					} else {
						cov = null;
						if(DISK_ENABLED) {
							long startd = System.nanoTime();
							cov = readFromDisk(testWithData);
							long stopd = System.nanoTime();
							timeDisk += (stopd - startd);
						}
						if(cov != null) {
							cacheHitDisk++;

							ret.add(cov);
							cache.put(testWithData, cov);
						} else {
							long startm = System.nanoTime();

							cacheMiss++;
							Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager> ctx = CoverageExecutionManager.getContext(finder, test, data);
							ctx.setRecycleClassLoader(!reloadClasses);
							fut = runner.execute(ctx);

							evaluating.put(testWithData, fut);
							ret.add(testWithData, fut);

							long stopm = System.nanoTime();

							timeMiss += (stopm-startm);
						}
					}
					timeTot += (System.nanoTime() - start);
					return;
				}
			}
		}

		cacheHitRam++;
		ret.add(cov);
		timeTot += (System.nanoTime() - start);
	}

	private AtomicLong unique = new AtomicLong();

	public void writeToDisk(TestWithData test, ElementManager<String,CoverageInformation> cov) {
		String hash = Integer.toHexString(test.hashCode());
		String time = Long.toHexString(unique.incrementAndGet());

		String name = hash + "-" + test.test.getTest().length + "-" + test.data.length + "-" + time;

		File file = new File(dir, name + ".cache.gz");
		try {
			if(file.exists()) throw new IOException("file name collision!");

			Set<String> set = diskEntries.get(hash + "-" + test.test.getTest().length + "-" + test.data.length);
			if(set == null) {
				set = new HashSet<String>(1);
				diskEntries.put(hash + "-" + test.test.getTest().length + "-" + test.data.length, set);
			}
			set.add(time);

			ObjectOutput out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
			out.writeObject(test);
			out.writeObject(cov);
			out.close();

		} catch(IOException e) {
			System.err.println("Cannot write the cache to disk: " + e);
		}
	}

	@SuppressWarnings("unchecked")
	public ElementManager<String,CoverageInformation> readFromDisk(TestWithData test) {
		String hash = Integer.toHexString(test.hashCode());
		final String fileNamePrefix = hash + "-" + test.test.getTest().length + "-" + test.data.length;

		Set<String> suffixes = diskEntries.get(fileNamePrefix);
		if(suffixes == null) return null;

		for(String s : suffixes) {
			File f = new File(dir, fileNamePrefix + "-" + s + ".cache.gz");
			try {
				ObjectInput in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(f)));
				TestWithData t = (TestWithData) in.readObject();

				if(test.equals(t)) {
					ElementManager<String, CoverageInformation> cov = (ElementManager<String, CoverageInformation>) in.readObject();
					in.close();
					return cov;
				}

				in.close();
			} catch(IOException e) {
				System.err.println("Cannot read the cache from disk: " + e);
			} catch(ClassNotFoundException e) {
				System.err.println("Cannot read the cache from disk: " + e);
			}
		}

		return null;
	}

	public void updateCacheScore() {
		cache.updateScore();
	}

	private long totCacheHit;
	private long totCacheAccess;

	@Override
	public String toString() {
		if(!enabled) return null;

		long cacheHit = cacheHitRam + cacheHitDisk;
		long cacheAccess = cacheHit + cacheMiss;

		totCacheHit += cacheHit;
		totCacheAccess += cacheAccess;

		String ret = String.format("  Cache: %5.2f%% full; hit ratio: %5.2f%% (%5.2f%% global)", cache.size()*100.0/cache.getMaxSize(), cacheHit*100.0/cacheAccess, totCacheHit*100.0/totCacheAccess);
		if(DISK_ENABLED)
			ret += String.format(": %5.2f%% from ram, %5.2f%% from disk", cacheHitRam*100.0/cacheAccess, cacheHitDisk*100.0/cacheAccess);

		ret += "\n    " 
			+ "time mgmt:"
			+ " pre: " + timePrepare/10000/100.0 + "ms"
			+ " split: " + timeSplit/10000/100.0 + "ms"
			+ " post: " + timePostProcess/10000/100.0 + "ms"
			+ " reorg: " + timeReorganize/10000/100.0 + "ms"
			+ " rSort: " + timeReferenceSort/10000/100.0 + "ms";


		ret += "\n    " ;
		ret += "time: " + timeTot/10000/100.0 + "ms";
		ret += "; ram: " + (timeTot - timeMiss - timeDisk)/10000/100.0 + "ms";
		if(DISK_ENABLED) ret += ", disk: " + timeDisk/10000/100.0 + "ms";
		ret += ", miss: " + timeMiss/10000/100.0 + "ms";;

		ret += "\n    "; 
		ret += "jobs: " + cacheAccess;
		ret += " (" + 100*(cacheAccess+1)/(origJobs+1)/100.0 + "x)";
		ret += "; ram: "  + (cacheHitRam);
		if(DISK_ENABLED) ret += ", disk: " + (cacheHitDisk);
		ret += ", miss: " + (cacheMiss);
		ret += " (" + 100*(cacheMiss+1)/(origJobs+1)/100.0 + "x)";

		ret += "\n    ";
		ret += "per job: " + timeTot/(cacheAccess + 1)/10000/100.0 + "ms";
		ret += "; ram: "  + (timeTot - timeMiss - timeDisk)/(cacheHitRam+1)/10000/100.0 + "ms";
		if(DISK_ENABLED) ret += ", disk: " + timeDisk/(cacheHitDisk+1)/10000/100.0 + "ms";
		ret += ", miss: " + timeMiss/(cacheMiss+1)/10000/100.0 + "ms";


		origJobs = 0;
		cacheHitRam = 0;
		cacheHitDisk = 0;
		cacheMiss = 0;

		timeSplit = 0;
		timePrepare = 0;
		timePostProcess = 0;
		timeReorganize = 0;
		timeReferenceSort = 0;
		timeTot = 0;
		timeDisk = 0;
		timeMiss = 0;

		return ret;
	}

	private static class TestWithData implements Serializable {
		private static final long serialVersionUID = 751080279791998226L;

		final Test test;
		final TrackerDatum[] data;
		final int hashCode;

		public TestWithData(Test test, TrackerDatum[] data) {
			this.test = test;
			this.data = data;
			this.hashCode = 31*(31 + Arrays.hashCode(data)) + ((test == null) ? 0 : test.hashCode());
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(obj == null) return false;
			if(!(obj instanceof TestWithData)) return false;
			TestWithData other = (TestWithData) obj;
			if(!Arrays.equals(data, other.data)) return false;
			if(test == null) {
				if(other.test != null) return false;
			} else if(!test.equals(other.test)) return false;
			return true;
		}
		
		
		
	}
	
	private class CachingFuture implements Future<ElementManager<String, CoverageInformation>> {

		private final ElementManager<String, CoverageInformation> coverage;
		private final Map<TestWithData, Future<ElementManager<String, CoverageInformation>>> waiting;

		public CachingFuture() {
			this.coverage = new ElementManager<String, CoverageInformation>();
			this.waiting = new LinkedHashMap<TestWithData, Future<ElementManager<String,CoverageInformation>>>();
		}

		public void add(TestWithData test, Future<ElementManager<String, CoverageInformation>> future) {
			waiting.put(test, future);
		}

		public void add(ElementManager<String,CoverageInformation> cov) {
			ElementManager<String, CoverageInformation> clone;
			try {
				clone = cov.clone();
			} catch(CloneNotSupportedException e) {
				clone = Cloner.copy(cov);
			}

			for(CoverageInformation info : clone) {
				CoverageInformation merge = coverage.get(info.getKey());
				if(merge == null) {
					merge = info.createEmpty();
					coverage.put(merge);
				}
				merge.merge(info);
			}
		}

		@Override
		public ElementManager<String, CoverageInformation> get() throws InterruptedException, ExecutionException {
			for(Entry<TestWithData, Future<ElementManager<String, CoverageInformation>>> w : waiting.entrySet()) {
				TestWithData test = w.getKey();
				ElementManager<String, CoverageInformation> cov = w.getValue().get();

				add(cov);

				if(!cache.containsKey(test)) {
					synchronized(evaluating) {
						if(!cache.containsKey(test)) {
							evaluating.remove(test);
							cache.put(test, cov);
							if(DISK_ENABLED) writeToDisk(test, cov);
						}
					}
				}
			}

			waiting.clear();

			return coverage;
		}

		@Override
		public ElementManager<String, CoverageInformation> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return get();
		}

		@Override
		public boolean isDone() {
			return waiting.isEmpty();
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}
	}

	private static final class Cache<K, V> implements Map<K, V>{
		private class Value {
			final V value;
			int score;
			Value(V value) {
				this.value = value;
				this.score = initialScore;
			}

			public void addScore() {
				score += initialScore;
				if(score > maxScore) score = maxScore;
			}

			public void updateScore() {
				score >>= 1;
			}
			
			public boolean isExpired() {
				return score == 0;
			}
		}

		private final int maxSize;
		private final int initialScore;
		private final int maxScore;

		private final Map<K, Value> map;

		public Cache(int maxSize, int initialScore) {
			this.maxSize = maxSize;
			this.initialScore = initialScore;
			this.maxScore = initialScore*initialScore;
			this.map = new HashMap<K, Value>(maxSize);
		}

		public int getMaxSize() {
			return maxSize;
		}

		private void removeExpired() {
			int nRemoved = 0;
			Iterator<Entry<K, Value>> iter = map.entrySet().iterator();
			while(iter.hasNext()) {
				if(iter.next().getValue().isExpired()) {
					iter.remove();
					if(++nRemoved >= 10) return;
				}
			}
		}

		public void updateScore() {
			Iterator<Value> iter = map.values().iterator();
			while(iter.hasNext())
				iter.next().updateScore();
		}

		@Override
		public void clear() {
			map.clear();
		}

		@Override
		public boolean containsKey(Object key) {
			return map.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return map.containsValue(value);
		}

		@Override
		public Set<Entry<K, V>> entrySet() {
			throw new NullPointerException("Operation not supported");
			//			return map.entrySet();
		}

		@Override
		public V get(Object key) {
			Value v = map.get(key);

			if(v == null) return null;

			v.addScore();
			return v.value;
		}

		@Override
		public boolean isEmpty() {
			return map.isEmpty();
		}

		@Override
		public Set<K> keySet() {
			return map.keySet();
		}

		@Override
		public V put(K key, V value) {
			if(map.size()+1 >= maxSize) removeExpired();
			while(map.size()+1 >= maxSize) {
				updateScore();
				removeExpired();
			}

			Value r = map.put(key, new Value(value));

			if(r != null) return r.value;
			return null;
		}

		@Override
		public void putAll(Map<? extends K, ? extends V> m) {
			if(map.size()+m.size() >= maxSize) removeExpired();
			while(map.size()+m.size() >= maxSize) {
				updateScore();
				removeExpired();
			}

			for(Entry<? extends K, ? extends V> e : m.entrySet()) {
				map.put(e.getKey(), new Value(e.getValue()));
			}
		}

		@Override
		public V remove(Object key) {
			Value r = map.remove(key);
			if(r != null) return r.value;
			return null;
		}

		@Override
		public int size() {
			return map.size();
		}

		@Override
		public Collection<V> values() {
			throw new NullPointerException("Operation not supported");
			//			return map.values();
		}
	}

	public static void main(String[] args) {
		for(String f : args) {
			write(new File(f));
		}
	}

	@SuppressWarnings("unchecked")
	private static void write(File file) {
		if(file.isDirectory()) {
			for(File file2 : file.listFiles()) write(file2);
		} else {
			try {
				ObjectInput in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
				Test t = (Test) in.readObject();
				ElementManager<String, CoverageInformation> covs = (ElementManager<String, CoverageInformation>) in.readObject();
				in.close();

				PrintWriter out = new PrintWriter(file.getAbsolutePath() + ".txt");
				out.println("---  test  ---");
				out.println(t.toString());
				out.println("---coverage---");
				for(CoverageInformation cov : covs) {
					out.println(cov.getName() + ": " + cov.getQuality());
					out.println("-");
					out.println(cov);
					out.println("---");
				}
				out.close();
			} catch(IOException e) {
				System.err.println("Cannot write test " + file + ": " + e);
			} catch(ClassNotFoundException e) {
				System.err.println("Cannot write test " + file + ": " + e);
			}
		}
	}
}
