package com.hngd.tool.util;

import java.io.File;
import java.io.IOException;

public class BuildInfoUtilsTest {

	public static void main(String[] args) throws IOException {
		
		File projectBaseDir=new File(".");
		File output=new File("./target");
		BuildInfoUtils.generateBuildInfo(projectBaseDir, output);
	}
}
