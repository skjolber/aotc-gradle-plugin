package com.github.skjolber.aotc.agent;

import java.io.File;

import org.junit.jupiter.api.Test;


public class PreMainTest {

	@Test
	public void testInitialize() throws Exception {
		File file = new File("build/aotc/");
		file.mkdirs();
		
		PreMain.premain("build/aotc/touched.txt,java", null);
	}
}
