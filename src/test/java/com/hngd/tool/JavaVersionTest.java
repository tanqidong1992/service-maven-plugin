package com.hngd.tool;

import org.apache.commons.lang3.JavaVersion;

import com.hngd.tool.util.JreUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JavaVersionTest {

	public static void main(String[] args) {
		System.getProperties().forEach((k,v)->{
			if("java.specification.version".equals(k)) {
			    System.out.println(k+"-->"+v);
			}
		});
		JavaVersion jv=JavaVersion.JAVA_RECENT;
        System.out.println(JreUtils.atLeastJava11());
	}

	@Test
	public void test(){
		System.setProperty("java.version","11.0.11");
		Assertions.assertTrue(JreUtils.atLeastJava11());

		System.setProperty("java.version","11.0.10");
		Assertions.assertTrue(!JreUtils.atLeastJava11());

		System.setProperty("java.version","11.1.10");
		Assertions.assertTrue(JreUtils.atLeastJava11());

		System.setProperty("java.version","17.0.1");
		Assertions.assertTrue(JreUtils.atLeastJava11());

		System.setProperty("java.version","1.8.0_312");
		Assertions.assertTrue(!JreUtils.atLeastJava11());

	}

}
