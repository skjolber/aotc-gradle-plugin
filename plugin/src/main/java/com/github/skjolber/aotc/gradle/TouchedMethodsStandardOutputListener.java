package com.github.skjolber.aotc.gradle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.gradle.api.logging.StandardOutputListener;

// given that LineBufferingOutputStream exists, could part of this logic be skipped?
// https://github.com/gradle/gradle/issues/6068

public class TouchedMethodsStandardOutputListener implements StandardOutputListener {

	private Writer writer;
	private boolean printing = false;
	private File file;
	
	private StringBuilder builder = new StringBuilder();
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public void open() throws FileNotFoundException {
		this.writer = new OutputStreamWriter(new FileOutputStream(file, true));
	}

	@Override
	public synchronized void onOutput(CharSequence statement) {
		int length = builder.length();
		builder.append(statement);
		
		int offset = 0;
		for(int i = length; i < builder.length(); i++) {
			if(builder.charAt(i) == '\n') {
				processLine(builder, offset, i);
				
				offset += i+1;
			}
		}
		builder.delete(0, offset);
	}

	private void processLine(StringBuilder b, int start, int end) {
		String string = b.substring(start, end).trim();
		try {
			if(!printing) {
				if(string.equals("# Method::print_touched_methods version 1")) {
					printing = true;
				}
			} else if(printing) {
				if(!string.isEmpty()) {
					if(isValid(string)) {
						if(writer != null) {
							writer.write(string + "\n");
							writer.flush();
						}
					}
				}
			}
		} catch (IOException e) {
			// ignore
			System.err.println("Problem capturing console output:" + e.toString());
		}

	}

	private boolean isValid(CharSequence statement) {
		boolean slash = false;
		
		for(int i = 0; i < statement.length(); i++) {
			char c = statement.charAt(i);
			
			if(!slash) {
				slash = c == '/';
			}
			if(Character.isUpperCase(c) && !slash) {
				return false;
			}

			if(c == ' ') {
				return false;
			}
		}
		return true;
	}

	public void close() throws IOException {
		processLine(builder, 0, builder.length());
		if(writer != null) {
			writer.close();
		}
	}
}
