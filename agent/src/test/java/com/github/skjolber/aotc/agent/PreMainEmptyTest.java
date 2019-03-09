package com.github.skjolber.aotc.agent;

import java.io.File;

import org.junit.jupiter.api.Test;


public class PreMainEmptyTest {

	@Test
	public void testInitialize() throws Exception {
		File file = new File("build/aotc/");
		file.mkdirs();
		
		PreMain.premain(null, null);
	}
}
