package testful.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

public class LogFileReader implements Iterable<String>, Iterator<String> {
	
	private final BufferedReader reader;
	private final String pattern;
	private String next;

	public LogFileReader(File filename, String pattern) throws IOException {
		this.pattern = pattern;
		
		FileInputStream fis = new FileInputStream(filename);
		fis.read();
		fis.read();

		reader = new BufferedReader(new InputStreamReader(fis));
		
		getNext();
	}

	private String getNext() {
		String tmp = next;
		try {
			String line;
			
			while((line = reader.readLine()) != null) {
				int idx = line.indexOf(pattern);
				if(idx >= 0) {
					next = line.substring(idx + pattern.length());
					return tmp;
				}
			}
			
		} catch (IOException e) {
		}
		
		next = null;
		return tmp;
	}

	@Override
	public Iterator<String> iterator() {
		return this;
	}
	
	@Override
	public String next() {
		return getNext();
	}
	
	@Override
	public boolean hasNext() {
		return next != null;
	}
	
	@Override
	public void remove() {
	}
}
