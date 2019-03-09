package com.github.skjolber.aotc.gradle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.internal.jvm.Jvm;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.util.RelativePathUtil;

import com.google.common.collect.ImmutableList;

/**
 * 
 * This class originally also used -XX:+PrintTouchedMethodsAtExit, however
 * it turned out impossible to properly control gradle console output, so 
 * not it dumps a lot in the console as seen by the user.
 *
 */

public class LogMethodsCommandLineProvider implements CommandLineArgumentProvider {

	private static final List<String> arguments = ImmutableList.of(
    		"-XX:+UnlockDiagnosticVMOptions",
    		"-XX:+LogTouchedMethods"
    		);

	private final Test task;
	private final AotcAgentJar agent;
	private final Provider<File> target;
	private final Property<String> captureMode;
	
	public LogMethodsCommandLineProvider(Test task, AotcAgentJar jar, Provider<File> target, Property<String> captureMode) {
		super();
		this.task = task;
		this.agent = jar;
		this.target = target;
		this.captureMode = captureMode;
	}

    @Override
    public Iterable<String> asArguments() {
        List<String> arguments = new ArrayList<>();
        arguments.addAll(LogMethodsCommandLineProvider.arguments);
        
        String captureMode = this.captureMode.getOrElse("jcmd");
        if(captureMode.equals("console")) {
        	arguments.add("-XX:+PrintTouchedMethodsAtExit");
        } else if(captureMode.equals("jcmd") || captureMode.equals("mbean")) {
	        StringBuilder builder = new StringBuilder();
	        builder.append("-javaagent:");
	        builder.append(RelativePathUtil.relativePath(task.getWorkingDir(), agent.getJar()));
	        builder.append('=');
	        builder.append(target.get().getAbsolutePath());
	        if(captureMode.equals("jcmd")) {
		        builder.append(",");
		        builder.append(Jvm.current().getExecutable("jcmd"));
	        }
	        arguments.add(builder.toString());
        }
        
        return arguments;
    }


}
