package com.github.skjolber.aotc.test;

import static com.google.common.truth.Truth.*;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

public class PluginProjectTest extends AbstractPluginProjectTest {
	
	@Test
	public void testPluginWithDefaults() throws Exception {
		// https://gradle.github.io/gradle-script-kotlin-docs/userguide/custom_plugins.html
		append(buildFile, "aotc { }\n");
		BuildResult result = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withArguments("aotcLibrary", "--info", "--stacktrace")
				.withPluginClasspath()
				.build();
		
		assertThat(result.getOutput()).contains("--compile-for-tiered");;
		assertThat(result.getOutput()).contains("--ignore-errors");
		
		File output = new File(testProjectDir.getAbsolutePath() + "/build/aotc/");
		assertTrue(new File(output, "test_touched_methods.txt").length() > 0);
		assertTrue(new File(output, "compile_commands.txt").length() > 0);
		assertTrue(new File(output, "aotLibrary.so").length() > 0);
	}
	
	@Test
	public void testPluginWithNonTiered() throws Exception {
		// https://gradle.github.io/gradle-script-kotlin-docs/userguide/custom_plugins.html
		append(buildFile, "aotc { tiered = false }\n");
		BuildResult result = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withArguments("aotcLibrary", "--info", "--stacktrace")
				.withPluginClasspath()
				.build();
		
		assertThat(result.getOutput()).doesNotContain("--compile-for-tiered");
		assertThat(result.getOutput()).contains("--ignore-errors");
		
		File output = new File(testProjectDir.getAbsolutePath() + "/build/aotc/");
		assertTrue(new File(output, "test_touched_methods.txt").length() > 0);
		assertTrue(new File(output, "compile_commands.txt").length() > 0);
		assertTrue(new File(output, "aotLibrary.so").length() > 0);
	}
	
	@Test
	public void testPluginWithNonIgnoreErrors() throws Exception {
		// https://gradle.github.io/gradle-script-kotlin-docs/userguide/custom_plugins.html
		append(buildFile, "aotc { ignoreErrors = false }\n");
		BuildResult result = GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withArguments("aotcLibrary", "--info", "--stacktrace")
				.withPluginClasspath()
				.build();
		
		assertThat(result.getOutput()).contains("--compile-for-tiered");
		assertThat(result.getOutput()).doesNotContain("--ignore-errors");
		
		File output = new File(testProjectDir.getAbsolutePath() + "/build/aotc/");
		assertTrue(new File(output, "test_touched_methods.txt").length() > 0);
		assertTrue(new File(output, "compile_commands.txt").length() > 0);
		assertTrue(new File(output, "aotLibrary.so").length() > 0);
	}		
}
