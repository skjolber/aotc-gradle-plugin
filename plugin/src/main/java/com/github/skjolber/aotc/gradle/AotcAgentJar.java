package com.github.skjolber.aotc.gradle;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.specs.Spec;

/**
 * Helper to resolve the {@code jacocoagent.jar} from inside of the {@code org.jacoco.agent.jar}.
 */
public class AotcAgentJar {

    private final Project project;
    private FileCollection agentConf;
    private File agentJar;

    /**
     * Constructs a new agent JAR wrapper.
     *
     * @param project a project that can be used to resolve files
     */
    public AotcAgentJar(Project project) {
        this.project = project;
    }

    /**
     * @return the configuration that the agent JAR is located in
     */
    public FileCollection getAgentConf() {
        return agentConf;
    }

    public void setAgentConf(FileCollection agentConf) {
        this.agentConf = agentConf;
    }

    /**
     * Unzips the resolved jar to retrieve the agent jar.
     *
     * @return a file pointing to the agent jar
     */
    public File getJar() {
        if (agentJar == null) {
        	
            agentJar = getAgentConf().filter(new Spec<File>() {
                @Override
                public boolean isSatisfiedBy(File file) {
                    return file.getName().equals("agent-" + AotcPlugin.DEFAULT_VERSION + ".jar");
                }
            }).getSingleFile();
        }
        return agentJar;
    }

}