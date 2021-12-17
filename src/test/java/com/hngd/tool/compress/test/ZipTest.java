package com.hngd.tool.compress.test;

import java.io.File;

import com.hngd.tool.util.CompressUtils;

public class ZipTest {

	public static void main(String[] args) {
		File root = new File("target");
		File zipFile = new File("test-output/target.tar.gz");
		CompressUtils.compress(zipFile, root);
	}

	private static String entryName(File root, File f) {
		String rootPath=root.getAbsoluteFile().getParentFile().getAbsolutePath()+File.separator;
		String relativePath=f.getAbsolutePath().replace(rootPath, "");
		return relativePath;
	}

}
