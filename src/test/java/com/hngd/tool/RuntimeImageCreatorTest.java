package com.hngd.tool;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.zeroturnaround.exec.InvalidExitValueException;

public class RuntimeImageCreatorTest {

	public static void main(String[] args) throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
		File dependentLibDirectory=new File("W:\\company\\wmp-proxy\\hnoss-mini-helper\\target\\hnoss-mini-helper\\libs");
		 
		File mainJar=new File("W:\\company\\wmp-proxy\\hnoss-mini-helper\\target\\hnoss-mini-helper\\hnoss-mini-helper-0.1.0.jar");
		RuntimeImageCreator.build(mainJar, dependentLibDirectory, new File("./jre"), "11","0");
	}
}
