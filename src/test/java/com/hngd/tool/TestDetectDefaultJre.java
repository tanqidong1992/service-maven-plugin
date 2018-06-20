package com.hngd.tool;

import org.junit.Test;

public class TestDetectDefaultJre {

	private static final String JAVA_HOMRE = "java.home";

	@Test
	public void testGetJrePath(){
		
		System.getProperties().forEach((key,value)->{
			System.out.println(key+"-->"+value);
		});
		String defaultJrePath=System.getProperty(JAVA_HOMRE);
		System.out.println("default jre path:"+defaultJrePath);
	}
}
