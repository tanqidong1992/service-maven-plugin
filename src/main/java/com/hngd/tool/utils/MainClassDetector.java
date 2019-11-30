package com.hngd.tool.utils;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.codehaus.plexus.util.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.squark.nestedjarclassloader.NestedJarClassLoader;

public class MainClassDetector {
	private static Logger logger=LoggerFactory.getLogger(MainClassDetector.class);
	@SuppressWarnings("deprecation")
	public static Optional<ClassWeight> findTheMostAppropriateMainClass(String mainJarFilePath) throws IOException{
		NestedJarClassLoader loader=new NestedJarClassLoader(MainClassDetector.class.getClassLoader(), logger);
		File file=new File(mainJarFilePath);
		if(!file.getName().endsWith(".jar")) {
			File file1=File.createTempFile(file.getName(), ".jar");
			FileUtils.copyFile(file, file1);
			file=file1;
		}
		try {
			loader.addURLs("main",file.toURL());
		} catch (IOException e) {
			logger.error("",e);
			return Optional.empty();
		}
		List<String> classNames=loader.listAllClass("main");
		if(classNames==null) {
			return Optional.empty();
		}
		Optional<ClassWeight> cw=classNames.stream()
		  .map(name->loader.getClassByteCache("main", name))
		  .map(clazz->caculateWeight(clazz))
		  .filter(w->w.weight>0)
		  .max(Comparator.comparing(w->w.weight));
		return cw;
	}

	public static ClassWeight caculateWeight(byte[] data) {

		ClassWeight w = new ClassWeight();

		ClassReader cr = new ClassReader(data);

		ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM4) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
					String[] exceptions) {
				if ("main".equals(name)) {
					w.weight++;
				} else if ("onStart".equals(name)) {
					w.weight++;
				} else if ("onStop".equals(name)) {
					w.weight++;
				}
				return super.visitMethod(access, name, descriptor, signature, exceptions);
			}
		};
		cr.accept(classVisitor, ClassReader.SKIP_DEBUG);
		w.name = cr.getClassName().replace("/", ".");
		return w;
	}


}
