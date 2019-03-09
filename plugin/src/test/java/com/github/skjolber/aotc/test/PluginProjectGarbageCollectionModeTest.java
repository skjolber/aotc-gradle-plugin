package com.github.skjolber.aotc.test;



import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

public class PluginProjectGarbageCollectionModeTest extends AbstractPluginProjectTest {

	@Test
	public void testConsolePluginG1() throws Exception {
		append(buildFile, "aotc { garbageCollector 'g1'}\n");
		
		BuildResult result = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withArguments("aotcLibrary", "--info", "--stacktrace")
				.withPluginClasspath()
				.build();
		
		assertThat(result.getOutput()).contains("-J-XX:+UseG1GC");
		
		File output = new File(testProjectDir.getAbsolutePath() + "/build/aotc/");
		assertTrue(new File(output, "test_touched_methods.txt").length() > 0);
		assertTrue(new File(output, "compile_commands.txt").length() > 0);
		assertTrue(new File(output, "aotLibrary.so").length() > 0);
	}

	@Test
	public void testConsolePluginParalell() throws Exception {
		append(buildFile, "aotc { garbageCollector 'parallel'}\n");
		
		BuildResult result = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withArguments("aotcLibrary", "--info", "--stacktrace")
				.withPluginClasspath()
				.build();
		
		assertThat(result.getOutput()).contains("-J-XX:+UseParallelGC");
		
		File output = new File(testProjectDir.getAbsolutePath() + "/build/aotc/");
		assertTrue(new File(output, "test_touched_methods.txt").length() > 0);
		assertTrue(new File(output, "compile_commands.txt").length() > 0);
		assertTrue(new File(output, "aotLibrary.so").length() > 0);
	}

}
