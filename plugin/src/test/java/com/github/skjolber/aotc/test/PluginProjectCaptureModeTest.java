package com.github.skjolber.aotc.test;


import static org.junit.Assert.assertTrue;

import java.io.File;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

public class PluginProjectCaptureModeTest extends AbstractPluginProjectTest {

	@Test
	public void testConsolePlugin() throws Exception {
		append(buildFile, "aotc { captureMode 'console'}\n");
		
		BuildResult result = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withArguments("aotcCompileCommands", "--info", "--stacktrace")
				.withPluginClasspath()
				.build();
		
		File output = new File(testProjectDir.getAbsolutePath() + "/build/aotc/");
		assertTrue(new File(output, "test_touched_methods.txt").length() > 0);
		assertTrue(new File(output, "compile_commands.txt").length() > 0);
	}

	@Test
	public void testJcmdPlugin() throws Exception {
		append(buildFile, "aotc { captureMode 'jcmd'}\n");
		
		BuildResult result = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withArguments("aotcCompileCommands", "--info", "--stacktrace")
				.withPluginClasspath()
				.build();
		
		File output = new File(testProjectDir.getAbsolutePath() + "/build/aotc/");
		assertTrue(new File(output, "test_touched_methods.txt").length() > 0);
		assertTrue(new File(output, "compile_commands.txt").length() > 0);
	}
	
	@Test
	public void testMBeanPlugin() throws Exception {
		append(buildFile, "aotc { captureMode 'mbean'}\n");
		
		BuildResult result = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withArguments("aotcCompileCommands", "--info", "--stacktrace")
				.withPluginClasspath()
				.build();
		
		File output = new File(testProjectDir.getAbsolutePath() + "/build/aotc/");
		assertTrue(new File(output, "test_touched_methods.txt").length() > 0);
		assertTrue(new File(output, "compile_commands.txt").length() > 0);
	}	

}
