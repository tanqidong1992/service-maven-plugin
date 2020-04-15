package com.hngd.tool.utils;

import org.apache.commons.lang3.JavaVersion;

public class JreUtils {

	private static final String JAVA_HOME = "java.home";
	public static String getDefaultJrePath(){
		String defaultJrePath=System.getProperty(JAVA_HOME);
		return defaultJrePath;
	}
	
	public static boolean atLeastJava11() {
		return JavaVersion.JAVA_RECENT.atLeast(JavaVersion.JAVA_11);
	}
}
