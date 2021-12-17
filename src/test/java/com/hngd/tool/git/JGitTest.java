package com.hngd.tool.git;

import java.io.File;
import java.io.IOException;


import com.hngd.tool.utils.BuildInfoUtils;


public class JGitTest {

	public static void main(String[] args) throws IOException {
		 
		File gitDir=new File("W:\\company\\hnoss\\hnoss-mini-helper");
		BuildInfoUtils.generateBuildInfo(gitDir, gitDir);

	}

}
