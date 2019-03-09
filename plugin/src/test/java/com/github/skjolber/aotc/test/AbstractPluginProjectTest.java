package com.github.skjolber.aotc.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

public class AbstractPluginProjectTest {

	@TempDir
	protected File testProjectDir;
	protected File buildFile;

	@BeforeEach
	public void init() throws IOException {
		testProjectDir.mkdir();
		
		buildFile = new File(testProjectDir, "build.gradle");
		FileUtils.copyFile(new File("./src/functTest/junit/build.gradle"), buildFile);
		
		File settingsFile =  new File(testProjectDir, "settings.gradle");
		FileUtils.copyFile(new File("./src/functTest/junit/settings.gradle"), settingsFile);

		File propertiesFile =  new File(testProjectDir, "gradle.properties");
		FileUtils.copyFile(new File("./src/functTest/junit/gradle.properties"), propertiesFile);

		File resources = new File(buildFile.getParentFile() + "/src");
		FileUtils.copyDirectory(new File("./src/functTest/junit/src"), resources);
	}
	
	public void append(File file, String string) throws IOException {
		FileOutputStream fout = new FileOutputStream(file, true);
		fout.write(string.getBytes(StandardCharsets.UTF_8));
		fout.close();
	}
}
