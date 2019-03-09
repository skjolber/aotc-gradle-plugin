package com.github.skjolber.aotc.gradle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.jvm.Jvm;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.process.internal.ExecAction;
import org.gradle.process.internal.ExecActionFactory;

public class LibraryTask extends ConventionTask implements CommandLineArgumentProvider {
	
    private final ExecAction execAction;

	private final Property<File> compileCommands;
	private final Property<File> compiledLibrary;

	protected final Property<Boolean> tiered;
    protected final Property<String> garbageCollector;
    protected final Property<String> memory;
	protected final Property<Boolean> ignoreErrors;
	
	@InputFile
	public Property<File> getCompileCommands() {
		return compileCommands;
	}
	
	public void setCompileCommands(File file) {
		this.compileCommands.set(file);
	}
	
    public void setCompileCommands(Provider<File> file) {
        this.compileCommands.set(file);
    }

	@OutputFile
	public Property<File> getCompiledLibrary() {
		return compiledLibrary;
	}
	
	public void setCompiledLibrary(File file) {
		this.compiledLibrary.set(file);
	}
	
    public void setCompiledLibrary(Provider<File> file) {
        this.compiledLibrary.set(file);
    }
	
    public LibraryTask() {
    	execAction = getExecActionFactory().newExecAction();
        execAction.getArgumentProviders().add(this);
        execAction.executable(Jvm.current().getExecutable("jaotc"));

    	compileCommands = getProject().getObjects().property(File.class);
    	compiledLibrary = getProject().getObjects().property(File.class);

		tiered = getProject().getObjects().property(Boolean.class);
		memory = getProject().getObjects().property(String.class);
		garbageCollector = getProject().getObjects().property(String.class);
		
		ignoreErrors = getProject().getObjects().property(Boolean.class);
    }

    @Inject
    protected ExecActionFactory getExecActionFactory() {
        throw new UnsupportedOperationException();
    }

    @TaskAction
    public void exec() {
        execAction.execute();
        
    }
    
	@Override
	public Iterable<String> asArguments() {
    	List<String> commandLine = new ArrayList<String>();
    	
    	compiledLibrary.get().getParentFile().mkdirs();
    	
		commandLine.add("--output");
		commandLine.add(compiledLibrary.get().getAbsolutePath());
		commandLine.add("--compile-commands");
		commandLine.add(compileCommands.get().getAbsolutePath());
		commandLine.add("--module");
		commandLine.add("java.base");
		
		if(tiered.getOrElse(true)) {
			commandLine.add("--compile-for-tiered");
		}
		
		if(ignoreErrors.getOrElse(true)) {
			commandLine.add("--ignore-errors");
		}
		
		switch(garbageCollector.getOrElse("default")) {
			case "parallel": {
				commandLine.add("-J-XX:+UseParallelGC");
				break;
			}
			case "g1": {
				commandLine.add("-J-XX:+UseG1GC");
				break;
			}
			case "z" : {
				// for future proofing. Currently z and aotc does not work together
				commandLine.add("-J-XX:+UnlockExperimentalVMOptions");
				commandLine.add("-J-XX:+UseZGC");
				break;
			}
			case "default" : {
				// do nothing
				break;
			}
			default : {
				throw new IllegalArgumentException("Unknown garbage collector name '" + garbageCollector.get() + "' expected 'parallel' or 'g1'");
			}
		}
		
		if(memory.isPresent()) {
			commandLine.add("-J-Xmx" + memory.get());
		}
		
        JavaPluginConvention javaPluginConvention = getProject().getConvention().getPlugin(JavaPluginConvention.class);
        FileCollection runtimeClasspath = javaPluginConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME).getRuntimeClasspath();
        
		List<File> jars = new ArrayList<File>();
		List<File> directories = new ArrayList<File>();
        for (File runtimeFile : runtimeClasspath) {
			if(runtimeFile.isFile()) {
				jars.add(runtimeFile);
			} else if(runtimeFile.isDirectory()) {
				directories.add(runtimeFile);
			}
		}
		
        if(!directories.isEmpty()) {
    		commandLine.add("--directory");
    		commandLine.add(toString(directories));
        }

        if(!jars.isEmpty()) {
    		commandLine.add("--jar");
    		commandLine.add(toString(jars));
    		commandLine.add("-J-cp");
    		commandLine.add("-J" + toString(jars));
        }
		
    	return commandLine;
	}

	private String toString(List<File> directories) {
		StringBuilder builder = new StringBuilder();
		for (File file : directories) {
			builder.append(file.getAbsolutePath());
			builder.append(":");
		}
		builder.setLength(builder.length() - 1);
		
		return builder.toString();
	}    
    
    @Input
    public Property<Boolean> getTiered() {
        return tiered;
    }

    public void setTiered(boolean enabled) {
        this.tiered.set(enabled);
    }	
    
    @Input
    public Property<Boolean> getIgnoreErrors() {
        return ignoreErrors;
    }

    public void setIgnoreErrors(boolean enabled) {
        this.ignoreErrors.set(enabled);
    }	    

	@Input
    public Property<String> getGarbageCollector() {
		return garbageCollector;
	}
    
    public void setGarbageCollector(String xmx) {
    	this.garbageCollector.set(xmx);
    }
    
    public Property<String> getMemory() {
		return memory;
	}
    
    public void setMemory(String xmx) {
    	this.memory.set(xmx);
    }

}
