package com.hngd.tool;

import org.apache.commons.lang3.JavaVersion;

import com.hngd.tool.utils.JreUtils;

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

}
