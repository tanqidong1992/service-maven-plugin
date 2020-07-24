package com.hngd.tool.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class CompressUtils {

	private static String entryName(File root, File f) {
		String rootPath=root.getAbsoluteFile().getParentFile().getAbsolutePath()+File.separator;
		String relativePath=f.getAbsolutePath().replace(rootPath, "");
		return relativePath;
	}
	public static void compress(File target, File... filesToArchive) {
        Map<String,File> files=new HashMap<>();
        for(File root:filesToArchive) {
        	if(root.isFile()) {
        		files.put(root.getName(), root);
        	}else {
        		Collection<File> children = 
        				FileUtils.listFilesAndDirs(root, TrueFileFilter.TRUE,
        				TrueFileFilter.TRUE);
        		for(File child:children) {
        			String relativePath=entryName(root,child);
        			files.put(relativePath, child);
        		}
        		
        	}
        }
		try (ArchiveOutputStream o = creaateArchiveOutputStream(target)) {
			for (Entry<String,File> f : files.entrySet()) {
				// maybe skip directories for formats like AR that don't store directories
				ArchiveEntry entry = o.createArchiveEntry(f.getValue(), f.getKey());
				// potentially add more flags to entry
				o.putArchiveEntry(entry);
				if (f.getValue().isFile()) {
					try (InputStream i = Files.newInputStream(f.getValue().toPath())) {
						IOUtils.copy(i, o);
					}
				}
				o.closeArchiveEntry();
			}
			o.finish();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static ArchiveOutputStream creaateArchiveOutputStream(File target) throws IOException {
		
		String extension=FilenameUtils.getExtension(target.getName());
		if("zip".equals(extension)) {
			return new ZipArchiveOutputStream(target);
		}else if("tar".equals(extension)) {
			return new TarArchiveOutputStream(new FileOutputStream(target));
		}else if("gz".equals(extension)) {
			return new TarArchiveOutputStream(new GzipCompressorOutputStream(new FileOutputStream(target)));
		}else  {
			throw new RuntimeException("Unsupported Archive File Type:"+extension);
		} 
	}
}
