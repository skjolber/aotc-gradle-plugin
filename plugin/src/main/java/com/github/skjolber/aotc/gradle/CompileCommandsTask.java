package com.github.skjolber.aotc.gradle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskCollection;

@CacheableTask
public class CompileCommandsTask extends DefaultTask {

	protected final ConfigurableFileCollection touchedMethodFiles;
	
	protected final Property<File> compileCommands;
	
	protected final ConfigurableFileCollection additionalCommands;
	
	public CompileCommandsTask() {
		touchedMethodFiles = getProject().files();
		compileCommands = getProject().getObjects().property(File.class);
		additionalCommands = getProject().files();
	}

    @TaskAction
    public void generate() {
    	getProject().getLogger().info("Generating compile commands..");
    	
        try (
			FileOutputStream fout = new java.io.FileOutputStream(compileCommands.get());
			OutputStreamWriter writer = new OutputStreamWriter(fout);
    		) {
    
        	Set<String> history = new HashSet<>();
        	boolean present = false;
        	Set<File> files = touchedMethodFiles.getFiles();
        	for(File t : files) {
				if(t.exists()) {
					present = true;
					
	        		try (
    					FileInputStream fin = new FileInputStream(t);
    					BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
    	        		){
	    				String line = reader.readLine();
	    				while(line != null) {
	    					line = line.trim();
	    					if(!line.isEmpty() && !line.startsWith("#")) {
	    						String command = "compileOnly " + fix(line);
	    						if(!history.contains(command)) {
	    							history.add(command);
	    							
		    						writer.write(command);
		    						writer.write("\n");
	    						}
	    					}
	    					line = reader.readLine();
	    				}
	    			} catch (IOException e) {
	    				throw new RuntimeException(e);
	    			}    	
				}
			}
        	
        	if(!present) {
				getProject().getLogger().warn("Unable to produce compile commands outputs from unit tests - no unit tests?");
        	}

        	for(File t : additionalCommands.getFiles()) {
				if(t.exists()) {
					present = true;
					
	        		try (
    					FileInputStream fin = new FileInputStream(t);
    					BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
    	        		){
	    				String line = reader.readLine();
	    				while(line != null) {
	    					if(!line.trim().isEmpty() && !line.startsWith("#")) {
	    						if(!history.contains(line)) {
	    							history.add(line);

		    						writer.write(line);
		    						writer.write("\n");
	    						}
	    					}
	    					line = reader.readLine();
	    				}
	    			} catch (IOException e) {
	    				throw new RuntimeException(e);
	    			}    	
				} else {
					getProject().getLogger().warn("Additional commands file " + t + " does not exist");
				}
        	}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}    	
    }

    @PathSensitive(PathSensitivity.NONE)
    @InputFiles
    public ConfigurableFileCollection getTouchedMethodFiles() {
        return touchedMethodFiles;
    }    

    public void touchedMethodFiles(Task... tasks) {
        for (Task task : tasks) {
            final AotcTaskExtension extension = task.getExtensions().findByType(AotcTaskExtension.class);
            if (extension != null) {
            	touchedMethodFiles.getFrom().add(extension.getDestinationFile().get());
                mustRunAfter(task);
            }
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void touchedMethodFiles(TaskCollection tasks) {
        tasks.all(new Action<Task>() {
            @Override
            public void execute(Task task) {
                touchedMethodFiles(task);
            }
        });
    }
    
	@OutputFile
	public Property<File> getCompileCommands() {
		return compileCommands;
	}
	
	public void setCompileCommands(File file) {
		this.compileCommands.set(file);
	}
	
    public void setCompileCommands(Provider<File> file) {
        this.compileCommands.set(file);
    }
    
    private String fix(String line) {
    	StringBuilder builder = new StringBuilder();

    	boolean seenParentheses = false;
    	for(int k = 0; k < line.length(); k++) {
    		char i = line.charAt(k);
    		switch (i) {
    		case ':':
    			continue; // skip
    		case '/':
    			if (!seenParentheses) {
    				i = '.';
    			}
    			break;
    		case '(':
    			seenParentheses = true;
    			break;
    		}
    		builder.append(i);
    	}

    	return builder.toString();

    }
    
	@InputFiles
	@PathSensitive(PathSensitivity.NONE)
	public ConfigurableFileCollection getAdditionalCommands() {
		return additionalCommands;
	}
	

}