package com.github.skjolber.aotc.gradle;

import java.io.File;
import java.util.concurrent.Callable;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.testing.Test;

public class AotcPluginExtension {

	public static final String TASK_EXTENSION_NAME = "oatc";
	
    protected final Project project;
    protected final Property<File> outputDirectory;
    //protected final Property<Boolean> enabled;
    protected final Property<String> memory;
    protected final Property<String> gc;
    protected final ConfigurableFileCollection additionalCommands;
    protected final Property<Boolean> tiered;
    protected final Property<Boolean> ignoreErrors;
    protected final Property<String> captureMode;
    
	@javax.inject.Inject
	public AotcPluginExtension(Project project) {
		this.project = project;
		
		outputDirectory = project.getObjects().property(File.class);
		//enabled = project.getObjects().property(Boolean.class);
		memory = project.getObjects().property(String.class);
		gc = project.getObjects().property(String.class);
		
		additionalCommands = project.files();
		tiered = project.getObjects().property(Boolean.class);
		ignoreErrors = project.getObjects().property(Boolean.class);
		captureMode = project.getObjects().property(String.class);
		
		tiered.set(true);
		ignoreErrors.set(true);
		
		gc.set("default");
	}

	@InputFile
	public Property<File> getOutputDirectory() {
		return outputDirectory;
	}
	
	public void setOutputDirectory(File file) {
		this.outputDirectory.set(file);
	}
	
    public void setOutputDirectory(Provider<File> file) {
        this.outputDirectory.set(file);
    }
    
	@Input
    public Property<String> getGarbageCollector() {
		return gc;
	}
    
    public void setGarbageCollector(String xmx) {
    	this.gc.set(xmx);
    }
    
	@Input
    public Property<String> getCaptureMode() {
		return captureMode;
	}
    
    public void setCaptureMode(String captureMode) {
    	this.captureMode.set(captureMode);
    }    
    
    @Input
    public Property<String> getMemory() {
		return memory;
	}
    
    public void setMemory(String xmx) {
    	this.memory.set(xmx);
    }
    /*
	@Input
	public Property<Boolean> getEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled.set(enabled);
	}
	*/
	
	@Input
	public Property<Boolean> getTiered() {
		return tiered;
	}
	
	public void setTiered(boolean enabled) {
		this.tiered.set(enabled);
	}	
	
	public void setAdditionalCommands(ConfigurableFileCollection definitions) {
		this.additionalCommands.setFrom(definitions);
	}

	@InputFiles
	@PathSensitive(PathSensitivity.RELATIVE)
	public ConfigurableFileCollection getAdditionalCommands() {
		return additionalCommands;
	}
	
	@Input
	public Property<Boolean> getIgnoreErrors() {
		return ignoreErrors;
	}
	
	public void setIgnoreErrors(boolean enabled) {
		this.ignoreErrors.set(enabled);
	}	

	public void apply(Test task, AotcAgentJar jar) {
        final String taskName = task.getName();
        final AotcTaskExtension extension = task.getExtensions().create(TASK_EXTENSION_NAME, AotcTaskExtension.class, project);
        
		 Provider<File> tocuhedMethodsProvider = project.provider(new Callable<File>() {
	            @Override
	            public File call() throws Exception {
	                return project.file(outputDirectory.get().getAbsolutePath() + "/" + taskName + "_touched_methods.txt");
	            }
		 }) ;
        
        extension.getDestinationFile().set(tocuhedMethodsProvider);

		LogMethodsCommandLineProvider commandLineProvider = new LogMethodsCommandLineProvider(task, jar, tocuhedMethodsProvider, captureMode);

		task.getJvmArgumentProviders().add(commandLineProvider);
		
		final TouchedMethodsStandardOutputListener listener = new TouchedMethodsStandardOutputListener();
		
        task.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
        		if(JavaVersion.current() == JavaVersion.VERSION_1_7 || JavaVersion.current() == JavaVersion.VERSION_1_8){
        		    throw new GradleException("This build must be run with java 9 or higher");
        		}

        		boolean captureToConsole = captureMode.isPresent() && captureMode.get().equals("console");
        		if(captureToConsole) {
        			task.getLogging().addStandardOutputListener(listener);
        		}
        		
                try {
                	File directory = project.file(outputDirectory.get().getAbsolutePath());
                	if(!directory.exists()) {
                		directory.mkdirs();
                	}
                	
                    File touchedMethods = extension.getDestinationFile().get();
                    project.delete(touchedMethods);
                    
                    if(captureToConsole) {
	                	listener.setFile(touchedMethods);
	                    listener.open();
                    }                    
                } catch(Exception e) {
                	throw new RuntimeException(e);
                }
            }
        });
	
	}
    
}