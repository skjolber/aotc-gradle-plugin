package com.github.skjolber.aotc.test;



import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

public class PluginProjectMemoryTest extends AbstractPluginProjectTest {

	@Test
	public void testConsolePluginWithMemory() throws Exception {
		append(buildFile, "aotc { memory '1024m'}\n");
		
		BuildResult result = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withArguments("aotcLibrary", "--info", "--stacktrace")
				.withPluginClasspath()
				.build();
		
		assertThat(result.getOutput()).contains("-J-Xmx1024m");
		
		File output = new File(testProjectDir.getAbsolutePath() + "/build/aotc/");
		assertTrue(new File(output, "test_touched_methods.txt").length() > 0);
		assertTrue(new File(output, "compile_commands.txt").length() > 0);
		assertTrue(new File(output, "aotLibrary.so").length() > 0);

	}
	
	
}
