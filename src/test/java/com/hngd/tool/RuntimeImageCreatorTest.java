package com.hngd.tool;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.hngd.tool.util.RuntimeImageCreator;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.zeroturnaround.exec.InvalidExitValueException;

public class RuntimeImageCreatorTest {

	public static void main(String[] args) throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
		File dependentLibDirectory=
				new File("/work/company/hnoss/hnoss-mini-helper/target/hnoss-mini-helper/libs");
		 
		File mainJar=
				new File("/work/company/hnoss/hnoss-mini-helper/target/hnoss-mini-helper/hnoss-mini-helper-0.1.0.jar");
		RuntimeImageCreator.build(mainJar, dependentLibDirectory, new File("./jre"), "11","0");
		
		
	}

	public void testJar() {
		
		File testJar=new File("/work/company/hnoss/hnoss-mini-helper/target/hnoss-mini-helper/libs/log4j-core-2.10.0.jar");
		List<String> mods=RuntimeImageCreator.resolveJreDependencies(testJar, "11");
		System.out.println(StringUtils.join(mods, ","));
	}
	
	
	
}
