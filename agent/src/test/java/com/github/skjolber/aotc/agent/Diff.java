package com.github.skjolber.aotc.agent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;

public class Diff {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		Set<String> a = readFile("/tmp/1551574138573/build/aotc/test_touched_methods.txt");
		Set<String> b = readFile("/tmp/1551573670304/build/aotc/test_touched_methods.txt");
		
		Set<String> justInA = new HashSet<>(a);
		justInA.removeAll(b);

		System.out.println("Just in A");
		for(String s : justInA) {
			if(!s.contains("Lambda")) {
				System.out.println(s);
			}
		}
		
		Set<String> justInB = new HashSet<>(b);
		justInB.removeAll(a);
		System.out.println("Just in B");
		for(String s : justInB) {
			if(!s.contains("Lambda")) {
				System.out.println(s);
			}
		}

		
	}

	private static Set<String> readFile(String string) throws FileNotFoundException, IOException {
		return new HashSet<>(IOUtils.readLines(new FileInputStream(string), StandardCharsets.UTF_8));
	}
}
