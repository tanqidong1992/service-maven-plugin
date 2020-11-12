package com.hngd.tool;

import com.hngd.tool.utils.JreUtils;

//import com.hngd.tool.utils.JreUtils;

public class OSTypeTest {

	public static void main(String[] args) {

		String os=System.getProperty("os.name");
        //boolean retVal=JreUtils.isLinux();
        System.out.println("isLinux:"+JreUtils.isLinux());
        System.out.println("isWindows:"+JreUtils.isWindows());
	}

}
