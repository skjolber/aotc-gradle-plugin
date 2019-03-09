package com.github.skjolber.aotc.gradle;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;

public class AotcTaskExtension {

    protected final Property<File> destinationFile;
    private boolean enabled = true;
    
	@javax.inject.Inject
	public AotcTaskExtension(Project project) {
		destinationFile = project.getObjects().property(File.class);
	}

	@OutputFile
	public Property<File> getDestinationFile() {
		return destinationFile;
	}
	
    public void setDestinationFile(Provider<File> destinationFile) {
        this.destinationFile.set(destinationFile);
    }

    public void setDestinationFile(File destinationFile) {
        this.destinationFile.set(destinationFile);
    }
    
    @Input
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
}