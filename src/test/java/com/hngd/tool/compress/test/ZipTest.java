package com.hngd.tool.compress.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.hngd.tool.utils.CompressUtils;

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
