package com.github.skjolber.aotc.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Destroys;
import org.gradle.api.tasks.TaskAction;

public class CleanTask extends DefaultTask {

	private final AotcPluginExtension extension;
	
	@Inject
	public CleanTask(AotcPluginExtension extension) {
		this.extension = extension;
	}

	@TaskAction
	public void clean() throws IOException {
		File destination = extension.getOutputDirectory().get();

		if(destination.exists()) {
			// recursive delete
			Files.walkFileTree(destination.toPath(), new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
				
			});    	
		}
		
		destination.mkdirs();
	}

	@Destroys
	public Property<File> getOutputDirectory() {
		return extension.getOutputDirectory();
	}	
}