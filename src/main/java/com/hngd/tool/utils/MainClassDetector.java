package com.hngd.tool.utils;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.squark.nestedjarclassloader.NestedJarClassLoader;

public class MainClassDetector {
	
	private static Logger logger=LoggerFactory.getLogger(MainClassDetector.class);
	
	public static Optional<ClassWeight> findTheMostAppropriateMainClass(String mainJarFilePath) throws IOException{
		NestedJarClassLoader loader=new NestedJarClassLoader(MainClassDetector.class.getClassLoader(), logger);
		File file=new File(mainJarFilePath);
		try {
			loader.addURLs("main",file.toURI().toURL());
		} catch (IOException e) {
			logger.error("",e);
			return Optional.empty();
		}
		List<String> classNames=loader.listAllClass("main");
		if(classNames==null) {
			return Optional.empty();
		}
		Optional<ClassWeight> optionalMaxWeightClass=classNames.stream()
		  .map(name->loader.getClassByteCache("main", name))
		  .map(clazz->caculateWeight(clazz))
		  .filter(w->w.weight>0)
		  .max(Comparator.comparing(w->w.weight));
		return optionalMaxWeightClass;
	}

	public static ClassWeight caculateWeight(byte[] data) {

		ClassWeight classWeight = new ClassWeight();
		ClassReader classReader = new ClassReader(data);
		ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM7) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
					String[] exceptions) {
				//public static void *(String[]args)
				if((Opcodes.ACC_STATIC&access)>0 && (Opcodes.ACC_PUBLIC&access)>0 &&"([Ljava/lang/String;)V".equals(descriptor)) {
					if ("main".equals(name)) {
						classWeight.weight++;
					} else if ("onStart".equals(name)) {
						classWeight.weight++;
					} else if ("onStop".equals(name)) {
						classWeight.weight++;
					}
				}
				return super.visitMethod(access, name, descriptor, signature, exceptions);
			}
		};
		classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
		classWeight.name = classReader.getClassName().replace("/", ".");
		return classWeight;
	}


}
