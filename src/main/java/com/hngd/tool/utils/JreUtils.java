package com.hngd.tool.utils;


public class JreUtils {

	private static final String JAVA_HOMRE = "java.home";
 
	public static String getDefaultJrePath(){
		String defaultJrePath=System.getProperty(JAVA_HOMRE);
		return defaultJrePath;
	}
}
