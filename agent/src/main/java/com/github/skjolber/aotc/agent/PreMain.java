package com.github.skjolber.aotc.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.lang.ProcessHandle; 

public final class PreMain {

	private PreMain() {
		// no instances
	}

	/**
	 * This method is called by the JVM to initialize Java agents.
	 * 
	 * @param options agent options
	 * @param inst instrumentation callback provided by the JVM
	 * @throws Exception in case initialization fails
	 */

	public static void premain(final String options, final Instrumentation inst) throws Exception {
		// http://lewisleo.blogspot.com/2013/08/order-of-shutdown-hook-executions.html

		if(options != null && options.length() > 0) {
			String[] parts = options.split(",");
			if(parts.length != 1 && parts.length != 2) {
				return;
			}

			final String path = parts[0];

			final String command;
			if(parts.length > 1) {
				command = parts[1];
			} else {
				command = null;
			}
			
			Runtime.getRuntime().addShutdownHook(new Thread()  { 
				public void run()  {
					File file = new File(path);
					try (FileOutputStream fout = new FileOutputStream(file)){
						String touchedMethods;
						if(command != null) {
							touchedMethods = getTouchedMethods(command);
						} else {
							touchedMethods = getTouchedMethods();							
						}
						
						fout.write(touchedMethods.getBytes(StandardCharsets.UTF_8));
					} catch(Exception e) {
						System.err.println("Problem writing touched methods to file " + file.getAbsolutePath());
						e.printStackTrace();
					}
				}


				
			}); 
		} 
	}

	private static String getTouchedMethods() {
		VirtualMachineDiagnostics d = VirtualMachineDiagnostics.newInstance();
		String touchedMethods = d.getPrintTouchedMethods();
		return touchedMethods;
	}
	
	private static String getTouchedMethods(String command) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder();

		long pid = ProcessHandle.current().pid();
		
		// Run a shell command
		processBuilder.command(command, Long.toString(pid), "VM.print_touched_methods");

		Process process = processBuilder.start();

		StringBuilder output = new StringBuilder();

		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		String line = reader.readLine(); // discard first line
		while ((line = reader.readLine()) != null) {
			output.append(line + "\n");
		}

		return output.toString();
	}

}
