package com.github.skjolber.aotc.gradle;

import java.io.File;
import java.util.concurrent.Callable;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectEvaluationListener;
import org.gradle.api.ProjectState;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.testing.Test;

public class AotcPlugin implements Plugin<Project> {

    public static final String AGENT_VERSION = "1.0.0";

	private static final String AGENT_CONFIGURATION_NAME = "aotcAgent";

	protected Project project;
	
	public AotcPlugin() {
	}

	public void apply(Project project) {
		this.project = project;
		
		// https://discuss.gradle.org/t/extension-values-are-not-available-in-task-created-by-custom-plugin/4695
		
		final AotcPluginExtension extension = project.getExtensions().create("aotc", AotcPluginExtension.class, project);

		File outputDirectory = project.file(project.getBuildDir() + "/aotc");
		
		extension.getOutputDirectory().set(outputDirectory);
		
		outputDirectory.mkdirs();
		
		AotcAgentJar jar = new AotcAgentJar(project);
		
		CompileCommandsTask compileCommandsTask = project.getTasks().create("aotcCompileCommands", CompileCommandsTask.class, (task) -> { 
			task.getCompileCommands().set(
					project.provider(
						new Callable<File>() {
	                        public File call() throws Exception {
	        					return project.file(extension.outputDirectory.get() + "/compile_commands.txt");
	                        }
	                    })					
					);
			task.getAdditionalCommands().setFrom(extension.additionalCommands.getFiles());
		});

        LibraryTask aotcTask = project.getTasks().create("aotcLibrary", LibraryTask.class, (task) -> { 
        	task.getTiered().set(extension.getTiered());
        	task.getMemory().set(extension.getMemory());
        	task.getGarbageCollector().set(extension.getGarbageCollector());
        	task.getIgnoreErrors().set(extension.getIgnoreErrors());
        	
        	task.setCompileCommands(compileCommandsTask.getCompileCommands());
        	
			task.getCompiledLibrary().set(
					project.provider(
						new Callable<File>() {
	                        public File call() throws Exception {
	        					return project.file(extension.outputDirectory.get() + "/aotLibrary.so");
	                        }
	                    })					
					);
		});
		
        // use project evaluation so that there is test tasks and such when the code runs
        // alternatively actively apply plugins at the start of the code, i..e
        // plugins.apply(JavaPlugin.class);

        project.getGradle().addProjectEvaluationListener(new ProjectEvaluationListener() {
			
			@Override
			public void beforeEvaluate(Project arg0) {

			}
			
			@Override
			public void afterEvaluate(Project arg0, ProjectState arg1) {
		        configureTaskClasspathDefaults(extension, jar);

		        TaskCollection<Test> testTasks = project.getTasks().withType(Test.class);
		        
		        testTasks.configureEach(new Action<Test>() {
		            @Override
		            public void execute(Test task) {
		            	extension.apply(task, jar);          	
		            	
		            }
		        });
		    	compileCommandsTask.touchedMethodFiles(testTasks);
		    	
				compileCommandsTask.dependsOn(project.getTasks().findByName("test"));
				
				//finally create a clean task
				CleanTask cleanTask = project.getTasks().create("aotcClean", CleanTask.class, extension);
				Task clean = project.getTasks().findByName("clean");
				clean.dependsOn(cleanTask);
			}
		});        
        
		aotcTask.dependsOn(compileCommandsTask);
	}

    private void configureTaskClasspathDefaults(AotcPluginExtension extension, AotcAgentJar agent) {
        Configuration agentConf = project.getConfigurations().create(AGENT_CONFIGURATION_NAME);
        agentConf.setVisible(false);
        agentConf.setTransitive(true);
        agentConf.setDescription("The instrumentation agent to use to capture touched methods.");

        agent.setAgentConf(agentConf);
        agentConf.defaultDependencies(new Action<DependencySet>() {
            @Override
            public void execute(DependencySet dependencies) {
                dependencies.add(project.getDependencies().create("com.github.skjolber.aotc:agent:" + AGENT_VERSION));
            }
        });
    }
	
}