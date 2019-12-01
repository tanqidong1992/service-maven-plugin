package com.hngd.tool.utils;

import java.io.IOException;
import java.util.Optional;

public class MainClassDetectorTest {

	public static void main(String[] args) throws IOException {
		
		String mainJarFilePath="W:\\workspaces\\build-tools\\ntservice-demo\\target\\ntservice-demo-0.0.1-SNAPSHOT.jar";
		
		Optional<ClassWeight> s=MainClassDetector.findTheMostAppropriateMainClass(mainJarFilePath);
		System.out.println(s.get());
	}
}
